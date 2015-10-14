package nl.qkrijger.gradle.docker
import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerJavaPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.logger.debug 'Applying DockerJavaPlugin'

    project.tasks.test.doFirst {
      project.logger.info 'Loading docker service information into the JVM as system properties'
      project.docker.services.each { serviceName, serviceInfo ->
        serviceInfo.each { infoKey, infoVal ->
          def systemEnvKey = "docker.$serviceName.$infoKey"
          project.logger.debug 'Loading {} with value {}', systemEnvKey, infoVal
          systemProperties[systemEnvKey] = infoVal
        }
      }
    }

    project.tasks.check.dependsOn project.tasks.dockerCheck
    project.tasks.dockerTest.dependsOn project.tasks.test
    project.tasks.test.mustRunAfter project.tasks.dockerTestStart
  }

}
