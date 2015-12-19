package org.stackwork.gradle.docker
import org.gradle.api.Plugin
import org.gradle.api.Project

class StackworkJavaPlugin implements Plugin<Project> {

  Project project

  @Override
  void apply(Project project) {
    this.project = project
    project.logger.debug 'Applying StackworkJavaPlugin'

    project.tasks.test.doFirst {
      project.logger.info 'Loading docker service information into the JVM as system properties'
      getComposeProject().stackwork.services.each { serviceName, serviceInfo ->
        serviceInfo.each { infoKey, infoVal ->
          def systemEnvKey = "stackwork.$serviceName.$infoKey"
          project.logger.debug 'Loading {} with value {}', systemEnvKey, infoVal
          systemProperties[systemEnvKey] = infoVal
        }
      }
    }

    project.tasks.check.dependsOn project.tasks.stackworkCheck
    project.tasks.stackworkTest.dependsOn project.tasks.test
    project.tasks.test.mustRunAfter project.tasks.stackworkTestStart
  }

  private Project getComposeProject() {
    project.extensions.getByType(StackworkExtension).composeProject
  }

}
