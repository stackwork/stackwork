package org.stackwork.gradle.docker

enum DockerModuleType {

  /**
   * Only exists to avoid null-checks. No meaning other than that no type has been set explicitly
   */
  DEFAULT,
  /**
   * Test module.
   *
   * Runs Java (or Groovy or Scala) tests against the Docker Compose setup. Does not build an image.
   *
   * Is assumed to have applied the {@link org.gradle.api.plugins.JavaPlugin}.
   * The test task JVM will be enriched with runtime information for the Docker Compose setup.
   */
  TEST,
  /**
   * Test image module.
   *
   * Builds a test image, and run that agains the Docker Compose setup.
   */
  TEST_IMAGE,
  /**
   * Image module.
   *
   * Any image needed in other modules docker compose setups can be created in an image module.
   */
  IMAGE,
  /**
   * Image module for a deliverable image. For a deliverable image, tag and push tasks are created.
   *
   * Often, the root project has this type, building the image under test.
   *
   * Multiple modules can have this type, allowing to push multiple image in the same build job.
   */
  DELIVERABLE_IMAGE,
  /**
   * Docker compose module.
   *
   * Either the root project that has a docker compose file as deliverable that you want to test in the build.
   * Or a module which supplies the docker compose setup to multiple other modules.
   */
  COMPOSE,

}
