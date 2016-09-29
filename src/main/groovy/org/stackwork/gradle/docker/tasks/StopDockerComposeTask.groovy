package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class StopDockerComposeTask extends AbstractTask {

  final static NAME = 'stopDockerCompose'
  @Internal final StackworkObject stackwork = project.stackwork

  StopDockerComposeTask() {

    group = 'Stackwork'
    description = 'Stops the docker compose services'

    onlyIf {
      project.extensions.getByType(StackworkExtension).stopContainers
    }

    doLast {
      stackwork.dockerComposeRunner.stop()
    }
  }

}
