package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Internal
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject
import org.yaml.snakeyaml.Yaml

class RunDockerComposeTask extends AbstractTask {

  final static NAME = 'runDockerCompose'
  @Internal final StackworkObject stackwork = project.stackwork

  @Internal List<String> longRunningServices
  @Internal String composeFile = stackwork.dockerComposeRunner.composeFilePath
  @Internal Process composeProcess
  @Internal String composeProject = stackwork.dockerComposeRunner.projectId

  RunDockerComposeTask() {
    description = 'Runs the generated docker compose file.'
    group = 'Stackwork'

    int composeVersion

    doLast {
      stackwork.dockerComposeRunner.loadComposeInfo()
      composeVersion = stackwork.dockerComposeRunner.composeVersion
      longRunningServices = stackwork.dockerComposeRunner.longRunningServices
    }

    doLast {
      // start the compose project and monitor it's output
      String[] command = ['docker-compose', '-f', composeFile, '-p', composeProject, 'up'] + this.longRunningServices

      String marker = project.extensions.getByType(StackworkExtension).stackIsRunningWhenLogContains

      if (marker) {
        project.logger.info 'Log marker defined: "{}". Using this to scan logs for start indicator.', marker
      } else {
        project.logger.info 'No log marker defined, compose will be started "fire and forget" style.'
      }

      project.logger.info 'Starting Docker Compose using command: {}', Arrays.toString(command)

      spawnProcessAndWaitFor command, { String line ->
        project.logger.info line
        if (marker == null || line == null) {
          boolean allServicesHaveStarted = true
          longRunningServices.each { String serviceName ->
            String containerId = this.askComposeServicesContainerId(serviceName)
            if (containerId == null || containerId.empty) allServicesHaveStarted = false
          }
          return allServicesHaveStarted
        }

        if (line.contains(marker)) {
          project.logger.info 'Found marker "{}" in compose logs. Stack started.', marker
          return true
        }

        return false
      }
    }

    // write the services ports and hosts
    doLast {
      longRunningServices.each { String serviceName ->
        Map<String, Object> serviceInfo = [:]
        String containerId = this.askComposeServicesContainerId(serviceName)
        Map<String, Object> containerInfo = this.dockerInspectContainer(containerId)

        // dockerHost is null in case of local Docker deamon, or the host to connect to for a remote Docker host

        containerInfo.NetworkSettings.Ports.each { String exposedPortProto, forwardedPorts ->
          int exposedPort = (exposedPortProto =~ /\d+/)[0] as int

          if (stackwork.host) {
            if (!forwardedPorts) {
              project.logger.warn("No port forwarding defined for service '$serviceName'. " +
                      "No port configuration will be exposed.")
              return
            }
            if (forwardedPorts.isEmpty()) {
              project.logger.warn("No forwarded port found for exposed port: '$exposedPort'")
            } else {
              if (forwardedPorts.size() > 1) {
                project.logger.warn("Multiple forwarded ports found for exposed port: '$exposedPort'. " +
                        "Continuing with a randomly selected port.")
              }
              serviceInfo.port = forwardedPorts.first().HostPort
            }
            serviceInfo.host = stackwork.host
          } else {
            serviceInfo.port = exposedPort
            serviceInfo["port.${exposedPort}"] = exposedPort
            if (composeVersion == 1) {
              serviceInfo.host = containerInfo.NetworkSettings.IPAddress
            } else if (composeVersion == 2) {
              Map<String, Object> networks = containerInfo.NetworkSettings.Networks
              if (networks.size() != 1) {
                throw new IllegalStateException("${networks.size()} networks found, only 1 supported!")
              }
              serviceInfo.host = networks.values().first().IPAddress
            } else {
              throw new IllegalStateException('Unsupported docker compose version')
            }
          }
        }
        stackwork.services["$serviceName"] = serviceInfo
        project.logger.info "Setting stackwork.services.$serviceName: $serviceInfo"
      }
    }
  }

  static String createRandomString() {
    def pool = ['A'..'Z', 0..9].flatten()
    Random rand = new Random(System.currentTimeMillis())
    def passChars = (0..8).collect { pool[rand.nextInt(pool.size())] }
    passChars.join()
  }

  /**
   * Spawn an external process and monitor it's standard and error output for a certain predicate
   *
   * TODO: add configurable time-out functionality in case the predicate is never fulfilled
   */
  void spawnProcessAndWaitFor(String[] command, Closure logPredicate) {

    File logDir = project.file("${stackwork.buildDir}/logs")
    logDir.mkdirs()
    File logFile = new File(logDir, "docker-compose-${stackwork.dockerComposeRunner.projectId}.log")
    stackwork.composeLogFile = logFile
    logFile.createNewFile()
    Process compose = new ProcessBuilder(command).
        redirectErrorStream(true).
        redirectOutput(logFile).
        start()
    this.composeProcess = compose

    logFile.withReader { reader ->
      String line
      while (reader.ready() || compose.alive) {
        line = reader.readLine()
        if (line) {
          if (logPredicate(line)) {
            break
          }
        } else {
          // compose is still running, so we can expect new log lines later
          Thread.sleep(100)
        }
      }
    }
  }

  String askComposeServicesContainerId(String serviceName) {
    OutputStream os = new ByteArrayOutputStream()
    project.exec {
      setCommandLine(['docker-compose', '-f', this.composeFile, '-p', stackwork.dockerComposeRunner.projectId,
                      'ps', '-q', serviceName])
      setStandardOutput(os)
    }
    os.toString().trim()
  }

  Map<String, Object> dockerInspectContainer(String containerId) {
    OutputStream containerInfoOS = new ByteArrayOutputStream()
    project.exec {
      setCommandLine(['docker', 'inspect', containerId])
      setStandardOutput(containerInfoOS)
    }
    (new Yaml().load(containerInfoOS.toString()))[0] as Map<String, Object>
  }
}
