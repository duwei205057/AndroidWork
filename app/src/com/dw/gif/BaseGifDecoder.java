package com.dw.gif;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tianmiao on 17-5-23.
 */
public class BaseGifDecoder extends Thread{
    private static final String TAG = "BaseGifDecoder";
    private static final boolean DEBUG = false;

    public static final int STATUS_PARSING = 0;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OPEN_ERROR = 2;
    public static final int STATUS_FINISH = -1;
    public static final int STATUS_INTERRUPT = -2;

    // input data
    private InputStream mInputStream;
    private byte[] mGifData = null;
    private ByteBuffer mRawData;
    public final static int COUNT = 6;
    public final static int STEP = COUNT / 2;
    BlockingQueue<Integer> mFrameQueue;

    //
    private int mStatus;
    public int mWidth; // full image width
    public int mHeight; // full image height
    private int mLoopCount = 1; // iterations; 0 = repeat forever

    private int[] mAct; // active color table

    private int mBgColor; // background color
    private int mLastBgColor; // previous bg color

    private Bitmap mImage; // current frame
    private int mLrx, mLry, mLrw, mLrh;
    private Bitmap mLastImage; // previous frame
    // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
    private int mLastDispose = 0;

    private GifFrame mCurrentFrame = null;
    private boolean mIsShow = false;

    private GifHeaderData mGifHeader = new GifHeaderData();
    private byte[] mBlock = new byte[256]; // current data block
    private int mBlockSize = 0; // block size

    private static final int MaxStackSize = 4096;
    // max decoder pixel stack size

    // LZW decoder working arrays
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private byte[] pixels;

    private int mFrameCount;
    private GifFrame mGifFrame; // frames read from current file
    private GifAction mAction = null;

    private int mImageRequireWidth = -1;
    private int mImageRequireHeight = -1;
    private boolean mNeedCheckMemory = true;
    private boolean mUseRawData = false;
    private int mTotalLoopTimes = -1;
    private int mLoopedTimes = 0;

    private final byte[] mLastImageLock = new byte[0];
    private final byte[] mRawDataLock = new byte[0];
    private final byte[] mThreadLock = new byte[0];
    private Lock mLock = new ReentrantLock();

    public boolean mStopped = true;

    public BaseGifDecoder(byte[] data,GifAction act,boolean useRawData){
        this.setName(TAG);
        mUseRawData = false;
        if (data != null) {
            if (useRawData) {
                mUseRawData = true;
                mGifData = null;
                ByteBuffer bytes = ByteBuffer.wrap(data);
                mRawData = bytes.asReadOnlyBuffer();
                mRawData.position(0);
                mRawData.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                mRawData = null;
                mGifData = data;
            }
            mAction = act;
        } else {
            mGifData = null;
            mRawData = null;
            mStatus = STATUS_OPEN_ERROR;
        }
    }

    public BaseGifDecoder(InputStream is,GifAction act){
        this.setName(TAG);
        mUseRawData = false;
        mInputStream = is;
        mAction = act;
    }

    public void run(){
        if(mInputStream != null){
            readStream();
        } else if (mGifData != null){
            readByte();
        } else if (mRawData != null) {
            mStopped = false;
            readRawData();
        }
    }

    public void recycleBitmap(int gifFrameIndex) {
        if (gifFrameIndex < 0)
            return;
        LOGD("recycle bitmap   -----------  index = " + gifFrameIndex);
        GifFrame frame = mGifFrame;
        int i = 0;
        while (frame != null) {
            if (i == gifFrameIndex) {
                if (frame.image != null) {
                    frame.image = null;
                }
                return;
            } else {
                frame = frame.nextFrame;
            }
            i++;
        }
    }

