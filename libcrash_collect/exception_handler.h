/*
 * exception_handler.h
 *
 *  Created on: 2015-10-28
 *      Author: chenjinyi
 */

#ifndef EXCEPTION_HANDLER_H_
#define EXCEPTION_HANDLER_H_

#include <signal.h>
#include <stdint.h>
#include "environment.h"
#include "utils/clog.h"
#include "utils/utf.h"
#include "event_queue.h"
#include "inotify_utils.h"
#ifndef BREAKPAD_ENABLE
#include "crash_handler.h"
#else
#include <client/linux/handler/minidump_descriptor.h>
#include "client/linux/handler/exception_handler.h"
#endif
#include <pthread.h>
#include <jni.h>


namespace native_crash_collector {

class ExceptionHandler {

public:

    ExceptionHandler(JavaVM *vm);

    void InitCrashCollect();
    void InitAnrCollect();
    void SetAnrLogSavePath(const uint16_t *str16, uint32_t size);
    void SetCrashLogSavePath(const uint16_t *str16, uint32_t size);
    void SetPackageName(const uint16_t *str16, uint32_t size);
    void SetHeadInfo(const uint16_t *head, uint32_t size);
    void SetKeyboardShownState(int state);
    void SetCollectSwitch(int which, bool state);
    static void SetDumpJavaFinish();

    static void WaitDumpJava();

    static bool collect_anr_on;
    static bool collect_crash_on;
    static char* crash_save_path;
    static char* anr_save_path;
    static char* head_info;
    static char* package_name;
    static int keyboard_state;
    static pthread_cond_t cond_finish;
    static pthread_mutex_t mutex_finish;
    static pthread_cond_t cond;
    static pthread_mutex_t mutex;
    static jclass jobj;
    static jmethodID callbackId;
    static char* dump_java_info;

    static const int c_waitSecond = 5;
    static JavaVM *javaVM;

    ~ExceptionHandler() {
        delete crash_save_path;
        delete anr_save_path;
        delete head_info;
        delete package_name;
        head_info = 0;
    };

private:
    void RegisterANRHandler();
    static void* InitANRFileNotify(void* para);
    static void HandleSignal(int sig, siginfo_t* info, void* uc);
    static bool InstallHandlersLocked();

    static int ms_tidCrash;

    static void *DumpJavaThreadInfo(void *argv);

    static void RegisterJavaDumpThread();

#ifdef BREAKPAD_ENABLE
    static bool dumpCallback(const google_breakpad::MinidumpDescriptor &descriptor, void *context, bool succeeded);
#endif
};

}

#endif /* EXCEPTION_HANDLER_H_ */
