package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Copy

class GenerateDockerComposeFileTask extends Copy {

  final static NAME = 'generateDockerComposeFile'

  GenerateDockerComposeFileTask() {

    // disable task caching
    outputs.upToDateWhen {false}

    project.stackwork.composeFile = "${project.stackwork.buildDir}/docker-compose.yml"

    group = 'Stackwork'
    description = 'Generates the Docker Compose file'

    from project.projectDir
    into project.stackwork.buildDir
    include 'docker-compose.yml.template'
    rename { file -> 'docker-compose.yml' }
    expand(project.properties)
  }

}