    public void free(){
        mStopped = true;
        synchronized (mThreadLock) {
            mThreadLock.notify();
        }
        GifFrame fg = mGifFrame;
        while(fg != null){
            if (fg.image != null) {
                fg.image = null;
            }
            fg = null;
            mGifFrame = mGifFrame.nextFrame;
            fg = mGifFrame;
        }
        mImage = null;
        if (mLastImage != null) {
            synchronized (mLastImageLock) {
                if (mLastImage != null) {
                    mLastImage = null;
                }
            }
        }
        if(mInputStream != null){
            try{
                mInputStream.close();
            } catch(Exception ex){}
            mInputStream = null;
        }
        mGifData = null;
        if (mRawData != null) {
            synchronized (mRawDataLock) {
                if (mRawData != null) {
                    mRawData.clear();
                    mRawData = null;
                }
            }
        }
        if (mGifHeader != null) {
            mGifHeader.recycle();
        }
    }

    public int getStatus(){
        return mStatus;
    }

    public void setImageRequireSize(int requireWidth, int requireHeight) {
        mImageRequireWidth = requireWidth;
        mImageRequireHeight = requireHeight;
    }

    public void setLoopTimes(int times) {
        mTotalLoopTimes = times;
    }

    public void setNeedCHeckMemory(boolean need) {
        mNeedCheckMemory = need;
    }

    public boolean parseOk(){
        return mStatus == STATUS_FINISH;
    }

    public int getFrameCount() {
        return mFrameCount;
    }

    public Bitmap getImage() {
        return getFrameImage(0);
    }

    public int getLoopCount() {
        return mLoopCount;
    }

