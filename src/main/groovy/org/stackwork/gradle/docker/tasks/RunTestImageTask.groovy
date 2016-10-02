package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class RunTestImageTask extends AbstractTask {

  final static NAME = 'runTestImage'
  @Internal final StackworkObject stackwork = project.stackwork

  RunTestImageTask() {
    description = 'Runs, logs and removes the test image that is built in the project (module) ' +
            'via the docker compose configuration'
    group = 'Stackwork'

    doLast {
      Project composeProject = project.extensions.getByType(StackworkExtension).composeProject
      ExecutableImageRunner.runTestImage(composeProject, project)
    }
  }
}
