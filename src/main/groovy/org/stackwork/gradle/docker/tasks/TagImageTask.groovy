package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.execution.TaskValidator
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class TagImageTask extends Exec {

  final static NAME = 'tagImage'
  @Internal final StackworkObject stackwork = project.stackwork

  TagImageTask() {

    description = 'Tag the built during "buildImage" with the stackwork { imageName } and project.version.'
    group = 'Stackwork'
    addValidator(new TaskValidator() {
      @Override
      void validate(TaskInternal task, Collection<String> messages) {
        if (project.version == Project.DEFAULT_VERSION) {
          messages.add 'No project version defined. Cannot tag image. Please set "project.version".'
        }
        if (!getImageName()) {
          messages.add 'No docker image name defined. Cannot tag image. Please set "stackwork { imageName }".'
        }
      }
    })

    commandLine 'docker', 'tag', "${-> stackwork.imageId}", "${-> getImageName()}:${-> project.version}"
    doLast {
      stackwork.fullImageName = "${getImageName()}:${project.version}"
    }
  }

  @Internal
  String getImageName() {
    project.extensions.getByType(StackworkExtension).imageName
  }

}
