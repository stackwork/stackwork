package org.stackwork.gradle.docker.tasks

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class ExecutableImageRunner {

  static void runTestImage(Project composeProject, Project testImageProject) {
    runExecutableImage(composeProject, testImageProject)
  }

  static void runBuildImage(Project buildComposeProject, List<Project> builderImageProjects) {
    builderImageProjects.each { builderImageProject ->
      runExecutableImage(buildComposeProject, builderImageProject)
    }

    OutputStream buildComposeContainerIdOutput = new ByteArrayOutputStream()
    StackworkObject buildComposeStackwork = stackworkFor buildComposeProject
    buildComposeProject.exec {
      setCommandLine(['docker-compose', '-f', buildComposeStackwork.dockerComposeRunner.composeFilePath,
                      '-p', buildComposeStackwork.dockerComposeRunner.projectId, 'ps', '-q', buildComposeProject.name])
      setStandardOutput buildComposeContainerIdOutput
    }
    buildComposeContainerIdOutput.toString().trim()
  }

  private static List runExecutableImage(Project composeProject, Project executableImageProject) {
    StackworkObject composeProjectStackwork = stackworkFor(composeProject)
    StackworkObject executableImageProjectStackwork = stackworkFor(executableImageProject)

    OutputStream containerNameOutput = new ByteArrayOutputStream()
    composeProject.exec {
      setCommandLine(['docker-compose', '-f', composeProjectStackwork.dockerComposeRunner.composeFilePath,
                      '-p', composeProjectStackwork.dockerComposeRunner.projectId, 'run',
                      '-d', executableImageProject.name])
      setStandardOutput containerNameOutput
    }
    String containerName = containerNameOutput.toString().trim()
    executableImageProjectStackwork.containerId = containerName

    composeProject.exec {
      setCommandLine(['docker', 'logs', '-f', executableImageProjectStackwork.containerId])
    }

    OutputStream exitCodeOutput = new ByteArrayOutputStream()
    composeProject.exec {
      setCommandLine(['docker', 'inspect', '-f=\'{{.State.ExitCode}}\'', executableImageProjectStackwork.containerId])
      setStandardOutput exitCodeOutput
    }
    int exitCode = exitCodeOutput.toString().trim() as int

    if (executableImageProject.extensions.getByType(StackworkExtension).stopContainers) {
      composeProject.exec {
        setCommandLine(['docker', 'rm', executableImageProjectStackwork.containerId])
      }
    }

    if (exitCode != 0) {
      throw new GradleException("Executable (i.e. test- or builder-) image '${containerName}' " +
          "exited with code '${exitCode}'")
    }
  }

  private static StackworkObject stackworkFor(Project project) {
    project.stackwork as StackworkObject
  }

}
