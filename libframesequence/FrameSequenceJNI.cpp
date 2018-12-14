/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <android/bitmap.h>
#include <generate/neuquant.h>
#include "JNIHelpers.h"
#include "utils/log.h"
#include "FrameSequence.h"

#include "FrameSequenceJNI.h"

#define JNI_PACKAGE "com/sogou/webp"


static struct {
    jclass clazz;
    jmethodID ctor;
} gFrameSequenceClassInfo;

int imgw, imgh;
int optCol = 256, optQuality = 100, optDelay = 4;
unsigned char *data32bpp = NULL;
NeuQuant *neuQuant = NULL;
DIB inDIB, *outDIB;
static char s[128];

FILE *pGif = NULL;

///////////////////////////generate gif/////////////////////////////////////////

int max_bits(int);

int GIF_LZW_compressor(DIB *, unsigned int, FILE *, int);

static jint gifflen_init(JNIEnv *ioEnv, jobject ioThis,
                                                           jstring gifName,
                                                           jint w, jint h, jint numColors,
                                                           jint quality, jint frameDelay) {
    const char *str;
    str = ioEnv->GetStringUTFChars(gifName, NULL);
    if (str == NULL) {
        return -1; /* OutOfMemoryError already thrown */
    }

    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", str);

    if ((pGif = fopen(str, "wb")) == NULL) {
        ioEnv->ReleaseStringUTFChars(gifName, str);
        __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen open file failed : ", str);
        return -2;
    }

    ioEnv->ReleaseStringUTFChars(gifName, str);

    optDelay = frameDelay;
    optCol = numColors;
    optQuality = quality;
    imgw = w;
    imgh = h;

    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", "Allocating memory for input DIB");
    data32bpp = new unsigned char[imgw * imgh * PIXEL_SIZE];

    inDIB.bits = data32bpp;
    inDIB.width = imgw;
    inDIB.height = imgh;
    inDIB.bitCount = 32;
    inDIB.pitch = imgw * PIXEL_SIZE;
    inDIB.palette = NULL;

    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", "Allocating memory for output DIB");
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


static void gifflen_close(JNIEnv *ioEnv, jobject ioThis) {
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
        fputc(';', pGif);
        fclose(pGif);
        pGif = NULL;
    }
    if (neuQuant) {
        delete neuQuant;
        neuQuant = NULL;
    }

    jclass clazz = ioEnv->GetObjectClass(ioThis);
    jmethodID method = ioEnv->GetMethodID(clazz, "onEncodeFinish", "()V");
    if (method != 0) {
        ioEnv->CallVoidMethod(ioThis, method);
    }
}


static jint gifflen_addFrame(JNIEnv *ioEnv, jobject ioThis, jintArray inArray) {
    //把上层的像素数组inArray,复制到inDIB.bits暂存区域内.(start=0, len=width*height)
    ioEnv->GetIntArrayRegion(inArray, (jint) 0, (jint) (inDIB.width * inDIB.height), (jint *) (inDIB.bits));

    s[0] = '!';
    s[1] = 0xF9;
    s[2] = 4;
    s[3] = 0;
    s[4] = optDelay & 0xFF;
    s[5] = optDelay / 0x100;
    s[6] = s[7] = 0;
    s[8] = ',';
    s[9] = s[10] = s[11] = s[12] = 0;
    s[13] = imgw & 0xFF;
    s[14] = imgw / 0x100;
    s[15] = imgh & 0xFF;
    s[16] = imgh / 0x100;
    s[17] = 0x80 + max_bits(optCol) - 1;

    fwrite(s, 1, 18, pGif);

    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", "Quantising");

    neuQuant->quantise(outDIB, &inDIB, optCol, optQuality, 0, &imgw, &imgh);

    fwrite(outDIB->palette, 1, optCol * 3, pGif);

    __android_log_write(ANDROID_LOG_VERBOSE, "com_lchad_gifflen", "Doing GIF encoding");
    GIF_LZW_compressor(outDIB, optCol, pGif, 0);

    return 0;
}

////////////////////////////////////////////////////////////////////////////////
// Frame sequence
////////////////////////////////////////////////////////////////////////////////

