#ifndef MAINAPP_JNIUTILS_H
#define MAINAPP_JNIUTILS_H

#include <jni.h>
#include <string>
#include <android/log.h>

#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif //MAINAPP_JNIUTILS_H
