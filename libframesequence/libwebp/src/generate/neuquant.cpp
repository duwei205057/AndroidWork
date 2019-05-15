#include "neuquant.h"
#include "../../../Gifflen.h"

/* Network Definitions
   ------------------- */

#define maxnetpos    (netsize-1)
#define netbiasshift    4            /* bias for colour values */
#define ncycles        100            /* no. of learning cycles */

/* defs for freq and bias */
#define intbiasshift    16            /* bias for fractions */
#define intbias        (((int) 1)<<intbiasshift)
#define gammashift    10            /* gamma = 1024 */
#define gamma    (((int) 1)<<gammashift)
#define betashift    10
#define beta        (intbias>>betashift)    /* beta = 1/1024 */
#define betagamma    (intbias<<(gammashift-betashift))

/* defs for decreasing radius factor */
#define initrad        (netsize>>3)        /* for 256 cols, radius starts */
#define radiusbiasshift    6            /* at 32.0 biased by 6 bits */
#define radiusbias    (((int) 1)<<radiusbiasshift)
#define initradius    (initrad*radiusbias)    /* and decreases by a */
#define radiusdec    30            /* factor of 1/30 each cycle */

/* defs for decreasing alpha factor */
#define alphabiasshift    10            /* alpha starts at 1.0 */
#define initalpha    (((int) 1)<<alphabiasshift)

/* radbias and alpharadbias used for radpower calculation */
#define radbiasshift    8
#define radbias        (((int) 1)<<radbiasshift)
#define alpharadbshift  (alphabiasshift+radbiasshift)
#define alpharadbias    (((int) 1)<<alpharadbshift)


/* Types and Global Variables
   -------------------------- */

/*
  Call this function to generate a paletted bitmap with N colors from a 24-bit bitmap.

  srcimage           Pointer to the source (24-bit) DIB
  numColors          The number of colors to reduce the bitmap to. Valid range is 2..256.
  quality            Quantization quality. Valid range is 1..100.
*/
void NeuQuant::quantise(DIB *destimage, DIB *srcimage, int numColors, int quality, int dither, int* imgw, int* imgh) {
    int i, j;
    //DIB *destimage = new DIB(srcimage->width, srcimage->height, 8);
    //unsigned char *pPalette = (unsigned char*)malloc(768);

    //destimage->palette = pPalette;

    quality /= 3;
    if (quality > 30) {
        quality = 30;
    } else if (quality < 1) {
        quality = 1;
    }

    if (numColors < 2) {
        numColors = 2;
    } else if (numColors > 256) {
        numColors = 256;
    }

    netsize = numColors;
    initnet(srcimage->bits, srcimage->width * srcimage->height * PIXEL_SIZE, 31 - quality);
    learn();
    unbiasnet();
    for (i = 0; i < numColors; i++) {
        for (j = 0; j < 3; j++) {
            destimage->palette[i * 3 + j] = network[i][2 - j];
        }
    }
    inxbuild();

    if (dither == 2) {
        *imgw = srcimage->width;
        *imgh = srcimage->height;
    }

    //printf("NeuQuant: Mapping colors.\n");

    for (i = srcimage->height - 1; i >= 0; i--) {
        if (i & 1) {
            for (j = srcimage->width - 1; j >= 0; j--) {
                if (srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 3] == 0) {
                    destimage->bits[i * srcimage->width + j] = transparentColorIndex;
                } else {
                    destimage->bits[i * srcimage->width + j] = inxsearch(
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE],
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 1],
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 2],
                            dither,
                            j,
                            i);
                }

            }
        } else {
            for (j = 0; j < srcimage->width; j++) {
                if (srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 3] == 0) {
                    destimage->bits[i * srcimage->width + j] = transparentColorIndex;
                } else {
                    destimage->bits[i * srcimage->width + j] = inxsearch(
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE],
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 1],
                            srcimage->bits[i * srcimage->width * PIXEL_SIZE + j * PIXEL_SIZE + 2],
                            dither,
                            j,
                            i);
                }

            }
        }
    }

}





/* Initialise network in range (0,0,0) to (255,255,255) and set parameters
   ----------------------------------------------------------------------- */

