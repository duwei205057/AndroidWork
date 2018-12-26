//
// Created by chenjinyi on 17-7-12.
//

#ifndef NATIVE_EXCEPTION_COLLECT_CRASH_HANDLER_H
#define NATIVE_EXCEPTION_COLLECT_CRASH_HANDLER_H

#include <dlfcn.h>
#include <sys/ucontext.h>
#include "ucontext.h"
#include "report_writer.h"
#include <sys_bits.h>
#include <clog.h>
#include <memory.h>
#include <stacktrace.h>

#define REGISTER_CONTEXT_BEFORE (5)
#define REGISTER_CONTEXT_AFTER (10)

struct CrashContext {
    siginfo_t siginfo;
    pid_t tid;
    struct ucontext context;
};

void handle_crash(char *filepath, char *head_info, char *dump_info, void *ucontext);

void* read_memory(uint8_t *local, addr_s *src, int size_in_int8);

#endif //NATIVE_EXCEPTION_COLLECT_CRASH_HANDLER_H
