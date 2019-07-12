#include <jni.h>
#include <assert.h>
#include <stdint.h>
//#include <unistd.h>
#include <android/log.h>
#include <string.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include <elf.h>
#include <stdlib.h>
#include <sys/mman.h>

#define LOG_FATAL_IF(...)
#ifndef NELEM
#define NELEM(x)  ((int)(sizeof(x)/sizeof((x)[0])))
#endif

#define LOG_TAG_H "xx"
#define H_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_H, __VA_ARGS__)
#define H_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_H, __VA_ARGS__)

jstring get_StringFromNative(JNIEnv*,jobject) __attribute__((section (".mytext")));


void init_getString() __attribute__((constructor));
unsigned long getLibAddr();

void init_getString(){
    #ifndef NOENCRYPT
    char name[15];
    unsigned int nblock;
    unsigned int nsize;
    unsigned long base;
    unsigned long text_addr;
    unsigned int i;
    Elf32_Ehdr *ehdr;
    Elf32_Shdr *shdr;

    base = getLibAddr();

    ehdr = (Elf32_Ehdr *)base;
//    text_addr = ehdr->e_version + base;
//    text_addr = ehdr->e_shoff + base;
//    text_addr = *((long *)ehdr + 3) + base;   //存储信息在e_ident[16]后四个字节中
    text_addr = ehdr->e_flags + base;  //存储信息在e_flags中

    nblock = ehdr->e_entry >> 16;
    nsize = ehdr->e_entry & 0xffff;

    H_LOGE("nblock =  0x%x,nsize:%d ehdr->e_machine:%d  ehdr->e_phoff:%d", nblock,nsize,ehdr->e_version,ehdr->e_phoff);
    H_LOGE("base =  0x%x  text_addr =  0x%x", base , text_addr);
    printf("nblock = %d\n", nblock);

    /*第一个参数：需要修改内存的起始地址

    必须需要页面对齐，也就是必须是页面PAGE_SIZE(0x1000=4096)的整数倍

    第二个参数：需要修改的大小

    占用的页数*PAGE_SIZE

    第三个参数：权限值*/

    if(mprotect((void *) (text_addr / PAGE_SIZE * PAGE_SIZE), 4096 * nsize, PROT_READ | PROT_EXEC | PROT_WRITE) != 0){
        puts("mem privilege change failed");
        H_LOGE("mem privilege change failed");
    }

    for(i=0;i< nblock; i++){
        char *addr = (char*)(text_addr + i);
        *addr = ~(*addr);
    }
    H_LOGE("base =  0x%x  text_addr =  0x%x", base , text_addr);

    if(mprotect((void *) (text_addr / PAGE_SIZE * PAGE_SIZE), 4096 * nsize, PROT_READ | PROT_EXEC) != 0){
        puts("mem privilege change failed");
    }
    puts("Decrypt success");
    #endif
}

unsigned long getLibAddr(){
    unsigned long ret = 0;
    char name[] = "myso";
    char buf[4096], *temp;
    int pid;
    FILE *fp;
    pid = getpid();
    sprintf(buf, "/proc/%d/maps", pid);
    fp = fopen(buf, "r");
    if(fp == NULL)
    {
        puts("open failed");
        goto _error;
    }
    while(fgets(buf, sizeof(buf), fp)){
        if(strstr(buf, name)){
            temp = strtok(buf, "-");
            ret = strtoul(temp, NULL, 16);
            break;
        }
    }
    _error:
    fclose(fp);
    return ret;
}


jstring get_StringFromNative(JNIEnv *env, jobject jobj) {
    //int i = gettid();
//    char *text = "I am from C";
//    int x = 0;
//    int y = 5 / x;
//    x =+ y;

//    char * exception = NULL;
//    strcmp(exception, "anr");
//    sprintf(exception, "anr");
//    kill(getpid(), SIGABRT);
//    __android_log_assert("a","b",NULL);
    return env->NewStringUTF("string from native");
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