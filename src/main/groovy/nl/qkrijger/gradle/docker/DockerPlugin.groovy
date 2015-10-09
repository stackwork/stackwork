package nl.qkrijger.gradle.docker

import nl.qkrijger.gradle.docker.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin

import static nl.qkrijger.gradle.docker.DockerModuleType.TEST
import static nl.qkrijger.gradle.docker.DockerModuleType.TEST_IMAGE

class DockerPlugin implements Plugin<Project> {

  public static final String COLLECT_IMAGE_DEPENDENCIES_TASK_NAME = 'collectImageDependencies'
  public static final String BUILD_IMAGE_TASK_NAME = 'buildImage'
  public static final String GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME = 'generateDockerComposeFile'
  public static final String RUN_DOCKER_COMPOSE_TASK_NAME = 'runDockerCompose'
  public static final String STOP_DOCKER_COMPOSE_TASK_NAME = 'stopDockerCompose'
  public static final String CLEAN_DOCKER_COMPOSE_TASK_NAME = 'cleanDockerCompose'
  public static final String TAG_IMAGE_TASK_NAME = 'tagImage'
  public static final String PUSH_IMAGE_TASK_NAME = 'pushImage'
  public static final String RUN_TEST_IMAGE_TASK_NAME = 'runTestImage'

  @Override
  void apply(Project project) {

    this.project = project
    this.isRootProject = project.rootProject == project

    project.ext.docker = [:]
    project.docker.services = [:]
    project.docker.modules = [:]

    evaluateEnvironment()

    project.extensions.create('docker', DockerExtension, project)

    project.configurations {
      docker {
        description = 'Docker artifacts to be made available for an image build'
      }
    }

    registerTasks()

    project.plugins.withType(JavaPlugin) {
      project.plugins.apply(DockerJavaPlugin)
    }

    project.afterEvaluate {
      if (project.parent && project.hasProperty('dockerModuleType')) {
        project.parent.docker.modules["${project.name}"] = project.dockerModuleType
      }
      createTaskOrdering()
      if (!project.plugins.hasPlugin(JavaPlugin)) {
        project.task('check') {
          description: 'Check task added by Docker Plugin (apparently, the JavaPlugin was not loaded in this project)'
        }
      }
      integrateWithCheckTask()
    }
  }
  private Project project

  private boolean isRootProject

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
    project.task(COLLECT_IMAGE_DEPENDENCIES_TASK_NAME, type: CollectImageDependenciesTask)
            .onlyIf { isRootProject || isModuleType(TEST_IMAGE) }

    project.task(BUILD_IMAGE_TASK_NAME, type: BuildImageTask)
            .dependsOn(COLLECT_IMAGE_DEPENDENCIES_TASK_NAME)
            .onlyIf { isRootProject || isModuleType(TEST_IMAGE) }

    project.task(TAG_IMAGE_TASK_NAME, type: TagImageTask)
            .dependsOn(BUILD_IMAGE_TASK_NAME)
            .onlyIf { isRootProject }

    project.task(PUSH_IMAGE_TASK_NAME, type: PushImageTask)
            .dependsOn(TAG_IMAGE_TASK_NAME)
            .onlyIf { isRootProject }

    project.task(GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME, type: GenerateDockerComposeFileTask)
            .dependsOn(BUILD_IMAGE_TASK_NAME)
            .onlyIf { isModuleType(TEST_IMAGE) || isModuleType(TEST) }

    project.task(RUN_DOCKER_COMPOSE_TASK_NAME, type: RunDockerComposeTask)
            .dependsOn(GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME)
            .onlyIf { isModuleType(TEST_IMAGE) || isModuleType(TEST) }

    project.task(STOP_DOCKER_COMPOSE_TASK_NAME, type: StopDockerComposeTask)
            .dependsOn(RUN_DOCKER_COMPOSE_TASK_NAME)
            .onlyIf { isModuleType(TEST_IMAGE) || isModuleType(TEST) }

    project.task(CLEAN_DOCKER_COMPOSE_TASK_NAME, type: CleanDockerComposeTask)
            .dependsOn(STOP_DOCKER_COMPOSE_TASK_NAME)
            .onlyIf { isModuleType(TEST_IMAGE) || isModuleType(TEST) }

    project.task(RUN_TEST_IMAGE_TASK_NAME, type: RunTestImageTask)
            .dependsOn(BUILD_IMAGE_TASK_NAME)
            .dependsOn(RUN_DOCKER_COMPOSE_TASK_NAME)
            .onlyIf { isModuleType(TEST_IMAGE) }

    project.tasks.getByName(STOP_DOCKER_COMPOSE_TASK_NAME)
            .mustRunAfter(RUN_TEST_IMAGE_TASK_NAME)

    project.tasks.getByName(RUN_DOCKER_COMPOSE_TASK_NAME)
            .finalizedBy(CLEAN_DOCKER_COMPOSE_TASK_NAME)

  }

  private void createTaskOrdering() {
    Project parentOrSelf = project.parent ?: project
    project.tasks.getByName(GENERATE_DOCKER_COMPOSE_FILE_TASK_NAME)
            .dependsOn parentOrSelf.getTasksByName(BUILD_IMAGE_TASK_NAME, true)
  }

  private Task integrateWithCheckTask() {
    project.tasks.check.dependsOn RUN_TEST_IMAGE_TASK_NAME, CLEAN_DOCKER_COMPOSE_TASK_NAME
  }

  private boolean isModuleType(DockerModuleType moduleType) {
    project.hasProperty('dockerModuleType') && project.dockerModuleType == moduleType.value
  }

}
