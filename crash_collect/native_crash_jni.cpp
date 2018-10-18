//
// Created by duwei on 18-10-15.
//

#include <jni.h>
#include <stdio.h>
#include <assert.h>
#include "exception_handler.h"

#ifndef NELEM
#define NELEM(x)  ((int)(sizeof(x)/sizeof((x)[0])))
#endif

static native_crash_collector::ExceptionHandler* exception_handler = NULL;
JavaVM *g_vm = NULL;


static jint native_initCrashCollect(JNIEnv* env, jobject thiz, jcharArray outputArray, jint size) {
    CRASH_LOGD("native_initCrashCollect");
    if(outputArray == NULL || size == 0) return JNI_ERR;
    jchar* outputChars = env->GetCharArrayElements(outputArray, NULL);
    if(exception_handler != NULL) {
        exception_handler->SetCrashLogSavePath(outputChars, size);
        exception_handler->InitCrashCollect();
    }
    env->ReleaseCharArrayElements(outputArray, outputChars, JNI_OK);
    return JNI_OK;
}

static jint native_initAnrCollect(JNIEnv* env, jobject thiz, jcharArray procNameArray, jint procNameSize, jcharArray pathArray, jint pathSize) {
    CRASH_LOGD("native_initAnrCollect");
    jchar* procNameChars = env->GetCharArrayElements(procNameArray, NULL);
    jchar* pathChars = env->GetCharArrayElements(pathArray, NULL);
    if(exception_handler != NULL) {
        exception_handler->SetPackageName(procNameChars, procNameSize);
        exception_handler->SetAnrLogSavePath(pathChars, pathSize);
        exception_handler->InitAnrCollect();
    }
    env->ReleaseCharArrayElements(procNameArray, procNameChars, JNI_OK);
    env->ReleaseCharArrayElements(pathArray, pathChars, JNI_OK);
    return JNI_OK;
}


static jint ime_postVersionInfoToNative(JNIEnv* env, jobject thiz, jcharArray outputArray, jint size)
{
    CRASH_LOGD("postVersionInfoToNative");

    jchar* outputChars = env->GetCharArrayElements(outputArray, NULL);
    if(exception_handler != NULL) {
        exception_handler->SetHeadInfo(outputChars, size);
    }
    env->ReleaseCharArrayElements(outputArray, outputChars, JNI_OK);
    return 0;
}


static jint ime_postKeyboardShownStateToNative(JNIEnv* env, jobject thiz, jint state)
{
    CRASH_LOGD("postKeyboardShownStateToNative");

    if(exception_handler != NULL) {
        // no such method
//        exception_handler->SetKeyboardShownState(state);
    }

    return 0;
}

static jint ime_setNativeCollectSwitch(JNIEnv* env, jobject thiz, jint which, jboolean state)
{
    if(exception_handler != NULL) {
        exception_handler->SetCollectSwitch(which, state);
    }

    return 0;
}

static JNINativeMethod gMethods[] = {
        {"initCrashCollectNative",         "([CI)I",     (void *) native_initCrashCollect},
        {"initAnrCollectNative",           "([CI[CI)I",     (void *) native_initAnrCollect},

        {"setVersionInfoNative",        "([CI)I",  (void *) ime_postVersionInfoToNative},
        {"setKeyboardShownStateNative", "(I)I",    (void *) ime_postKeyboardShownStateToNative},

        {"setNativeCollectSwitchNative",         "(IZ)I",   (void *) ime_setNativeCollectSwitch},
};

static int register_methods(JNIEnv* env) {
    CRASH_LOGD("INFO: register_method called\n");
    char const *const kClassPathName = "com/sogou/nativecrashcollector/NativeInterface";
    jclass clazz;
    clazz = env->FindClass(kClassPathName);
    if(clazz == NULL) {
        printf("cannot get class:%s\n", kClassPathName);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, NELEM(gMethods)) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    CRASH_LOGD("INFO: JNI_OnLoad called\n");
    (void) reserved;
    JNIEnv* env = NULL;
    jint result = -1;
    g_vm = vm;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    assert(env != NULL);
    if (register_methods(env) < 0) {
        CRASH_LOGE("ERROR: native registration failed\n");
        goto bail;
    }
    /*regeister crash collect modle*/
    exception_handler = new native_crash_collector::ExceptionHandler(g_vm);

    result = JNI_VERSION_1_4;
    bail:
    return result;
}

