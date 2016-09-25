package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class CleanDockerComposeTask extends Exec {

  final static NAME = 'cleanDockerCompose'
  @Internal final StackworkObject stackwork = project.stackwork

  CleanDockerComposeTask() {
    description = 'Removes the stopped docker compose services.'
    group = 'Stackwork'

    onlyIf {
      project.extensions.getByType(StackworkExtension).stopContainers
    }

    commandLine 'docker-compose', '-f', "${-> stackwork.dockerComposeRunner.composeFilePath}",
        '-p', "${-> stackwork.dockerComposeRunner.projectId}", 'down'
  }

}
