package com.dw.filemap;

import android.util.Log;

import com.dw.DynamicApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by dw on 18-10-26.
 */

public class FileMapUtils {
    static MappedByteBuffer mbb;
    static int index = 0;
    static int[] size = new int[]{10,50,100,300,400,500};

    public static void load() {
        String fileDirString = DynamicApplication.mRealApplication.getFilesDir().getAbsolutePath();
        String bigFilePath = fileDirString + File.separator + "bigfile";
        try {
            RandomAccessFile rf = new RandomAccessFile(bigFilePath, "rw");
            long length = rf.length();
            Log.d("xx", "FileMapUtils  length == "+length);
            mbb = rf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
            int i = 0;
            int curSize = size[index++ % size.length];
            Log.d("xx", "FileMapUtils  load  begin == ");
            long start = System.currentTimeMillis();
            while ( i++ < curSize * 1024 * 1024)
                mbb.get();
            Log.d("xx", "FileMapUtils  load  end == "+(System.currentTimeMillis() - start)+ "  mem size =="+curSize+" M");
            Log.d("xx","max mem ="+Runtime.getRuntime().maxMemory()+" total mem ="+Runtime.getRuntime().totalMemory()+" free mem ="+Runtime.getRuntime().freeMemory());
            rf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
