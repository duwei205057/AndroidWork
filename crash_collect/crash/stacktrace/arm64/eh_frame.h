//
// Created by chenjinyi on 17-7-6.
//
#if defined(__aarch64__)

#ifndef NATIVE_CRASH_COLLECT_EH_FRAME_H
#define NATIVE_CRASH_COLLECT_EH_FRAME_H
#include <stdint.h>
#include <string.h>
#include "sys_bits.h"
#include "pointer.h"
#include "leb128.h"

#define USE_EXTENDED_LENGTH 0xFFFFFFFF
#pragma pack(1)

typedef struct eh_frame_hdr {
    uint8_t version;
    uint8_t eh_frame_ptr_enc;
    uint8_t fde_count_enc;
    uint8_t table_enc;
//    uint64_t eh_frame_ptr;
//    uint64_t fde_count;
    uint32_t eh_frame_ptr;
    uint32_t fde_count;
    void *binary_search_table;
//    addr_s eh_frame_addr;
} HDR;

typedef struct eh_frame_cie {
    uint64_t total_length;
    uint64_t length;
    uint32_t CIE_ID;
    uint8_t version;
    char* augmentation;
    uint64_t en_data;
    uint32_t code_alignment_factor;
    int32_t data_alignment_factor;
    uint32_t return_address_register;
    uint32_t augmentation_data_length;
    uint8_t *augmentation_data;
    uint8_t *initial_instructions;
    uint64_t inst_length;
} CIE;

typedef struct eh_frame_fde {
    uint64_t total_length;
    uint64_t length;
    uint32_t CIE_Potnter;
    addr_s pc_begin;
    uint32_t pc_range;
    uint32_t augmentation_data_length;
    uint8_t *augmentation_data;
    uint8_t *call_frame_instructions;
    uint64_t inst_length;
} FDE;

#pragma pack()
HDR *decode_hdr(uint8_t *eh_frame_hdr_start);
CIE* decode_cie(uint8_t *eh_frame_start);
FDE *decode_fde(uint8_t *fde_start, CIE *cie);

#endif //NATIVE_CRASH_COLLECT_EH_FRAME_H
#endif