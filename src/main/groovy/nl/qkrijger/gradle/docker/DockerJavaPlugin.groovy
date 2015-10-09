package nl.qkrijger.gradle.docker
import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerJavaPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.logger.debug 'Applying DockerJavaPlugin'

    def runDockerComposeTask = project.tasks.getByName(DockerPlugin.RUN_DOCKER_COMPOSE_TASK_NAME)
    def stopDockerComposeTask = project.tasks.getByName(DockerPlugin.STOP_DOCKER_COMPOSE_TASK_NAME)
    def testTask = project.tasks.test

    testTask.doFirst {
      project.logger.info 'Loading docker service information into the JVM as system properties'
      project.docker.services.each { serviceName, serviceInfo ->
        serviceInfo.each { infoKey, infoVal ->
          def systemEnvKey = "docker.$serviceName.$infoKey"
          project.logger.debug 'Loading {} with value {}', systemEnvKey, infoVal
          systemProperties[systemEnvKey] = infoVal
        }
      }
    }

    testTask.dependsOn runDockerComposeTask
    stopDockerComposeTask.mustRunAfter testTask
  }

}
