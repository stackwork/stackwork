package org.stackwork.gradle.docker

import org.gradle.api.PathValidation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.stackwork.gradle.docker.tasks.*

import static org.stackwork.gradle.docker.ModuleType.*

class StackworkPlugin implements Plugin<Project> {

  private static final boolean RECURSIVE = true
  private static final boolean NOT_RECURSIVE = false
  private Project project

  private Task stackworkTest
  private Task stackworkTestStart
  private Task stackworkCheck

  private Task collectDependencies
  private Task parseDockerFileTemplate
  private Task buildImage
  private Task cleanCompose
  private Task runCompose
  private Task stopCompose
  private Task pushImage
  private Task tagImage
  private Task runTestImage
  private Task parseComposeTemplate

  private StackworkObject stackworkObject

  @Override
  void apply(Project project) {
    this.project = project

    project.extensions.create('stackwork', StackworkExtension, project)
    stackworkObject = new StackworkObject(project)
    project.ext.stackwork = stackworkObject
    stackworkObject.dockerComposeRunner = new DockerComposeRunner(project, stackworkObject, 'docker-compose.yml')

    evaluateEnvironment()

    project.configurations {
      stackwork {
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
      project.plugins.apply(StackworkJavaPlugin)
    }

    project.gradle.projectsEvaluated {
      StackworkObject stackworkOfComposeProject = getComposeProject().stackwork
      stackworkOfComposeProject.modules["${project.name}"] = getModuleType()
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
      stackworkObject.host = new URI(dockerHost).host
    } else {
      // in case of local docker daemon
      project.logger.info('No DOCKER_HOST system variable found. Will expose docker container ips and exposed ports' +
              ' to the test classes.')
    }
  }

  private void registerHookTasks() {
    stackworkTest = project.task(HookTaskNames.STACKWORK_TEST_TASK_NAME) {
      group = 'Stackwork'
    }
    stackworkTestStart = project.task(HookTaskNames.STACKWORK_TEST_START_TASK_NAME) {
      group = 'Stackwork'
    }
    stackworkCheck = project.task(HookTaskNames.STACKWORK_CHECK_TASK_NAME) {
      group = 'Stackwork'
    }
  }

  private void registerInternalTasks() {
    Closure<Task> createTask = { Class<? extends Task> namedClazz ->
      project.task(namedClazz.NAME, type: namedClazz)
    }

    collectDependencies = createTask(CollectImageDependenciesTask)
    parseDockerFileTemplate = createTask(ParseDockerFileTemplateTask)
    buildImage = createTask(BuildImageTask)
    tagImage = createTask(TagImageTask)
    pushImage = createTask(PushImageTask)
    parseComposeTemplate = createTask(ParseComposeTemplateTask)
    runCompose = createTask(RunDockerComposeTask)
    stopCompose = createTask(StopDockerComposeTask)
    cleanCompose = createTask(CleanDockerComposeTask)
    runTestImage = createTask(RunTestImageTask)
  }

  private void setupHooksIntoInternalTasks() {
    stackworkCheck.dependsOn stackworkTest, cleanCompose

    stackworkTest.dependsOn stackworkTestStart
    project.gradle.projectsEvaluated {
      stackworkTestStart.dependsOn getComposeProject().tasks.getByName(RunDockerComposeTask.NAME)
      getComposeProject().tasks.getByName(StopDockerComposeTask.NAME).mustRunAfter stackworkTest
    }
    runCompose.finalizedBy stopCompose
    runCompose.finalizedBy cleanCompose

    tagImage.dependsOn project.getTasksByName(HookTaskNames.STACKWORK_CHECK_TASK_NAME, RECURSIVE)

    runTestImage.dependsOn stackworkTestStart
    stackworkTest.dependsOn runTestImage
  }

  private void orderInternalTasks() {
    buildImage.dependsOn collectDependencies, parseDockerFileTemplate
    tagImage.dependsOn buildImage
    pushImage.dependsOn tagImage
    parseComposeTemplate.dependsOn buildImage
    runCompose.dependsOn parseComposeTemplate
    stopCompose.dependsOn runCompose
    cleanCompose.mustRunAfter stopCompose
  }

  private void filterInternalTasksToRun() {
    Closure<Boolean> shouldBuildImage = {
      isModuleType(DELIVERABLE_IMAGE) || isModuleType(TEST_IMAGE) || isModuleType(IMAGE)
    }

    collectDependencies.onlyIf shouldBuildImage
    buildImage.onlyIf shouldBuildImage
    tagImage.onlyIf { isModuleType(DELIVERABLE_IMAGE) }
    pushImage.onlyIf { isModuleType(DELIVERABLE_IMAGE) }
    parseComposeTemplate.onlyIf { isComposeEnabledProject() }
    runCompose.onlyIf { isComposeEnabledProject() }
    stopCompose.onlyIf { isComposeEnabledProject() }
    cleanCompose.onlyIf { isComposeEnabledProject() }
    runTestImage.onlyIf { isModuleType(TEST_IMAGE) }
  }

  protected boolean isComposeEnabledProject() {
    def dependsOnComposeProject = project != getComposeProject()
    isModuleType(COMPOSE) ||
        (isModuleType(TEST_IMAGE) && !dependsOnComposeProject) ||
        (isModuleType(TEST) && !dependsOnComposeProject)
  }

  private void coupleGenerateDockerfileToBuildOfBaseImage() {
    def baseImageProject = getBaseImageProject()
    if (baseImageProject) {
      parseDockerFileTemplate.dependsOn baseImageProject.getTasksByName(BuildImageTask.NAME, NOT_RECURSIVE)
    }
  }

  private void coupleComposeTasksToRelatedModulesBuildTasks() {
    Project parentOrSelf = project.parent ?: project
    // make docker-compose run after all sibling and child projects have built their images
    parseComposeTemplate.dependsOn parentOrSelf.getTasksByName(BuildImageTask.NAME, RECURSIVE)
  }

  private void loadBaseComposeStackIfItExists() {
    def file = project.file('docker-compose.yml', PathValidation.NONE)
    if (file.exists()) {
      stackworkObject.baseComposeStack = file.getText('UTF-8')
    }
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private boolean isModuleType(ModuleType moduleType) {
    getModuleType() == moduleType
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private ModuleType getModuleType() {
    project.extensions.getByType(StackworkExtension).getModuleType()
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Project getBaseImageProject() {
    project.extensions.getByType(StackworkExtension).baseImageProject
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Project getComposeProject() {
    project.extensions.getByType(StackworkExtension).composeProject
  }

}
