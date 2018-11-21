#include "utf.h"

static void utf32_to_utf8(uint8_t* dstP, uint32_t srcChar, uint32_t bytes) {
  dstP += bytes;
  switch (bytes) {
   /* note: everything falls through. */
  case 4: *--dstP = (uint8_t)((srcChar | kByteMark) & kByteMask); srcChar >>= 6;
  case 3: *--dstP = (uint8_t)((srcChar | kByteMark) & kByteMask); srcChar >>= 6;
  case 2: *--dstP = (uint8_t)((srcChar | kByteMark) & kByteMask); srcChar >>= 6;
  case 1: *--dstP = (uint8_t)(srcChar | kFirstByteMark[bytes]);
  }
}

static uint32_t utf32_to_utf8_bytes(uint32_t srcChar) {
  uint32_t bytesToWrite;
  // Figure out how many bytes the result will require.
  if (srcChar < 0x00000080) {
    bytesToWrite = 1;
  }else if (srcChar < 0x00000800) {
    bytesToWrite = 2;
  }else if (srcChar < 0x00010000) {
    if ((srcChar < kUnicodeSurrogateStart) || (srcChar > kUnicodeSurrogateEnd)) {
      bytesToWrite = 3;
    }else {
      // Surrogates are invalid UTF-32 characters.
      return 0;
    }
  }
  // Max code point for Unicode is 0x0010FFFF.
  else if (srcChar <= kUnicodeMaxCodepoint) {
    bytesToWrite = 4;
  }else {
  // Invalid UTF-32 character.
    return 0;
  }
  return bytesToWrite;
}

uint32_t utf16_to_utf8(const uint16_t* src, uint32_t src_len, char* dst, uint32_t dst_len) {
  if (src == NULL || src_len == 0 || dst == NULL || dst_len == 0) {
    return 0;
  }
  const uint16_t* cur_utf16 = src;
  const uint16_t* const end_utf16 = src + src_len;
  char *cur = dst;
  const char* const end = dst + dst_len;
  while (cur_utf16 < end_utf16 && cur < end) {
  uint32_t utf32;
  // surrogate pairs
  if ((*cur_utf16 & 0xFC00) == 0xD800 && (cur_utf16 + 1) < end_utf16) {
    utf32 = (*cur_utf16++ - 0xD800) << 10;
    utf32 |= *cur_utf16++ - 0xDC00;
    utf32 += 0x10000;
  }else {
    utf32 = (uint32_t) *cur_utf16++;
  }
  uint32_t len = utf32_to_utf8_bytes(utf32);
  utf32_to_utf8((uint8_t*)cur, utf32, len);
  cur += len;
  }
  if (cur < end) {
    *cur = '\0';
  }
  return cur - dst;
}
