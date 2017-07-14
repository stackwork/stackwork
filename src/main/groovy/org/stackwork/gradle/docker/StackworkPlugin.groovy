package org.stackwork.gradle.docker

import org.gradle.api.PathValidation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.stackwork.gradle.docker.tasks.*

import static org.stackwork.gradle.docker.ModuleType.*
import static org.stackwork.gradle.docker.tasks.DockerComposeRunner.newBuildDockerComposeRunner
import static org.stackwork.gradle.docker.tasks.DockerComposeRunner.newDockerComposeRunner

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
  private Task buildStepWithStack

  private StackworkObject stackworkObject

  @Override
  void apply(Project project) {
    this.project = project

    project.extensions.create('stackwork', StackworkExtension, project)
    stackworkObject = new StackworkObject(project)
    project.ext.stackwork = stackworkObject
    stackworkObject.dockerComposeRunner = newDockerComposeRunner(project, stackworkObject)

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
      coupleComposeTasksToRelatedModulesBuildTasks()
      orderBuildTasksForImageBuildDependencies()
      if (applyBuildStepWithStack()) {
        stackworkObject.buildDockerComposeRunner = newBuildDockerComposeRunner(project, stackworkObject)
        registerAndOrderBuildStepWithStackTask()
      }
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
      project.logger.info 'No DOCKER_HOST system variable found, so assumed is you are using a local Docker daemon'

      OutputStream out = new ByteArrayOutputStream()
      project.exec {
        // check if Docker for Mac is the locally installed client, because that creates /var/run/docker.sock
        // instead of setting the DOCKER_HOST environment variable
        executable 'bash'
        args '-c', 'docker version --format "{{.Server.KernelVersion}}"'
        standardOutput = out
      }
      if (out.toString().contains('moby')) {
        project.logger.info 'Concluded you are using Docker for Mac. Will expose 127.0.0.1 as ip and forwarded ports ' +
            'to the test classes.'
        stackworkObject.host = '127.0.0.1'
      } else {
        project.logger.info 'Will expose docker container ips and exposed ports to the test classes.'
      }
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

  private final Closure<Task> createTask = { Class<? extends Task> namedClazz ->
    project.task(namedClazz.NAME, type: namedClazz)
  }

  private void registerInternalTasks() {
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

  private final Closure<Boolean> shouldBuildImage = {
    isModuleType(DELIVERABLE_IMAGE) || isModuleType(TEST_IMAGE) || isModuleType(IMAGE) || isModuleType(BUILDER_IMAGE)
  }

  private void filterInternalTasksToRun() {
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

  private void orderBuildTasksForImageBuildDependencies() {
    getImageBuildDependencies().each {
      project.tasks.parseDockerFileTemplate.dependsOn it.getTasksByName(BuildImageTask.NAME, NOT_RECURSIVE)
      project.tasks.buildImage.dependsOn it.getTasksByName(BuildImageTask.NAME, NOT_RECURSIVE)
    }
  }

  private void coupleComposeTasksToRelatedModulesBuildTasks() {
    Project parentOrSelf = project.parent ?: project
    // make docker-compose run after all sibling and child projects have built their images
    parseComposeTemplate.dependsOn parentOrSelf.getTasksByName(BuildImageTask.NAME, RECURSIVE)
  }

  private void registerAndOrderBuildStepWithStackTask() {
    buildStepWithStack = createTask(BuildStepWithStackTask)
    buildStepWithStack.dependsOn buildImage
    tagImage.dependsOn buildStepWithStack
    parseComposeTemplate.dependsOn buildStepWithStack
    buildStepWithStack.onlyIf shouldBuildImage
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
  private List<Project> getImageBuildDependencies() {
    project.extensions.getByType(StackworkExtension).imageBuildDependencies
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Project getComposeProject() {
    project.extensions.getByType(StackworkExtension).composeProject
  }

  /**
   * depends on extension, so must be called after evaluation
   */
  private Boolean applyBuildStepWithStack() {
    project.extensions.getByType(StackworkExtension).applyBuildStepWithStack
  }

}
