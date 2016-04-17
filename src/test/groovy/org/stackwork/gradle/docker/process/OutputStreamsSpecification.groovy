package org.stackwork.gradle.docker.process

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class OutputStreamsSpecification extends Specification {

  def 'OutputStreams does not accept null outputStreams'() {
    when:
    new OutputStreams([null])

    then:
    IllegalArgumentException e = thrown()
    e.message == 'Attempting to initialize OutputStreams with one or more null outputStreams'
  }

  def 'Multiple outputStreams receive the same data'() {
    given:
    def buffer1 = new ByteArrayOutputStream()
    def buffer2 = new ByteArrayOutputStream()

    when:
    OutputStreams outputStreams = new OutputStreams([buffer1, buffer2])
    def is = new ByteArrayInputStream('Hello world'.getBytes(UTF_8))
    IOUtils.copy is, outputStreams

    then:
    buffer1.toString() == 'Hello world'
    buffer2.toString() == 'Hello world'

    cleanup:
    [buffer1, buffer2, is].each { it.close() }
  }

}
