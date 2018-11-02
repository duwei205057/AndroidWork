#include <jni.h>
#include <assert.h>
#include <stdint.h>
//#include <unistd.h>
#include <android/log.h>
#include <string.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>

#define LOG_FATAL_IF(...)
#ifndef NELEM
#define NELEM(x)  ((int)(sizeof(x)/sizeof((x)[0])))
#endif

#define LOG_TAG_H "xx"
#define H_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_H, __VA_ARGS__)
#define H_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_H, __VA_ARGS__)

static jstring get_StringFromNative(JNIEnv *env, jobject jobj) {
    //int i = gettid();
    char *text = "I am from C";
    int x = 0;
    int y = 5 / x;
    x =+ y;
    char * exception = NULL;
    strcmp(exception, "anr");
    sprintf(exception, "anr");
    return env->NewStringUTF((const char *) x);
}

static jstring get_CrashStringFromNative(JNIEnv *env, jobject jobj) {
    //int i = gettid();
    char *text = "String in crash case";
    return env->NewStringUTF(text);
}


/*
 * Table of methods associated with a single class.
 */
static JNINativeMethod gMethods[] = {
        /* Name,                Signature,                                          Function Pointer */
        {"getStringFromNative", "()Ljava/lang/String;", (void *) get_StringFromNative},
        {"getCrashStringFromNative", "()Ljava/lang/String;", (void *) get_CrashStringFromNative},

};

/*
 * Register native methods for all classes we know about.
 */
static int register_methods(JNIEnv *env) {
    char const *const kClassPathName = "com/dw/crash/NativeInterface";

    jclass clazz;

    clazz = env->FindClass(kClassPathName);
    LOG_FATAL_IF(clazz == NULL, "Unable to find Java class %s", kClassPathName);

    if (env->RegisterNatives(clazz, gMethods, NELEM(gMethods)) < 0) {
        return JNI_FALSE;
    }


    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    H_LOGE("enter helper OnLoad");
    (void) reserved;
    //crash_collect::ExceptionHandler *exception_handler = NULL;
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    assert(env != NULL);

    if (register_methods(env) < 0) {
        H_LOGE("ERROR: sogouime native registration failed\n");
        goto bail;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    bail:
    return result;
}