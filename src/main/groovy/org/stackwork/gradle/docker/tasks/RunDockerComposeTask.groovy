package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject
import org.yaml.snakeyaml.Yaml

class RunDockerComposeTask extends AbstractTask {

  final static NAME = 'runDockerCompose'
  @Internal final StackworkObject stackwork = project.stackwork

  @Internal String composeFile = stackwork.dockerComposeRunner.composeFilePath
  @Internal String composeProject = stackwork.dockerComposeRunner.projectId

  RunDockerComposeTask() {
    description = 'Runs the generated docker compose file.'
    group = 'Stackwork'

    doLast {
      stackwork.dockerComposeRunner.run()
    }
  }

}
