package nl.qkrijger.gradle.docker

import spock.lang.Specification

class DockerPluginSpecifications extends Specification {

  def setup() {
    println '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> STARTING CHILD GRADLE TEST BUILD <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<'
  }

  def cleanup() {
    println '<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  ENDING CHILD GRADLE TEST BUILD  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<'
  }

  def "The build image task builds the DockerFile in the project root"() {
    when:
    GradleOutput output = runGradleTask('buildImage', 'build')

    then:
    output.process.exitValue() == 0
  }

  def "The tag image task tags a built image with 'docker.imageName:project.version' number, which is exposed as 'docker.fullImageName'"() {
    when:
    GradleOutput output = runGradleTask('tagImage', 'tag')

    then:
    output.process.exitValue() == 0
  }

  def "Tagging an image does not work if the project.version is not set"() {
    when:
    GradleOutput output = runGradleTask('tagImage', 'tag-no-version')

    then:
    output.process.exitValue() != 0
    output.standardErr.contains 'No project version defined. Cannot tag image. Please set "project.version".'
  }

  def "Tagging an image does not work if the docker.imageName is not set"() {
    when:
    GradleOutput output = runGradleTask('tagImage', 'tag-no-image-name')

    then:
    output.process.exitValue() != 0
    output.standardErr.contains 'No docker image name defined. Cannot tag image. Please set "docker.imageName".'
  }

  def "The push image task tries to push an image."() {
    when:
    GradleOutput output = runGradleTask('pushImage', 'push-public-hub-no-namespace')

    then:
    output.process.exitValue() != 0
    output.standardErr.contains 'You cannot push a "root" repository. Please rename your repository to <user>/<repo>'
  }

  private static GradleOutput runGradleTask(String task, String project) {
    def proc = "gradle $task -i --stacktrace --project-dir src/test-integration/gradle-projects/$project".execute()
    OutputStream standardOut = new ByteArrayOutputStream()
    OutputStream standardErr = new ByteArrayOutputStream()
    proc.waitForProcessOutput(standardOut, standardErr)
    println "<<<<<<<<<<<<<  Standard Out  <<<<<<<<<<<<<<<\n$standardOut"
    println "<<<<<<<<<<<<<  Standard Err  <<<<<<<<<<<<<<<\n$standardErr"
    new GradleOutput(proc, standardOut.toString("UTF-8"), standardErr.toString("UTF-8"))
  }

  private static class GradleOutput {
    private final Process process
    private final String standardOut
    private final String standardErr

    GradleOutput(Process process, String standardOut, String standardErr) {
      this.process = process
      this.standardOut = standardOut
      this.standardErr = standardErr
    }

    Process getProcess() {
      return process
    }

    String getStandardOut() {
      return standardOut
    }

    String getStandardErr() {
      return standardErr
    }
  }

}
