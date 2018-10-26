/*
 * exception_handler.cpp
 *
 *  Created on: 2015-10-28
 *      Author: chenjinyi
 */

#include <jni.h>
#include <unistd.h>
#include "exception_handler.h"

extern JavaVM* g_vm;

int keep_running = 1;

namespace native_crash_collector {

    char* ExceptionHandler::crash_save_path = NULL;
    char* ExceptionHandler::anr_save_path = NULL;
    char* ExceptionHandler::package_name = NULL;
    char* ExceptionHandler::head_info = NULL;
    char* ExceptionHandler::dump_java_info = NULL;
    bool ExceptionHandler::collect_anr_on = true;
    bool ExceptionHandler::collect_crash_on = true;
    int ExceptionHandler::ms_tidCrash = 0;
    int ExceptionHandler::keyboard_state = -1;
    pthread_cond_t ExceptionHandler::cond_finish = PTHREAD_COND_INITIALIZER;
    pthread_mutex_t ExceptionHandler::mutex_finish = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t ExceptionHandler::cond = PTHREAD_COND_INITIALIZER;
    pthread_mutex_t ExceptionHandler::mutex = PTHREAD_MUTEX_INITIALIZER;
    JavaVM *ExceptionHandler::javaVM = NULL;
    jclass ExceptionHandler::jobj = NULL;
    jmethodID ExceptionHandler::callbackId = NULL;
    pthread_t ntid;

    const int mExceptionSignals[] = {
        SIGSEGV, SIGABRT, SIGFPE, SIGILL, SIGBUS, SIGTRAP
    };

    const int mNumHandledSignals = sizeof(mExceptionSignals) / sizeof(mExceptionSignals[0]);

    struct sigaction old_handlers[mNumHandledSignals];

    ExceptionHandler::ExceptionHandler(JavaVM *vm) {
        if (javaVM != NULL) {
            javaVM = NULL;
        }
        javaVM = vm;
        keyboard_state = -1;
    }

    void ExceptionHandler::InitCrashCollect() {
        InstallHandlersLocked();
        RegisterJavaDumpThread();
    }

    void ExceptionHandler::InitAnrCollect() {
        RegisterANRHandler();
    }

    void ExceptionHandler::HandleSignal(int sig, siginfo_t* info, void* uc) {
        CrashContext *context = new CrashContext();
        memset(context, 0, sizeof(*context));
        memcpy(&context->siginfo, info, sizeof(siginfo_t));
        memcpy(&context->context, uc, sizeof(struct ucontext));

        if(collect_crash_on) {
            CRASH_LOGD("crash_on");
            ms_tidCrash = gettid();
            if (ntid != NULL){
                pthread_mutex_lock(&mutex);
                pthread_cond_signal(&cond);
                pthread_mutex_unlock(&mutex);
//                pthread_join(ntid, NULL);
                WaitDumpJava();
            }
            handle_crash(crash_save_path, head_info, dump_java_info, context);
        }
        delete context;

        old_handlers[sig].sa_handler(sig);
        exit(0);
    }

    bool ExceptionHandler::InstallHandlersLocked() {

        for (int i = 0; i < mNumHandledSignals; ++i) {
            if (sigaction(mExceptionSignals[i], NULL, &old_handlers[i]) == -1)
                return false;
        }

        stack_t stack;
        memset(&stack, 0, sizeof(stack));
/* Reserver the system default stack size. We don't need that much by the way. */
        stack.ss_size = SIGSTKSZ;
        stack.ss_sp = malloc(stack.ss_size);
        stack.ss_flags = 0;
/* Install alternate stack size. Be sure the memory region is valid until you revert it. */
        if (stack.ss_sp != NULL && sigaltstack(&stack, NULL) != 0) {
            return false;
        }

        struct sigaction sa;
        memset(&sa, 0, sizeof(sa));
        sigemptyset(&sa.sa_mask);

        for (int i = 0; i < mNumHandledSignals; ++i)
            sigaddset(&sa.sa_mask, mExceptionSignals[i]);

        sa.sa_sigaction = HandleSignal;
        sa.sa_flags = SA_RESETHAND | SA_ONSTACK | SA_SIGINFO;

        for (int i = 0; i < mNumHandledSignals; ++i) {
            if (sigaction(mExceptionSignals[i], &sa, NULL) == -1) {
            }
        }

        return true;
    }

