package org.stackwork.gradle.docker.process

import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.AbstractProject
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Exec
import org.gradle.internal.reflect.DirectInstantiator
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.service.DefaultServiceRegistry
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.GradleConnector
import spock.lang.IgnoreRest
import spock.lang.Specification

class OutputStreamsSpecification extends Specification {

  def 'OutputStreams does not accept null outputStreams'() {
    when:
    new OutputStreams([null])

    then:
    IllegalArgumentException e = thrown()
    e.message == 'Attempting to initialize OutputStreams with one or more null outputStreams'
  }

  @IgnoreRest
  def 'Multiple outputStreams receive the same data'() {
    given:
    def buffer1 = new ByteArrayOutputStream()
    def buffer2 = new ByteArrayOutputStream()
    OutputStreams outputStreams = new OutputStreams([buffer1, buffer2])

    Project project = ProjectBuilder.builder().build()
    project.task('testTask', type: Exec) {
      commandLine 'echo', 'Hello world'
      standardOutput outputStreams
    }

    when:
    project.tasks['testTask'].execute()

    then:
    buffer1.toString() == 'Hello world'
    buffer2.toString() == 'Hello world'
  }

//  static DefaultProject createRootProject() {
//    createRootProject(TestNameTestDirectoryProvider.newInstance().testDirectory)
//  }
//
//  private TaskInternal createTask(String name) {
//    AbstractProject project = createRootProject()
//    DefaultServiceRegistry registry = new DefaultServiceRegistry()
//    registry.add(Instantiator, DirectInstantiator.INSTANCE)
//    TaskInternal task = rootFactory.createChild(project, instantiator).createTask(GUtil.map(Task.TASK_TYPE, TestTask, Task.TASK_NAME, name))
//    assertTrue(TestTask.isAssignableFrom(task.getClass()))
//    return task
//  }

//  /**
//   * A JUnit rule which provides a unique temporary folder for the test.
//   */
//  public class TestNameTestDirectoryProvider extends AbstractTestDirectoryProvider {
//    static {
//      // NOTE: the space in the directory name is intentional
//      root = new TestFile(new File("build/tmp/test files"));
//    }
//
//    public static TestNameTestDirectoryProvider newInstance() {
//      return new TestNameTestDirectoryProvider();
//    }
//
//    public static TestNameTestDirectoryProvider newInstance(FrameworkMethod method, Object target) {
//      TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider();
//      testDirectoryProvider.init(method.getName(), target.getClass().getSimpleName());
//      return testDirectoryProvider;
//    }
//
//    public TestNameTestDirectoryProvider withSuppressCleanup() {
//      suppressCleanup();
//      return this;
//    }
//  }
}
