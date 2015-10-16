package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.tasks.Exec

class CleanDockerComposeTask extends Exec {

  final static NAME = 'cleanDockerCompose'

  CleanDockerComposeTask() {
    description = 'Removes the stopped docker compose services.'
    group = 'Docker'

    onlyIf {
      project.extensions.getByType(DockerExtension).stopContainers
    }

    commandLine 'docker-compose', '-f', "${->project.docker.composeFile}", '-p', "${->project.docker.composeProject}",
                'rm', '-f'
  }

}
