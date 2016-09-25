package org.stackwork.gradle.docker

import org.gradle.api.Project
import org.stackwork.gradle.docker.tasks.DockerComposeRunner

class StackworkObject {

  final Project project
  final String buildDir

  /**
   * Maps services names to information in the form of a map
   */
  Map<String, Map<String, Object>> services = [:]

  /**
   * Module name to type
   */
  Map<String, ModuleType> modules = [:]

  /**
   * Absolute path of this project's Docker File
   */
  String dockerFile
  String imageId
  String fullImageName
  String composeFile
  String composeProject
  String host
  String baseComposeStack

  /**
   * Container ID in case of a module that has an executable image such as a {@link ModuleType.TEST_IMAGE}
   */
  File composeLogFile
  String containerId
  DockerComposeRunner dockerComposeRunner

  StackworkObject(Project project) {
    this.project = project
    buildDir = "${project.buildDir}/stackwork-plugin"
    dockerComposeRunner = new DockerComposeRunner(project)
  }

}
