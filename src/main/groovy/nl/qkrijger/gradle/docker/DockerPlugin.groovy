package nl.qkrijger.gradle.docker

import nl.qkrijger.gradle.docker.tasks.BuildImageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class DockerPlugin implements Plugin<Project> {

  private static final String BUILD_IMAGE_TASK_NAME = 'buildImage'

  @Override
  void apply(Project project) {

    project.ext.docker = [:]

    exportShellScripts(project)
    registerBuildImageTask(project)
  }

  private static void exportShellScripts(Project project) {

    Path targetCache = Files.createDirectories(Paths.get(project.file('.gradle/docker').absolutePath))

    def scripts = ['build.sh']

    scripts.each { script ->
      def source = getClass().getResourceAsStream("/docker/$script")
      def destination = targetCache.resolve(script)
      Files.copy source, destination, StandardCopyOption.REPLACE_EXISTING
    }
  }

  private static BuildImageTask registerBuildImageTask(Project project) {
    project.task(BUILD_IMAGE_TASK_NAME, type: BuildImageTask) {
      description = 'Builds the Dockerfile in your project root folder.'
      group = "Docker"
    } as BuildImageTask
  }
}
