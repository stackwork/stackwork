package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask

class RunTestImageTask extends AbstractTask {

  final static NAME = 'runTestImage'

  RunTestImageTask() {
    description = 'Runs, logs and removes the test image that is built in the project (module) ' +
            'via the docker compose configuration'
    group = 'Docker'

    doLast {
      Project composeProject = project.extensions.getByType(DockerExtension).composeProject

      OutputStream containerNameOutput = new ByteArrayOutputStream()
      project.exec {
        setCommandLine(['docker-compose', '-f', composeProject.docker.composeFile,
                        '-p', composeProject.docker.composeProject, 'run', '-d', project.name])
        setStandardOutput containerNameOutput
      }
      String containerName = containerNameOutput.toString().trim()
      project.docker.containerId = containerName

      project.exec {
        setCommandLine(['docker', 'logs', '-f', project.docker.containerId])
      }

      OutputStream exitCodeOutput = new ByteArrayOutputStream()
      project.exec {
        setCommandLine(['docker', 'inspect', '-f=\'{{.State.ExitCode}}\'', project.docker.containerId])
        setStandardOutput exitCodeOutput
      }
      int exitCode = exitCodeOutput.toString().trim() as int

      if (project.extensions.getByType(DockerExtension).stopContainers) {
        project.exec {
          setCommandLine(['docker', 'rm', project.docker.containerId])
        }
      }

      if (exitCode != 0) {
        throw new IllegalArgumentException("Test image '${containerName}' exited with code '${exitCode}'")
      }
    }
  }
}