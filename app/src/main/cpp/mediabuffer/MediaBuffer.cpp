#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <assert.h>
#include <time.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <signal.h>

#include "MediaBuffer.h"
#include "jniutils.h"

#define SWAP32(x) (((x) & 0xff) << 24 | ((x) & 0xff00) << 8 | ((x) & 0xff0000) >> 8 | ((x) >> 24) & 0xff)
#define SWAP64(x) (SWAP32((x) & 0xffffffff)  << 32 | SWAP32(((x) >> 32) & 0xffffffff))

#define PACKAGE_TAG    0x55AA55AAu

#define TAG "mediabuffer-jni"

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

namespace {

class Lock {
public:
    Lock(pthread_mutex_t *mutex) : mutex(mutex) {
        pthread_mutex_lock(this->mutex);
    }

    ~Lock() {
        pthread_mutex_unlock(mutex);
    }

private:
    pthread_mutex_t *mutex;
};

/* return current time in milliseconds */
unsigned long current_time_ms() {
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

void *thread_func(void *arg) {
    MediaBuffer *bufObj = (MediaBuffer *) arg;
    unsigned long start_time_ms = 0;
    int fd = -1;

    ALOGW("iamlbccc, Start tid: %d  0x%08x !\n", gettid(), bufObj);

    bufObj->thread_exit = false;
    bufObj->slice_count = 0;
    while (!bufObj->isStop()) {
        bufObj->wait();
        if (bufObj->isStop()) break;

        if (bufObj->isEmpty()) {
            continue;
        } else {
            if (fd > 0) {
                unsigned long diff = current_time_ms() - start_time_ms;
                if (diff > 1000ul) {
                    ALOGE("iamlbccc, who 0x%08x %ld Close file %d  %ld   name:%s\n", bufObj,
                          gettid(),
                          current_time_ms(), start_time_ms, bufObj->file_name);
                    close(fd);
                    fd = -1;
                    bufObj->notify();
                }
            }

            if (fd < 0) {
                fd = bufObj->cacheCreate(current_time_ms());
                if (fd < 0) {
                    ALOGE("Error(%d): %s", errno, strerror(errno));
                    continue;
                }
                start_time_ms = current_time_ms();
            }

            bufObj->cacheWrite(fd);
        }
    }
    if (fd > 0) {
        close(fd);
    }

    ALOGW("iamlbccc, tid: %d joined. 3\n", gettid());
    return NULL;
}
} //namespace

MediaBuffer::MediaBuffer(jobject obj, size_t bufferSize, const char *dir, callback_t callback)
    : obj(obj)
    , buffer_size(bufferSize)
    , callback(callback)
    , thread_exit(false)
    , thread_id(0)
{
    strcpy(file_path, dir);
    const size_t end = strlen(file_path) - 1;
    if (file_path[end] != '/') {
        file_path[end + 1] = '/';
        file_path[end + 2] = '\0';
        file_name = &file_path[end + 2];
    } else {
        file_name = &file_path[end + 1];
    }

    buffer_ptr = new jbyte[buffer_size];
    buffer_fence = buffer_ptr + buffer_size;
    head_ptr = tail_ptr = buffer_ptr;

    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&data_available, NULL);
    pthread_mutex_init(&lock, NULL);
    ALOGW("iamlbccc, NEW 0x%08x !\n", this);
}

MediaBuffer::~MediaBuffer()
{
    pthread_mutex_destroy(&lock);
    pthread_cond_destroy(&data_available);
    pthread_mutex_destroy(&mutex);
    delete [] buffer_ptr;
}

void MediaBuffer::start()
{
    pthread_create(&thread_id, NULL ,thread_func, this);
}

void MediaBuffer::stop()
{
    thread_exit = true;
    pthread_cond_signal(&data_available);
    pthread_join(thread_id, NULL);
    thread_id = 0;
}

void MediaBuffer::reset()
{
    Lock locker(&lock);
    head_ptr = tail_ptr = buffer_ptr;
    pthread_cond_init(&data_available, NULL);
}

