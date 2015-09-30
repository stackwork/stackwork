package nl.qkrijger.gradle.docker.tasks

import org.gradle.api.tasks.Copy

class GenerateDockerComposeFileTask extends Copy {

  GenerateDockerComposeFileTask() {

    // disable task caching
    outputs.upToDateWhen {false}

    String composeOutputDir = "${project.buildDir}/docker-plugin"
    project.docker.composeFile = "$composeOutputDir/docker-compose.yml"

    group = 'Docker'
    description = 'Generates the Docker Compose file'

    from 'src/test/'
    into composeOutputDir
    include 'docker-compose.yml.template'
    rename { file -> 'docker-compose.yml' }
    expand(project.properties)
  }

}
