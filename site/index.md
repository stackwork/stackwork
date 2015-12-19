---
title: Stackwork
subtitle: Make your stack work
layout: default
---
Full stack application testing with Docker, Docker Compose and Gradle.

Stackwork allows you to integrate Docker into your project.
Specifically, for testing purposes, which is a great use case for Docker.
It uses **Docker Compose** to easily define application stack definitions.

Stackwork is a Gradle plugin to deliver **great flexibility**.
This allows you to integrate with services such as Docker Hub, Travis CI or Tutum, keeping all your options open.

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

## Documentation, source code and contributions
At the Github project page: [stackwork](http://github.com/Krijger/docker-gradle).
And yes, that name is Docker Gradle instead of Stackwork. We're branding now :)
