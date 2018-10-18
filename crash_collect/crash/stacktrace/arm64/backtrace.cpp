#if defined(__aarch64__)

#include <dlfcn.h>
#include "backtrace.h"

int step(sigcontext *sig_ctx) {
    dwarf_frame *frame = new dwarf_frame();
    Dl_info *dlip = new Dl_info();
    const addr_s pc = (addr_s) sig_ctx->pc;
    memset(dlip, 0, sizeof(Dl_info));
    dladdr((void *)pc, dlip);
    if(dlip->dli_fname == NULL || strstr(dlip->dli_fname, ".odex") != NULL) return -1;
    CRASH_LOGE("so name = %s", dlip->dli_fname);
    /*elf头地址*/
    Elf64_Ehdr * pELFHdr = (Elf64_Ehdr *) dlip->dli_fbase;
    /*程序头头地址*/
    Elf64_Phdr * pPHdr = (Elf64_Phdr *)((char*) dlip->dli_fbase + pELFHdr->e_phoff);
    for(int i = pELFHdr->e_phnum-1; i >= 0; i--) {
        if((pPHdr + i)->p_type == PT_GNU_EH_FRAME) {
            pPHdr += i;
            break;
        }
    }
    if(pPHdr->p_type != PT_GNU_EH_FRAME) return -1;
    /*计算eh_frame段在物理内存中的地址*/
    addr_s eh_frame_hdr_start = (addr_s )(pPHdr->p_paddr + (char*) dlip->dli_fbase);
    HDR *hdr = decode_hdr((uint8_t *) eh_frame_hdr_start);
    addr_s eh_frame_start = hdr->eh_frame_addr;
    CRASH_LOGE("eh_frame_offest = %016llx", eh_frame_start - (uint64_t) dlip->dli_fbase);
    CIE *cie = decode_cie((uint8_t *) eh_frame_start);
    frame->cie = cie;
    uint64_t offest = cie->total_length;
    for(; ;) {
        FDE *fde = decode_fde((uint8_t *)(eh_frame_start + offest), cie);
        if(fde->length == 0)
            return -1;
        addr_s pc_end = fde->pc_begin + fde->pc_range;
        if(sig_ctx->pc < pc_end && sig_ctx->pc >= fde->pc_begin) {
            CRASH_LOGE("pc_begin_offest = %016llx, pc_end_offest = %016llx, pc_offest = %016llx",
                   fde->pc_begin - (addr_s) dlip->dli_fbase, fde->pc_begin + fde->pc_range - (addr_s) dlip->dli_fbase, sig_ctx->pc - (addr_s) dlip->dli_fbase);
            frame->fde = fde;
            copyregs_to_frame(sig_ctx, frame);
            frame->cfa_pc = (uint64_t) fde->pc_begin;

            execute_insns(cie->initial_instructions,
                          cie->initial_instructions + cie->inst_length,
                          frame);

            execute_insns(fde->call_frame_instructions,
                          fde->call_frame_instructions + fde->inst_length,
                          frame);

            copyregs_to_sigctx(frame, sig_ctx);
            CRASH_LOGE("r29 = %016lx, r30 = %016lx, pc = %016lx, sp = %016lx", frame->regs[29], frame->regs[30], frame->regs[ARM64_PC], frame->regs[ARM64_SP]);
            break;
        }
        offest += fde->total_length;
        delete fde;
    }
    delete frame;
    delete dlip;
    //if(ret < 0) return ret;
    return 1;
}

#endif