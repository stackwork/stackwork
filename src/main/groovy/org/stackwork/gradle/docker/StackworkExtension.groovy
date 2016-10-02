package org.stackwork.gradle.docker

import org.gradle.api.Project

import static ModuleType.DEFAULT

class StackworkExtension {

  /**
   * Indicates whether containers should be stopped after the tests are executed. Defaults to true.
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
   * The projects on which this projects image build depends. Defaults to an empty list, meaning no dependency.
   */
  List<Project> imageBuildDependencies = []

  /**
   * Name of the image. E.g. repo:1234/namespace/name. The name should not contain a version, because this name will be
   * concatenated with the project.version to create the full name.
   */
  String imageName

  /**
   * Marker that Docker Compose logs are scanned for. When found this indicates the stack is running.
   */
  String stackIsRunningWhenLogContains

  /**
   * Denotes that after processing the Dockerfile[.template], the build.docker-compose.yml[.template] file should be
   * used the modify the image. Often used in conjunction with dependencies on other projects using 'imageBuildDependencies'.
   * Defaults to false.
   */
  Boolean applyBuildStepWithStack = false

  /**
   * Marker that Docker Compose logs are scanned for during the build process in case of using a 'builderImageProject'.
   * when found this indicates the stack is running.
   */
  String buildStackIsRunningWhenLogContains

  private final Project project

  StackworkExtension(Project project) {
    this.project = project
    this.composeProject = this.project
  }

  void setImageBuildDependencies(List<Project> imageBuildDependencies) {
    this.imageBuildDependencies = imageBuildDependencies
  }

  void setImageBuildDependencies(Project imageBuildDependency) {
    this.imageBuildDependencies = [imageBuildDependency]
  }

  @Override
  public String toString() {
    return "StackworkExtension{" +
            "stopContainers=" + stopContainers +
            ", moduleType=" + moduleType +
            ", composeProject=" + composeProject +
            ", imageBuildDependencies=" + imageBuildDependencies +
            ", imageName='" + imageName + '\'' +
            ", stackIsRunningWhenLogContains='" + stackIsRunningWhenLogContains + '\'' +
            ", applyBuildStepWithStack='" + applyBuildStepWithStack + '\'' +
            ", buildStackIsRunningWhenLogContains='" + buildStackIsRunningWhenLogContains + '\'' +
            ", project=" + project +
            '}';
  }
}
