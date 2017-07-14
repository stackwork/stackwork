package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkObject
import org.stackwork.gradle.docker.process.OutputStreams

class BuildImageTask extends Exec {

  final static NAME = 'buildImage'
  @Internal final StackworkObject stackwork = project.stackwork

  BuildImageTask() {

    description = 'Builds the Dockerfile in your project root folder.'
    group = 'Stackwork'

    executable 'docker'
    args 'build', '--file', "${-> stackwork.dockerFile}", project.projectDir

    def buffer = new ByteArrayOutputStream()
    standardOutput = new OutputStreams([standardOutput, buffer])

    doLast {
      def output = buffer.toString('UTF-8')
      def imageIdMatcher = (output =~ /(?s).*Successfully built (?<imageId>[0-9a-f]+).*/)

      if (!imageIdMatcher.matches()) {
        throw new IllegalStateException('Failed to parse an image id from the Docker build response')
      }

      def imageId = imageIdMatcher.group('imageId')

      logger.info 'Docker image id: {}', imageId
      stackwork.imageId = imageId
    }
  }
}
