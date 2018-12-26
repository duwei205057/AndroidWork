
#ifndef RASTERMILL_GIFFLEN_H
#define RASTERMILL_GIFFLEN_H

#include "libwebp/src/generate/dib.h"
#include "libwebp/src/generate/neuquant.h"
#include "utils/log.h"

#define hash 11003

class Gifflen {
private:
    unsigned int stat_bits;
    unsigned int code_in_progress;
    unsigned int LZWpos;
    char LZW[256];
    short int hashtree[hash][3];
    int imgw, imgh;
    int optCol = 256, optQuality = 100, optDelay = 4;
    unsigned char *data32bpp = NULL;
    NeuQuant *neuQuant = NULL;
    DIB inDIB, *outDIB;
    FILE *pGif = NULL;
    char s[128];

    int find_hash(int pre, int suf);
    int max_bits(int num);
    void append_code(FILE *handle, int code);
    void write_code(FILE *handle, int no_bits, int code);
    int GIF_LZW_compressor(DIB *srcimg, unsigned int numColors, FILE *handle, int interlace);

public:
    int init(const char * gifName, int w, int h, int numColors, int quality);
    void close();
    int addFrame(int* data, int length, int delay);
};


#endif //RASTERMILL_GIFFLEN_H
