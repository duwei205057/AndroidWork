#include <dlfcn.h>
#include "log.h"
#include <asm/sigcontext.h>
#include <map>
#include "stacktrace.h"
#include "backtrace.h"

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "xx"
#endif

static const char* CHECK_LIST = "libsogouime.so";
typedef struct {
    unsigned long regs[16];
}unwind_context;

#ifndef __thumb__
#define GET_CONTEXT(uc) (({					\
  unwind_context *unw_ctx = (uc);					\
  register unsigned long *unw_base asm ("r0") = unw_ctx->regs;		\
  __asm__ __volatile__ (						\
    "stmia %[base], {r0-r15}"						\
    : : [base] "r" (unw_base) : "memory");				\
  }), 0)
#else /* __thumb__ */
#define GET_CONTEXT(uc) (({					\
  unwind_context *unw_ctx = (uc);					\
  register unsigned long *unw_base asm ("r0") = unw_ctx->regs;		\
  __asm__ __volatile__ (						\
    ".align 2\nbx pc\nnop\n.code 32\n"					\
    "stmia %[base], {r0-r15}\n"						\
    "orr %[base], pc, #1\nbx %[base]"					\
    : [base] "+r" (unw_base) : : "memory", "cc");			\
  }), 0)
#endif

void* ReadMemory(uint8_t *local, ADDR* src, int size_in_int8) {
    if(local == NULL || src ==NULL) return NULL;
    ADDR stack_len = size_in_int8*sizeof(uint8_t);
    memcpy(local, src, stack_len);
    return local;
}

static int index = 0;
multimap<int,int> memleak;

void show_stack(size_t size, void *old_addr, void *new_addr, int op) {
    sigcontext sigctx = {0};
    unwind_context uc = {0};
    GET_CONTEXT(&uc);
    unsigned long *pDst = (unsigned long *)&sigctx;
    memcpy(pDst + 3, &uc, sizeof(long)*16);
    int ret = 1;
    int step = 0;
    index ++;
    LOGE("INDEX:%d -------------------------------------------------------------\n", index);
    while(ret > 0 && step < 6) {
        Dl_info info = {0};
        ret = stacktrace::Step(&sigctx);
        step++;
        dladdr((void *)sigctx.arm_pc, &info);
        if(info.dli_fname != 0 && (strstr(info.dli_fname, "memcheck") != 0)) {
            LOGE("INDEX:%d [%08x] [%08x] size = %d malloc by [%08x]:%s\n",index, old_addr, new_addr, size, (char*)sigctx.arm_pc - (char*)info.dli_fbase, info.dli_fname);
            continue;
        }
        if(info.dli_fname != 0 && (strstr(info.dli_fname, CHECK_LIST) != 0)) {
            if(info.dli_sname != 0 && strstr(info.dli_sname, "Znwj") != 0) continue;
//            Dl_info dl_info = {0};
//            dladdr((void *)sigctx.arm_pc, &dl_info);
            if ((char*)sigctx.arm_pc - (char*)info.dli_fbase == 0x0004fa4b) {
                LOGE("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh %08x", new_addr);
            }
            if(op == MALLOC) {
                //LOGE("[%08x] malloc %d by [%08x]:%s\n", addr, size, (char*)sigctx.arm_pc - (char*)dl_info.dli_fbase, dl_info.dli_fname);
                LOGE("INDEX:%d [%08x] [%08x] size = %d malloc by [%08x]:%s\n",index, old_addr, new_addr, size, (char*)sigctx.arm_pc - (char*)info.dli_fbase, info.dli_fname);
                memleak.insert(pair<int, int>((int)new_addr, (char*)sigctx.arm_pc - (char*)info.dli_fbase));
            } else if(op == REALLOC) {
                    LOGE("INDEX:%d [%08x] [%08x] size = %d realloc by [%08x]:%s\n",index,  old_addr, new_addr, size, (char*)sigctx.arm_pc - (char*)info.dli_fbase, info.dli_fname);
            } else if(op == FREE) {
                LOGE("INDEX:%d [%08x] [%08x] free by [%08x]:%s\n",index,  old_addr, new_addr, (char*)sigctx.arm_pc - (char*)info.dli_fbase, info.dli_fname);
                memleak.erase((int)old_addr);
            }
//            break;
        }
    }
    return;
}

void show_leak() {
    multimap<int, int>::iterator iter;
    for(iter = memleak.begin(); iter != memleak.end(); iter++) {
        LOGE("LEAKINFO ---addr=%08x------------func=%08x----------------------------------------------\n", iter->first, iter->second);
    }
    memleak.clear();
}