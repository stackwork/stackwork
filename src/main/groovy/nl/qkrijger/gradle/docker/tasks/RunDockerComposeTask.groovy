package nl.qkrijger.gradle.docker.tasks
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.yaml.snakeyaml.Yaml

class RunDockerComposeTask extends Exec {

  RunDockerComposeTask() {
    description = 'Runs the generated docker compose file.'
    group = 'Docker'

    String composeFile = project.docker.composeFile

    commandLine 'docker-compose', '-f', composeFile, 'up', '-d'

    // write the services ports and hosts
    doLast {
      Map<String, Object> compose = (Map<String, Object>) new Yaml().load(project.file(composeFile).text);
      compose.each { String serviceName, Object serviceConfiguration ->
        Map<String, Object> serviceInfo = [:]
        String containerId = RunDockerComposeTask.askComposeServicesContainerId(project, serviceName, composeFile)
        Map<String, Object> containerInfo = RunDockerComposeTask.dockerInspectContainer(project, containerId)

        String dockerHost = project.docker.host

        if (dockerHost) {
          containerInfo.NetworkSettings.Ports.each { exposedPortProto, forwardedPorts ->

            if (!forwardedPorts) {
              project.logger.warn("No port forwarding defined for service '$serviceName'. " +
                      "No port configuration will be exposed.")
              return
            }

            int exposedPort = ((exposedPortProto.toString() =~ /\d+/)[0]).toInteger()
            if (forwardedPorts.isEmpty()) {
              project.logger.warn("No forwarded port found for exposed port: '$exposedPort'")
            } else {
              if (forwardedPorts.size() > 1) {
                project.logger.warn("Multiple forwarded ports found for exposed port: '$exposedPort'. " +
                        "Continuing with a randomly selected port.")
              }
              serviceInfo.port = forwardedPorts.first().HostPort
            }

            serviceInfo.host = dockerHost
          }
        } else {
          containerInfo.NetworkSettings.Ports.each { exposedPortProto, forwardedPort ->
            int port = ((exposedPortProto.toString() =~ /\d+/)[0]).toInteger()
            serviceInfo.port = port
            serviceInfo["port.$port"] = port
          }
          serviceInfo.host = containerInfo.NetworkSettings.IPAddress
        }
        project.docker.services["$serviceName"] = serviceInfo
        println "Setting docker.services.$serviceName: $serviceInfo"
      }
    }
  }

  private static String askComposeServicesContainerId(Project project, String service, String composeFile) {
    OutputStream os = new ByteArrayOutputStream()
    project.exec {
      setCommandLine(['docker-compose', '-f', composeFile, 'ps', '-q', service])
      setStandardOutput(os)
    }
    os.toString().trim()
  }

  private static Map<String, Object> dockerInspectContainer(Project project, String containerId) {
    OutputStream containerInfoOS = new ByteArrayOutputStream()
    project.exec {
      setCommandLine(['docker', 'inspect', containerId])
      setStandardOutput(containerInfoOS)
    }
    (new Yaml().load(containerInfoOS.toString()))[0] as Map<String, Object>
  }
}
