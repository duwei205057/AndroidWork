package com.dw.utils;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FileOperator {

    public static final boolean deleteDir(File dir) {
        boolean bRet = false;
        if (dir != null && dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                int sz = entries.length;
                for (int i = 0; i < sz; i++) {
                    if (entries[i].isDirectory()) {
                        deleteDir(entries[i]);
                    } else {
                        entries[i].delete();
                    }
                }
                dir.delete();
                bRet = true;
            }
        }
        return bRet;
    }

    public static final boolean writeByteToFile(byte[] src, String filePath) {
        if (src == null || src.length <= 0 || TextUtils.isEmpty(filePath)) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(filePath));
            fos.write(src);
            return true;
        } catch (Exception e) {
        } finally {
            StreamUtil.closeStream(fos);
        }
        return false;
    }

    public static final boolean clearDir(File dir, FileFilter filter) {
        boolean bRet = false;
        if (dir != null && dir.isDirectory()) {
            File[] entries = dir.listFiles(filter);
            if (entries == null)
                return false;
            int sz = entries.length;
            for(int i = 0; i < sz; i++) {
                if(entries[i].isDirectory()) {
                    deleteDir(entries[i]);
                } else {
                    entries[i].delete();
                }
            }
            bRet = true;
        }
        return bRet;
    }

    public static final boolean deleteFile(File file){
        if(file != null && file.isDirectory()){
            return false;
        } else if(file != null && file.isFile()) {
            return file.delete();
        }
        return true;
    }

    public static final boolean deleteFile(String filePath){
    	if (filePath == null) {
    		return true;
    	}
    	File file = new File(filePath);
        if(file != null && file.isDirectory()){
            return false;
        } else if(file != null && file.isFile()) {
            return file.delete();
        }
        return true;
    }

    public static final boolean deleteDirByCmd(String dirPath) {
        if (dirPath == null)
            return false;
        final File file = new File(dirPath);
        if (file.exists()) {
            String deleteCmd = "rm -r " + dirPath;
            try {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(deleteCmd);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static final long getDirectorySize(File dir) {
        long retSize = 0;
        if ((dir == null) || !dir.isDirectory()) {
            return retSize;
        }
        File[] entries = dir.listFiles();
        if(entries == null) return 0;
        int count = entries.length;
        for (int i = 0; i < count; i++) {
            if (entries[i].isDirectory()) {
                retSize += getDirectorySize(entries[i]);
            } else {
                retSize += entries[i].length();
            }
        }
        return retSize;
    }


    public static final long getDirectorySize(File dir, FileFilter filter) {
        long retSize = 0;
        if ((dir == null) || !dir.isDirectory()) {
            return retSize;
        }
        File[] entries = dir.listFiles(filter);
        int count = entries.length;
        for (int i = 0; i < count; i++) {
            if (entries[i].isDirectory()) {
                retSize += getDirectorySize(entries[i]);
            } else {
                retSize += entries[i].length();
            }
        }
        return retSize;
    }

    public static final void createDirectory(String strDir, boolean authorization){
        createDirectory(strDir, authorization, true);
    }

    public static final void createDirectory(String strDir, boolean authorization, boolean clearIfExist) {
        if (strDir == null)
            return;
        try {
            boolean mkdirOk = false;
            File file = new File(strDir);
            if (!file.isDirectory()) {
                file.delete();
                mkdirOk = file.mkdirs();
            } else {
                if (clearIfExist)
                    clearDir(file, null);
            }
            if (mkdirOk && authorization) {
                Runtime.getRuntime().exec(
                        "chmod 777 " + strDir);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final void createDirectoryWithNoMedia(String strDir, boolean authorization, boolean clearIfExist) {
        createDirectory(strDir, authorization, clearIfExist);
        String noMediaFilePath = strDir + "/.nomedia";
        File noMediaFile = new File(noMediaFilePath);
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
            }
        }
    }

    public static final void moveFile(String strOriginal, String strDest) {
        try {
            File fileOriginal = new File(strOriginal);
            File fileDest = new File(strDest);
            fileOriginal.renameTo(fileDest);
        } catch (Exception e) {
        }
    }

    public static final boolean renameFile(String strOriginal, String strDest) {
    	boolean rename = false;
        try {
            File fileOriginal = new File(strOriginal);
            File fileDest = new File(strDest);
            rename = fileOriginal.renameTo(fileDest);
        } catch (Exception e) {
        }
        return rename;
    }

    /**
     *
     * @param srcFilePath file's absolute path eg: /data/data/com.sohu.inputmethod.sogou/files/msgtmp.xml
     * @param destFolderName eg: /data/data/com.sohu.inputmethod.sogou/files/hotdict/
     * @param destFileName eg: example.xml
     */
    public static final boolean copyFile(String srcFilePath, String destFolderName, String destFileName) {
        boolean tmp = false;
        if(srcFilePath == null || destFolderName == null || destFileName == null) {
            return false;
        }

        File srcFile = new File(srcFilePath);
        if(!srcFile.exists() || srcFile.isDirectory()) {
            return false;
        }


        File folder = new File(destFolderName);
        if(!folder.exists()) {
            tmp = folder.mkdirs();
            if(!tmp) {
                return false;
            }
        }

        File destFile = new File(destFolderName + destFileName);
        if(!destFile.exists()) {
            try {
                tmp = destFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
            if(!tmp) {
                return false;
            }
        }

        InputStream is = null;
        BufferedInputStream bis = null;

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            is = new FileInputStream(srcFile);
            bis = new BufferedInputStream(is);

            fos = new FileOutputStream(destFile);
            bos = new BufferedOutputStream(fos);
            byte buffer[] = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            bos.flush();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeStream(bos);
            StreamUtil.closeStream(fos);
            StreamUtil.closeStream(bis);
            StreamUtil.closeStream(is);
        }
        return false;
    }

    /**
     *
     * @param srcFilePath file's absolute path eg: /mnt/sdcard/sogou/download/haomatong.apk.tmp
     * @param destFilePath eg: /mnt/sdcard/sogou/download/haomatong.apk
     */
    public static final boolean copyFile(String srcFilePath, String destFilePath) {
        boolean tmp = false;
        if(srcFilePath == null || destFilePath == null) {
            return false;
        }
        File srcFile = new File(srcFilePath);
        if(!srcFile.exists() || srcFile.isDirectory()) {
            return false;
        }
        File destFile = new File(destFilePath);
        if (srcFile.equals(destFile) || destFile.exists()) return false;
        return copyFile(srcFile, destFile);
    }

    public static final boolean copyFile(File srcFile, File destFile) {
        boolean tmp = false;
        if(srcFile == null || destFile == null) {
            return false;
        }
        if(!destFile.exists()) {
            try {
                tmp = destFile.createNewFile();
            } catch (IOException e) {
            	e.printStackTrace();
                return false;
            }
            if(!tmp) {
                return false;
            }
        }
        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            is = new FileInputStream(srcFile);
            bis = new BufferedInputStream(is);

            fos = new FileOutputStream(destFile);
            bos = new BufferedOutputStream(fos);
            byte buffer[] = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            bos.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeStream(bos);
            StreamUtil.closeStream(fos);
            StreamUtil.closeStream(bis);
            StreamUtil.closeStream(is);
        }
        return false;
    }

    public static final boolean copyDir(String srcFilePath, String destFilePath) throws IOException {
        if(srcFilePath == null || destFilePath == null) {
            return false;
        }

        File srcFile = new File(srcFilePath);
        if(!srcFile.exists() || !srcFile.isDirectory()) {
            return false;
        }

        File destFile = new File(destFilePath);
        if(!destFile.exists()) {
            destFile.mkdirs();
        }

        boolean result = true;
        File[] file = (srcFile).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                File sourceFile = file[i];
                File targetFile = new File(new File(destFilePath).getAbsolutePath() + File.separator + file[i].getName());
                result = result && copyFile(sourceFile, targetFile);
                if (!result)
                    return false;
            }
            if (file[i].isDirectory()) {
                String dir1 = srcFilePath + "/" + file[i].getName();
                String dir2 = destFilePath + "/" + file[i].getName();
                result = result && copyDir(dir1, dir2);
                if (!result)
                    return false;
            }
        }
        return result;
    }

    public static final boolean copyDirFileExceptFolder(String srcFilePath, String destFilePath) throws IOException {
        if(srcFilePath == null || destFilePath == null) {
            return false;
        }
        File srcFile = new File(srcFilePath);
        if(!srcFile.exists() || !srcFile.isDirectory()) {
            return false;
        }
        File destFile = new File(destFilePath);
        if(!destFile.exists()) {
            destFile.mkdirs();
        }

        File[] file = (srcFile).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                File sourceFile = file[i];
                File targetFile = new File(new File(destFilePath).getAbsolutePath() + File.separator + file[i].getName());
                boolean result = copyFile(sourceFile, targetFile);
                if (!result) return false;
            }
        }
        return true;
    }

    public static final boolean clearDirExceptFolder(String dirPath) {
        if (dirPath == null) return true;
        boolean bRet = false;
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                int sz = entries.length;
                for (int i = 0; i < sz; i++) {
                    if (entries[i].isDirectory()) {

                    } else {
                        entries[i].delete();
                    }
                }
                bRet = true;
            }
        }
        return bRet;
    }

    public static final String readFileToString(File file) {
        InputStreamReader reader = null;
        StringWriter writer = new StringWriter();
        try {

            reader = new InputStreamReader(new FileInputStream(file));
            char[] buffer = new char[1024];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                    writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            return null;
        } finally {
            StreamUtil.closeStream(reader);
        }
        if (writer != null)
                return writer.toString();
        else return null;
    }


    public static final boolean writeStringToFile(String string, String filePath) {
        boolean flag = true;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
                File distFile = new File(filePath);
                if (distFile.getParentFile() != null && !distFile.getParentFile().exists()) distFile.getParentFile().mkdirs();
                bufferedReader = new BufferedReader(new StringReader(string));
                bufferedWriter = new BufferedWriter(new FileWriter(distFile));
                char buf[] = new char[1024];
                int len;
                while ((len = bufferedReader.read(buf)) != -1) {
                        bufferedWriter.write(buf, 0, len);
                }
                bufferedWriter.flush();
                bufferedReader.close();
                bufferedWriter.close();
        } catch (Exception e) {
            flag = false;
            return flag;
        } finally {
            StreamUtil.closeStream(bufferedWriter);
            StreamUtil.closeStream(bufferedReader);
        }
        return flag;
    }
    
    
    public static byte[] readFromFile(String fileName, int offset, int len) {
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }

        if (len == -1) {
            len = (int) file.length();
        }


        if(offset <0){
            return null;
        }
        if(len <=0 ){
            return null;
        }
        if(offset + len > (int) file.length()){
            return null;
        }

        byte[] b = null;
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(fileName, "r");
            b = new byte[len];
            in.seek(offset);
            in.readFully(b);
//            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeStream(in);
        }
        return b;
    }

}
