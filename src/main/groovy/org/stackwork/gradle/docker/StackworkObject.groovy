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
   * Absolute path of this project's Docker File
   */
  String dockerFile
  String imageId
  String fullImageName
  String composeProject
  String host
  String baseComposeStack

  /**
   * Container ID in case of a module that has an executable image such as a {@link ModuleType.TEST_IMAGE}
   */
  String containerId
  DockerComposeRunner dockerComposeRunner
  DockerComposeRunner buildDockerComposeRunner
  File composeLogFile
  File buildComposeLogFile

  StackworkObject(Project project) {
    this.project = project
    buildDir = "${project.buildDir}/stackwork-plugin"
  }


  @Override
  String toString() {
    return "StackworkObject{" +
        "project=" + project +
        ", buildDir='" + buildDir + '\'' +
        ", services=" + services +
        ", dockerFile='" + dockerFile + '\'' +
        ", imageId='" + imageId + '\'' +
        ", fullImageName='" + fullImageName + '\'' +
        ", composeProject='" + composeProject + '\'' +
        ", host='" + host + '\'' +
        ", baseComposeStack='" + baseComposeStack + '\'' +
        ", containerId='" + containerId + '\'' +
        ", dockerComposeRunner=" + dockerComposeRunner +
        ", buildDockerComposeRunner=" + buildDockerComposeRunner +
        ", composeLogFile=" + composeLogFile +
        ", buildComposeLogFile=" + buildComposeLogFile +
        '}'
  }
}
