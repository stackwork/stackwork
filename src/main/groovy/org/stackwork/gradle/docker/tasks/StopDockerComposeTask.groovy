package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Exec
import org.stackwork.gradle.docker.StackworkExtension

class StopDockerComposeTask extends Exec {

  final static NAME = 'stopDockerCompose'

  StopDockerComposeTask() {
    description = 'Stops the docker compose services'
    group = 'Stackwork'

    onlyIf {
      project.extensions.getByType(StackworkExtension).stopContainers
    }

    commandLine 'docker-compose', '-f', "${->project.stackwork.composeFile}", '-p', "${->project.stackwork.composeProject}",
            'stop'
  }

}