void NeuQuant::initnet(unsigned char *thepic, int len, int sample) {
    register int i;
    register int *p;

    thepicture = thepic;
    lengthcount = len;
    samplefac = sample;

    for (i = 0; i < netsize; i++) {
        p = network[i];
        p[0] = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
        freq[i] = intbias / netsize;    /* 1/netsize */
        bias[i] = 0;
    }
}


/* Unbias network to give byte values 0..255 and record position i to prepare for sort
   ----------------------------------------------------------------------------------- */

void NeuQuant::unbiasnet() {
    int i, j, temp;

    for (i = 0; i < netsize; i++) {
        for (j = 0; j < 3; j++) {
            /* OLD CODE: network[i][j] >>= netbiasshift; */
            /* Fix based on bug report by Juergen Weigert jw@suse.de */
            temp = (network[i][j] + (1 << (netbiasshift - 1))) >> netbiasshift;
            if (temp > 255) {
                temp = 255;
            }
            network[i][j] = temp;
        }
        network[i][3] = i;            /* record colour no */
    }
}


/* Output colour map
   ----------------- */

void NeuQuant::writecolourmap(FILE *f) {
    int i, j;

    for (i = 2; i >= 0; i--) {
        for (j = 0; j < netsize; j++) {
            putc(network[j][i], f);
        }
    }
}

/* Insertion sort of network and building of netindex[0..255] (to do after unbias)
   ------------------------------------------------------------------------------- */

void NeuQuant::inxbuild() {
    int i, j, smallpos, smallval;
    int *p, *q;
    int previouscol, startpos;

    previouscol = 0;
    startpos = 0;
    for (i = 0; i < (int) netsize; i++) {
        p = network[i];
        smallpos = i;
        smallval = p[1];            /* index on g */
        /* find smallest in i..netsize-1 */
        for (j = i + 1; j < (int) netsize; j++) {
            q = network[j];
            if (q[1] < smallval) {        /* index on g */
                smallpos = j;
                smallval = q[1];    /* index on g */
            }
        }
        q = network[smallpos];
        /* swap p (i) and q (smallpos) entries */
        if (i != smallpos) {
            j = q[0];
            q[0] = p[0];
            p[0] = j;
            j = q[1];
            q[1] = p[1];
            p[1] = j;
            j = q[2];
            q[2] = p[2];
            p[2] = j;
            j = q[3];
            q[3] = p[3];
            p[3] = j;
        }
        /* smallval entry is now in position i */
        if (smallval != previouscol) {
            netindex[previouscol] = (startpos + i) >> 1;
            for (j = previouscol + 1; j < smallval; j++) netindex[j] = i;
            previouscol = smallval;
            startpos = i;
        }
    }
    netindex[previouscol] = (startpos + maxnetpos) >> 1;
    for (j = previouscol + 1; j < 256; j++) netindex[j] = maxnetpos; /* really 256 */
}


inline int luma_diff(int r1, int g1, int b1, int r2, int g2, int b2) {
    return (r1 * 299 + g1 * 587 + b1 * 114) - (r2 * 299 + g2 * 587 + b2 * 114);
}

/* Search for BGR values 0..255 (after net is unbiased) and return colour index
   ---------------------------------------------------------------------------- */

