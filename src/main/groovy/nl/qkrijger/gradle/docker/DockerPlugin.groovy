package nl.qkrijger.gradle.docker

import nl.qkrijger.gradle.docker.tasks.BuildImageTask
import nl.qkrijger.gradle.docker.tasks.TagImageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class DockerPlugin implements Plugin<Project> {

  private static final String BUILD_IMAGE_TASK_NAME = 'buildImage'
  private static final String TAG_IMAGE_TASK_NAME = 'tagImage'

  private Project project

  @Override
  void apply(Project project) {

    this.project = project

    project.ext.docker = [:]

    exportShellScripts()

    project.task(BUILD_IMAGE_TASK_NAME, type: BuildImageTask) {
      description = 'Builds the Dockerfile in your project root folder.'
      group = "Docker"
    }

    project.task(TAG_IMAGE_TASK_NAME, type: TagImageTask) {
      description = 'Tag the built during "buildImage" with the docker.imageName and project.version.'
      group = "Docker"
    }.dependsOn BUILD_IMAGE_TASK_NAME

  }

  private void exportShellScripts() {
    Path targetCache = Files.createDirectories(Paths.get(project.file('.gradle/docker').absolutePath))

    def scripts = ['build.sh']

    scripts.each { script ->
      def source = getClass().getResourceAsStream("/docker/$script")
      def destination = targetCache.resolve(script)
      Files.copy source, destination, StandardCopyOption.REPLACE_EXISTING
    }
  }

}
