package com.tencent.gradle

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the plugin's tasks.
 *
 * @author sim sun (sunsj1231@gmail.com)
 */

class AndResGuardPlugin implements Plugin<Project> {
  public static final String USE_APK_TASK_NAME = "UseApk"

  @Override
  public void apply(Project project) {
    if (!project.plugins.hasPlugin('com.android.application')) {
      throw new GradleException('generateARGApk: Android Application plugin required')
    }
    project.apply plugin: 'com.google.osdetector'
    //添加 andResGuard 扩展
    project.extensions.create('andResGuard', AndResGuardExtension)
    //添加 sevenzip 扩展
    project.extensions.add("sevenzip", new ExecutorExtension("sevenzip"))

    project.afterEvaluate {
      def android = project.extensions.getByName("android")
      android.applicationVariants.all { variant ->
        variant as ApplicationVariantImpl
        def variantName = variant.name.capitalize()
        println("variantName " + variantName)
        createTask(project, variantName, variant)
      }

      //查找7zip依赖 并依赖给项目
      project.extensions.findByName("sevenzip").loadArtifact(project)
    }
  }

  private static void createTask(Project project, variantName, ApplicationVariantImpl variant) {
    def taskName = "resguard${variantName}"
    if (project.tasks.findByPath(taskName) == null) {
      def task = project.task(taskName, type: AndResGuardTask)
      if (variantName != USE_APK_TASK_NAME && variant.buildType.name == "release") {
        variant.assemble.finalizedBy(task)
      }
    }
  }
}