static jobject createJavaFrameSequence(JNIEnv* env, FrameSequence* frameSequence) {
    if (!frameSequence) {
        return NULL;
    }
    return env->NewObject(gFrameSequenceClassInfo.clazz, gFrameSequenceClassInfo.ctor,
            reinterpret_cast<jlong>(frameSequence),
            frameSequence->getWidth(),
            frameSequence->getHeight(),
            frameSequence->isOpaque(),
            frameSequence->getFrameCount(),
            frameSequence->getDefaultLoopCount());
}

static jobject nativeDecodeByteArray(JNIEnv* env, jobject clazz,
        jbyteArray byteArray, jint offset, jint length) {
    jbyte* bytes = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(byteArray, NULL));
    if (bytes == NULL) {
        jniThrowException(env, ILLEGAL_STATE_EXEPTION,
                "couldn't read array bytes");
        return NULL;
    }
    MemoryStream stream(bytes + offset, length, NULL);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    env->ReleasePrimitiveArrayCritical(byteArray, bytes, 0);
    return createJavaFrameSequence(env, frameSequence);
}

static jobject nativeDecodeByteBuffer(JNIEnv* env, jobject clazz,
        jobject buf, jint offset, jint limit) {
    jobject globalBuf = env->NewGlobalRef(buf);
    JavaVM* vm;
    env->GetJavaVM(&vm);
    MemoryStream stream(
        (reinterpret_cast<uint8_t*>(
            env->GetDirectBufferAddress(globalBuf))) + offset,
        limit,
        globalBuf);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    jobject finalSequence = createJavaFrameSequence(env, frameSequence);
    return finalSequence;
}

static jobject nativeDecodeStream(JNIEnv* env, jobject clazz,
        jobject istream, jbyteArray byteArray) {
    JavaInputStream stream(env, istream, byteArray);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    return createJavaFrameSequence(env, frameSequence);
}

static void nativeDestroyFrameSequence(JNIEnv* env, jobject clazz,
        jlong frameSequenceLong) {
    FrameSequence* frameSequence = reinterpret_cast<FrameSequence*>(frameSequenceLong);
    jobject buf = frameSequence->getRawByteBuffer();
    if (buf != NULL) {
        env->DeleteGlobalRef(buf);
    }
    delete frameSequence;
}

static jlong nativeCreateState(JNIEnv* env, jobject clazz, jlong frameSequenceLong) {
    FrameSequence* frameSequence = reinterpret_cast<FrameSequence*>(frameSequenceLong);
    FrameSequenceState* state = frameSequence->createState();
    return reinterpret_cast<jlong>(state);
}

////////////////////////////////////////////////////////////////////////////////
// Frame sequence state
////////////////////////////////////////////////////////////////////////////////

static void nativeDestroyState(
        JNIEnv* env, jobject clazz, jlong frameSequenceStateLong) {
    FrameSequenceState* frameSequenceState =
            reinterpret_cast<FrameSequenceState*>(frameSequenceStateLong);
    delete frameSequenceState;
}

void throwIae(JNIEnv* env, const char* message, int errorCode) {
    char buf[256];
    snprintf(buf, sizeof(buf), "%s, error %d", message, errorCode);
    jniThrowException(env, ILLEGAL_STATE_EXEPTION, buf);
}

static jlong JNICALL nativeGetFrame(
        JNIEnv* env, jobject clazz, jlong frameSequenceStateLong, jint frameNr,
        jobject bitmap, jint previousFrameNr) {
    FrameSequenceState* frameSequenceState =
            reinterpret_cast<FrameSequenceState*>(frameSequenceStateLong);
    int ret;
    AndroidBitmapInfo info;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        throwIae(env, "Couldn't get info from Bitmap", ret);
        return 0;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        throwIae(env, "Bitmap pixels couldn't be locked", ret);
        return 0;
    }

    int pixelStride = info.stride >> 2;
    jlong delayMs = frameSequenceState->drawFrame(frameNr,
            (Color8888*) pixels, pixelStride, previousFrameNr);

    AndroidBitmap_unlockPixels(env, bitmap);
    return delayMs;
}

