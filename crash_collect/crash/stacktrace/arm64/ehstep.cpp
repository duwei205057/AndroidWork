#if defined(__aarch64__)
#include "ehstep.h"

int execute_insns(uint8_t *insn_start, uint8_t *insn_end, dwarf_frame *frame) {
    if(frame == NULL) return -1;
    const uint8_t *current_insn = insn_start;
    int leb128_len = 0;
    uint32_t operand_uint32_1, operand_uint32_2;
    int32_t operand_int32_1, operand_int32_2;
    while (current_insn < insn_end && frame->cfa_pc <= frame->regs[ARM64_PC]) {
        uint8_t op = *current_insn;
        MOVE_1(current_insn);
        if((op & 0xc0) == DW_CFA_advance_loc) {
            operand_uint32_1 = (uint32_t) (op & 0x3f);
            operand_uint32_1 *= frame->cie->code_alignment_factor;
            CRASH_LOGE("DW_CFA_advance_loc %d", operand_uint32_1);
            frame->cfa_pc += operand_uint32_1;
            continue;
        }else if((op & 0xc0) == DW_CFA_offset) {
            operand_uint32_1 = (uint32_t) (op & 0x3f);
            leb128_len = read_uleb128(current_insn, &operand_uint32_2);
            MOVE_n(current_insn, leb128_len);
            CRASH_LOGE("DW_CFA_offset r%d %d", operand_uint32_1, operand_uint32_2);
            frame->regs[operand_uint32_1] = *(addr_s *) (frame->cfa + (int64_t) operand_uint32_2 * frame->cie->data_alignment_factor);
            continue;
        }else if((op & 0xc0) == DW_CFA_restore) {
            operand_uint32_1 = (uint32_t) (op & 0x3f);
            /*do something*/
            continue;
        }

        switch (op) {
            case DW_CFA_nop:
                CRASH_LOGE("DW_CFA_nop");
                frame->regs[ARM64_PC] = frame->regs[frame->cie->return_address_register] - 4;
                return 1;
            case DW_CFA_set_loc:
                /*do something*/
                break;
            case DW_CFA_advance_loc1:
                operand_uint32_1 = *current_insn;
                MOVE_1(current_insn);
                CRASH_LOGE("DW_CFA_advance_loc1 %d", operand_uint32_1);
                operand_uint32_1 *= frame->cie->code_alignment_factor;
                frame->cfa_pc += operand_uint32_1;
                break;
            case DW_CFA_advance_loc2:
                operand_uint32_1 = *(uint16_t *) current_insn;
                MOVE_2(current_insn);
                CRASH_LOGE("DW_CFA_advance_loc2 %d", operand_uint32_1);
                operand_uint32_1 *= frame->cie->code_alignment_factor;
                frame->cfa_pc += operand_uint32_1;
                break;
            case DW_CFA_advance_loc4:
                operand_uint32_1 = *(uint32_t *) current_insn;
                MOVE_4(current_insn);
                CRASH_LOGE("DW_CFA_advance_loc4 %d", operand_uint32_1);
                operand_uint32_1 *= frame->cie->code_alignment_factor;
                frame->cfa_pc += operand_uint32_1;
                break;
            case DW_CFA_offset_extended:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_uleb128(current_insn, &operand_uint32_2);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_offset_extended r%d %d", operand_uint32_1, operand_uint32_2);
                frame->regs[operand_uint32_1] = *(addr_s *) (frame->cfa + (int64_t) operand_uint32_2 * frame->cie->data_alignment_factor);
                break;
            case DW_CFA_restore_extended:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_restore_extended r%d", operand_uint32_1);
                /*do something*/
                break;
            case DW_CFA_undefined:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_undefined r%d", operand_uint32_1);
                /*no need do anything*/
                break;
            case DW_CFA_same_value:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_same_value r%d", operand_uint32_1);
                /*no need do anything*/
                break;
            case DW_CFA_register:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_uleb128(current_insn, &operand_uint32_2);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_register r%d r%d", operand_uint32_1, operand_uint32_2);
                frame->regs[operand_uint32_1] = frame->regs[operand_uint32_2];
                break;
            case DW_CFA_remember_state:
                CRASH_LOGE("DW_CFA_remember_state");
                /*do something*/
                break;
            case DW_CFA_restore_state:
                CRASH_LOGE("DW_CFA_restore_state");
                /*do something*/
                break;
            case DW_CFA_def_cfa:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_uleb128(current_insn, &operand_uint32_2);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_def_cfa r%d %d", operand_uint32_1, operand_uint32_2);
                frame->cfa_register = operand_uint32_1;
                frame->cfa_offset = operand_uint32_2;
                frame->cfa = frame->regs[frame->cfa_register] + frame->cfa_offset;
                break;
            case DW_CFA_def_cfa_register:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_def_cfa_register r%d", operand_uint32_1);
                frame->cfa_register = operand_uint32_1;
                frame->cfa = frame->regs[frame->cfa_register] + frame->cfa_offset;
                break;
            case DW_CFA_def_cfa_offset:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_def_cfa_offset %d", operand_uint32_1);
                frame->cfa_offset = operand_uint32_1;
                frame->cfa = frame->regs[frame->cfa_register] + frame->cfa_offset;
                break;
            case DW_CFA_def_cfa_expression:
                /*read data*/
                CRASH_LOGE("DW_CFA_def_cfa_expression");
                /*do something*/
                break;
            case DW_CFA_expression:
                /*read data*/
                CRASH_LOGE("DW_CFA_expression");
                /*do something*/
                break;
            case DW_CFA_offset_extended_sf:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_leb128(current_insn, &operand_int32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_offset_extended_sf r%d %d", operand_uint32_1, operand_int32_1);
                frame->regs[operand_uint32_1] = *(addr_s *) (frame->cfa + operand_int32_1 * frame->cie->data_alignment_factor);
                break;
            case DW_CFA_def_cfa_sf:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_leb128(current_insn, &operand_int32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_def_cfa_sf r%d %d", operand_uint32_1, operand_int32_1);
                frame->cfa_register = operand_uint32_1;
                frame->cfa_offset = operand_int32_1 * frame->cie->data_alignment_factor;
                frame->cfa = frame->regs[frame->cfa_register] + frame->cfa_offset;
                break;
            case DW_CFA_def_cfa_offset_sf:
                leb128_len = read_leb128(current_insn, &operand_int32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_def_cfa_offset_sf %d", operand_int32_1);
                frame->cfa_offset = operand_int32_1 * frame->cie->data_alignment_factor;
                frame->cfa = frame->regs[frame->cfa_register] + frame->cfa_offset;
                break;
            case DW_CFA_val_offset:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_uleb128(current_insn, &operand_uint32_2);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_val_offset %d %d", operand_uint32_1, operand_uint32_2);
                frame->regs[operand_uint32_1] = frame->cfa + (int64_t) operand_uint32_2 * frame->cie->data_alignment_factor;
                break;
            case DW_CFA_val_offset_sf:
                leb128_len = read_uleb128(current_insn, &operand_uint32_1);
                MOVE_n(current_insn, leb128_len);
                leb128_len = read_leb128(current_insn, &operand_int32_1);
                MOVE_n(current_insn, leb128_len);
                CRASH_LOGE("DW_CFA_val_offset_sf %d %d", operand_uint32_1, operand_int32_1);
                frame->regs[operand_uint32_1] = frame->cfa + operand_int32_1 * frame->cie->data_alignment_factor;
                break;
            case DW_CFA_val_expression:
                /*read data*/
                CRASH_LOGE("DW_CFA_val_expression");
                /*do something*/
                break;
            case DW_CFA_lo_user:
                /*do something*/
                CRASH_LOGE("DW_CFA_lo_user");
                break;
            case DW_CFA_hi_user:
                /*do something*/
                CRASH_LOGE("DW_CFA_hi_user");
                break;
            default:
                break;
        }
    }
    frame->regs[ARM64_PC] = frame->regs[frame->cie->return_address_register] - 4;
    return 1;
}

int copyregs_to_frame(sigcontext *sig_ctx, dwarf_frame *frame) {
    memcpy(frame->regs, &sig_ctx->regs[0], NUM_REGS * sizeof(uint64_t));
    return 0;
}

int copyregs_to_sigctx(dwarf_frame *frame, sigcontext *sig_ctx) {
    memcpy(&sig_ctx->regs[0], frame->regs, NUM_REGS * sizeof(uint64_t));
    return 0;
}

#endif
