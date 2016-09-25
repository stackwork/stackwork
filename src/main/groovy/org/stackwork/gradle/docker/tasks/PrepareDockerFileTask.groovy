package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkObject

class PrepareDockerFileTask extends Copy {

  final static NAME = 'prepareDockerFile'
  @Internal final StackworkObject stackwork = project.stackwork

  PrepareDockerFileTask() {
    group = 'Stackwork'
    description = 'Prepares the Dockerfile, activates parsing a template if needed'
    onlyIf { usingDockerfileTemplate() }
    outputs.upToDateWhen { false }
    description = 'Parsing the Dockerfile.template'

    from project.projectDir
    into stackwork.buildDir
    include 'Dockerfile.template'
    rename { file -> 'Dockerfile' }
    expand project.properties

    stackwork.dockerFile = project.file('Dockerfile').absolutePath

    doLast {
      if (usingDockerfileTemplate()) {
        stackwork.dockerFile = project.file("${stackwork.buildDir}/Dockerfile").absolutePath
      }
    }
  }

  boolean usingDockerfileTemplate() {
    project.file('Dockerfile.template').exists()
  }

}
