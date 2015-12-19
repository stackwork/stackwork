package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.stackwork.gradle.docker.StackworkExtension

class TagImageTask extends Exec {

  final static NAME = 'tagImage'

  TagImageTask() {

    description = 'Tag the built during "buildImage" with the stackwork { imageName } and project.version.'
    group = 'Stackwork'

    doFirst {
      if (project.version == Project.DEFAULT_VERSION) {
        throw new IllegalStateException('No project version defined. Cannot tag image. Please set "project.version".')
      }
      if (!getImageName()) {
        throw new IllegalStateException('No docker image name defined. Cannot tag image. Please set "stackwork { imageName }".')
      }
    }
    commandLine 'docker', 'tag', '-f', "${-> project.stackwork.imageId}", "${-> getImageName()}:${-> project.version}"
    doLast {
      project.stackwork.fullImageName = "${getImageName()}:${project.version}"
    }
  }

  String getImageName() {
    project.extensions.getByType(StackworkExtension).imageName
  }

}
