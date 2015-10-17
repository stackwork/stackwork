package nl.qkrijger.gradle.docker
import nl.qkrijger.gradle.docker.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin

import static nl.qkrijger.gradle.docker.DockerModuleType.*

class DockerPlugin implements Plugin<Project> {

  private Project project

  private Task dockerTest
  private Task dockerTestStart
  private Task dockerCheck

  private Task collectDependencies
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

    evaluateEnvironment()

    project.extensions.create('docker', DockerExtension, project)

    project.configurations {
      docker {
        description = 'Docker artifacts to be made available for an image build'
      }
    }

    registerInternalTasks()
    orderInternalTasks()
    filterInternalTasksToRun()

    registerHookTasks()
    setupHooksIntoInternalTasks()

    project.plugins.withType(JavaPlugin) {
      project.plugins.apply(DockerJavaPlugin)
    }

    project.afterEvaluate {
      if (project.parent) {
        project.parent.docker.modules["${project.name}"] = getDockerModuleType()
      }
      coupleComposeTasksToRelatedModulesBuildTasks()
      coupleTestTasksToComposeModule()
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
    dockerTest = project.task(HookTaskNames.DOCKER_TEST_TASK_NAME)
    dockerTestStart = project.task(HookTaskNames.DOCKER_TEST_START_TASK_NAME)
    dockerCheck = project.task(HookTaskNames.DOCKER_CHECK_TASK_NAME)
  }

  private void registerInternalTasks() {
    Closure<Task> createTask = { Class<? extends Task> namedClazz ->
      project.task(namedClazz.NAME, type: namedClazz)
    }

    collectDependencies = createTask(CollectImageDependenciesTask)
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

    dockerTestStart.dependsOn runCompose
    dockerTest.dependsOn dockerTestStart
    stopCompose.mustRunAfter dockerTest
    runCompose.finalizedBy cleanCompose

    tagImage.dependsOn dockerCheck

    runTestImage.dependsOn dockerTestStart
    dockerTest.dependsOn runTestImage
  }

  private void orderInternalTasks() {
    buildImage.dependsOn collectDependencies
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

  private void coupleComposeTasksToRelatedModulesBuildTasks() {
    Project parentOrSelf = project.parent ?: project
    // make docker-compose run after all sibling and child projects have built their images
    def relevantBuildImageTasks = parentOrSelf.getTasksByName(BuildImageTask.NAME, true)
    if (!relevantBuildImageTasks.empty) {
      generateComposeFile.dependsOn relevantBuildImageTasks
    }
  }

  private void coupleTestTasksToComposeModule() {
    def composeProject = getComposeProject()
    if (composeProject != project) {
      project.tasks.dockerTestStart.dependsOn composeProject.tasks.dockerTestStart
      composeProject.tasks.dockerTest.dependsOn project.tasks.dockerTest
    }
  }

  private boolean isModuleType(DockerModuleType moduleType) {
    getDockerModuleType() == moduleType
  }

  private DockerModuleType getDockerModuleType() {
    project.extensions.getByType(DockerExtension).dockerModuleType
  }

  private Project getComposeProject() {
    project.extensions.getByType(DockerExtension).composeProject
  }

}
