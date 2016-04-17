package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask
import org.stackwork.gradle.docker.StackworkExtension

class StopDockerComposeTask extends AbstractTask {

  final static NAME = 'stopDockerCompose'

  StopDockerComposeTask() {

    group = 'Stackwork'
    description = 'Stops the docker compose services'

    RunDockerComposeTask runDockerComposeTask = project.tasks.runDockerCompose as RunDockerComposeTask

    onlyIf {
      project.extensions.getByType(StackworkExtension).stopContainers
    }

    doLast {
      OutputStream out = new ByteArrayOutputStream()
      project.exec {
        // count the number of containers already exited with a non-zero exit code. Running container are seen by
        // docker inspect to have ExitCode = 0
        String commandToSeeIfAnyContainersOfTheStackFailed =
            "docker-compose -f ${runDockerComposeTask.composeFile} -p ${runDockerComposeTask.composeProject} ps -q | xargs docker inspect -f '{{ .State.ExitCode }}' | grep -v 0 | wc -l | tr -d ' '"
        commandLine('bash', '-c', commandToSeeIfAnyContainersOfTheStackFailed)
        standardOutput = out
      }
      int nrOfNonZeroExitCodes = out.toString() as Integer
      logger.info "Stack will be exited. '$nrOfNonZeroExitCodes' container(s) already have a non-zero exit code."
      if (nrOfNonZeroExitCodes != 0) {
        def msg = "The docker compose process in project '$project.name' will be shut down. However, " +
            "'$nrOfNonZeroExitCodes' container(s) exited with a non-zero exit code, so we're failing the build."
        logger.error msg
        throw new RuntimeException(msg)
      }
    }

    doLast {
      project.exec {
        commandLine 'docker-compose', '-f', "${->project.stackwork.composeFile}",
            '-p', "${->project.stackwork.composeProject}", 'stop'
      }
    }
  }

}
