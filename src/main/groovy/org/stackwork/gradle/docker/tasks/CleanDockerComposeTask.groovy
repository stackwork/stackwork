package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Exec
import org.stackwork.gradle.docker.StackworkExtension

class CleanDockerComposeTask extends Exec {

  final static NAME = 'cleanDockerCompose'

  CleanDockerComposeTask() {
    description = 'Removes the stopped docker compose services.'
    group = 'Stackwork'

    onlyIf {
      project.extensions.getByType(StackworkExtension).stopContainers
    }

    commandLine 'docker-compose', '-f', "${->project.stackwork.composeFile}", '-p',
        "${->project.stackwork.composeProject}", 'down'
  }

}
