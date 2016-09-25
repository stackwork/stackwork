package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkObject

class GenerateDockerComposeFileTask extends Copy {

  final static NAME = 'generateDockerComposeFile'
  @Internal final StackworkObject stackwork = project.stackwork

  GenerateDockerComposeFileTask() {

    outputs.upToDateWhen {false}

    stackwork.composeFile = "${stackwork.buildDir}/docker-compose.yml"

    group = 'Stackwork'
    description = 'Generates the Docker Compose file'

    from project.projectDir
    into stackwork.buildDir
    include 'docker-compose.yml.template'
    rename { file -> 'docker-compose.yml' }
    expand(project.properties)
  }

}
