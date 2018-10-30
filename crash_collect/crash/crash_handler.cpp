#include <time.h>
#include "crash_handler.h"

#define MAX_STEP 30
/* 3*(4*256)Byte */
#define MAX_STACK 3*256
#define BUFFER_LENGTH 1024

#include <jni.h>
#include <unistd.h>
#include <dlfcn.h>
#include "exception_handler.h"

using namespace native_crash_collector;

extern JavaVM *g_vm;

const char* get_current_data() {
    time_t rawtime;
    struct tm *timeinfo;
    time(&rawtime);
    timeinfo = localtime(&rawtime);
    char *time_s = asctime(timeinfo);
    return time_s;
}


int check_mem_readable(unsigned long addr, int len)
{
    pid_t pid ;
    char access, maps[32] , buff[1024];
    unsigned long start_addr, end_addr, last_addr;
    FILE *fmap;

    pid = getpid();
    sprintf(maps, "/proc/%d/maps", pid);
    fmap = fopen(maps, "rb");
    if(!fmap){
        printf("open %s file failed!/n", maps);
        return 0;
    }

    while(fgets(buff, sizeof(buff)-1, fmap) != NULL) {
        /* "%*c"表示忽略第一个字符 */
        sscanf(buff, "%lx-%lx %*c%c", &start_addr, &end_addr, &access);
        if((addr >= start_addr) && (addr <= end_addr)){
            if('w' != access){
                fclose(fmap);
                return 0;
            }

            if((addr + len) < end_addr){
                fclose(fmap);
                return 1;
            }else {
                last_addr = end_addr;
                len = len - (end_addr - addr);
                addr = last_addr;
            }
        }
    }

    fclose(fmap);
    return 0;

}

bool IsReadable(addr_s addr, int size_in_byte) {
//    return access_ok(VERIFY_READ, addr, size_in_byte);
    return check_mem_readable(addr, size_in_byte) > 0;
}

// 获取指定地址前后的内存内容
void WriteMemoryNearby(ReportWriter *rw, addr_s addr, size_t ctx_before, size_t ctx_after) {
    if (NULL == rw) return;

    // 1. detect if addr is valid
    const char* format;
    const char* curformat;
    const char* errformat;
    const char* curerrformat;
    uint8_t* memLine;
    // 2. look up for memory
#if defined(__arm__)
    format = "    %08x";
    curformat = "--> %08x";
    errformat = "    %08x ----Illegal Address----\n";
    curerrformat = "--> %08x ----Illegal Address----\n";
    uint8_t local[16] = {0};
    memLine = local;
#elif defined(__aarch64__)
    format = "    %016llx";
    curformat = "--> %016llx";
    errformat = "    %016llx Illegal Address\n";
    curerrformat = "--> %016llx Illegal Address\n";
    uint8_t local[32] = {0};
    memLine = local;
#endif
    int wordLineBytes = sizeof(memLine);
    int i=0;
    ctx_before = ctx_before * wordLineBytes > addr? addr - addr % wordLineBytes: ctx_before * wordLineBytes;
    ctx_after = ctx_after * wordLineBytes;
    addr_s addr_pt = addr - ctx_before;
    while (ctx_before > 0) {
        if (IsReadable(addr_pt, wordLineBytes)) {
            read_memory(memLine, (addr_s *)addr_pt, wordLineBytes);
            rw->Write(format, addr_pt);
            for (i=0;i< sizeof(memLine);i++) {
                rw->Write(" %08x", *(memLine + i));
            }
            rw->Write("\n");
            addr_pt += wordLineBytes;
            ctx_before -= wordLineBytes;
        } else {
            rw->Write(errformat, addr_pt);
            return;
        }
    }

    if (!IsReadable(addr, wordLineBytes)) {
        rw->Write(curerrformat, addr);
        return;
    } else {
        read_memory(memLine, (addr_s *)addr_pt, wordLineBytes);
        rw->Write(curformat, addr_pt);
        for (i=0;i< sizeof(memLine);i++) {
            rw->Write(" %08x", *(memLine + i));
        }
        rw->Write("\n");
        addr_pt += wordLineBytes;

        while (ctx_after > 0) {
            if (IsReadable(addr_pt, wordLineBytes)) {
                read_memory(memLine, (addr_s *)addr_pt, wordLineBytes);
                rw->Write(format, addr_pt);
                for (i=0;i< sizeof(memLine);i++) {
                    rw->Write(" %08x", *(memLine + i));
                }
                rw->Write("\n");
                addr_pt += wordLineBytes;
                ctx_after -= wordLineBytes;
            } else {
                rw->Write(errformat, addr_pt);
                return;
            }
        }
    }
}


