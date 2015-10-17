package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class PushImageTask extends Exec {

  final static NAME = 'pushImage'

  PushImageTask() {

    description = 'Push the image tagged during "tagImage"'
    group = 'Docker'

    doFirst {
      if (project.version == Project.DEFAULT_VERSION) {
        throw new IllegalStateException('No project version defined. Cannot tag image. Please set "project.version".')
      }
      if (!project.extensions.getByType(DockerExtension).imageName) {
        throw new IllegalStateException('No docker image name defined. Cannot tag image. Please set "docker { imageName }".')
      }
    }
    commandLine 'docker', 'push', "${-> project.docker.fullImageName}"
  }

}
