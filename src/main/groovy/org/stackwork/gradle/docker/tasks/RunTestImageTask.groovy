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
      StackworkObject composeProjectStackwork = composeProject.stackwork

      OutputStream containerNameOutput = new ByteArrayOutputStream()
      project.exec {
        setCommandLine(['docker-compose', '-f', composeProjectStackwork.composeFile,
                        '-p', composeProjectStackwork.composeProject, 'run', '-d', project.name])
        setStandardOutput containerNameOutput
      }
      String containerName = containerNameOutput.toString().trim()
      stackwork.containerId = containerName

      project.exec {
        setCommandLine(['docker', 'logs', '-f', stackwork.containerId])
      }

      OutputStream exitCodeOutput = new ByteArrayOutputStream()
      project.exec {
        setCommandLine(['docker', 'inspect', '-f=\'{{.State.ExitCode}}\'', stackwork.containerId])
        setStandardOutput exitCodeOutput
      }
      int exitCode = exitCodeOutput.toString().trim() as int

      if (project.extensions.getByType(StackworkExtension).stopContainers) {
        project.exec {
          setCommandLine(['docker', 'rm', stackwork.containerId])
        }
      }

      if (exitCode != 0) {
        throw new IllegalArgumentException("Test image '${containerName}' exited with code '${exitCode}'")
      }
    }
  }
}
