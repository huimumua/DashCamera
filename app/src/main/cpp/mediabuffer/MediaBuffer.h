#ifndef MAINAPP_MEDIABUFFER_H
#define MAINAPP_MEDIABUFFER_H

#include <stdio.h>

#include <semaphore.h>
#include <pthread.h>

typedef void (*callback_t)(jobject, const char*);

struct MediaBuffer {
    MediaBuffer(jobject obj, size_t bufferSize, const char *dir, callback_t callback);
    ~MediaBuffer();
    void start();
    void stop();
    void reset();
    bool isStop() { return thread_exit;}
    bool writeSampleData(int type, int event, int64_t time, int8_t *buf, size_t size, int64_t pts, int flags);
    void notify();
    int cacheCreate(unsigned long time);
    void cacheWrite(int fd);
    bool isEmpty() { return (tail_ptr == head_ptr);}
    void wait();

    jobject obj;
    size_t buffer_size = 0;
    callback_t callback;
    jbyte *buffer_ptr;
    char file_path[256];
    char *file_name;
    jbyte *head_ptr;
    jbyte *tail_ptr;
    jbyte *buffer_fence;
    uint32_t slice_count;

    pthread_mutex_t mutex;
    pthread_cond_t  data_available;
    pthread_mutex_t lock;
    bool thread_exit;
    pthread_t thread_id;

};


#endif //MAINAPP_MEDIABUFFER_H