int NeuQuant::inxsearch(int b, int g, int r, int dither, int xpos, int ypos) {
    int i, j, dist, a, index;
    int *p;
    int bestd, bestd_dark, bestd_bright;
    int bestd_r, bestd_g, bestd_b, dist_r, dist_g, dist_b;
    int best;
    int lumadiff;
    int darker, brighter;
    //float *errors = &dither_errors[xpos*ypos*3];

    bestd = 1000;        /* biggest possible dist is 256*3 */
    best = -1;
    bestd_dark = 1000;
    bestd_bright = 1000;
    darker = brighter = -1;
    i = netindex[g];    /* index on g */
    j = i - 1;        /* start at netindex[g] and work outwards */

    if (dither == 1) {
        while ((i < (int) netsize) || (j >= 0)) {
            if (i < (int) netsize) {
                p = network[i];
                dist = p[1] - g;        /* inx key */
                lumadiff = luma_diff(p[2], p[1], p[0], r, g, b);
                if (dist >= bestd) i = netsize;    /* stop iter */
                else {
                    i++;
                    if (dist < 0) { dist = -dist; }
                    a = p[0] - b;
                    if (a < 0) { a = -a; }
                    dist += a;
                    if (1) { //(dist<bestd) {
                        a = p[2] - r;
                        if (a < 0) { a = -a; }
                        dist += a;
                        //if (dist<bestd) {
                        if (dist == 0) {
                            bestd_dark = bestd_bright = dist;
                            darker = brighter = p[3];
                        } else if ((lumadiff < 0) && (dist < bestd_dark)) {
                            bestd_dark = dist;
                            darker = p[3];
                        } else if ((lumadiff > 0) && (dist < bestd_bright)) {
                            bestd_bright = dist;
                            brighter = p[3];
                        }

                        //bestd=dist; best=p[3];
                        //}
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                lumadiff = luma_diff(p[2], p[1], p[0], r, g, b);
                dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestd) j = -1; /* stop iter */
                else {
                    j--;
                    //isdark = 0;
                    if (dist < 0) { dist = -dist; }
                    a = p[0] - b;
                    if (a < 0) { a = -a; }
                    dist += a;
                    if (1) { //(dist<bestd) {
                        a = p[2] - r;
                        if (a < 0) { a = -a; }
                        dist += a;
                        //if (dist<bestd) { //{bestd=dist; best=p[3];}
                        if (dist == 0) {
                            bestd_dark = bestd_bright = dist;
                            darker = brighter = p[3];
                        } else if ((lumadiff < 0) && (dist < bestd_dark)) {
                            bestd_dark = dist;
                            darker = p[3];
                        } else if ((lumadiff > 0) && (dist < bestd_bright)) {
                            bestd_bright = dist;
                            brighter = p[3];
                        }
                        //}
                    }
                }
            }
        }
    } else {
        while ((i < (int) netsize) || (j >= 0)) {
            if (i < (int) netsize) {
                p = network[i];
                dist = p[1] - g;        /* inx key */
                if (dist >= bestd) i = netsize;    /* stop iter */
                else {
                    i++;
                    if (dist < 0) dist = -dist;
                    a = p[0] - b;
                    if (a < 0) a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestd) j = -1; /* stop iter */
                else {
                    j--;
                    if (dist < 0) dist = -dist;
                    a = p[0] - b;
                    if (a < 0) a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }
    }

    if ((darker == -1) && (brighter != -1)) darker = brighter;
    else if ((brighter == -1) && (darker != -1)) {
        brighter = darker;
    }

    if (dither == 1) {
        if ((xpos ^ ypos) & 1) {
            index = darker;
        } else {
            index = brighter;
        }
    } else {
        index = best;
    }
    if (index == transparentColorIndex)
        index = transparentColorIndex - 1;
    return index;
}


/* Search for biased BGR values
   ---------------------------- */

int NeuQuant::contest(register int b, register int g, register int r) {
    /* finds closest neuron (min dist) and updates freq */
    /* finds best neuron (min dist-bias) and returns position */
    /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
    /* bias[i] = gamma*((1/netsize)-freq[i]) */

    register int i, dist, a, biasdist, betafreq;
    int bestpos, bestbiaspos, bestd, bestbiasd;
    register int *p, *f, *n;

    bestd = ~(((int) 1) << 31);
    bestbiasd = bestd;
    bestpos = -1;
    bestbiaspos = bestpos;
    p = bias;
    f = freq;

    for (i = 0; i < netsize; i++) {
        n = network[i];
        dist = n[0] - b;
        if (dist < 0) {
            dist = -dist;
        }
        a = n[1] - g;
        if (a < 0) {
            a = -a;
        }
        dist += a;
        a = n[2] - r;
        if (a < 0) {
            a = -a;
        }
        dist += a;
        if (dist < bestd) {
            bestd = dist;
            bestpos = i;
        }
        biasdist = dist - ((*p) >> (intbiasshift - netbiasshift));
        if (biasdist < bestbiasd) {
            bestbiasd = biasdist;
            bestbiaspos = i;
        }
        betafreq = (*f >> betashift);
        *f++ -= betafreq;
        *p++ += (betafreq << gammashift);
    }
    freq[bestpos] += beta;
    bias[bestpos] -= betagamma;
    return (bestbiaspos);
}


/* Move neuron i towards biased (b,g,r) by factor alpha
   ---------------------------------------------------- */

void NeuQuant::altersingle(register int alpha, register int i, register int b, register int g,
                           register int r) {
    register int *n;

    n = network[i];                /* alter hit neuron */
    *n -= (alpha * (*n - b)) / initalpha;
    n++;
    *n -= (alpha * (*n - g)) / initalpha;
    n++;
    *n -= (alpha * (*n - r)) / initalpha;
}


/* Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in radpower[|i-j|]
   --------------------------------------------------------------------------------- */

void NeuQuant::alterneigh(int rad, int i, register int b, register int g, register int r) {
    register int j, k, lo, hi, a;
    register int *p, *q;

    lo = i - rad;
    if (lo < -1) {
        lo = -1;
    }
    hi = i + rad;
    if (hi > netsize) {
        hi = netsize;
    }

    j = i + 1;
    k = i - 1;
    q = radpower;
    while ((j < hi) || (k > lo)) {
        a = (*(++q));
        if (j < hi) {
            p = network[j];
            *p -= (a * (*p - b)) / alpharadbias;
            p++;
            *p -= (a * (*p - g)) / alpharadbias;
            p++;
            *p -= (a * (*p - r)) / alpharadbias;
            j++;
        }
        if (k > lo) {
            p = network[k];
            *p -= (a * (*p - b)) / alpharadbias;
            p++;
            *p -= (a * (*p - g)) / alpharadbias;
            p++;
            *p -= (a * (*p - r)) / alpharadbias;
            k--;
        }
    }
}


/**
 * Main Learning Loop
 */
void NeuQuant::learn() {
    int i, j, b, g, r;
    int radius, rad, alpha, step, delta, samplepixels;
    //unsigned char *p;
    unsigned int *p;
    unsigned char *lim;

    alphadec = 30 + ((samplefac - 1) / 3);
    p = (unsigned int *) thepicture;
    lim = thepicture + lengthcount;
    samplepixels = lengthcount / (PIXEL_SIZE * samplefac);
    delta = samplepixels / ncycles;
    alpha = initalpha;
    radius = initradius;

    rad = radius >> radiusbiasshift;
    if (rad <= 1) {
        rad = 0;
    }
    for (i = 0; i < rad; i++) {
        radpower[i] = alpha * (((rad * rad - i * i) * radbias) / (rad * rad));
    }
    //fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);
//    sprintf(s, "samplepixels = %d, rad = %d, a=%d, ad=%d, d=%d", samplepixels, rad, alpha, alphadec,
//            delta);
//    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", s);

    if ((lengthcount % prime1) != 0) {
        step = prime1;
    } else {
        if ((lengthcount % prime2) != 0) {
            step = prime2;
        } else {
            if ((lengthcount % prime3) != 0) {
                step = prime3;
            } else {
                step = prime4;
            }
        }
    }

    i = 0;
    while (i < samplepixels) {
        /*b = p[0] << netbiasshift;
        g = p[1] << netbiasshift;
        r = p[2] << netbiasshift;*/
        b = (((*p)) & 0xff) << netbiasshift;
        g = (((*p) >> 8) & 0xff) << netbiasshift;
        r = (((*p) >> 16) & 0xff) << netbiasshift;
        j = contest(b, g, r);

        altersingle(alpha, j, b, g, r);
        if (rad) {
            alterneigh(rad, j, b, g, r); /* alter neighbours */
        }

        p += step;

        if (p >= (unsigned int *) lim) {
            p = (unsigned int *) thepicture;
        }

        i++;
        if (i % delta == 0) {
            alpha -= alpha / alphadec;
            radius -= radius / radiusdec;
            rad = radius >> radiusbiasshift;
            if (rad <= 1) {
                rad = 0;
            }
            for (j = 0; j < rad; j++)
                radpower[j] = alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
        }
    }

//    sprintf(s, "final alpha = %f", ((float) alpha) / initalpha);
//    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", s);
}
