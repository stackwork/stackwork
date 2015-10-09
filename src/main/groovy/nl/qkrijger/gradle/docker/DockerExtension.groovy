package nl.qkrijger.gradle.docker

import static nl.qkrijger.gradle.docker.DockerModuleType.DEFAULT

class DockerExtension {

  /**
   * Indicates whether containers should be stopped after the tests are executed.
   */
  Boolean stopContainers = true
  /**
   * Indoicates the type of {@link DockerModuleType} of a Gradle sub project
   */
  DockerModuleType dockerModuleType = DEFAULT

}
