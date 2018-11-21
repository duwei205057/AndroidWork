package com.dw.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * Created by dw on 18-7-20.
 */

public class PingBackUtils {

    private static final int DEVICEINFO_UNKNOWN = -1;

    public static int getNumberOfCPUCores() {
        // Gingerbread doesn't support giving a single application access to both cores
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            cores = DEVICEINFO_UNKNOWN;
        }
        return cores;
    }
    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };

    /**
     * @return freq in KH
     */
    public static int[] getCPUMaxFreqKHz() {
        int maxFreq = -1;
        int maxFreqNum = -1;
        FileInputStream stream = null;
        for (int i = 0; i < getNumberOfCPUCores(); i++) {
            String filename =
                    "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
            File cpuInfoMaxFreqFile = new File(filename);
            if (cpuInfoMaxFreqFile.exists()) {
                byte[] buffer = new byte[128];
                try {
                    stream = new FileInputStream(cpuInfoMaxFreqFile);
                    stream.read(buffer);
                    int endIndex = 0;
                    while (buffer[endIndex] >= '0' && buffer[endIndex] <= '9' && endIndex < buffer.length)
                        endIndex++;
                    String str = new String(buffer, 0, endIndex);
                    Integer freqBound = Integer.parseInt(str);
                    if (freqBound > maxFreq) {
                        maxFreq = freqBound;
                        maxFreqNum = 1;
                    } else if (freqBound == maxFreq) {
                        maxFreqNum++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (maxFreq == -1) {
            try {
                stream = new FileInputStream("/proc/cpuinfo");
                InputStreamReader bis = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(bis);
                String line;
                while ((line = br.readLine()) != null) {
                    int end = line.indexOf("GHz");
                    if (end > 0) {
                        int start = end - 1;
                        boolean foundEnd = false;
                        while (start >= 0) {
                            if ((line.charAt(start) >= '0' && line.charAt(start) <= '9') || line.charAt(start) == '.') {
                                if (foundEnd) start --;
                                else {
                                    foundEnd = true;
                                    end = start + 1;
                                }
                            } else {
                                if (foundEnd) {
                                    break;
                                } else start --;
                            }
                        }
                        if (foundEnd){
                            maxFreq = (int)(Float.parseFloat(line.substring(start + 1, end)) * 1000 * 1000);
                            maxFreqNum = 1;

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                StreamUtil.closeStream(stream);
            }
        }
        return new int[]{maxFreq, maxFreqNum};
    }

    public static String getCpuName() {
        FileInputStream stream = null;
        String cpuName = null;
        try {
            stream = new FileInputStream("/proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(ir);
            String nameProcessor = "Processor";
            String nameModel = "model name";
            String line;
            String[] pair = null;
            while ((line = br.readLine()) != null) {
                pair = line.split(":");
                if (pair.length != 2)
                    continue;
                String key = pair[0].trim();
                String val = pair[1].trim();
                if (key.compareTo(nameProcessor) == 0) {
                    cpuName = val.replaceAll("\\s","_");
                    continue;
                }

                if (key.compareToIgnoreCase(nameModel) == 0) {
                    cpuName = val.replaceAll("\\s","_");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            StreamUtil.closeStream(stream);
        }
        return cpuName;

    }

    public static String getDeviceCpuABI() {
        if (Build.VERSION.SDK_INT >= 21)
            return Arrays.toString(Build.SUPPORTED_ABIS).replaceAll("\\s","");
        else
            return Build.CPU_ABI;
    }

    static class StreamUtil{
        public static void closeStream(InputStream input){
            if(input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