void handle_crash(char *filepath, char *head_info, char *dump_java_info ,void *ucontext) {
    CRASH_LOGD("\nhandle_crash start\n");
    CrashContext *context = (CrashContext *) ucontext;
    siginfo_t* info = &context->siginfo;
    sigcontext* sig_ctx = &context->context.uc_mcontext;

    ReportWriter *rw = new ReportWriter(filepath);
    if(!rw->open_success) {
        CRASH_LOGE("\nreport writer open fail:%s\n", filepath);
        return;
    }

    if(head_info != NULL) rw->Write(head_info);
    Dl_info *dlip = new Dl_info;

    if(dump_java_info != NULL) {
        rw->Write("\n******Java Crash Report******\n");
        CRASH_LOGE("\ndump_java_info content==:%s length==:%d\n", dump_java_info, strlen(dump_java_info));
        rw->WriteString(dump_java_info);
    }

    rw->Write("\n******Native Crash Report******\n");

#if defined(__aarch64__)
    const addr_s pc = (addr_s) sig_ctx->pc;
    const addr_s sp = (addr_s) sig_ctx->sp;

    CRASH_LOGE("\n******Native Crash 64******\n");
    /*1 error code*/
    memset(dlip, 0, sizeof(dlip));
    dladdr((void *)pc, dlip);
    rw->Write("\n[Error Code] %s_%llx  \n", dlip->dli_fname, pc-(addr_s )dlip->dli_fbase);
    CRASH_LOGE("\n[Error Code] %s_%llx  %llx  \n", dlip->dli_fname, pc-(addr_s )dlip->dli_fbase, dlip->dli_saddr);

    /*2 crash time*/
    rw->Write("\n[Time] %s  ", get_current_data());
    CRASH_LOGE("\n[Time] %s  ", get_current_data());
    /*3 sign info*/
    rw->Write("\n[Signal number] %d  [Signal code] %d  [Signal errno] %d  [Fault addr] %08x", info->si_signo, info->si_code, info->si_errno, info->si_addr);
    CRASH_LOGE("\n[Signal number] %d  [Signal code] %d  [Signal errno] %d  [Fault addr] %08x", info->si_signo, info->si_code, info->si_errno, info->si_addr);
    /*4 keyboard state*/

    /*5 registers*/
    rw->Write("\n\n[Registers]  \n");
    rw->Write("  x00 %016llx  x01 %016llx  x02 %016llx  \n", (addr_s) sig_ctx->regs[0], (addr_s) sig_ctx->regs[1], (addr_s) sig_ctx->regs[2]);
    rw->Write("  x03 %016llx  x04 %016llx  x05 %016llx  \n", (addr_s) sig_ctx->regs[3], (addr_s) sig_ctx->regs[4], (addr_s) sig_ctx->regs[5]);
    rw->Write("  x06 %016llx  x07 %016llx  x08 %016llx  \n", (addr_s) sig_ctx->regs[6], (addr_s) sig_ctx->regs[7], (addr_s) sig_ctx->regs[8]);
    rw->Write("  x09 %016llx  x10 %016llx  x11 %016llx  \n", (addr_s) sig_ctx->regs[9], (addr_s) sig_ctx->regs[10], (addr_s) sig_ctx->regs[11]);
    rw->Write("  x12 %016llx  x13 %016llx  x14 %016llx  \n", (addr_s) sig_ctx->regs[12], (addr_s) sig_ctx->regs[13], (addr_s) sig_ctx->regs[14]);
    rw->Write("  x15 %016llx  x16 %016llx  x17 %016llx  \n", (addr_s) sig_ctx->regs[15], (addr_s) sig_ctx->regs[16], (addr_s) sig_ctx->regs[17]);
    rw->Write("  x18 %016llx  x19 %016llx  x20 %016llx  \n", (addr_s) sig_ctx->regs[18], (addr_s) sig_ctx->regs[19], (addr_s) sig_ctx->regs[20]);
    rw->Write("  x21 %016llx  x22 %016llx  x23 %016llx  \n", (addr_s) sig_ctx->regs[21], (addr_s) sig_ctx->regs[22], (addr_s) sig_ctx->regs[23]);
    rw->Write("  x24 %016llx  x25 %016llx  x26 %016llx  \n", (addr_s) sig_ctx->regs[24], (addr_s) sig_ctx->regs[25], (addr_s) sig_ctx->regs[26]);
    rw->Write("  x27 %016llx  x28 %016llx  x29 %016llx  \n", (addr_s) sig_ctx->regs[27], (addr_s) sig_ctx->regs[28], (addr_s) sig_ctx->regs[29]);
    rw->Write("  x30 %016llx  sp %016llx  pc %016llx  \n", (addr_s) sig_ctx->regs[30], (addr_s) sig_ctx->sp, (addr_s) sig_ctx->pc);
    /*6 invoke link*/

    CRASH_LOGE("\n\n[Backtrace]  \n");

    /*7 backtrace*/
    rw->Write("\n\n[Backtrace]  \n");
    CRASH_LOGE("\n\n[Backtrace]  \n");
    int ret = 1;
    int step = 0;
    while(ret > 0 &&  step < MAX_STEP) {
        memset(dlip, 0, sizeof(dlip));
        dladdr((void *)sig_ctx->pc, dlip);
        rw->Write("  #%02d %016llx %s \n", step, sig_ctx->pc-(addr_s )dlip->dli_fbase, dlip->dli_fname, dlip->dli_sname == NULL ? "" : dlip->dli_sname);
        CRASH_LOGE("  #%02d %016llx %s %s\n", step, sig_ctx->pc-(addr_s )dlip->dli_fbase, dlip->dli_fname, dlip->dli_sname == NULL ? "" : dlip->dli_sname);
        step++;
        ret = stacktrace::Step(sig_ctx);
    }
    // TODO scan memory near the registers

#elif defined(__arm__)


    const addr_s pc = (addr_s) sig_ctx->arm_pc;
    const addr_s sp = (addr_s) sig_ctx->arm_sp;

    CRASH_LOGE("\n******Native Crash 32******\n");
    /*1 error code*/
    memset(dlip, 0, sizeof(dlip));
    dladdr((void *)pc, dlip);
    rw->Write("\n[Error Code] %s_%x  \n", dlip->dli_fname, pc-(addr_s )dlip->dli_fbase);
    CRASH_LOGE("\n[Error Code] %s_%lx  \n", dlip->dli_fname, pc-(addr_s )dlip->dli_fbase);

    /*2 crash time*/
    rw->Write("\n[Time] %s  ", get_current_data());

    /*3 sign info*/
    rw->Write("\n[Signal number] %d  [Signal code] %d  [Signal errno] %d  [Fault addr_s] %08x", info->si_signo, info->si_code, info->si_errno, info->si_addr);
    CRASH_LOGE("\n[Signal number] %d  [Signal code] %d  [Signal errno] %d  [Fault addr_s] %08x", info->si_signo, info->si_code, info->si_errno, info->si_addr);

    /*4 keyboard state*/
    int state = ExceptionHandler::keyboard_state;
    if (state >= 0 ) rw->Write("\n\n[Keyboard Show State] %d  ", state);

    /*5 registers*/
    rw->Write("\n\n[Registers]  \n");
    rw->Write("  r0 %08x  ",(addr_s) sig_ctx->arm_r0);
    rw->Write("r1 %08x  ",(addr_s) sig_ctx->arm_r1);
    rw->Write("r2 %08x  ",(addr_s) sig_ctx->arm_r2);
    rw->Write("r3 %08x  \n",(addr_s) sig_ctx->arm_r3);
    rw->Write("  r4 %08x  ",(addr_s) sig_ctx->arm_r4);
    rw->Write("r5 %08x  ",(addr_s) sig_ctx->arm_r5);
    rw->Write("r6 %08x  ",(addr_s) sig_ctx->arm_r6);
    rw->Write("r7 %08x  \n",(addr_s) sig_ctx->arm_r7);
    rw->Write("  r8 %08x  ",(addr_s) sig_ctx->arm_r8);
    rw->Write("r9 %08x  ",(addr_s) sig_ctx->arm_r9);
    rw->Write("sl %08x  ",(addr_s) sig_ctx->arm_r10);
    rw->Write("fp %08x  \n",(addr_s) sig_ctx->arm_fp);
    rw->Write("  ip %08x  ",(addr_s) sig_ctx->arm_ip);
    rw->Write("sp %08x  ",(addr_s) sig_ctx->arm_sp);
    rw->Write("lr %08x  ",(addr_s) sig_ctx->arm_lr);
    rw->Write("pc %08x  ",(addr_s) sig_ctx->arm_pc);

    /* 8 memory near the registers */
    rw->Write("\n\n[Memory]  \n");
    rw->Write("r0 %08x  \n",(addr_s) sig_ctx->arm_r0);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r0, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r1 %08x  \n",(addr_s) sig_ctx->arm_r1);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r1, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r2 %08x  \n",(addr_s) sig_ctx->arm_r2);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r2, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r3 %08x  \n",(addr_s) sig_ctx->arm_r3);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r3, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r4 %08x  \n",(addr_s) sig_ctx->arm_r4);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r4, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r5 %08x  \n",(addr_s) sig_ctx->arm_r5);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r5, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r6 %08x  \n",(addr_s) sig_ctx->arm_r6);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r6, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r7 %08x  \n",(addr_s) sig_ctx->arm_r7);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r7, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r8 %08x  \n",(addr_s) sig_ctx->arm_r8);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r8, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("r9 %08x  \n",(addr_s) sig_ctx->arm_r9);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r9, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("sl %08x  \n",(addr_s) sig_ctx->arm_r10);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_r10, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("fp %08x  \n",(addr_s) sig_ctx->arm_fp);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_fp, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("ip %08x  \n",(addr_s) sig_ctx->arm_ip);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_ip, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("sp %08x  \n",(addr_s) sig_ctx->arm_sp);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_sp, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("lr %08x  \n",(addr_s) sig_ctx->arm_lr);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_lr, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);
    rw->Write("pc %08x  \n",(addr_s) sig_ctx->arm_pc);
    WriteMemoryNearby(rw, (addr_s) sig_ctx->arm_pc, REGISTER_CONTEXT_BEFORE,
                      REGISTER_CONTEXT_AFTER);

    /*6 invoke link*/

    /*7 backtrace*/
    rw->Write("\n\n[Backtrace]  \n");
    int ret = 1;
    int step = 0;
    while(ret > 0 &&  step < MAX_STEP) {
        memset(dlip, 0, sizeof(dlip));
        dladdr((void *)sig_ctx->arm_pc, dlip);
        CRASH_LOGE("  #%02d pc=%08lx %s \n", step, sig_ctx->arm_pc, dlip->dli_fname);
        CRASH_LOGE("  #%02d %08lx %s \n", step, sig_ctx->arm_pc-(addr_s )dlip->dli_fbase, dlip->dli_fname);
        rw->Write("  #%02d %08x %s \n", step++, sig_ctx->arm_pc-(addr_s )dlip->dli_fbase, dlip->dli_fname);
        if(step <= 2 && dlip->dli_fname == NULL) {
            int stack_offset = 0;
            uint8_t local[4] = {0};
            while(stack_offset < MAX_STACK && step < MAX_STEP) {
                uint32_t *data = (uint32_t *) read_memory(local, (addr_s *) (sp + stack_offset * 4),
                                                          4);
                if(data == NULL) break;
                memset(dlip, 0, sizeof(dlip));
                dladdr((void *)(*data), dlip);
                if(dlip->dli_fname != NULL) {
                    rw->Write("  $%02d %08x %s %s  \n", step++, *data-(addr_s )dlip->dli_fbase, dlip->dli_fname, dlip->dli_sname);
                }
                stack_offset++;
            }
            break;
        }
        if(dlip->dli_sname != NULL) {
            rw->Write("               %s\n",dlip->dli_sname);
        }
        rw->Write("               r4 %08x  ",(addr_s) sig_ctx->arm_r4);
        rw->Write("r5 %08x  ",(addr_s) sig_ctx->arm_r5);
        rw->Write("r6 %08x  ",(addr_s) sig_ctx->arm_r6);
        rw->Write("r7 %08x  ",(addr_s) sig_ctx->arm_r7);
        rw->Write("r8 %08x  ",(addr_s) sig_ctx->arm_r8);
        rw->Write("r9 %08x  ",(addr_s) sig_ctx->arm_r9);
        rw->Write("sl %08x  ",(addr_s) sig_ctx->arm_r10);
        rw->Write("fp %08x  ",(addr_s) sig_ctx->arm_fp);
        rw->Write("ip %08x  ",(addr_s) sig_ctx->arm_ip);
        rw->Write("sp %08x\n\n",(addr_s) sig_ctx->arm_sp);

        if(dlip->dli_sname != NULL && strcmp(dlip->dli_sname, "__pthread_clone") == 0) {
            int stack_offset = 0;
            uint8_t local[4] = {0};
            while(stack_offset < MAX_STACK && step < MAX_STEP) {
                uint32_t *data = (uint32_t *) read_memory(local, (addr_s *) (sp + stack_offset * 4),
                                                          4);
                if(data == NULL) break;
                memset(dlip, 0, sizeof(dlip));
                dladdr((void *)(*data), dlip);
                if(dlip->dli_fname != NULL) {
                    sig_ctx->arm_sp += stack_offset * 4 + 4;
                    sig_ctx->arm_pc = *data;
                    rw->Write("  $%02d %08x %s %s  \n", step++, *data-(addr_s )dlip->dli_fbase, dlip->dli_fname, dlip->dli_sname);
                    ret = 1;
                    break;
                }
                stack_offset++;
            }
        }

        ret = stacktrace::Step(sig_ctx);
        if(ret == -2) {
            int stack_offset = 0;
            uint8_t local[4] = {0};
            while(stack_offset < MAX_STACK && step < MAX_STEP) {
                uint32_t *data = (uint32_t *) read_memory(local, (addr_s *) (sig_ctx->arm_sp +
                                                                             stack_offset * 4), 4);
                if(data == NULL) break;
                memset(dlip, 0, sizeof(dlip));
                dladdr((void *)(*data), dlip);
                if(dlip->dli_fname != NULL) {
                    sig_ctx->arm_sp += stack_offset * 4 + 4;
                    sig_ctx->arm_pc = *data;
                    rw->Write("  $%02d %08x %s %s  \n", step++, *data-(addr_s )dlip->dli_fbase, dlip->dli_fname, dlip->dli_sname);
                    ret = 1;
                    break;
                }
                stack_offset++;
            }
        }
    }

#endif
    CRASH_LOGD("\n******Native Crash Report End******\n");
    rw->Write("\n******Native Crash Report End******\n");
    delete dlip;
    delete rw;

}

void* read_memory(uint8_t *local, addr_s *src, int size_in_int8) {
    if(local == NULL || src ==NULL) return NULL;
    addr_s stack_len = size_in_int8 * sizeof(uint8_t);
    memcpy(local, src, stack_len);
    return local;
}
