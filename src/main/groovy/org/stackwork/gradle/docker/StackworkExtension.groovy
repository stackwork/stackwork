package org.stackwork.gradle.docker

import org.gradle.api.Project

import static ModuleType.DEFAULT

class StackworkExtension {

  /**
   * Indicates whether containers should be stopped after the tests are executed.
   */
  Boolean stopContainers = true

  /**
   * Indicates the type of {@link ModuleType} of a Gradle sub project
   */
  ModuleType moduleType = DEFAULT

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

  /**
   * Marker that Docker Compose logs are scanned for, when found this indicates the stack is running.
   */
  String stackIsRunningWhenLogContains

  private Project project

  StackworkExtension(Project project) {
    this.project = project
    this.composeProject = this.project
  }

  @Override
  public String toString() {
    return "StackworkExtension{" +
            "stopContainers=" + stopContainers +
            ", moduleType=" + moduleType +
            ", composeProject=" + composeProject +
            ", baseImageProject=" + baseImageProject +
            ", imageName='" + imageName + '\'' +
            ", stackIsRunningWhenLogContains='" + stackIsRunningWhenLogContains + '\'' +
            ", project=" + project +
            '}';
  }
}
