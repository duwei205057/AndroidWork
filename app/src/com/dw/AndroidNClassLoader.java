package com.dw;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AndroidNClassLoader extends PathClassLoader {
    private final static String CHECK_CLASSLOADER_CLASS = "com.sogou.hotfix.patcher.CheckClassLoader";

    private static ArrayList<DexFile> oldDexFiles = new ArrayList<>();
    private final PathClassLoader originClassLoader;
    private String applicationClassName;

    private AndroidNClassLoader(String dexPath, PathClassLoader parent, Application application) {
        super(dexPath, parent.getParent());
        originClassLoader = parent;
        String name = application.getClass().getName();
        if (name != null && !name.equals("android.app.Application")) {
            applicationClassName = name;
        }
    }

    private static AndroidNClassLoader createAndroidNClassLoader(PathClassLoader original, Application application) throws NoSuchFieldException,
            IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IOException {
        //let all element ""
        AndroidNClassLoader androidNClassLoader = new AndroidNClassLoader("",  original, application);
        Field originPathList = com.dw.ReflectUtil.findField(original, "pathList");
        Object originPathListObject = originPathList.get(original);
        //should reflect definingContext also
        Field originClassloader = com.dw.ReflectUtil.findField(originPathListObject, "definingContext");
        originClassloader.set(originPathListObject, androidNClassLoader);
        //copy pathList
        Field pathListField = com.dw.ReflectUtil.findField(androidNClassLoader, "pathList");
        //just use PathClassloader's pathList
        pathListField.set(androidNClassLoader, originPathListObject);

        //we must recreate dexFile due to dexCache
        List<File> additionalClassPathEntries = new ArrayList<>();
        Field dexElement = ReflectUtil.findField(originPathListObject, "dexElements");
        Object[] originDexElements = (Object[]) dexElement.get(originPathListObject);
        for (Object element : originDexElements) {
            DexFile dexFile = (DexFile) ReflectUtil.findField(element, "dexFile").get(element);
            if (dexFile == null) {
                continue;
            }
            additionalClassPathEntries.add(new File(dexFile.getName()));
            //protect for java.lang.AssertionError: Failed to close dex file in finalizer.
            oldDexFiles.add(dexFile);
        }
        Method makePathElements = ReflectUtil.findMethod(originPathListObject, "makePathElements", List.class, File.class,
                List.class);
        ArrayList<IOException> suppressedExceptions = new ArrayList<>();
        Object[] newDexElements = (Object[]) makePathElements.invoke(originPathListObject, additionalClassPathEntries, null, suppressedExceptions);
        dexElement.set(originPathListObject, newDexElements);

        try {
            Class.forName(CHECK_CLASSLOADER_CLASS, true, androidNClassLoader);
        } catch (Throwable thr) {
            Log.e("cjy", "load TinkerTestAndroidNClassLoader fail, try to fixDexElementsForProtectedApp");
            fixDexElementsForProtectedApp(application, newDexElements);
        }

        return androidNClassLoader;
    }

    private static void reflectPackageInfoClassloader(Application application, ClassLoader reflectClassLoader) throws NoSuchFieldException, IllegalAccessException {
        String defBase = "mBase";
        String defPackageInfo = "mPackageInfo";
        String defClassLoader = "mClassLoader";

        Context baseContext = (Context) ReflectUtil.findField(application, defBase).get(application);
        Object basePackageInfo = ReflectUtil.findField(baseContext, defPackageInfo).get(baseContext);
        Field classLoaderField = ReflectUtil.findField(basePackageInfo, defClassLoader);
        Thread.currentThread().setContextClassLoader(reflectClassLoader);
        classLoaderField.set(basePackageInfo, reflectClassLoader);
    }

    public static AndroidNClassLoader inject(PathClassLoader originClassLoader, Application application) throws NoSuchFieldException,
            IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IOException {
        AndroidNClassLoader classLoader = createAndroidNClassLoader(originClassLoader, application);
        reflectPackageInfoClassloader(application, classLoader);
        return classLoader;
    }

    // Basically this method would use base.apk to create a dummy DexFile object,
    // then set its fileName, cookie, internalCookie field to the value
    // comes from original DexFile object so that the encrypted dex would be taking effect.
    private static void fixDexElementsForProtectedApp(Application application, Object[] newDexElements) throws NoSuchFieldException,
            IllegalAccessException, IOException {
        Field zipField = null;
        Field dexFileField = null;
        final Field mFileNameField = ReflectUtil.findField(DexFile.class, "mFileName");
        final Field mCookieField = ReflectUtil.findField(DexFile.class, "mCookie");
        final Field mInternalCookieField = ReflectUtil.findField(DexFile.class, "mInternalCookie");

        // Always ignore the last element since it should always be the base.apk.
        for (int i = 0; i < newDexElements.length - 1; ++i) {
            final Object newElement = newDexElements[i];

            if (zipField == null && dexFileField == null) {
                zipField = ReflectUtil.findField(newElement, "zip");
                dexFileField = ReflectUtil.findField(newElement, "dexFile");
            }

            final DexFile origDexFile = oldDexFiles.get(i);
            final String origFileName = (String) mFileNameField.get(origDexFile);
            final Object origCookie = mCookieField.get(origDexFile);
            final Object origInternalCookie = mInternalCookieField.get(origDexFile);

            final DexFile dupOrigDexFile = DexFile.loadDex(application.getApplicationInfo().sourceDir, null, 0);
            mFileNameField.set(dupOrigDexFile, origFileName);
            mCookieField.set(dupOrigDexFile, origCookie);
            mInternalCookieField.set(dupOrigDexFile, origInternalCookie);

            dexFileField.set(newElement, dupOrigDexFile);

            // Just for better looking when dump new classloader.
            // Avoid such output like this: DexPathList{zip file: /xx/yy/zz/uu.odex}
            final File newZip = (File) zipField.get(newElement);
            final String newZipPath = (newZip != null ? newZip.getAbsolutePath() : null);
            if (newZipPath != null && !newZipPath.endsWith(".zip") && !newZipPath.endsWith(".jar") && !newZipPath.endsWith(".apk")) {
                zipField.set(newElement, null);
            }
        }
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        if ((name != null
                && (name.startsWith("com.sogou.hotfix.patcher") || name.startsWith("com.sogou.hotfix.versionmanager"))
                && !name.equals(CHECK_CLASSLOADER_CLASS))
                || (applicationClassName != null && TextUtils.equals(applicationClassName, name))) {
            return originClassLoader.loadClass(name);
        }
        return super.findClass(name);
    }

    @Override
    public String findLibrary(String name) {
        return super.findLibrary(name);
    }
}

