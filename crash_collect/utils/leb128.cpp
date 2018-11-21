#include <stdint.h>
#include "leb128.h"

int read_uleb128(const void *buffer, unsigned int *value) {
    uint8_t *buf = (uint8_t *) buffer;
    int pos = 0;
    int offset = 0;
    *value = 0;
    while(true) {
        *value |= ((buf[pos] & 0x7F) << offset);
        offset += 7;
        if(!(buf[pos++] & 0x80))
            break;
    }
    return pos;
}

int read_leb128(const void *buffer, int *value) {
    uint8_t *buf = (uint8_t *) buffer;
    int pos = 0;
    int offset = 0;
    *value = 0;
    uint8_t byte = 0;
    while(true) {
        byte = buf[pos++];
        *value |= (byte & 0x7f) << offset;
        offset += 7;
        if(!(byte & 0x80))
            break;
    }
    if(byte & 0x40)
        *value |= -(1 << offset);
    return pos;
}
