package nl.qkrijger.gradle.docker

import org.gradle.api.Project

class DockerExtension {

  Project project

  /**
   * Indicates whether containers should be stopped after the tests are executed.
   */
  Boolean stopContainers = true

  DockerExtension(Project project) {
    this.project = project
  }
}
