package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.stackwork.gradle.docker.StackworkExtension

class PushImageTask extends Exec {

  final static NAME = 'pushImage'

  PushImageTask() {

    description = 'Push the image tagged during "tagImage"'
    group = 'Stackwork'

    doFirst {
      if (project.version == Project.DEFAULT_VERSION) {
        throw new IllegalStateException('No project version defined. Cannot tag image. Please set "project.version".')
      }
      if (!project.extensions.getByType(StackworkExtension).imageName) {
        throw new IllegalStateException('No docker image name defined. Cannot tag image. Please set "stackwork { imageName }".')
      }
    }
    commandLine 'docker', 'push', "${-> project.stackwork.fullImageName}"
  }

}
