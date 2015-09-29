package nl.qkrijger.gradle.docker
import nl.qkrijger.gradle.docker.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class DockerPlugin implements Plugin<Project> {

  private static final String BUILD_IMAGE_TASK_NAME = 'buildImage'
  private static final String GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME = 'generateDockerComposeFile'
  private static final String RUN_DOCKER_COMPOSE_TASK_NAME = 'runDockerCompose'
  private static final String STOP_DOCKER_COMPOSE_TASK_NAME = 'stopDockerCompose'
  private static final String CLEAN_DOCKER_COMPOSE_TASK_NAME = 'cleanDockerCompose'
  private static final String TAG_IMAGE_TASK_NAME = 'tagImage'
  private static final String PUSH_IMAGE_TASK_NAME = 'pushImage'

  private Project project

  @Override
  void apply(Project project) {

    this.project = project

    project.ext.docker = [:]
    project.docker.services = [:]
    evaluateEnvironment()

    exportShellScripts()

    project.task(BUILD_IMAGE_TASK_NAME, type: BuildImageTask) {
      description = 'Builds the Dockerfile in your project root folder.'
      group = 'Docker'
    }

    project.task(TAG_IMAGE_TASK_NAME, type: TagImageTask) {
      description = 'Tag the built during "buildImage" with the docker.imageName and project.version.'
      group = 'Docker'
    }.dependsOn BUILD_IMAGE_TASK_NAME

    project.task(PUSH_IMAGE_TASK_NAME, type: PushImageTask) {
      description = 'Push the image tagged during "tagImage"'
      group = 'Docker'
    }.dependsOn TAG_IMAGE_TASK_NAME

    project.task(GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME, type: GenerateDockerComposeFileTask)
            .dependsOn BUILD_IMAGE_TASK_NAME

    project.task(RUN_DOCKER_COMPOSE_TASK_NAME, type: RunDockerComposeTask)
            .dependsOn GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME

    project.task(STOP_DOCKER_COMPOSE_TASK_NAME, type: StopDockerComposeTask)
            .dependsOn RUN_DOCKER_COMPOSE_TASK_NAME

    project.task(CLEAN_DOCKER_COMPOSE_TASK_NAME, type: CleanDockerComposeTask)
            .dependsOn STOP_DOCKER_COMPOSE_TASK_NAME
  }

  private void evaluateEnvironment() {
    String dockerHost = System.properties['DOCKER_HOST']
    if (dockerHost) { // in case of remote connection to docker daemon
      project.logger.info("DOCKER_HOST system variable with value '$dockerHost' found. Will expose docker " +
              "container forwarded ports and the DOCKER_HOST to the test classes.")
      // TODO #18: parse the docker host
      project.docker.host = dockerHost
    } else { // in case of local docker daemon
      project.logger.info('No DOCKER_HOST system variable found. Will expose docker container ips and exposed ports' +
              ' to the test classes.')
      project.docker.host = false
    }
  }

  private void exportShellScripts() {
    Path targetCache = Files.createDirectories(Paths.get(project.file('.gradle/docker').absolutePath))

    def scripts = ['build.sh']

    scripts.each { script ->
      def source = getClass().getResourceAsStream("/docker/$script")
      def destination = targetCache.resolve(script)
      Files.copy source, destination, StandardCopyOption.REPLACE_EXISTING
    }
  }

}
