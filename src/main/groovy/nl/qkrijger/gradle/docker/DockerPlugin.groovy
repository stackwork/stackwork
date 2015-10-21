package nl.qkrijger.gradle.docker
import nl.qkrijger.gradle.docker.tasks.*
import org.gradle.api.PathValidation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin

import static nl.qkrijger.gradle.docker.DockerModuleType.*

class DockerPlugin implements Plugin<Project> {

  private static final boolean RECURSIVE = true
  private Project project

  private Task dockerTest
  private Task dockerTestStart
  private Task dockerCheck

  private Task collectDependencies
  private Task prepareDockerFile
  private Task buildImage
  private Task cleanCompose
  private Task runCompose
  private Task stopCompose
  private Task pushImage
  private Task tagImage
  private Task runTestImage
  private Task generateComposeFile

  @Override
  void apply(Project project) {
    this.project = project

    project.ext.docker = [:]
    project.docker.services = [:]
    project.docker.modules = [:]
    project.docker.buildDir = "${project.buildDir}/docker-plugin"

    evaluateEnvironment()

    project.extensions.create('docker', DockerExtension, project)

    project.configurations {
      docker {
        description = 'Docker artifacts to be made available for an image build'
      }
    }

    loadBaseComposeStackIfItExists()

    registerInternalTasks()
    orderInternalTasks()
    filterInternalTasksToRun()

    registerHookTasks()
    setupHooksIntoInternalTasks()

    project.plugins.withType(JavaPlugin) {
      project.plugins.apply(DockerJavaPlugin)
    }

    project.gradle.projectsEvaluated {
      getComposeProject().docker.modules["${project.name}"] = getDockerModuleType()
      coupleComposeTasksToRelatedModulesBuildTasks()
      coupleGenerateDockerfileToBuildOfBaseImage()
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

  private void registerHookTasks() {
    dockerTest = project.task(HookTaskNames.DOCKER_TEST_TASK_NAME) {
      group = 'Docker'
    }
    dockerTestStart = project.task(HookTaskNames.DOCKER_TEST_START_TASK_NAME) {
      group = 'Docker'
    }
    dockerCheck = project.task(HookTaskNames.DOCKER_CHECK_TASK_NAME) {
      group = 'Docker'
    }
  }

  private void registerInternalTasks() {
    Closure<Task> createTask = { Class<? extends Task> namedClazz ->
      project.task(namedClazz.NAME, type: namedClazz)
    }

    collectDependencies = createTask(CollectImageDependenciesTask)
    prepareDockerFile = createTask(PrepareDockerFileTask)
    buildImage = createTask(BuildImageTask)
    tagImage = createTask(TagImageTask)
    pushImage = createTask(PushImageTask)
    generateComposeFile = createTask(GenerateDockerComposeFileTask)
    runCompose = createTask(RunDockerComposeTask)
    stopCompose = createTask(StopDockerComposeTask)
    cleanCompose = createTask(CleanDockerComposeTask)
    runTestImage = createTask(RunTestImageTask)
  }

  private void setupHooksIntoInternalTasks() {
    dockerCheck.dependsOn dockerTest, cleanCompose

    dockerTest.dependsOn dockerTestStart
    project.gradle.projectsEvaluated {
      dockerTestStart.dependsOn getComposeProject().tasks.getByName(RunDockerComposeTask.NAME)
      getComposeProject().tasks.getByName(StopDockerComposeTask.NAME).mustRunAfter dockerTest
    }
    runCompose.finalizedBy cleanCompose

    tagImage.dependsOn project.getTasksByName(HookTaskNames.DOCKER_CHECK_TASK_NAME, RECURSIVE)

    runTestImage.dependsOn dockerTestStart
    dockerTest.dependsOn runTestImage
  }

  private void orderInternalTasks() {
    buildImage.dependsOn collectDependencies, prepareDockerFile
    tagImage.dependsOn buildImage
    pushImage.dependsOn tagImage
    generateComposeFile.dependsOn buildImage
    runCompose.dependsOn generateComposeFile
    stopCompose.dependsOn runCompose
    cleanCompose.dependsOn stopCompose
  }

  private void filterInternalTasksToRun() {
    Closure<Boolean> shouldRunDockerCompose = {
      def dependsOnComposeProject = project != getComposeProject()
      isModuleType(COMPOSE) ||
      (isModuleType(TEST_IMAGE) && !dependsOnComposeProject) ||
      (isModuleType(TEST) && !dependsOnComposeProject)
    }

    Closure<Boolean> shouldBuildImage = {
      isModuleType(DELIVERABLE_IMAGE) || isModuleType(TEST_IMAGE) || isModuleType(IMAGE)
    }

    collectDependencies.onlyIf shouldBuildImage
    buildImage.onlyIf shouldBuildImage
    tagImage.onlyIf { isModuleType(DELIVERABLE_IMAGE) }
    pushImage.onlyIf { isModuleType(DELIVERABLE_IMAGE) }
    generateComposeFile.onlyIf shouldRunDockerCompose
    runCompose.onlyIf shouldRunDockerCompose
    stopCompose.onlyIf shouldRunDockerCompose
    cleanCompose.onlyIf shouldRunDockerCompose
    runTestImage.onlyIf { isModuleType(TEST_IMAGE) }
  }

  private void coupleGenerateDockerfileToBuildOfBaseImage() {
    def baseImageProject = getBaseImageProject()
    if (baseImageProject) {
      prepareDockerFile.dependsOn baseImageProject.tasks["${BuildImageTask.NAME}"]
    }
  }

  private void coupleComposeTasksToRelatedModulesBuildTasks() {
    Project parentOrSelf = project.parent ?: project
    // make docker-compose run after all sibling and child projects have built their images
    generateComposeFile.dependsOn parentOrSelf.getTasksByName(BuildImageTask.NAME, RECURSIVE)
  }

  private void loadBaseComposeStackIfItExists() {
    def file = project.file('docker-compose.yml', PathValidation.NONE)
    if (file.exists()) {
      project.docker.baseComposeStack = file.getText('UTF-8')
    }
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private boolean isModuleType(DockerModuleType moduleType) {
    getDockerModuleType() == moduleType
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private DockerModuleType getDockerModuleType() {
    project.extensions.getByType(DockerExtension).dockerModuleType
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Project getBaseImageProject() {
    project.extensions.getByType(DockerExtension).baseImageProject
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Project getComposeProject() {
    project.extensions.getByType(DockerExtension).composeProject
  }

}
