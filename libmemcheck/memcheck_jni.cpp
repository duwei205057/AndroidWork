#include <jni.h>
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include "backtrace.h"
#include "log.h"
#include "inlineHook.h"
#include "elfhook.h"
#include "elfHook/elfhook.h"

#ifndef NELEM
#define NELEM(x)  ((int)(sizeof(x)/sizeof((x)[0])))
#endif

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "xx"
//#define LOG_TAG "MEMCHECK"
#endif

static int count = 0;
typedef void* (*t_malloc)(size_t);
void *g_malloc = NULL;

typedef void* (*t_realloc)(void*, size_t);
void *g_realloc = NULL;

typedef void (*t_free)(void*);
void *g_free = NULL;

class MemCache {
    uint32_t cache[10101];
};

void *my_malloc(size_t size) {
    t_malloc tmalloc = (t_malloc) g_malloc;
    void *pRet = tmalloc(size);
    show_stack(size, 0, pRet, MALLOC);
    return pRet;
}

void *my_realloc(void* ptr, size_t size) {
    t_realloc trealloc = (t_realloc) g_realloc;
    void *pRet = trealloc(ptr, size);
    show_stack(size, ptr, pRet, REALLOC);
    return pRet;
}

void my_free(void *ptr) {
    t_free tfree = (t_free) g_free;
    show_stack(0, ptr, 0, FREE);
    tfree(ptr);
}

void RegisterInlineHook() {
    registerInlineHook((uint32_t)malloc, (uint32_t)my_malloc, (uint32_t **)&g_malloc);
    registerInlineHook((uint32_t)realloc, (uint32_t)my_realloc, (uint32_t **)&g_realloc);
    registerInlineHook((uint32_t)free, (uint32_t)my_free, (uint32_t **)&g_free);
    inlineHookAll();
}

void RegisterElfHook() {
    LOGE("RegisterElfHook");
    elfHook("libmyso.so", "malloc", (void*)my_malloc, &g_malloc);
    elfHook("libmyso.so", "free", (void*)my_free, &g_free);
    elfHook("myso", "malloc", (void*)my_malloc, &g_malloc);
    elfHook("myso", "free", (void*)my_free, &g_free);
}

static jint init_module(JNIEnv* env, jobject thiz) {
    //RegisterInlineHook();
    RegisterElfHook();
    return 0;
}

static jint mem_malloc(JNIEnv* env, jobject thiz) {
    //void *addr = malloc(10101);
    //realloc(NULL, 10101);
    //new MemCache();
    show_leak();
    return 0;
}

static JNINativeMethod gMethods[] = {
    {"initModule",     "()I",   	(void*) init_module},
    {"memMalloc",     "()I",   	(void*) mem_malloc}
};

static int register_methods(JNIEnv* env) {
    char const *const kClassPathName = "com/dw/memcheck/MemCheck";
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
    (void) reserved;
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    goto bail;
    }
    assert(env != NULL);
    if (register_methods(env) < 0) {
    LOGE("ERROR: native registration failed\n");
    goto bail;
    }
    //RegisterInlineHook();
    //RegisterElfHook();
    result = JNI_VERSION_1_4;
bail:
    return result;
}

