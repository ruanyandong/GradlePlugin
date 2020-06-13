package com.wings.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class CustomBuildSrcPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.task('showCustomPluginInBuildSrc') {
            doLast {
                println("InBuildSrc: Module Name is $project.name")
            }
        }
    }
}