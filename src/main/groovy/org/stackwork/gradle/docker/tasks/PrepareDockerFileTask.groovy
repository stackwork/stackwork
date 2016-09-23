package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Copy

class PrepareDockerFileTask extends Copy {

  final static NAME = 'prepareDockerFile'

  PrepareDockerFileTask() {
    group = 'Stackwork'
    description = 'Prepares the Dockerfile, activates parsing a template if needed'
    onlyIf { usingDockerfileTemplate() }
    outputs.upToDateWhen { false }
    description = 'Parsing the Dockerfile.template'

    from project.projectDir
    into project.stackwork.buildDir
    include 'Dockerfile.template'
    rename { file -> 'Dockerfile' }
    expand project.properties

    project.stackwork.dockerFile = project.file('Dockerfile').absolutePath

    doLast {
      if (usingDockerfileTemplate()) {
        project.stackwork.dockerFile = project.file("${project.stackwork.buildDir}/Dockerfile").absolutePath
      }
    }
  }

  boolean usingDockerfileTemplate() {
    project.file('Dockerfile.template').exists()
  }

}
