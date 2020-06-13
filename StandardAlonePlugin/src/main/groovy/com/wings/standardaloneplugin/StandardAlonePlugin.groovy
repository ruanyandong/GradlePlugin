package com.wings.standardaloneplugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class StandardAlonePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.task("showStandardAlonePlugin"){
            doLast {
                println("task in StandardAlonePlugin")
            }
        }
    }
}