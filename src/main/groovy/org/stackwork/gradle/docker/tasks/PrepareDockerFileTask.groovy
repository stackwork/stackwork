package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Copy
import org.stackwork.gradle.docker.StackworkExtension

class PrepareDockerFileTask extends AbstractTask {

  final static NAME = 'prepareDockerFile'

  PrepareDockerFileTask() {

    group = 'Stackwork'
    description = 'Prepares the Dockerfile, activates parsing a template if needed'

    dependsOn project.task("parseDockerFileTemplate", type: Copy) {
      // disable task caching
      onlyIf { usingDockerfileTemplate() }
      outputs.upToDateWhen { false }
      description = 'Parsing the Dockerfile.template'

      from project.projectDir
      into project.stackwork.buildDir
      include 'Dockerfile.template'
      rename { file -> 'Dockerfile' }
      expand project.properties
    }

    doLast {
      project.stackwork.dockerFile = usingDockerfileTemplate() ?
              project.file("${project.stackwork.buildDir}/Dockerfile").absolutePath :
              project.file('Dockerfile').absolutePath
    }
  }

  boolean usingDockerfileTemplate() {
    project.file('Dockerfile.template').exists()
  }

  Project getBaseImageProject() {
    project.extensions.getByType(StackworkExtension).baseImageProject
  }

}