    private void setPixels(GifFrameData frame) {
        int[] dest = new int[mWidth * mHeight];
        // fill in starting image contents based on last image's dispose code
        if (mLastDispose > 0) {
            if (mLastDispose == 3) {
                // use image before last
                int n = mFrameCount - 2;
                if (n > 0) {
                    mLastImage = getFrameImage(n - 1);
                } else {
                    mLastImage = null;
                }
            }
            if (mLastImage != null && !mLastImage.isRecycled()) {
                synchronized (mLastImageLock) {
                    if (mLastImage != null && !mLastImage.isRecycled()) {
                        mLastImage.getPixels(dest, 0, mWidth, 0, 0, mWidth, mHeight);
                        // copy pixels
                        if (mLastDispose == 2) {
                            // fill last image rect area with background color
                            int c = 0;
                            if (!frame.transparency) {
                                c = mLastBgColor;
                            }
                            for (int i = 0; i < mLrh; i++) {
                                int n1 = (mLry + i) * mWidth + mLrx;
                                int n2 = n1 + mLrw;
                                for (int k = n1; k < n2; k++) {
                                    dest[k] = c;
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < frame.ih; i++) {
            int line = i;
            if (frame.interlace) {
                if (iline >= frame.ih) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += frame.iy;
            if (line < mHeight) {
                int k = line * mWidth;
                int dx = k + frame.ix; // start of line in dest
                int dlim = dx + frame.iw; // end of dest line
                if ((k + mWidth) < dlim) {
                    dlim = k + mWidth; // past dest edge
                }
                int sx = i * frame.iw; // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    int index = ((int) pixels[sx++]) & 0xff;
                    int c = mAct[index];
                    if (c != 0) {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }
        if (mNeedCheckMemory) {
            int needMemory = (mWidth * mHeight) * 4;
            if (!GifCommonUtil.checkMemory(needMemory)) {
                if (mTotalLoopTimes < 0)
                    mImage = GifCommonUtil.getDefaultBitmap();
                mStatus = STATUS_INTERRUPT;
            } else {
                mImage = Bitmap.createBitmap(dest, mWidth, mHeight, Bitmap.Config.ARGB_4444);
            }
        } else {
            mImage = Bitmap.createBitmap(dest, mWidth, mHeight, Bitmap.Config.ARGB_4444);
        }
    }


    public Bitmap getFrameImage(int n) {
        GifFrame frame = getFrame(n);
        if (frame == null)
            return null;
        else
            return frame.image;
    }

    public GifFrame getFrame(int n) {
        GifFrame frame = mGifFrame;
        int i = 0;
        while (frame != null) {
            if (i == n) {
                return frame;
            } else {
                frame = frame.nextFrame;
            }
            i++;
        }
        return null;
    }


    public void reset(){
        mLock.lock();
        mCurrentFrame = mGifFrame;
        mLock.unlock();
    }

//    public GifFrame nextFrame() {
//
//    }

    public GifFrame next() {
        mLock.lock();
        try {
            if(mIsShow == false){
                mIsShow = true;
                return mGifFrame;
            } else {
                if(mCurrentFrame == null) {
                    return mGifFrame;
                }
                if(mStatus == STATUS_PARSING) {
                    if (mUseRawData) {
                        int lastFrameIndex = mCurrentFrame.index;
                        if (lastFrameIndex % BaseGifDecoder.STEP == 0 && lastFrameIndex != mGifHeader.frameCount-1
                                && lastFrameIndex != 0) {
                            addQueue(lastFrameIndex + STEP);
                        }
                    }

                    if(mCurrentFrame.nextFrame != null) {
                        mCurrentFrame = mCurrentFrame.nextFrame;
                    } else {
                        return null;
                    }
                } else {
                    mCurrentFrame = mCurrentFrame.nextFrame;
                    if (mCurrentFrame == null) {
                        mLoopedTimes++;
                        if (mTotalLoopTimes > 0 && mLoopedTimes >= mTotalLoopTimes) {
                            if (mUseRawData) {
                                mStopped = true;
                                synchronized (mThreadLock) {
                                    mThreadLock.notify();
                                }
                            }
                            return null;
                        } else {
                            if (mUseRawData) {
                                synchronized (mThreadLock) {
                                    mThreadLock.notify();
                                }
                                return null;
                            } else {
                                mCurrentFrame = mGifFrame;
                            }
                        }
                    }
                }
                return mCurrentFrame;
            }
        } finally {
            mLock.unlock();
        }
    }

    public int getLoopedTimes() {
        return mLoopedTimes;
    }

    private int readByte(){
        mInputStream = new ByteArrayInputStream(mGifData);
        mGifData = null;
        return readStream();
    }

    private int readStream(){
        init();
        if(mInputStream != null){
            readHeader();
            if(!err()){
                readContents();
                if(err() || mFrameCount < 0){
                    mStatus = STATUS_FORMAT_ERROR;
                    mAction.parseOk(false,-1);
                } else {
                    mStatus = STATUS_FINISH;
                    mAction.parseOk(true,-1);
                }
            } else {
                mStatus = STATUS_FORMAT_ERROR;
                mAction.parseOk(false, -1);
            }
            try {
                mInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mStatus = STATUS_OPEN_ERROR;
            mAction.parseOk(false,-1);
        }
        return mStatus;
    }

    private int readRawData() {
        init();
        mFrameQueue = new ArrayBlockingQueue<Integer>(COUNT);
        if(mRawData != null){
            readHeader();
            if(!err()){
                readContents();
                if (err()) {
                    mAction.parseOk(false,-1);
                } else {
                    synchronized (mThreadLock) {
                        try {
                            while (!mStopped) {
                                playImages();
                                mThreadLock.wait();
                            }
                            synchronized (mRawDataLock) {
                                if (mRawData != null) {
                                    mRawData.clear();
                                    mRawData = null;
                                }
                            }
                        } catch (Exception e) {
                            synchronized (mRawDataLock) {
                                if (mRawData != null) {
                                    mRawData.clear();
                                    mRawData = null;
                                }
                            }
                        }
                    }
                }
            } else {
                synchronized (mRawDataLock) {
                    if (mRawData != null) {
                        mRawData.clear();
                        mRawData = null;
                    }
                }
            }
        } else {
            mStatus = STATUS_OPEN_ERROR;
            mAction.parseOk(false,-1);
        }
        return mStatus;
    }

    private void decodeImageData(GifFrameData frame) {
        if (frame == null) return;
        int NullCode = -1;
        int npix = frame.iw * frame.ih;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

        if ((pixels == null) || (pixels.length < npix)) {
            pixels = new byte[npix]; // allocate new pixel array
        }
        if (prefix == null) {
            prefix = new short[MaxStackSize];
        }
        if (suffix == null) {
            suffix = new byte[MaxStackSize];
        }
        if (pixelStack == null) {
            pixelStack = new byte[MaxStackSize + 1];
        }
        if (mUseRawData) {
            synchronized (mRawDataLock){
                if (mRawData == null) return;
                try {
                    mRawData.position(frame.bufferFrameStart);
                } catch (Exception e) {

                }
            }
        }
        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = NullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }

        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix;) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) mBlock[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;

                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    pixelStack[top++] = suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                first = ((int) suffix[code]) & 0xff;
                // Add a new string to the string table,
                /*if (available >= MaxStackSize) {
                    break;
                }*/
                pixelStack[top++] = (byte) first;
                if(available < MaxStackSize) {
                    prefix[available] = (short) old_code;
                    suffix[available] = (byte) first;
                    available++;
                    if (((available & code_mask) == 0)
                            && (available < MaxStackSize)) {
                        code_size++;
                        code_mask += available;
                    }
                }
                old_code = in_code;
            }

            // Pop a pixel off the pixel stack.
            top--;
            pixels[pi++] = pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            pixels[i] = 0; // clear missing pixels
        }
    }

    private boolean err() {
        return mStatus != STATUS_PARSING;
    }

    private void init() {
        mStatus = STATUS_PARSING;
        mFrameCount = 0;
        mGifFrame = null;
        if (mRawData != null) {
            mRawData.position(0);
        }
        if (mGifHeader != null) {
            mGifHeader.reset();
        }
    }

    private int read() {
        int curByte = 0;
        try {
            if (mUseRawData) {
                synchronized (mRawDataLock) {
                    if (mRawData == null) return 0;
                    curByte = mRawData.get() & 0x000000FF;
                }
            } else {
                curByte = mInputStream.read();
            }
        } catch (Exception e) {
            mStatus = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    private int readBlock() {
        mBlockSize = read();
        int n = 0;
        if (mBlockSize > 0) {
            try {
                int count = 0;
                while (n < mBlockSize) {
                    if (mUseRawData) {
                        count = mBlockSize - n;
                        synchronized (mRawDataLock) {
                            if (mRawData == null) return 0;
                            mRawData.get(mBlock, n, count);
                        }
                    } else {
                        count = mInputStream.read(mBlock, n, mBlockSize - n);
                        if (count == -1) {
                            break;
                        }
                    }
                    n += count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n < mBlockSize) {
                mStatus = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    private int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            if (mUseRawData) {
                n = nbytes;
                synchronized (mRawDataLock){
                    if (mRawData == null) return null;
                    mRawData.get(c);
                }
            } else {
                n = mInputStream.read(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            mStatus = STATUS_FORMAT_ERROR;
        } else {
            tab = new int[256]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return tab;
    }

    private void initGlobalArray() {
        mLastBgColor = 0;
        mLastImage = null;
        mLastDispose = 0;
    }

    public void addQueue(int start) {
        try {
            int num=0;
            for(int i=start;i<mGifHeader.frameCount;i++) {
                GifFrame frame = getFrame(i);
                if (frame != null && frame.image != null)
                    continue;
                num++;
                if (mFrameQueue.size() < COUNT) {
                    mFrameQueue.put(i);
                    if (num >= COUNT)
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initQueue(int count) {
        try {
            mFrameQueue.clear();
            for (int i=0;i<count;i++) {
                mFrameQueue.put(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void playImages() {
        if (mGifHeader == null || mGifHeader.frameCount <= 0) {
            mStatus = STATUS_FORMAT_ERROR;
            mAction.parseOk(false,-1);
            return;
        }
        initGlobalArray();
        initQueue(COUNT);
        mGifFrame = null;
        mCurrentFrame = null;
        mFrameCount = 0;
        mStatus = STATUS_PARSING;
        try {
            while (!mStopped) {
                int index = mFrameQueue.take();
                GifFrameData frame = mGifHeader.getFrames().get(index);
                playSingleFrame(frame, index);
                if (index == mGifHeader.frameCount -1)
                    break;
            }
        } catch (Exception e) {
        }
        mStatus = STATUS_FINISH;
        mAction.parseOk(true,-1);
    }

    private void readContents() {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    if (mGifHeader.currentFrame == null) {
                        mGifHeader.currentFrame = new GifFrameData();
                    }
                    readImage();
                    if (mStatus == STATUS_INTERRUPT) return;
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            mGifHeader.currentFrame = new GifFrameData();
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            String app = "";
                            for (int i = 0; i < 11; i++) {
                                app += (char) mBlock[i];
                            }
                            if (app.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens
                    break;
                default:
                    mStatus = STATUS_FORMAT_ERROR;
            }
        }
    }

    private void readGraphicControlExt() {
        int blocksize = read(); // block size
//        LOGD("++++++++++++++++++++++++++++++++block size:"+ blocksize);
        int packed = read(); // packed fields
        mGifHeader.currentFrame.dispose = (packed & 0x1c) >> 2; // disposal method
        if (mGifHeader.currentFrame.dispose == 0) {
            mGifHeader.currentFrame.dispose = 1; // elect to keep old image if discretionary
        }
        mGifHeader.currentFrame.transparency = (packed & 1) != 0;
        mGifHeader.currentFrame.delay = readShort() * 10; // delay in milliseconds
        mGifHeader.currentFrame.delay = mGifHeader.currentFrame.delay == 0 ? 100 : mGifHeader.currentFrame.delay;
//        LOGD("++++++++++++++++++++++++++++++++delay time:"+ mGifHeader.currentFrame.delay);
        mGifHeader.currentFrame.transIndex = read(); // transparent color index
        read(); // block terminator
    }

    private void readHeader() {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read();
        }
        LOGD("+++++++++++++++gif header : "+ id);
        if (!id.startsWith("GIF")) {
            mStatus = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (mGifHeader.gctFlag && !err()) {
            mGifHeader.gct = readColorTable(mGifHeader.gctSize);
            if (mGifHeader.gct == null) {
                mStatus = STATUS_FORMAT_ERROR;
                return;
            }
            mBgColor = mGifHeader.gct[mGifHeader.bgIndex];
        }
    }

    private void readImage() {
        mGifHeader.currentFrame.ix = readShort(); // (sub)image position & size
        mGifHeader.currentFrame.iy = readShort();
        mGifHeader.currentFrame.iw = readShort();
        mGifHeader.currentFrame.ih = readShort();
        int packed = read();
        mGifHeader.currentFrame.lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
        mGifHeader.currentFrame.interlace = (packed & 0x40) != 0; // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        mGifHeader.currentFrame.lctSize = 2 << (packed & 7); // 6-8 - local color table size
        if (mGifHeader.currentFrame.lctFlag) {
            mGifHeader.currentFrame.lct = readColorTable(mGifHeader.currentFrame.lctSize); // read table
        } else {
            mGifHeader.currentFrame.lct = null;
        }
        if (mUseRawData) {
            synchronized (mRawDataLock) {
                if (mRawData == null) return;
                mGifHeader.currentFrame.bufferFrameStart = mRawData.position();
            }
            if (err()) {
                return;
            }
            read();
            skip();
            if (mGifHeader.getFrames() != null) {
                mGifHeader.frameCount++;
                mGifHeader.getFrames().add(mGifHeader.currentFrame);
            }
            return;
        }
        playSingleFrame(mGifHeader.currentFrame);
    }
    private void playSingleFrame(GifFrameData frame) {
        if (frame.lctFlag) {
            mAct = frame.lct; // make local table active
        } else {
            mAct = mGifHeader.gct; // make global table active
            if (mGifHeader.bgIndex == frame.transIndex) {
                mBgColor = 0;
            }
        }
        if (mAct == null) {
            mStatus = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err()) {
            return;
        }
        int save = 0;
        if (frame.transparency) {
            save = mAct[frame.transIndex];
            mAct[frame.transIndex] = 0; // set transparent color if specified
        }
        decodeImageData(frame);
        if (!mUseRawData) skip();
        if (err()) {
            return;
        }
        mFrameCount++;
        setPixels(frame);
        mLock.lock();
        if (mGifFrame == null) {
            mGifFrame = new GifFrame(mImage, frame.delay, 0);
            mCurrentFrame = mGifFrame;
        } else {
            GifFrame f = mGifFrame;
            while (f.nextFrame != null) {
                f = f.nextFrame;
            }
            f.nextFrame = new GifFrame(mImage, frame.delay, f.index+1);
        }
        if (frame.transparency) {
            mAct[frame.transIndex] = save;
        }
        resetFrame(frame);
        mLock.unlock();
        mAction.parseOk(true, mFrameCount);
    }

    private void playSingleFrame(GifFrameData frame, int frameIndex) {
        if (frame.lctFlag) {
            mAct = frame.lct; // make local table active
        } else {
            mAct = mGifHeader.gct; // make global table active
            if (mGifHeader.bgIndex == frame.transIndex) {
                mBgColor = 0;
            }
        }
        if (mAct == null) {
            mStatus = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err()) {
            return;
        }
        int save = 0;
        if (frame.transparency) {
            save = mAct[frame.transIndex];
            mAct[frame.transIndex] = 0; // set transparent color if specified
        }
        decodeImageData(frame);
        if (!mUseRawData) skip();
        if (err()) {
            return;
        }
        mFrameCount++;
        setPixels(frame);
        mLock.lock();
        if (mGifFrame == null) {
            mGifFrame = new GifFrame(mImage, frame.delay, 0);
            mCurrentFrame = mGifFrame;
        } else {
            GifFrame f = getFrame(frameIndex-1);
            if (f.nextFrame == null) {
                f.nextFrame = new GifFrame(mImage, frame.delay, frameIndex);
            } else {
                f.nextFrame.image =  mImage;
            }
        }
        if (frame.transparency) {
            mAct[frame.transIndex] = save;
        }
        resetFrame(frame);
        mLock.unlock();
        mAction.parseOk(true, mFrameCount);
    }

    private void readLSD() {
        // logical screen size
        mWidth = readShort();
        mHeight = readShort();

        float scale = 1;
        if (mImageRequireHeight != -1 && mImageRequireHeight < mHeight) {
            int scaleHeight = mHeight / mImageRequireHeight;
            if (scale < scaleHeight) scale = scaleHeight;
        }
        if (mImageRequireWidth != -1 && mImageRequireWidth < mWidth) {
            int scaleWidth = mWidth / mImageRequireWidth;
            if (scale < scaleWidth) scale = scaleWidth;
        }
        mWidth = (int) (mWidth / scale);
        mHeight = (int) (mHeight / scale);

        mGifHeader.width = mWidth;
        mGifHeader.height = mHeight;

        // packed fields
        int packed = read();
        mGifHeader.gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        mGifHeader.gctSize = 2 << (packed & 7); // 6-8 : gct size
        mGifHeader.bgIndex = read(); // background color index
        mGifHeader.pixelAspect = read(); // pixel aspect ratio
    }

    private void readNetscapeExt() {
        do {
            readBlock();
            if (mBlock[0] == 1) {
                // loop count sub-block
                int b1 = ((int) mBlock[1]) & 0xff;
                int b2 = ((int) mBlock[2]) & 0xff;
                mLoopCount = (b2 << 8) | b1;
            }
        } while ((mBlockSize > 0) && !err());
    }

    private int readShort() {
        if (mUseRawData) {
            synchronized (mRawDataLock) {
                if (mRawData == null) return 0;
                return mRawData.getShort();
            }
        } else {
            // read 16-bit value, LSB first
            return read() | (read() << 8);
        }
    }

    private void resetFrame(GifFrameData frame) {
        if (frame == null) return;
        mLastDispose = frame.dispose;
        mLrx = frame.ix;
        mLry = frame.iy;
        mLrw = frame.iw;
        mLrh = frame.ih;
        mLastImage = mImage;
        mLastBgColor = mBgColor;
        if (!mUseRawData) {
            frame.dispose = 0;
            frame.transparency = false;
            frame.delay = 0;
            frame.lct = null;
        }
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    private void skip() {
        do {
            readBlock();
        } while ((mBlockSize > 0) && !err());
    }

    private void LOGD(String log) {
        if (DEBUG)
            Log.d(TAG, log);
    }
}
