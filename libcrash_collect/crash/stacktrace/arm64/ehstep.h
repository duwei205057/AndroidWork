#if defined(__aarch64__)

#include <stdint.h>
#include <asm/sigcontext.h>
#include "clog.h"
#include "sys_bits.h"
#include "pointer.h"
#include "leb128.h"
#include "eh_frame.h"

#define DW_CFA_advance_loc 0x40
#define DW_CFA_offset 0x80
#define DW_CFA_restore 0xc0
#define DW_CFA_nop 0x00
#define DW_CFA_set_loc 0x01
#define DW_CFA_advance_loc1 0x02
#define DW_CFA_advance_loc2 0x03
#define DW_CFA_advance_loc4 0x04
#define DW_CFA_offset_extended 0x05
#define DW_CFA_restore_extended 0x06
#define DW_CFA_undefined 0x07
#define DW_CFA_same_value 0x08
#define DW_CFA_register 0x09
#define DW_CFA_remember_state 0x0a
#define DW_CFA_restore_state 0x0b
#define DW_CFA_def_cfa 0x0c
#define DW_CFA_def_cfa_register 0x0d
#define DW_CFA_def_cfa_offset 0x0e
#define DW_CFA_def_cfa_expression 0x0f
#define DW_CFA_expression 0x10
#define DW_CFA_offset_extended_sf 0x11
#define DW_CFA_def_cfa_sf 0x12
#define DW_CFA_def_cfa_offset_sf 0x13
#define DW_CFA_val_offset 0x14
#define DW_CFA_val_offset_sf 0x15
#define DW_CFA_val_expression 0x16
#define DW_CFA_lo_user 0x1c
#define DW_CFA_hi_user 0x3f

#define ARM64_SP 31
#define ARM64_PC 32

#define NUM_REGS 33

struct dwarf_frame {
    uint64_t regs[NUM_REGS];
    CIE *cie;
    FDE *fde;
    uint64_t cfa;
    uint64_t cfa_pc;
    /* Valid when DW_FRAME_CFA_REG_OFFSET is set in flags */
    uint32_t cfa_register;
    int64_t cfa_offset;
};

int execute_insns(uint8_t *insn_start, uint8_t *insn_end, dwarf_frame *);
int copyregs_to_frame(sigcontext *, dwarf_frame *);
int copyregs_to_sigctx(dwarf_frame *, sigcontext *);

#endif