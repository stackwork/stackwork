package org.stackwork.gradle.docker.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.stackwork.gradle.docker.StackworkObject

class ApplyBuildDockerComposeTask extends DefaultTask {

  final static NAME = 'applyBuildDockerCompose'
  @Internal final StackworkObject stackwork = project.stackwork

  ApplyBuildDockerComposeTask() {
    group = 'Stackwork'
    description = 'Parses the build.docker-compose.yml.template file and applies it to built image, committing the ' +
            'result'
  }

  @TaskAction
  def apply() {
    // create compose runner
    // parse build compose template
    // parse build compose file for info on services etc.
    // start long running images
    // wait for build log marker
    // execute builder image
    // docker compose stop
    // docker commit & overwrite stackwork.imageId
    // docker compose down
  }

  File parseBuildDockerComposeFileTemplate() {
    String buildComposeFilename = 'build.docker-compose.yml'
    project.copy {
      from project.projectDir
      into stackwork.buildDir
      include 'build.docker-compose.yml.template'
      rename { file -> buildComposeFilename }
      expand(project.properties)
    }
    project.file "${stackwork.buildDir}/${buildComposeFilename}"
  }

}
