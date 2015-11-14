[![Build Status](https://travis-ci.org/Krijger/docker-gradle.svg?branch=master)](https://travis-ci.org/Krijger/docker-gradle)
# docker-gradle
Gradle plugin to integrate Docker into your project.
It uses **Docker Compose** to easily define application stack definitions.

This plugin uses Gradle to deliver **great flexibility**.
That also allows you to integrate with services such as Docker Hub, Travis CI or Tutum, keeping all your options open.

Use this plugin if you want to easily do **full stack application testing**.
Or, just pick a couple application's components to simplify integration testing.
You can create completely funky setups, with e.g. network disturbing components.

Here follows a list of the current features to give you an idea.
Note that modules refer to Gradle modules.

1. **Build, test, tag and push workflow** for a Docker image from a Dockerfile.
In case an image is the deliverable of your project.
2. Not using feature 1, but instead define an application stack of other images, e.g. to test compatibility of certain versions.
3. Any number of test modules.
4. Test modules defining their own application stack, meaning you can use:
5. **Any number of application stack definitions**, for different test suites.
6. **Test image modules** that build and use a test image that can be linked to application components.
This gives ultimate freedom in defining your tests: the contract is the exit code of the test image's process.
Examples include selenium user interface tests.
7. **Test modules** with tests defined in code.
Integration with the Gradle Java plugin (giving Groovy and Scala for free) is automatic.
The plugin makes the connection variables for the application component available to the tests.
8. **Image modules** to build any image you would like to use in one or more application definitions.
This allows you to define any stub, or inspector, or the funky stuff like chaos monkeys.
Very useful are components with an API that can be used by test images or test code.
9. Providing **dependencies** (such as artifacts downloaded from Nexus) to any docker build.
This allows clean Dockerfiles and use of the Docker caching mechanism.
10. Docker Compose modules, allowing multiple test modules to use the same application stack.
Useful in case it is heavy to start, although it can lead to problems in case test modules are not orthogonal.
11. Extending from image modules: modules that build an image can extend on another.
So if an image is meant to be extended, you can test that.
12. Compatibility with local (on Linux) or remote (Docker Machine, OSX) docker daemons.
13. **Gradle tasks that act as hooks to define your own logic**.

The plugin executes commands to the Docker CLI and Docker Compose, meaning those are prerequisites.
It also means out of the box support for the Linux socket, and any tweaking such as an --insecure-registry will be used automatically.

## Usage

*The best and most complete documentation are the test projects that you find in the source code.
They are very easy to read and quickly give an idea of use cases and strategies.
This documentation needs to be enhanced before a release 1.0.*

For any of the use cases below, your projects gradle file should include the plugin:

    // in your root project:
    buildscript {
      repositories {
        // for releases and release candidates:
        jcenter()
        // for snapshots:
        maven { url 'http://oss.jfrog.org/artifactory/oss-snapshot-local/' }
      }
      dependencies {
        classpath group: 'nl.qkrijger.gradle', name: 'docker-gradle', version: 'x.x.x[-rc.x][-SNAPSHOT]'
      }
    }
    // apply the plugin to every gradle project (i.e. subprojects that function as a docker module as well):
    apply plugin: 'docker'

### Building, tagging and pushing an image

For tagging, make sure your image has a name and a version:

    // build.gradle
    version = '1.1-SNAPSHOT'
    docker {
      imageName = 'my-image'
    }

Run by calling the Gradle `pushImage` task.

### Run groovy unit tests against an image built in your project

To allow your tests access to the service's (i.e. running container) host and port, you need to make your tests depend
on the `runDockerCompose` task and use the serviceInfo from the docker object populated by that task:

    // build.gradle
    tasks.test.doFirst {
      project.docker.services.each { serviceName, serviceInfo ->
        serviceInfo.each { infoKey, infoVal ->
          systemProperties["docker.$serviceName.$infoKey"] = infoVal
        }
      }
    }.dependsOn tasks.runDockerCompose

Clean up after yourself, also in case of failing tests by:

    tasks.runDockerCompose.finalizedBy tasks.cleanDockerCompose
    tasks.stopDockerCompose.mustRunAfter tasks.test

### Running without stopping containers

The plugin can be instructed to keep containers running after tests have been executed. To enable this behaviour
include the following configuration block in your build script:

    docker {
      stopContainers = false
    }

This will ensure that `StopDockerComposeTask` and subsequents clean-up tasks won't run. The feature is especially
useful when running on build services like Travis that, for security reasons, prevent containers from being stopped.

## Features

### Gradle docker object (runtime data)

The plugin exports a property object 'docker' that you can use in your Gradle build file.
The object is reachable via `project.docker.[property]`. For the following build tasks, these properties are

build task | property
---------- | -------------
n.a. done always | host
buildImage | imageId
tagImage   | fullImageName
generateDockerComposeFile | composeFile
runDockerCompose | services.SERVICE_NAME.host
runDockerCompose | services.SERVICE_NAME.port


### Gradle task buildImage

The `buildImage` task executes a docker build command in the root of your project.
You can have only a single Dockerfile, and the deliverable of the project is a single Docker image.
The resulting image id is exposed to Gradle via the docker object.

### Gradle task generateDockerComposeFile

The `generateDockerComposeFile` task parses the `docker-compose.yml.template` file in the projects `src/test` with all
project properties. Possible usage is a template file containing

    service:
      image: ${docker.imageId}

The resulting file including its path is exposed to Gradle via the Docker object for depending tasks.

Dependency: buildImage

### Gradle task runDockerCompose

The `runDockerCompose` task runs the generated docker compose file. It also exposes host and port for each service
through the docker object in the form of a map.

Dependency: generateDockerComposeFile

### Gradle task stopDockerCompose

The `stopDockerCompose` task stops the docker compose services.

Dependency: runDockerCompose

### Gradle task cleanDockerCompose

The `cleanDockerCompose` task cleans the stopped docker compose services.

Dependency: stopDockerCompose


### Gradle task tagImage

The `tagImage` task tags the image built during `buildImage` with the `project.version` (as tag) and
`docker { imageName }` (as name). The task will fail in case `project.version` or `docker { imageName }` doesn't exist.
The full image name including tag is exposed to Gradle via the docker object.
E.g. the `build.gradle` can contain:

    version = '1.1-SNAPSHOT'
    docker {
      imageName = 'my-image'
    }

Dependency: buildImage

### Gradle task pushImage

The `pushImage` task pushes the image tagged during `tagImage`. All logic for repositories, namespaces etc. comes from
the `docker { imageName }`.

Dependency: tagImage

## Get the plugin

The plugin is currently released to [Bintray][bt], while snapshots are pushed to [JFrog][jf] under [builds][jfb].
See the top of this document for a `build.gradle` example of applying the plugin.

[bt]: https://bintray.com/qkrijger/gradle-plugins/docker-gradle
[jf]: https://oss.jfrog.org
[jfb]: https://oss.jfrog.org/artifactory/webapp/#/builds/docker-gradle
