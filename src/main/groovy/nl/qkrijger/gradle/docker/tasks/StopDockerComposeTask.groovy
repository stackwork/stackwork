package nl.qkrijger.gradle.docker.tasks

import org.gradle.api.tasks.Exec

class StopDockerComposeTask extends Exec {

  StopDockerComposeTask() {
    description = 'Stops the docker compose services'
    group = 'Docker'

    commandLine 'docker-compose', '-f', project.docker.composeFile, 'stop'
  }

}