bool MediaBuffer::writeSampleData(int type, int event, int64_t time, int8_t *buf, size_t size, int64_t pts, int flags)
{
    bool res = true;
    Lock locker(&lock);
    if (head_ptr <= tail_ptr) {
        if (buffer_fence - tail_ptr > sizeof(packet_t) + size) {
            packet_t *packet = (packet_t *) tail_ptr;
            packet->tag = SWAP32(PACKAGE_TAG);
            packet->length = SWAP32(sizeof(frame_t) + size);
            packet->frame.type = (uint8_t) type;
            packet->frame.event = (uint8_t) event;
            packet->frame.time = SWAP64(time);
            packet->frame.size = (int32_t)SWAP32(size);
            packet->frame.flags = SWAP32(flags);
            packet->frame.pts = SWAP64(pts);
            memcpy(packet->frame.data, buf, size);
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
                packet->frame.size = (int32_t)SWAP32(size);
                packet->frame.flags = SWAP32(flags);
                packet->frame.pts = SWAP64(pts);
                if (packet->frame.data < buffer_fence) {
                    const size_t s1 = buffer_fence - packet->frame.data;
                    const size_t s2 = size - s1;
                    memcpy(packet->frame.data, buf, s1);
                    memcpy(buffer_ptr, buf + s1, s2);
                    tail_ptr = buffer_ptr + s2;
                } else {
                    memcpy(buffer_ptr, buf, size);
                    tail_ptr = buffer_ptr + size;
                }
            } else {
                packet_t packet;
                packet.tag = SWAP32(PACKAGE_TAG);
                packet.length = SWAP32(sizeof(frame_t) + size);
                packet.frame.type = (uint8_t) type;
                packet.frame.event = (uint8_t) event;
                packet.frame.time = SWAP64(time);
                packet.frame.size = (int32_t)SWAP32(size);
                packet.frame.flags = SWAP32(flags);
                packet.frame.pts = SWAP64(pts);
                const size_t s1 = buffer_fence - tail_ptr;
                const size_t s2 = sizeof(packet_t) - s1;
                int8_t *ptr = (int8_t *) &packet;
                memcpy(tail_ptr, ptr, s1);
                memcpy(buffer_ptr, ptr + s1, s2);
                tail_ptr = buffer_ptr + s2;
            }
            pthread_cond_signal(&data_available);
        } else {
            res = false;
        }
    } else {
        if (head_ptr - tail_ptr > sizeof(packet_t) + size) {
            packet_t *packet = (packet_t *) tail_ptr;
            packet->tag = SWAP32(PACKAGE_TAG);
            packet->length = SWAP32(sizeof(frame_t) + size);
            packet->frame.type = (uint8_t) type;
            packet->frame.event = (uint8_t) event;
            packet->frame.time = SWAP64(time);
            packet->frame.size = (int32_t)SWAP32(size);
            packet->frame.flags = SWAP32(flags);
            packet->frame.pts = SWAP64(pts);
            memcpy(packet->frame.data, buf, size);
            tail_ptr = packet->frame.data + size;
            pthread_cond_signal(&data_available);
        } else {
            res = false;
        }
    }
    return res;
}

void MediaBuffer::notify()
{
    if (callback != NULL) {
        callback(obj, file_path);
    }
}

int MediaBuffer::cacheCreate(unsigned long time)
{
    int n = sprintf(file_name, "%lu_%d_0x%08x", time, gettid(), this);
    file_name[n] = '\0';
    int fd = open(file_path, O_WRONLY | O_CREAT | O_APPEND);
    if (fd > 0) {
        fchmod(fd, 0644);
        slice_count++;
        uint32_t count = SWAP32(slice_count);
        write(fd, &count, sizeof(count));
    }
    ALOGW("iamlbccc, who 0x%08x %d Create file %s\n", this, gettid(), file_name);
    return fd;
}

void MediaBuffer::cacheWrite(int fd)
{
    Lock locker(&lock);
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
}

void MediaBuffer::wait() {
    Lock locker(&mutex);
    pthread_cond_wait(&data_available, &mutex);
}