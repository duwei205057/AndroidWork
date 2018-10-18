#include <stdint.h>

static const uint32_t kFirstByteMark[] = {
    0x00000000, 0x00000000, 0x000000C0, 0x000000E0, 0x000000F0
};
static const uint32_t kByteMask = 0x000000BF;
static const uint32_t kByteMark = 0x00000080;
static const uint32_t kUnicodeSurrogateHighStart  = 0x0000D800;
static const uint32_t kUnicodeSurrogateHighEnd    = 0x0000DBFF;
static const uint32_t kUnicodeSurrogateLowStart   = 0x0000DC00;
static const uint32_t kUnicodeSurrogateLowEnd     = 0x0000DFFF;
static const uint32_t kUnicodeSurrogateStart      = kUnicodeSurrogateHighStart;
static const uint32_t kUnicodeSurrogateEnd        = kUnicodeSurrogateLowEnd;
static const uint32_t kUnicodeMaxCodepoint        = 0x0010FFFF;

static void utf32_to_utf8(uint8_t* dstP, uint32_t srcChar, uint32_t bytes);
static uint32_t utf32_to_utf8_bytes(uint32_t srcChar);
uint32_t utf16_to_utf8(const uint16_t* src, uint32_t src_len, char* dst, uint32_t dst_len);

