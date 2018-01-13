package org.stackwork.gradle.docker

import groovy.transform.Memoized
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
  String baseComposeStack

  /**
   * Container ID in case of a module that has an executable image such as a {@link ModuleType#TEST_IMAGE}
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

  @Memoized
  String getHost() {
    String dockerHost = System.getenv('DOCKER_HOST')
    if (dockerHost) {
      // in case of remote connection to docker daemon
      project.logger.info("DOCKER_HOST system variable with value '$dockerHost' found. Will expose docker " +
              "container forwarded ports and the DOCKER_HOST to the test classes.")
      return new URI(dockerHost).host
    } else {
      // in case of local docker daemon
      project.logger.info 'No DOCKER_HOST system variable found, so assumed is you are using a local Docker daemon'

      if (detectDockerForMacNew() || detectDockerForMacOld()) {
        project.logger.info 'Concluded you are using Docker for Mac. Will expose 127.0.0.1 as ip and forwarded ports ' +
                'to the test classes.'
        return '127.0.0.1'
      } else {
        project.logger.info 'Will expose docker container ips and exposed ports to the test classes.'
        return null
      }
    }
  }

  @Memoized
  private boolean detectDockerForMacNew() {
    new ByteArrayOutputStream().withCloseable { out ->
      project.exec {
        // check if Docker for Mac is the locally installed client, because that creates /var/run/docker.sock
        // instead of setting the DOCKER_HOST environment variable
        executable '/bin/bash'
        args '-c', 'docker info'
        standardOutput = out
      }
      out.toString().contains 'Docker for Mac'
    }
  }

  @Memoized
  private boolean detectDockerForMacOld() {
    new ByteArrayOutputStream().withCloseable { out ->
      project.exec {
        // check if Docker for Mac is the locally installed client, because that creates /var/run/docker.sock
        // instead of setting the DOCKER_HOST environment variable
        executable '/bin/bash'
        args '-c', 'docker version --format "{{.Server.KernelVersion}}"'
        standardOutput = out
      }
      out.toString().contains 'moby'
    }
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
