#include <stdio.h>
#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>

#include "MediaBuffer.h"
#include "jniutils.h"

#define TAG "mediabuffer-jni"


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

struct fields_t {
    jmethodID arrayID;
};

typedef struct {
    JavaVM *vm;
    JNIEnv *env;
} context_t;

static fields_t gFields;
static context_t sVMContext = {NULL};

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

static void jniCallback(jobject obj, const char *path)
{
    JNIEnv *env = NULL;
    sVMContext.vm->AttachCurrentThread(&env, NULL);
    jstring str = env->NewStringUTF(path);
    env->CallVoidMethod(obj, method_reportCacheFile, str);
    checkAndClearExceptionFromCallback(env, __FUNCTION__);
    env->DeleteLocalRef(str);
    sVMContext.vm->DetachCurrentThread();
}

/**************************************************************************************************
 *
 **************************************************************************************************/
static void MediaBuffer_class_init_native(JNIEnv *env, jclass clazz) {
    sVMContext.env = env;
    INITIAL_METHOD(reportCacheFile, "(Ljava/lang/String;)V");
}

static jlong MediaBuffer_native_init(JNIEnv *env, jobject thiz, jint bufferSize, jstring dir) {
    jboolean copy = JNI_FALSE;
    const char *dir_str = env->GetStringUTFChars(dir, &copy);
    if (dir_str == NULL) {
    }
    MediaBuffer *buffer = new MediaBuffer(env->NewGlobalRef(thiz), (size_t)bufferSize, dir_str, jniCallback);
    env->ReleaseStringUTFChars(dir, dir_str);
    return reinterpret_cast<jlong> (buffer);
}

static void MediaBuffer_release(JNIEnv *env, jobject thiz, jlong native_object) {
    MediaBuffer *buffer = reinterpret_cast<MediaBuffer*> (native_object);
    delete buffer;
}

static void MediaBuffer_start(JNIEnv *env, jobject thiz, jlong native_object) {
    MediaBuffer *buffer = reinterpret_cast<MediaBuffer*> (native_object);
    buffer->start();
}

static void MediaBuffer_stop(JNIEnv *env, jobject thiz, jlong native_object) {
    MediaBuffer *buffer = reinterpret_cast<MediaBuffer*> (native_object);
    buffer->stop();
}

static void MediaBuffer_reset(JNIEnv *env, jobject thiz, jlong native_object) {
    MediaBuffer *buffer = reinterpret_cast<MediaBuffer*> (native_object);
    buffer->reset();
}

static void MediaBuffer_writeSampleData(JNIEnv *env, jobject thiz, jlong native_object,
                                          jint type, jint event, jlong time,
                                          jobject byteBuf, jint offset, jint size,
                                          jlong presentationTimeUs, jint flags) {

    MediaBuffer *buffer = reinterpret_cast<MediaBuffer*> (native_object);
    if (buffer->isStop()) {
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

    if(!buffer->writeSampleData(type, event, time, dst, (size_t)size, presentationTimeUs, flags)) {
        jniThrowException(env, "java/lang/RuntimeException", "buffer overflow");
    }

    if (byteArray != NULL) {
        env->ReleaseByteArrayElements(byteArray, dst, 0);
    }
}

/**************************************************************************************************
 *
 **************************************************************************************************/
static const JNINativeMethod gMethods[] = {
        {"class_init_native", "()V", (void *)MediaBuffer_class_init_native},
        { "native_init", "(ILjava/lang/String;)J", (void *)MediaBuffer_native_init },
        { "native_release", "(J)V", (void *)MediaBuffer_release },
        { "native_writeSampleData", "(JIIJLjava/nio/ByteBuffer;IIJI)V", (void *)MediaBuffer_writeSampleData },
        { "native_start", "(J)V", (void *)MediaBuffer_start },
        { "native_stop", "(J)V", (void *)MediaBuffer_stop },
        { "native_reset", "(J)V", (void *)MediaBuffer_reset },
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
