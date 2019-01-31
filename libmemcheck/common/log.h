#ifndef __LOG_H_
#define __LOG_H_

#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "CRASH_COLLECT"
#endif

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif
