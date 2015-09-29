package nl.qkrijger.gradle.docker.tasks

import org.gradle.api.tasks.Exec

class CleanDockerComposeTask extends Exec {

  CleanDockerComposeTask() {
    description = 'Removes the stopped docker compose services.'
    group = 'Docker'
    commandLine 'docker-compose', '-f', project.docker.composeFile, 'rm', '-f'
  }

}
