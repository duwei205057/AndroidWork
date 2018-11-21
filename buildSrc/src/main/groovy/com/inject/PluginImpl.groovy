package com.inject

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class PluginImpl implements Plugin<Project>{
    @Override
    public void apply(Project project){
        def isApp = project.plugins.hasPlugin(AppPlugin)
        project.logger.error("================自定义插件成功！=========="+isApp)
        if (isApp) {
            def android = project.extensions.findByType(AppExtension)
            android.registerTransform(new PreDexTransform(project))
        }
    }
}