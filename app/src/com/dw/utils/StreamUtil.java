package com.dw.utils;

import android.content.res.AssetFileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class StreamUtil {
	
    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
                stream = null;
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close stream", e);
            } catch (Exception e) {
				// TODO: handle exception
			}
        }
    }
    
    public static void closeZipFile(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
                zipFile = null;
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close zipfile", e);
            } catch (Exception e) {
				// TODO: handle exception
			}
        }
    }
    
    public static void closeZipInputStream(ZipInputStream zipInputStream) {
        if (zipInputStream != null) {
            try {
                zipInputStream.close();
                zipInputStream = null;
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close zipfile", e);
            } catch (Exception e) {
				// TODO: handle exception
			}
        }
    }
    
    public static void closeZipOutputStream(ZipOutputStream zipOutputStream) {
        if (zipOutputStream != null) {
            try {
                zipOutputStream.close();
                zipOutputStream = null;
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close zipfile", e);
            } catch (Exception e) {
				// TODO: handle exception
			}
        }
    }
    
    public static void closeAssetFileDescriptor(AssetFileDescriptor asset) {
        if (asset != null) {
            try {
                asset.close();
                asset = null;
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close AssetFileDescriptor", e);
            } catch (Exception e) {
				// TODO: handle exception
			}
        }
    }
    
}