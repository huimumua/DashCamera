#include <stdio.h>
#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <assert.h>
#include <time.h>
#include <semaphore.h>
#include <signal.h>
#include <pthread.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#define TAG "mediabuffer-jni"

#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#define DEFINE_METHOD(name) static jmethodID method_##name
#define INITIAL_METHOD(name, signature) method_##name=env->GetMethodID(clazz, #name, signature)

#define CALL_VOID_METHOD(name, ...)  do { \
         JNIEnv *env = NULL; \
         sVMContext.vm->AttachCurrentThread(&env, NULL); \
         env->CallVoidMethod(sVMContext.cb, method_##name, ## __VA_ARGS__); \
         checkAndClearExceptionFromCallback(env, __FUNCTION__); \
         sVMContext.vm->DetachCurrentThread(); \
     } while(0)

#define __android_second(dummy, second, ...)     second
#define __android_rest(first, ...)               , ## __VA_ARGS__
#define android_printAssert(cond, tag, fmt...) \
    __android_log_assert(cond, tag, \
    __android_second(0, ## fmt, NULL) __android_rest(fmt))

#define CONDITION(cond)     (__builtin_expect((cond)!=0, 0))
#ifndef LOG_ALWAYS_FATAL_IF
#define LOG_ALWAYS_FATAL_IF(cond, ...) \
    ( (CONDITION(cond)) \
    ? ((void)android_printAssert(#cond, TAG, ## __VA_ARGS__)) \
    : (void)0 )
#endif

#define LITERAL_TO_STRING_INTERNAL(x)    #x
#define LITERAL_TO_STRING(x) LITERAL_TO_STRING_INTERNAL(x)
#define CHECK(condition)                                \
    LOG_ALWAYS_FATAL_IF(                                \
            !(condition),                               \
            "%s",                                       \
            __FILE__ ":" LITERAL_TO_STRING(__LINE__)    \
            " CHECK(" #condition ") failed.")

#define SWAP32(x) (((x) & 0xff) << 24 | ((x) & 0xff00) << 8 | ((x) & 0xff0000) >> 8 | ((x) >> 24) & 0xff)
#define SWAP64(x) (SWAP32((x) & 0xffffffff)  << 32 | SWAP32(((x) >> 32) & 0xffffffff))

#define PACKAGE_TAG    0x55AA55AAu

struct fields_t {
    jmethodID arrayID;
};

typedef struct {
    JavaVM *vm;
    JNIEnv *env;
    jobject cb;  // callback object
} context_t;

typedef struct {
    uint8_t type;
    uint8_t event;
    int64_t time;
    int32_t size;
    int32_t flags;
    int64_t pts;
    int8_t data[0];
} __packed frame_t;

typedef struct {
    uint32_t tag;
    uint32_t length;
    frame_t frame;
} __packed packet_t;

static fields_t gFields;
static context_t sVMContext = {NULL};
static jbyte *buffer_ptr = NULL;
static jsize buffer_size = 0;
static char file_path[256];
static char *file_name;
static jbyte *head_ptr = NULL;
static jbyte *tail_ptr = NULL;
static jbyte *buffer_fence = NULL;
static uint32_t slice_count;

static pthread_mutex_t mutex;
static pthread_cond_t  data_available;
static pthread_mutex_t lock;
static bool thread_exit = true;
static pthread_t thread_id = 0;

// define callback function id
DEFINE_METHOD(reportCacheFile);

static void checkAndClearExceptionFromCallback(JNIEnv* env, const char* methodName) {
    if (env->ExceptionCheck()) {
        ALOGE("An exception was thrown by callback '%s'.", methodName);
        //LOGE_EX(env);
        env->ExceptionClear();
    }
}

/*
 * Throw an exception with the specified class and an optional message.
 */
static int jniThrowException(JNIEnv* env, const char* className, const char* msg) {
    jclass exceptionClass = env->FindClass(className);
    if (exceptionClass == NULL) {
        __android_log_print(ANDROID_LOG_ERROR,
                            TAG,
                            "Unable to find exception class %s",
                            className);
        return -1;
    }

    if (env->ThrowNew(exceptionClass, msg) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR,
                            TAG,
                            "Failed throwing '%s' '%s'",
                            className, msg);
    }
    env->DeleteLocalRef(exceptionClass);
    return 0;
}

/* return current time in milliseconds */
static unsigned long current_time_ms() {
    struct timeval now;
    if (gettimeofday(&now, NULL) != 0) {
        struct timespec ts;
        if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
            now.tv_sec = ts.tv_sec;
            now.tv_usec = ts.tv_nsec / 1000UL;
        } else {
            now.tv_sec = time(NULL);
            now.tv_usec = 0;
        }
    }
    return now.tv_sec * 1000UL + now.tv_usec / 1000UL;
}

static void *thread_func(void *arg) {
    unsigned long start_time_ms = 0;
    int fd = -1;
    slice_count = 0;
    while (!thread_exit) {
        pthread_mutex_lock(&mutex);
        pthread_cond_wait(&data_available, &mutex);
        pthread_mutex_unlock(&mutex);
        if (thread_exit) break;

        if (tail_ptr == head_ptr) {
            continue;
        } else {
            if (fd > 0) {
                unsigned long diff = current_time_ms() - start_time_ms;
                if (diff > 1000ul) {
                    close(fd);
                    fd = -1;
                    JNIEnv *env = NULL;
                    sVMContext.vm->AttachCurrentThread(&env, NULL);
                    jstring str = env->NewStringUTF(file_path);
                    env->CallVoidMethod(sVMContext.cb, method_reportCacheFile, str);
                    checkAndClearExceptionFromCallback(env, __FUNCTION__);
                    env->DeleteLocalRef(str);
                    sVMContext.vm->DetachCurrentThread();
                }
            }

            if (fd < 0) {
                int n = sprintf(file_name, "%lu", current_time_ms());
                file_name[n] = '\0';
                fd = open(file_path, O_WRONLY | O_CREAT | O_APPEND);
                if (fd < 0) {
                    ALOGE("Error(%d): %s", errno, strerror(errno));
                    continue;
                }
                fchmod(fd, 0644);
                start_time_ms = current_time_ms();
                slice_count++;
                uint32_t count = SWAP32(slice_count);
                write(fd, &count, sizeof(count));
            }

            pthread_mutex_lock(&lock);
            unsigned long t1 = current_time_ms();
            if (head_ptr < tail_ptr) {
                write(fd, head_ptr, tail_ptr - head_ptr);
                head_ptr = tail_ptr = buffer_ptr;
            } else if (head_ptr > tail_ptr) {
                write(fd, head_ptr, buffer_fence - head_ptr);
                write(fd, buffer_ptr, tail_ptr - buffer_ptr);
                head_ptr = tail_ptr = buffer_ptr;
            }
            unsigned long diff = current_time_ms() - t1;
            if (diff > 30) {
                ALOGW("write cache time: %lu", diff);
            }
            pthread_mutex_unlock(&lock);
        }
    }
    if (fd > 0) {
        close(fd);
    }
    return NULL;
}

/**************************************************************************************************
 *
 **************************************************************************************************/
static void MediaBuffer_class_init_native(JNIEnv *env, jclass clazz) {
    sVMContext.env = env;
    INITIAL_METHOD(reportCacheFile, "(Ljava/lang/String;)V");
}

static void MediaBuffer_native_init(JNIEnv *env, jobject thiz, jint bufferSize, jstring dir) {
    sVMContext.cb = env->NewGlobalRef(thiz);

    jboolean copy = JNI_FALSE;
    const char *dir_str = env->GetStringUTFChars(dir, &copy);
    if (dir_str == NULL) {
    }
    strcpy(file_path, dir_str);
    env->ReleaseStringUTFChars(dir, dir_str);
    const int end = strlen(file_path) - 1;
    if (file_path[end] != '/') {
        file_path[end + 1] = '/';
        file_path[end + 2] = '\0';
        file_name = &file_path[end + 2];
    } else {
        file_name = &file_path[end + 1];
    }

    buffer_size = bufferSize;
    buffer_ptr = new jbyte[buffer_size];
    buffer_fence = buffer_ptr + buffer_size;
    head_ptr = tail_ptr = buffer_ptr;

    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&data_available, NULL);
    pthread_mutex_init(&lock, NULL);
}

static void MediaBuffer_release(JNIEnv *env, jobject thiz) {
    delete[] buffer_ptr;
}

static void MediaBuffer_start(JNIEnv *env, jobject thiz) {
    thread_exit = false;
    pthread_create(&thread_id, NULL ,thread_func, NULL);
}

static void MediaBuffer_stop(JNIEnv *env, jobject thiz) {
    thread_exit = true;
    pthread_cond_signal(&data_available);
    pthread_join(thread_id, NULL);
    thread_id = 0;
}

static void MediaBuffer_reset(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&lock);
    head_ptr = tail_ptr = buffer_ptr;
    pthread_cond_init(&data_available, NULL);
    pthread_mutex_unlock(&lock);
}

static void MediaBuffer_writeSampleData(JNIEnv *env, jobject thiz,
                                          jint type, jint event, jlong time,
                                          jobject byteBuf, jint offset, jint size,
                                          jlong presentationTimeUs, jint flags) {

    if (thread_exit) {
        return;
    }

    jbyte *dst = (jbyte*)env->GetDirectBufferAddress(byteBuf);

    jlong dstSize;
    jbyteArray byteArray = NULL;

    if (dst == NULL) {
        byteArray = (jbyteArray) env->CallObjectMethod(byteBuf, gFields.arrayID);
        if (byteArray == NULL) {
            ALOGE("byteArray is null");
            jniThrowException(env, "java/lang/IllegalArgumentException", "byteArray is null");
            return;
        }

        jboolean isCopy;
        dst = env->GetByteArrayElements(byteArray, &isCopy);

        dstSize = env->GetArrayLength(byteArray);
    } else {
        dstSize = env->GetDirectBufferCapacity(byteBuf);
    }

    if (dstSize < (offset + size)) {
        ALOGE("writeSampleData saw wrong dstSize %lld, size  %d, offset %d", (long long) dstSize,
              size, offset);
        if (byteArray != NULL) {
            env->ReleaseByteArrayElements(byteArray, dst, 0);
        }
        ALOGE("sample has a wrong size");
        jniThrowException(env, "java/lang/IllegalArgumentException", "sample has a wrong size");
        return;
    }

    pthread_mutex_lock(&lock);
    if (head_ptr <= tail_ptr) {
        if (buffer_fence - tail_ptr > sizeof(packet_t) + size) {
            packet_t *packet = (packet_t *) tail_ptr;
            packet->tag = SWAP32(PACKAGE_TAG);
            packet->length = SWAP32(sizeof(frame_t) + size);
            packet->frame.type = (uint8_t) type;
            packet->frame.event = (uint8_t) event;
            packet->frame.time = SWAP64(time);
            packet->frame.size = SWAP32(size);
            packet->frame.flags = SWAP32(flags);
            packet->frame.pts = SWAP64(presentationTimeUs);
            memcpy(packet->frame.data, dst, size);
            tail_ptr = packet->frame.data + size;
            pthread_cond_signal(&data_available);
        } else if ((buffer_fence - tail_ptr) + (head_ptr - buffer_ptr) > sizeof(packet_t) + size) {
            if (buffer_fence - tail_ptr >= sizeof(packet_t)) {
                packet_t *packet = (packet_t *) tail_ptr;
                packet->tag = SWAP32(PACKAGE_TAG);
                packet->length = SWAP32(sizeof(frame_t) + size);
                packet->frame.type = (uint8_t) type;
                packet->frame.event = (uint8_t) event;
                packet->frame.time = SWAP64(time);
                packet->frame.size = SWAP32(size);
                packet->frame.flags = SWAP32(flags);
                packet->frame.pts = SWAP64(presentationTimeUs);
                if (packet->frame.data < buffer_fence) {
                    const size_t s1 = buffer_fence - packet->frame.data;
                    const size_t s2 = size - s1;
                    memcpy(packet->frame.data, dst, s1);
                    memcpy(buffer_ptr, dst + s1, s2);
                    tail_ptr = buffer_ptr + s2;
                } else {
                    memcpy(buffer_ptr, dst, size);
                    tail_ptr = buffer_ptr + size;
                }
            } else {
                packet_t packet;
                packet.tag = SWAP32(PACKAGE_TAG);
                packet.length = SWAP32(sizeof(frame_t) + size);
                packet.frame.type = (uint8_t) type;
                packet.frame.event = (uint8_t) event;
                packet.frame.time = SWAP64(time);
                packet.frame.size = SWAP32(size);
                packet.frame.flags = SWAP32(flags);
                packet.frame.pts = SWAP64(presentationTimeUs);
                const size_t s1 = buffer_fence - tail_ptr;
                const size_t s2 = sizeof(packet_t) - s1;
                int8_t *ptr = (int8_t *) &packet;
                memcpy(tail_ptr, ptr, s1);
                memcpy(buffer_ptr, ptr + s1, s2);
                tail_ptr = buffer_ptr + s2;
            }
            pthread_cond_signal(&data_available);
        } else {
            pthread_mutex_unlock(&lock);
            if (byteArray != NULL) {
                env->ReleaseByteArrayElements(byteArray, dst, 0);
            }
            jniThrowException(env, "java/lang/RuntimeException", "buffer overflow");
            return;
        }
    } else {
        if (head_ptr - tail_ptr > sizeof(packet_t) + size) {
            packet_t *packet = (packet_t *) tail_ptr;
            packet->tag = SWAP32(PACKAGE_TAG);
            packet->length = SWAP32(sizeof(frame_t) + size);
            packet->frame.type = (uint8_t) type;
            packet->frame.event = (uint8_t) event;
            packet->frame.time = SWAP64(time);
            packet->frame.size = SWAP32(size);
            packet->frame.flags = SWAP32(flags);
            packet->frame.pts = SWAP64(presentationTimeUs);
            memcpy(packet->frame.data, dst, size);
            tail_ptr = packet->frame.data + size;
            pthread_cond_signal(&data_available);
        } else {
            pthread_mutex_unlock(&lock);
            if (byteArray != NULL) {
                env->ReleaseByteArrayElements(byteArray, dst, 0);
            }
            jniThrowException(env, "java/lang/RuntimeException", "buffer overflow");
            return;
        }
    }
    pthread_mutex_unlock(&lock);
    if (byteArray != NULL) {
        env->ReleaseByteArrayElements(byteArray, dst, 0);
    }
}

/**************************************************************************************************
 *
 **************************************************************************************************/
static const JNINativeMethod gMethods[] = {
        {"class_init_native", "()V", (void *)MediaBuffer_class_init_native},
        { "native_init", "(ILjava/lang/String;)V", (void *)MediaBuffer_native_init },
        { "release", "()V", (void *)MediaBuffer_release },
        { "nativeWriteSampleData", "(IIJLjava/nio/ByteBuffer;IIJI)V", (void *)MediaBuffer_writeSampleData },
        { "start", "()V", (void *)MediaBuffer_start },
        { "stop", "()V", (void *)MediaBuffer_stop },
        { "reset", "()V", (void *)MediaBuffer_reset },
};

static int registerNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* methods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
        return JNI_FALSE;
    }

    jclass byteBufClass = env->FindClass("java/nio/ByteBuffer");
    CHECK(byteBufClass != NULL);

    gFields.arrayID = env->GetMethodID(byteBufClass, "array", "()[B");
    CHECK(gFields.arrayID != NULL);

    return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;
    const char* className = "com/askey/dvr/cdr7010/dashcam/core/jni/MediaBuffer";

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto error;
    }

    result = registerNativeMethods(env, className, gMethods, NELEM(gMethods));
    if (result < 0) {
        goto error;
    }

    sVMContext.vm = vm;
    result = JNI_VERSION_1_4;

    error:
    return result;
}
