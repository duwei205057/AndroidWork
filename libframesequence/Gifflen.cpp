
#include "Gifflen.h"


//Gifflen::Gifflen()
//        : s(),hashtree(),LZW(),pGif()
//{}

int Gifflen::find_hash(int pre, int suf) {
    int i, o;
    i = ((pre * 256) ^ suf) % hash;
    if (i == 0) {
        o = 1;
    } else {
        o = hash - i;
    }
    while (1) {
        if (hashtree[i][0] == -1) {
            return i;
        } else if ((hashtree[i][1] == pre) && (hashtree[i][2] == suf)) {
            return i;
        } else {
            i = i - o;
            if (i < 0) {
                i += hash;
            }
        }
    }

    return 0;
}

int Gifflen::max_bits(int num) {
    for (int b = 0; b < 14; b++) {
        if ((1 << b) >= num) {
            return b;
        }
    }
    return 0;
}

void Gifflen::append_code(FILE *handle, int code) {
    LZW[LZWpos++] = code;
    if (LZWpos == 256) {
        LZW[0] = 255;
        fwrite(LZW, 1, 256, handle);
        LZWpos = 1;
    }
}


void Gifflen::write_code(FILE *handle, int no_bits, int code) {
    code_in_progress = code_in_progress + (code << stat_bits); // * powers2[stat_bits+1]
    stat_bits = stat_bits + no_bits;
    while (stat_bits > 7) {
        append_code(handle, code_in_progress & 255);
        code_in_progress >>= 8;
        stat_bits -= 8;
    }
}


int Gifflen::GIF_LZW_compressor(DIB *srcimg, unsigned int numColors, FILE *handle, int interlace) {
    int xdim, ydim, clear, EOI, code, bits, pre, suf, x, y, i, max, bits_color, done, rasterlen;
    static short int rasters[768];

    stat_bits = 0;
    code_in_progress = 0;
    LZWpos = 1;

    for (i = 0; i < hash; i++) {
        hashtree[i][0] = hashtree[i][1] = hashtree[i][2] = -1;
    }
    if (handle == NULL) {
        return 0;
    }
    xdim = srcimg->width;
    ydim = srcimg->height;
    bits_color = max_bits(numColors) - 1;
    clear = (1 << (bits_color + 1)); //powers2[bits_color+2]
    EOI = clear + 1;
    code = EOI + 1;
    bits = bits_color + 2;
    max = (1 << bits); //powers2[bits+1]
    if (code == max) {
        clear = 4;
        EOI = 5;
        code = 6;
        bits++;
        max *= 2;
    }
    fputc(bits - 1, handle);
    write_code(handle, bits, clear);
    rasterlen = 0;
    if (interlace) {
        for (int e = 1; e <= 5; e += 4) {
            for (int f = e; f <= ydim; f += 8) {
                rasters[rasterlen++] = f;
            }
        }
        for (int e = 3; e <= ydim; e += 4) {
            rasters[rasterlen++] = e;
        }
        for (int e = 2; e <= ydim; e += 2) {
            rasters[rasterlen++] = e;
        }
    } else {
        for (int e = 1; e <= ydim; e++) {
            rasters[rasterlen++] = e - 1;
        }
    }
    pre = srcimg->bits[rasters[0] * xdim];
    x = 1;
    y = 0;
    done = 0;
    if (x >= xdim) {
        y++;
        x = 0;
    }
    while (1) {
        while (1) {
            if (!done) {
                suf = srcimg->bits[rasters[y] * xdim + x];
                x++;
                if (x >= xdim) {
                    y++;
                    x = 0;
                    if (y >= ydim) {
                        done = 1;
                    }
                }
                i = find_hash(pre, suf);
                if (hashtree[i][0] == -1) {
                    break;
                } else
                    pre = hashtree[i][0];
            } else {
                write_code(handle, bits, pre);
                write_code(handle, bits, EOI);
                if (stat_bits) {
                    write_code(handle, bits, 0);
                }
                LZW[0] = LZWpos - 1;
                fwrite(LZW, 1, LZWpos, handle);
                fputc(0, handle);
                return 1;
            }
        }
        write_code(handle, bits, pre);
        hashtree[i][0] = code;
        hashtree[i][1] = pre;
        hashtree[i][2] = suf;
        pre = suf;
        code++;
        if (code == max + 1) {
            max *= 2;
            if (bits == 12) {
                write_code(handle, bits, clear);
                for (i = 0; i < hash; i++) {
                    hashtree[i][0] = hashtree[i][1] = hashtree[i][2] = -1;
                }
                code = EOI + 1;
                bits = bits_color + 2;
                max = 1 << bits;
                if (bits == 2) {
                    clear = 4;
                    EOI = 5;
                    code = 6;
                    bits = 3;
                    max *= 2;
                }
            } else {
                bits++;
            }
        }
    }

    return 0;
}

