package org.stackwork.gradle.docker

import org.gradle.api.Project

import static DockerModuleType.DEFAULT

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

  /**
   * The project on which this projects image build depends. Defaults to null, meaning no dependency
   */
  Project baseImageProject

  /**
   * Name of the image. E.g. repo:1234/namespace/name. The name should not contain a version, because this name will be
   * concatenated with the project.version to create the full name.
   */
  String imageName


  private Project project

  DockerExtension(Project project) {
    this.project = project
    this.composeProject = this.project
  }


  @Override
  public String toString() {
    return "DockerExtension{" +
            "stopContainers=" + stopContainers +
            ", dockerModuleType=" + dockerModuleType +
            ", composeProject=" + composeProject +
            ", baseImageProject=" + baseImageProject +
            ", imageName='" + imageName + '\'' +
            ", project=" + project +
            '}';
  }
}
