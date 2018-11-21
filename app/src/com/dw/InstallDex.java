package com.dw;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public final class InstallDex {

    static final String TAG = "InstallDex";

    private InstallDex() {
    }

    static boolean isVMMultidexCapable(String versionString) {
        boolean isMultidexCapable = false;
        if(versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if(matcher.matches()) {
                try {
                    int e = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isMultidexCapable = e > 2 || e == 2 && minor >= 1;
                } catch (NumberFormatException var5) {
                    ;
                }
            }
        }

        Log.i("InstallDex", "VM with version " + versionString + (isMultidexCapable?" has InstallDex support":" does not have InstallDex support"));
        return isMultidexCapable;
    }

    public static void loadThirdPartyDexes(Application application, ClassLoader loader, File dexDir, List<File> files, boolean head) throws
            ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException, InstantiationException, ClassCastException {
        if(!files.isEmpty()) {
            ClassLoader classLoader = loader;
            if (Build.VERSION.SDK_INT >= 24) {
                InstallDex.V24.install(classLoader, files, dexDir, head);
            } else if (Build.VERSION.SDK_INT >= 23) {
                InstallDex.V23.install(classLoader, files, dexDir, head);
            } else if(Build.VERSION.SDK_INT >= 19) {
                InstallDex.V19.install(classLoader, files, dexDir, head);
            } else if(Build.VERSION.SDK_INT >= 14) {
                InstallDex.V14.install(classLoader, files, dexDir, head);
            } else {
                InstallDex.V4.install(classLoader, files, head);
            }
        }
    }


    /**
     *
     * @param loader
     * @param dexDir
     * @param files
     * @param head true: dex要插在最前面; false: dex插在最后面
     *
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws InstantiationException
     */
    public static void installFixDexes(Application application, ClassLoader loader, File dexDir, List<File> files, boolean head) throws
            ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException, InstantiationException, ClassCastException {
        if(!files.isEmpty()) {
            ClassLoader classLoader = loader;
            if (Build.VERSION.SDK_INT >= 24) {
                classLoader = AndroidNClassLoader.inject((PathClassLoader) loader, application);
            }

            if (Build.VERSION.SDK_INT >= 24) {
                InstallDex.V24.install(classLoader, files, dexDir, head);
            } else if (Build.VERSION.SDK_INT >= 23) {
                InstallDex.V23.install(classLoader, files, dexDir, head);
            } else if(Build.VERSION.SDK_INT >= 19) {
                InstallDex.V19.install(classLoader, files, dexDir, head);
            } else if(Build.VERSION.SDK_INT >= 14) {
                InstallDex.V14.install(classLoader, files, dexDir, head);
            } else {
                InstallDex.V4.install(classLoader, files, head);
            }
        }
    }

    private static void expandFieldArray(Object instance, String fieldName, Object[] extraElements, boolean head) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field jlrField = ReflectUtil.findField(instance, fieldName);
        Object[] original = (Object[])((Object[])jlrField.get(instance));
        Object[] combined = (Object[])((Object[]) Array.newInstance(original.getClass().getComponentType(), original.length + extraElements.length));

        if (head) {
            System.arraycopy(extraElements, 0, combined, 0, extraElements.length);
            System.arraycopy(original, 0, combined, extraElements.length, original.length);
        } else {
            System.arraycopy(original, 0, combined, 0, original.length);
            System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        }
        jlrField.set(instance, combined);
    }

    private static final class V4 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, boolean head) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, IOException {
            int extraSize = additionalClassPathEntries.size();
            Field pathField = ReflectUtil.findField(loader, "path");
            StringBuilder path = new StringBuilder((String)pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];

            String entryPath;
            int index;
            for(ListIterator iterator = additionalClassPathEntries.listIterator(); iterator.hasNext(); extraDexs[index] = DexFile.loadDex(entryPath, entryPath + ".dex", 0)) {
                File additionalEntry = (File)iterator.next();
                entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
            }

            pathField.set(loader, path.toString());
            InstallDex.expandFieldArray(loader, "mPaths", extraPaths, head);
            InstallDex.expandFieldArray(loader, "mFiles", extraFiles, head);
            InstallDex.expandFieldArray(loader, "mZips", extraZips, head);
            InstallDex.expandFieldArray(loader, "mDexs", extraDexs, head);
        }
    }

    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory, boolean head) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            InstallDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory), head);
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements", new Class[]{ArrayList.class, File.class});
            Object[] obj = new Object[]{files, optimizedDirectory};
            return (Object[])((Object[])makeDexElements.invoke(dexPathList, obj));
        }
    }

    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory, boolean head) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            InstallDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory, suppressedExceptions), head);
            if(suppressedExceptions.size() > 0) {
                Iterator suppressedExceptionsField = suppressedExceptions.iterator();

                while(suppressedExceptionsField.hasNext()) {
                    IOException dexElementsSuppressedExceptions = (IOException)suppressedExceptionsField.next();
                    Log.w("InstallDex", "Exception in makeDexElement", dexElementsSuppressedExceptions);
                }

                Field suppressedExceptionsField1 = ReflectUtil.findField(loader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions1 = (IOException[])((IOException[])suppressedExceptionsField1.get(loader));
                if(dexElementsSuppressedExceptions1 == null) {
                    dexElementsSuppressedExceptions1 = (IOException[])suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[suppressedExceptions.size() + dexElementsSuppressedExceptions1.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions1, 0, combined, suppressedExceptions.size(), dexElementsSuppressedExceptions1.length);
                    dexElementsSuppressedExceptions1 = combined;
                }

                suppressedExceptionsField1.set(loader, dexElementsSuppressedExceptions1);
            }

        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements", new Class[]{ArrayList.class, File.class, ArrayList.class});
            return (Object[])((Object[])makeDexElements.invoke(dexPathList, new Object[]{files, optimizedDirectory, suppressedExceptions}));
        }
    }

    private static final class V23 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory, boolean head)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException {

            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            Field dexElement = ReflectUtil.findField(dexPathList, "dexElements");
            Class<?> elementType = dexElement.getType().getComponentType();
            Method loadDex = ReflectUtil.findMethod(dexPathList, "loadDexFile", File.class, File.class);
            loadDex.setAccessible(true);

            Object dex = loadDex.invoke(null, additionalClassPathEntries.get(0), optimizedDirectory);
            Constructor<?> constructor = elementType.getConstructor(File.class, boolean.class, File.class, DexFile.class);
            constructor.setAccessible(true);
            Object element = constructor.newInstance(new File(""), false, additionalClassPathEntries.get(0), dex);

            Object[] newEles = new Object[1];
            newEles[0] = element;
            InstallDex.expandFieldArray(dexPathList, "dexElements", newEles, head);
        }

    }

    private static final class V24 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory, boolean head)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, ClassNotFoundException {

            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            Field dexElement = ReflectUtil.findField(dexPathList, "dexElements");
            Class<?> elementType = dexElement.getType().getComponentType();
            Method loadDex = ReflectUtil.findMethod(dexPathList, "loadDexFile", File.class, File.class, ClassLoader.class, dexElement.getType());
            loadDex.setAccessible(true);

            Object dex = loadDex.invoke(null, additionalClassPathEntries.get(0), optimizedDirectory, loader, dexElement.get(dexPathList));
            Constructor<?> constructor = elementType.getConstructor(File.class, boolean.class, File.class, DexFile.class);
            constructor.setAccessible(true);
            Object element = constructor.newInstance(new File(""), false, additionalClassPathEntries.get(0), dex);

            Object[] newEles = new Object[1];
            newEles[0] = element;
            InstallDex.expandFieldArray(dexPathList, "dexElements", newEles, head);
        }

    }
}
