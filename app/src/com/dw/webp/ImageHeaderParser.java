package com.dw.webp;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dw on 18-12-21.
 */

public class ImageHeaderParser {

    private static final String TAG = "ImageHeaderParser";

    public static final int MAX_WEBP_HEADER_SIZE = 21;
    private static final int GIF_HEADER = 0x474946;
    // "RIFF"
    private static final int RIFF_HEADER = 0x52494646;
    // "WEBP"
    private static final int WEBP_HEADER = 0x57454250;
    private static final int VP8_HEADER = 0x56503800;
    private static final int VP8_HEADER_MASK = 0xFFFFFF00;
    private static final int VP8_HEADER_TYPE_MASK = 0x000000FF;

    private static final int VP8_HEADER_TYPE = 0x00000020;
    // 'X'
    private static final int VP8_HEADER_TYPE_EXTENDED = 0x00000058;
    // 'L'
    private static final int VP8_HEADER_TYPE_LOSSLESS = 0x0000004C;
    private static final int WEBP_EXTENDED_ALPHA_FLAG = 1 << 4;
    private static final int WEBP_LOSSLESS_ALPHA_FLAG = 1 << 3;
    private static final int WEBP_EXTENDED_ANIM_FLAG = 1 << 1;

    enum ImageType {
        GIF,
        WEBP_ANIMATED,
        WEBP_STATIC,
        BITMAP;
    }


    public static ImageType getType(File file) throws IOException {
        if( file == null || file.isDirectory() || !file.exists())
            throw new IOException("file is null or not exits");
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getType(is);
        } finally {
            if (is != null) is.close();
        }
    }

    public static ImageType getType(InputStream is) throws IOException {

        if (is == null) {
            throw new IOException("is is null");
        }

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }

        is.mark(MAX_WEBP_HEADER_SIZE);
        try {
            return getType(new StreamReader(is));
        } finally {
            is.reset();
        }
    }


    private static ImageType getType(Reader reader) throws IOException {
        int firstTwoBytes = reader.getUInt16();

        final int firstFourBytes = firstTwoBytes << 16 & 0xFFFF0000 | reader.getUInt16() & 0xFFFF;

        // GIF from first 3 bytes.
        if (firstFourBytes >> 8 == GIF_HEADER) {
            return ImageType.GIF;
        }

        // WebP (reads up to 21 bytes). See https://developers.google.com/speed/webp/docs/riff_container
        // for details.
        if (firstFourBytes != RIFF_HEADER) {
            return ImageType.BITMAP;
        }
        // Bytes 4 - 7 contain length information. Skip these.
        reader.skip(4);
        final int thirdFourBytes = reader.getUInt16() << 16 & 0xFFFF0000 | reader.getUInt16() & 0xFFFF;
        if (thirdFourBytes != WEBP_HEADER) {
            return ImageType.BITMAP;
        }

        final int fourthFourBytes = reader.getUInt16() << 16 & 0xFFFF0000 | reader.getUInt16() & 0xFFFF;
        if ((fourthFourBytes & VP8_HEADER_MASK) != VP8_HEADER) {
            return ImageType.BITMAP;
        }

        WebpHeaderParser.WebpImageType webpImageType;
        if ((fourthFourBytes & VP8_HEADER_TYPE_MASK) == VP8_HEADER_TYPE_EXTENDED) {
            // Skip some more length bytes and check for transparency/alpha flag.
            reader.skip(4);
            int meta = reader.getByte();
            if ((meta & WEBP_EXTENDED_ANIM_FLAG) != 0) {
                webpImageType =  WebpHeaderParser.WebpImageType.WEBP_EXTENDED_ANIMATED;
            } else if ((meta & WEBP_EXTENDED_ALPHA_FLAG) != 0) {
                webpImageType = WebpHeaderParser.WebpImageType.WEBP_EXTENDED_WITH_ALPHA;
            } else {
                webpImageType =  WebpHeaderParser.WebpImageType.WEBP_EXTENDED;
            }
        } else if ((fourthFourBytes & VP8_HEADER_TYPE_MASK) == VP8_HEADER_TYPE_LOSSLESS) {
            // See chromium.googlesource.com/webm/libwebp/+/master/doc/webp-lossless-bitstream-spec.txt
            // for more info.
            reader.skip(4);
            webpImageType = (reader.getByte() & WEBP_LOSSLESS_ALPHA_FLAG) != 0 ? WebpHeaderParser.WebpImageType.WEBP_LOSSLESS_WITH_ALPHA : WebpHeaderParser.WebpImageType.WEBP_LOSSLESS;
        } else {
            webpImageType = WebpHeaderParser.WebpImageType.WEBP_SIMPLE;
        }
        return shouldDecode(webpImageType);
    }

    private static ImageType shouldDecode(WebpHeaderParser.WebpImageType webpType) {
        if (WebpHeaderParser.isStaticWebpType(webpType) && webpType != WebpHeaderParser.WebpImageType.WEBP_SIMPLE) {// handle lossless and transparent webp
            return WebpHeaderParser.sIsExtendedWebpSupported ? ImageType.WEBP_STATIC : ImageType.WEBP_ANIMATED;
        }else if (WebpHeaderParser.isAnimatedWebpType(webpType)) {// handle animated webp
            return ImageType.WEBP_ANIMATED;
        } else {
            return ImageType.WEBP_STATIC;
        }
    }

    private interface Reader {
        int getUInt16() throws IOException;
        short getUInt8() throws IOException;
        long skip(long total) throws IOException;
        int read(byte[] buffer, int byteCount) throws IOException;
        int getByte() throws IOException;
    }

    private static final class StreamReader implements Reader {
        private final InputStream is;
        StreamReader(InputStream is) {
            this.is = is;
        }

        public int getUInt16() throws IOException {
            return (is.read() << 8 & 0xFF00) | (is.read() & 0xFF);
        }

        public short getUInt8() throws IOException {
            return (short) (is.read() & 0xFF);
        }

        public long skip(long total) throws IOException {
            if (total < 0) {
                return 0;
            }
            long toSkip = total;
            while (toSkip > 0) {
                long skipped = is.skip(toSkip);
                if (skipped > 0) {
                    toSkip -= skipped;
                } else {
                    int testEofByte = is.read();
                    if (testEofByte == -1) {
                        break;
                    } else {
                        toSkip--;
                    }
                }
            }
            return total - toSkip;
        }

        public int read(byte[] buffer, int byteCount) throws IOException {
            int toRead = byteCount;
            int read;
            while (toRead > 0 && ((read = is.read(buffer, byteCount - toRead, toRead)) != -1)) {
                toRead -= read;
            }
            return byteCount - toRead;
        }

        public int getByte() throws IOException {
            return is.read();
        }
    }
}
