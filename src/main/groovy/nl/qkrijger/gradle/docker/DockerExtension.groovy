package nl.qkrijger.gradle.docker

import org.gradle.api.Project

import static nl.qkrijger.gradle.docker.DockerModuleType.DEFAULT

class DockerExtension {

  /**
   * Indicates whether containers should be stopped after the tests are executed.
   */
  Boolean stopContainers = true

  /**
   * Indicates the type of {@link DockerModuleType} of a Gradle sub project
   */
  DockerModuleType dockerModuleType = DEFAULT

  /**
   * The project that contains the docker compose setup for this modules tests. Defaults to the project itself.
   */
  Project composeProject


  private Project project

  DockerExtension(Project project) {
    this.project = project
    this.composeProject = this.project
  }

}
