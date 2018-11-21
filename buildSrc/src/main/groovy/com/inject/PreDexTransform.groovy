package com.inject

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class PreDexTransform extends Transform {

    Project project

    public PreDexTransform(Project project) {
        this.project = project

        // 获取到hack module的debug目录，也就是Antilazy.class所在的目录
        def libPath = project.project(':hack').buildDir.absolutePath.concat("\\intermediates\\classes\\release")
//        Inject.appendClassPath(libPath)
        Inject.appendClassPath(project.rootProject.rootDir.absolutePath + "/buildSrc/libs/android.jar")
        println("|||"+project.rootDir)
        println(project.rootProject.name)
    }

    @Override
    String getName() {
        return "preDex"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        // Transfrom的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        println("enter transform")
        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput directoryInput ->

                //TODO 这里可以对input的文件做处理，比如代码注入！
                project.logger.error("name =" + directoryInput.name + " directoryInput.contentTypes=" + directoryInput.contentTypes +  " directoryInput.scopes ="+ directoryInput.scopes+" filePath=" + directoryInput.file.absolutePath)
                Inject.injectDir(directoryInput.file.absolutePath)

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                project.logger.error("dest =" + dest)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->

                //TODO 这里可以对input的文件做处理，比如代码注入！
                String jarPath = jarInput.file.absolutePath;
                project.logger.error("jarInput=="+jarPath)
                String projectName = project.rootProject.name;

                if (jarPath.endsWith("classes.jar")
                        && jarPath.contains("exploded-aar\\" + projectName)
                        // hotpatch module是用来加载dex，无需注入代码
                        && !jarPath.contains("exploded-aar\\" + projectName + "\\hotpatch")) {
                    Inject.injectJar(jarPath)
                }

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                project.logger.error("jarName="+jarName+" md5Name="+md5Name+" dest =" + dest)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}