static jbyteArray nativeGetByteData (JNIEnv* env, jobject clazz, jlong frameSequenceLong) {
    FrameSequence* frameSequence = reinterpret_cast<FrameSequence*>(frameSequenceLong);
    WebPData data = frameSequence->getByteData();
    if (data.size > 0) {
        size_t length = data.size;
        jbyte *byteResult = new jbyte[length];
        for (int i = 0; i < length; i++) {
            byteResult[i] = data.bytes[i];
        }
        jbyteArray jretArr = env->NewByteArray(data.size);
        env->SetByteArrayRegion(jretArr, 0, data.size, byteResult);
        delete[] byteResult;
        return jretArr;
    }
    return NULL;
}

static JNINativeMethod frameMethods[] = {
    {   "nativeDecodeByteArray",
        "([BII)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeByteArray
    },
    {   "nativeDecodeByteBuffer",
        "(Ljava/nio/ByteBuffer;II)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeByteBuffer
    },
    {   "nativeDecodeStream",
        "(Ljava/io/InputStream;[B)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeStream
    },
    {   "nativeDestroyFrameSequence",
        "(J)V",
        (void*) nativeDestroyFrameSequence
    },
    {   "nativeCreateState",
        "(J)J",
        (void*) nativeCreateState
    },
    {   "nativeGetFrame",
        "(JILandroid/graphics/Bitmap;I)J",
        (void*) nativeGetFrame
    },
    {   "nativeDestroyState",
        "(J)V",
        (void*) nativeDestroyState
    },
    {   "nativeGetByteData",
        "(J)[B",
        (void*) nativeGetByteData
    },
};

static JNINativeMethod gifflenMethods[] = {
    {   "init",
        "(Ljava/lang/String;IIIII)I",
        (void*) gifflen_init
    },
    {   "close",
        "()V",
        (void*) gifflen_close
    },
    {   "addFrame",
        "([I)I",
        (void*) gifflen_addFrame
    },
};

jint FrameSequence_OnLoad(JNIEnv* env) {
    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.
    gFrameSequenceClassInfo.clazz = env->FindClass(JNI_PACKAGE "/FrameSequence");
    if (!gFrameSequenceClassInfo.clazz) {
        ALOGW("Failed to find " JNI_PACKAGE "/FrameSequence");
        return -1;
    }
    gFrameSequenceClassInfo.clazz = (jclass)env->NewGlobalRef(gFrameSequenceClassInfo.clazz);

    gFrameSequenceClassInfo.ctor = env->GetMethodID(gFrameSequenceClassInfo.clazz, "<init>", "(JIIZII)V");
    if (!gFrameSequenceClassInfo.ctor) {
        ALOGW("Failed to find constructor for FrameSequence - was it stripped?");
        return -1;
    }

    if (env->RegisterNatives(gFrameSequenceClassInfo.clazz, frameMethods, METHOD_COUNT(frameMethods)) < 0) {
        return JNI_FALSE;
    }
    jclass clazz;

    clazz = env->FindClass(JNI_PACKAGE "/Gifflen");
    if (env->RegisterNatives(clazz, gifflenMethods, METHOD_COUNT(gifflenMethods)) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}


/*************************************************************************************************************/



/*************************************************************************************************************/

#define hash 11003

unsigned int stat_bits;
unsigned int code_in_progress;
unsigned int LZWpos;
char LZW[256];
short int hashtree[hash][3];

int find_hash(int pre, int suf) {
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


int max_bits(int num) {
    for (int b = 0; b < 14; b++) {
        if ((1 << b) >= num) {
            return b;
        }
    }
    return 0;
}

void append_code(FILE *handle, int code) {
    LZW[LZWpos++] = code;
    if (LZWpos == 256) {
        LZW[0] = 255;
        fwrite(LZW, 1, 256, handle);
        LZWpos = 1;
    }
}


void write_code(FILE *handle, int no_bits, int code) {
    code_in_progress = code_in_progress + (code << stat_bits); // * powers2[stat_bits+1]
    stat_bits = stat_bits + no_bits;
    while (stat_bits > 7) {
        append_code(handle, code_in_progress & 255);
        code_in_progress >>= 8;
        stat_bits -= 8;
    }
}


int GIF_LZW_compressor(DIB *srcimg, unsigned int numColors, FILE *handle, int interlace) {
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



