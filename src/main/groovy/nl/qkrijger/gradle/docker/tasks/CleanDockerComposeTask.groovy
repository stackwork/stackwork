package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.tasks.Exec

class CleanDockerComposeTask extends Exec {

  CleanDockerComposeTask() {
    description = 'Removes the stopped docker compose services.'
    group = 'Docker'

    onlyIf {
      DockerExtension extension = project.rootProject.extensions.getByType(DockerExtension)
      extension.stopContainers
    }

    commandLine 'docker-compose', '-f', project.docker.composeFile, '-p', project.docker.composeProject, 'rm', '-f'
  }

}
