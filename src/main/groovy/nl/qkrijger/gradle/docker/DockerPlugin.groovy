package nl.qkrijger.gradle.docker
import nl.qkrijger.gradle.docker.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class DockerPlugin implements Plugin<Project> {

  public static final String BUILD_IMAGE_TASK_NAME = 'buildImage'
  public static final String GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME = 'generateDockerComposeFile'
  public static final String RUN_DOCKER_COMPOSE_TASK_NAME = 'runDockerCompose'
  public static final String STOP_DOCKER_COMPOSE_TASK_NAME = 'stopDockerCompose'
  public static final String CLEAN_DOCKER_COMPOSE_TASK_NAME = 'cleanDockerCompose'
  public static final String TAG_IMAGE_TASK_NAME = 'tagImage'
  public static final String PUSH_IMAGE_TASK_NAME = 'pushImage'

  private Project project

  @Override
  void apply(Project project) {

    this.project = project

    project.ext.docker = [:]
    project.docker.services = [:]

    evaluateEnvironment()

    project.extensions.create('docker', DockerExtension, project)
    registerTasks()

    project.plugins.withType(JavaPlugin) {
      project.plugins.apply(DockerJavaPlugin)
    }
  }

  private void evaluateEnvironment() {
    String dockerHost = System.getenv('DOCKER_HOST')
    if (dockerHost) {
      // in case of remote connection to docker daemon
      project.logger.info("DOCKER_HOST system variable with value '$dockerHost' found. Will expose docker " +
              "container forwarded ports and the DOCKER_HOST to the test classes.")
      project.docker.host = new URI(dockerHost).host
    } else {
      // in case of local docker daemon
      project.logger.info('No DOCKER_HOST system variable found. Will expose docker container ips and exposed ports' +
              ' to the test classes.')
    }
  }

  private void registerTasks() {
    project.task(BUILD_IMAGE_TASK_NAME, type: BuildImageTask)

    project.task(TAG_IMAGE_TASK_NAME, type: TagImageTask)
            .dependsOn BUILD_IMAGE_TASK_NAME

    project.task(PUSH_IMAGE_TASK_NAME, type: PushImageTask)
            .dependsOn TAG_IMAGE_TASK_NAME

    project.task(GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME, type: GenerateDockerComposeFileTask)
            .dependsOn BUILD_IMAGE_TASK_NAME

    project.task(RUN_DOCKER_COMPOSE_TASK_NAME, type: RunDockerComposeTask)
            .dependsOn GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME

    project.task(STOP_DOCKER_COMPOSE_TASK_NAME, type: StopDockerComposeTask)
            .dependsOn RUN_DOCKER_COMPOSE_TASK_NAME

    project.task(CLEAN_DOCKER_COMPOSE_TASK_NAME, type: CleanDockerComposeTask)
            .dependsOn STOP_DOCKER_COMPOSE_TASK_NAME
  }

}
