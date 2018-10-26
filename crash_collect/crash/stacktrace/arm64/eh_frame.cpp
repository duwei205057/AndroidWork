//
// Created by chenjinyi on 17-7-6.
//
#if defined(__aarch64__)
#include <memory.h>
#include <utils/clog.h>
#include "eh_frame.h"

HDR *decode_hdr(uint8_t *eh_frame_hdr_start) {
    HDR *hdr = new HDR();
    CRASH_LOGE("sizeof(HDR) = %d sizeof(hdr->binary_search_table)== %d ", sizeof(HDR), sizeof(hdr->binary_search_table)/*, sizeof(hdr->eh_frame_addr)*/);
    int size = sizeof(HDR)- sizeof(hdr->binary_search_table);
    memcpy(hdr, eh_frame_hdr_start, size);
    CRASH_LOGE("HDR version = %d eh_frame_ptr_enc = %d fde_count_enc==%d table_enc==%d frame_ptr = %08x  fde_count==%08x", hdr->version,hdr->eh_frame_ptr_enc,hdr->fde_count_enc,hdr->table_enc,hdr->eh_frame_ptr,hdr->fde_count);
    hdr->binary_search_table = eh_frame_hdr_start + size;
//    hdr->eh_frame_addr = hdr->eh_frame_ptr + (addr_s) eh_frame_hdr_start + 4;
    return hdr;
}

CIE* decode_cie(uint8_t *eh_frame_start) {
    uint8_t *current_pointer = eh_frame_start;
    CIE *cie = new CIE();
    cie->length = *((uint32_t *) current_pointer);
    if(cie->length == USE_EXTENDED_LENGTH) {
        MOVE_4(current_pointer);
        cie->length = *((uint64_t *) current_pointer);
        MOVE_8(current_pointer);
        cie->total_length = cie->length + 12;
    } else {
        MOVE_4(current_pointer);
        cie->total_length = cie->length + 4;
    }
    addr_s cie_end = cie->length + (addr_s) current_pointer;
    cie->CIE_ID = *((uint32_t *) current_pointer);
    MOVE_4(current_pointer);
    cie->version = *((uint8_t *) current_pointer);
    MOVE_1(current_pointer);
    cie->augmentation = (char *) current_pointer;
    size_t len = strlen(cie->augmentation);
    MOVE_n(current_pointer, len + 1);
    if(strstr(cie->augmentation, "zh") != NULL) {
        cie->en_data = *((uint64_t *) current_pointer);
        MOVE_8(current_pointer);
    }
    int uleb128_len = read_uleb128(current_pointer, &cie->code_alignment_factor);
    MOVE_n(current_pointer, uleb128_len);
    int leb128_len = read_leb128(current_pointer, &cie->data_alignment_factor);
    MOVE_n(current_pointer, leb128_len);
    uleb128_len = read_uleb128(current_pointer, &cie->return_address_register);
    MOVE_n(current_pointer, uleb128_len);

    if(strstr(cie->augmentation, "z") != NULL) {
        uleb128_len = read_uleb128(current_pointer, &cie->augmentation_data_length);
        MOVE_n(current_pointer, uleb128_len);
        cie->augmentation_data = current_pointer;
        MOVE_n(current_pointer, cie->augmentation_data_length);
    }
    cie->initial_instructions = current_pointer;
    cie->inst_length = cie_end - (addr_s) current_pointer;
    return cie;
}

FDE *decode_fde(uint8_t *fde_start, CIE *cie) {
    uint8_t *current_pointer = fde_start;
    FDE *fde = new FDE();
    fde->length = *((uint32_t *) current_pointer);
    if(fde->length == USE_EXTENDED_LENGTH) {
        MOVE_4(current_pointer);
        fde->length = *((uint64_t *) current_pointer);
        MOVE_8(current_pointer);
        fde->total_length = fde->length + 12;
    } else {
        MOVE_4(current_pointer);
        fde->total_length = fde->length + 4;
    }
    if(fde->length == 0) return fde;
    addr_s fde_end = fde->length + (addr_s) current_pointer;
    fde->CIE_Potnter = *((uint32_t *) current_pointer);
    MOVE_4(current_pointer);
    fde->pc_begin = *((int32_t *) current_pointer) + (uint64_t) current_pointer;
    MOVE_4(current_pointer);
    fde->pc_range = *((uint32_t *) current_pointer);
    MOVE_4(current_pointer);
    if(cie->augmentation != NULL && *cie->augmentation == 'z') {
        int size = read_uleb128(current_pointer, &fde->augmentation_data_length);
        int move_byte = size + fde->augmentation_data_length;
        MOVE_n(current_pointer, move_byte);
    }
    fde->call_frame_instructions = current_pointer;
    fde->inst_length = fde_end - (addr_s) current_pointer;
    return fde;
}

#endif