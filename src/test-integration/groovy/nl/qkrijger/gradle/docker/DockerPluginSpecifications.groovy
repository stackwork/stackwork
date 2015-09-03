package nl.qkrijger.gradle.docker

import spock.lang.Specification

class DockerPluginSpecifications extends Specification {

  def setup() {
    println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> STARTING CHILD GRADLE TEST BUILD <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
  }

  def cleanup() {
    println "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  ENDING CHILD GRADLE TEST BUILD  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
  }

  def "The build image task should build the DockerFile in the project root"() {
    when:
    def proc = "gradle buildImage -i --stacktrace --project-dir src/test-integration/resources/docker-project".execute()
    proc.waitForProcessOutput(System.out as OutputStream, System.err as OutputStream)

    then:
    proc.exitValue() == 0
  }

}