int Gifflen::init(const char * gifName, int w, int h, int numColors, int quality) {
    __android_log_print(ANDROID_LOG_VERBOSE, "gifflen", "gifName=%s w=%d h=%d numColors=%d quality=%d f", gifName, w, h, numColors, quality);
    if ((pGif = fopen(gifName, "wb")) == NULL) {
        return -1;
    }

    optCol = numColors;
    optQuality = quality;
    imgw = w;
    imgh = h;

    data32bpp = new unsigned char[imgw * imgh * PIXEL_SIZE];

    inDIB.bits = data32bpp;
    inDIB.width = imgw;
    inDIB.height = imgh;
    inDIB.bitCount = 32;
    inDIB.pitch = imgw * PIXEL_SIZE;
    inDIB.palette = NULL;

    outDIB = new DIB(imgw, imgh, 8);
    outDIB->palette = new unsigned char[768];

    neuQuant = new NeuQuant();

    // Output the GIF header and Netscape extension
    fwrite("GIF89a", 1, 6, pGif);
    s[0] = w & 0xFF;
    s[1] = w / 0x100;
    s[2] = h & 0xFF;
    s[3] = h / 0x100;
    s[4] = 0x50 + max_bits(numColors) - 1;
    s[5] = s[6] = 0;
    s[7] = 0x21;
    s[8] = 0xFF;
    s[9] = 0x0B;
    fwrite(s, 1, 10, pGif);
    fwrite("NETSCAPE2.0", 1, 11, pGif);
    s[0] = 3;
    s[1] = 1;
    s[2] = s[3] = s[4] = 0;
    fwrite(s, 1, 5, pGif);
    return 0;
}

int Gifflen::addFrame(int* data, int length, int delay) {
    int total = imgw * imgh;
    if (length < total) {
        return -1;
    }
    int* point = (int *) inDIB.bits;
    for (int i = 0; i < total; ++i) {
        point[i] = data[i];
    }
    s[0] = '!';//0x21
    s[1] = 0xF9;
    s[2] = 4;
    s[3] = 9;
    s[4] = delay & 0xFF;
    s[5] = delay / 0x100;
    s[6] = transparentColorIndex;
    s[7] = 0;
    s[8] = ',';//0x2c
    s[9] = s[10] = s[11] = s[12] = 0;
    s[13] = imgw & 0xFF;
    s[14] = imgw / 0x100;
    s[15] = imgh & 0xFF;
    s[16] = imgh / 0x100;
    s[17] = 0x80 + max_bits(optCol) - 1;

    fwrite(s, 1, 18, pGif);

    __android_log_write(ANDROID_LOG_VERBOSE, "gifflen", "Quantising");

    neuQuant->quantise(outDIB, &inDIB, optCol, optQuality, 0, &imgw, &imgh);

    fwrite(outDIB->palette, 1, optCol * 3, pGif);

    __android_log_write(ANDROID_LOG_VERBOSE, "gifflen", "Doing GIF encoding");
    GIF_LZW_compressor(outDIB, optCol, pGif, 0);
    return 0;
}

void Gifflen::close() {
    if (data32bpp) {
        delete[] data32bpp;
        data32bpp = NULL;
    }
    if (outDIB) {
        if (outDIB->palette) delete[] outDIB->palette;
        delete outDIB;
        outDIB = NULL;
    }
    if (pGif) {
        fputc(';', pGif);//0x3B
        fclose(pGif);
        pGif = NULL;
    }
    if (neuQuant) {
        delete neuQuant;
        neuQuant = NULL;
    }
}