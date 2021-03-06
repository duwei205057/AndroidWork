#if defined(__arm__)

#include "backtrace.h"
#include "../../utils/memcheck.h"

addr_s move_to_valid_addr(addr_s exidx_start) {
    /*key-value各占32bits*/
    addr_s value = *((addr_s *) exidx_start + 1);
    while(value == 0x1) {
        exidx_start = exidx_start + 8;
        value = *((addr_s *) exidx_start + 1);
    }
    return exidx_start;
}

int step(sigcontext *sig_ctx, bool bNextCode) {
    trace_cursor cursor;
    copyregs_to_cursor(sig_ctx, &cursor);
    Dl_info dlip;
    const addr_s sp = (addr_s) cursor.regs[ARM_SP];
    addr_s pc = (addr_s) cursor.regs[ARM_PC];
    memset(&dlip, 0, sizeof(dlip));
    dladdr((void *)pc, &dlip);
    if(dlip.dli_fname == NULL) {
        return -1;
    }
    /*elf头地址*/
    Elf32_Ehdr * pELFHdr = (Elf32_Ehdr *) dlip.dli_fbase;
    /*程序头头地址*/
    Elf32_Phdr * pPHdr = (Elf32_Phdr *)((char*) dlip.dli_fbase + pELFHdr->e_phoff);
    for(int i = pELFHdr->e_phnum-1; i >= 0; i--) {
        if(((Elf32_Phdr *)(pPHdr + i))->p_type == ARM_EXIDX) {
            pPHdr += i;
            break;
        }
    }
    if(pPHdr->p_type != ARM_EXIDX) return -1;

    if ( bNextCode ) {
        pc = get_real_pc(pc);
    }

    /*计算exidx段在物理内存中的地址*/
    addr_s exidx_start = (addr_s )(pPHdr->p_paddr + (char*) dlip.dli_fbase);
    CRASH_LOGE("  phdr offset=%08lx  \n",  pPHdr->p_paddr);
    cursor.exidx_start_addr = move_to_valid_addr(exidx_start);
    cursor.exidx_end_addr = (addr_s )(exidx_start + pPHdr->p_memsz - 8);
    cursor.base_addr = (addr_s ) dlip.dli_fbase;
    cursor.pc_offest = (addr_s )(pc - (addr_s ) dlip.dli_fbase);
    CRASH_LOGE("  cursor start=%08lx end=%08lx load addr=%08lx lib name=%s ps offset=%08lx \n",  cursor.exidx_start_addr,cursor.exidx_end_addr,cursor.base_addr,dlip.dli_fname,cursor.pc_offest);
    int ret = arm_exidx_step(&cursor);
    CRASH_LOGE("  step ret:%d\n", ret);
    copyregs_to_sigctx(&cursor, sig_ctx);
    return ret;
}
#endif