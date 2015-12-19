package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Exec
import org.stackwork.gradle.docker.DockerExtension

class StopDockerComposeTask extends Exec {

  final static NAME = 'stopDockerCompose'

  StopDockerComposeTask() {
    description = 'Stops the docker compose services'
    group = 'Docker'

    onlyIf {
      project.extensions.getByType(DockerExtension).stopContainers
    }

    commandLine 'docker-compose', '-f', "${->project.docker.composeFile}", '-p', "${->project.docker.composeProject}",
            'stop'
  }

}
