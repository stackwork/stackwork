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
            ", imageName='" + imageName + '\'' +
            ", project=" + project +
            '}';
  }
}
