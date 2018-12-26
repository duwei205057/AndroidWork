//
// Created by chenjinyi on 17-7-3.
//

#ifndef NATIVE_CRASH_COLLECT_LEB128_H
#define NATIVE_CRASH_COLLECT_LEB128_H

int read_uleb128(const void *buffer, unsigned int *value);
int read_leb128(const void *buffer, int *value);

#endif
