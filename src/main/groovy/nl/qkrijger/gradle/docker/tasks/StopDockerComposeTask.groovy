package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.tasks.Exec

class StopDockerComposeTask extends Exec {

  StopDockerComposeTask() {
    description = 'Stops the docker compose services'
    group = 'Docker'

    onlyIf {
      DockerExtension extension = project.extensions.getByType(DockerExtension)
      extension.stopContainers
    }

    commandLine 'docker-compose', '-f', project.docker.composeFile, 'stop'
  }

}