    void* ExceptionHandler::InitANRFileNotify(void* para) {
        int inotify_fd;
        keep_running = 1;
        inotify_fd = open_inotify_fd();
        if(inotify_fd > 0) {
            queue_t q;
            q = queue_create();
            int wd = 0;
            wd = watch_dir(inotify_fd, ANR_FILE_DIR, IN_ALL_EVENTS);
            if (wd > 0) {
                process_inotify_events(q, inotify_fd);
            }
            close_inotify_fd(inotify_fd);
            queue_destroy(q);
        }
        return NULL;
    }

    void ExceptionHandler::RegisterANRHandler() {
        pthread_t ntid;
        int err = pthread_create(&ntid, NULL, InitANRFileNotify, NULL);
    }

    void ExceptionHandler::SetAnrLogSavePath(const uint16_t *str16, uint32_t size) {
        if(anr_save_path != NULL)
            delete[] anr_save_path;
        anr_save_path = new char[size + 1];
        memset(anr_save_path, 0, size + 1);
        utf16_to_utf8(str16, size, anr_save_path, size);
    }

    void ExceptionHandler::SetCrashLogSavePath(const uint16_t *str16, uint32_t size) {
        if(crash_save_path != NULL)
            delete[] crash_save_path;
        crash_save_path = new char[size + 1];
        memset(crash_save_path, 0, size + 1);
        utf16_to_utf8(str16, size, crash_save_path, size);
    }

    void ExceptionHandler::SetPackageName(const uint16_t *str16, uint32_t size) {
        if(package_name != NULL)
            delete[] package_name;
        package_name = new char[size + 1];
        memset(package_name, 0, size + 1);
        utf16_to_utf8(str16, size, package_name, size);
    }

    void ExceptionHandler::SetKeyboardShownState(int state) {
        keyboard_state = state;
    }

    void ExceptionHandler::SetHeadInfo(const uint16_t *head, uint32_t size) {
        if(head_info != NULL)
            delete[] head_info;
        head_info = new char[size + 1];
        memset(head_info, 0, size + 1);
        utf16_to_utf8(head, size, head_info, size);
    }

    void ExceptionHandler::SetCollectSwitch(int which, bool state) {
        switch(which) {
            case CRASH_SWITCH :
                collect_crash_on = state;
                break;
            case ANR_SWITCH :
                collect_anr_on = state;
        }
    }

    void ExceptionHandler::SetDumpJavaFinish() {
        pthread_mutex_lock(&mutex_finish);
        pthread_cond_signal(&cond_finish);
        pthread_mutex_unlock(&mutex_finish);
    }

    void ExceptionHandler::WaitDumpJava(){
        struct timeval now;
        gettimeofday(&now, NULL);

        struct timespec outtime;
        outtime.tv_sec = now.tv_sec + c_waitSecond;
        outtime.tv_nsec = 0;
        pthread_mutex_lock(&mutex_finish);
        pthread_cond_timedwait(&cond_finish, &mutex_finish, &outtime);
        pthread_mutex_unlock(&mutex_finish);
    }

    void ExceptionHandler::RegisterJavaDumpThread() {

        char const *const kClassPathName = "com/sogou/nativecrashcollector/CrashCollectUtils";

        jclass clazz;
        JNIEnv* env = NULL;
        javaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4);

        clazz = env->FindClass(kClassPathName);
        jobj = (jclass) env->NewGlobalRef(clazz);
        callbackId = env->GetStaticMethodID(clazz, "getThreadStackTrace", "(I)Ljava/lang/String;");


        int err = pthread_create(&ntid, NULL, DumpJavaThreadInfo, NULL);
        if (err != 0) {
            CRASH_LOGD("create thread fail.");
        } else {
            CRASH_LOGD("create thread success.");
        }
    }

    void *ExceptionHandler::DumpJavaThreadInfo(void *argv) {
        int i;
        int status;
        JNIEnv *env;
        jboolean isAttached = JNI_FALSE;
        status = javaVM->GetEnv((void **) &env, JNI_VERSION_1_4);
        if (status < 0) {
            status = javaVM->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
            if (status < 0) return NULL;
            isAttached = JNI_TRUE;
        }
        pthread_mutex_lock(&mutex);
        //当条件不满足时等待
        while (ms_tidCrash == 0) {
            pthread_cond_wait(&cond, &mutex);

        }
        jstring ss = (jstring) env->CallStaticObjectMethod(jobj, callbackId, ms_tidCrash);//回调java层方法
        dump_java_info = (char *) env->GetStringUTFChars(ss, NULL);
        pthread_mutex_unlock(&mutex);
        SetDumpJavaFinish();

        if (isAttached) javaVM->DetachCurrentThread();      //当前线程退出的话,讲线程从vm的注销.
        return NULL;
    }

}
