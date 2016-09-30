package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkObject

class ParseComposeTemplateTask extends AbstractTask {

  final static NAME = 'parseComposeTemplate'
  @Internal final StackworkObject stackwork = project.stackwork

  ParseComposeTemplateTask() {

    outputs.upToDateWhen { false }

    group = 'Stackwork'
    description = 'Generates the Docker Compose file'

    doLast {
      stackwork.dockerComposeRunner.parseComposeTemplate()
    }
  }

}
