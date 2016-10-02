package org.stackwork.gradle.docker.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.stackwork.gradle.docker.ModuleType
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject

class BuildStepWithStackTask extends DefaultTask {

  final static NAME = 'buildStepWithStack'
  @Internal final StackworkObject stackwork = project.stackwork

  BuildStepWithStackTask() {
    group = 'Stackwork'
    description = 'Parses the build.docker-compose.yml.template file and applies it to built image, committing the ' +
            'result'
  }

  @TaskAction
  def apply() {
    def composeRunner = stackwork.buildDockerComposeRunner
    composeRunner.parseComposeTemplate()
    composeRunner.run()

    List<Project> buildDependencyProjects = project.extensions.getByType(StackworkExtension).imageBuildDependencies
    List<Project> builderImageProjects = buildDependencyProjects.findAll {
      it.extensions.getByType(StackworkExtension).moduleType == ModuleType.BUILDER_IMAGE
    }
    stackwork.imageId = ExecutableImageRunner.runBuildImage(project, builderImageProjects)

    if (project.extensions.getByType(StackworkExtension).stopContainers) {
      composeRunner.stop()
      composeRunner.clean()
    }
  }

}
