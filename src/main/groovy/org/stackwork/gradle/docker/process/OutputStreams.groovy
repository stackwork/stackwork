package org.stackwork.gradle.docker.process

import groovy.util.logging.Log

@Log
class OutputStreams extends OutputStream {

  private final List<OutputStream> outputStreams;

  public OutputStreams(List<OutputStream> outputStreams) {
    outputStreams.each {
      if (it == null) {
        String msg = 'Attempting to initialize OutputStreams with one or more null outputStreams'
        log.severe msg
        throw new IllegalArgumentException(msg)
      }
    }

    this.outputStreams = outputStreams
  }

  @Override
  public void write(int b) throws IOException {
    outputStreams.each {
      it.write b
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    outputStreams.each {
      it.write b
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    outputStreams.each {
      it.write b, off, len
    }
  }

  @Override
  public void flush() throws IOException {
    outputStreams.each {
      it.flush()
    }
  }

  @Override
  public void close() throws IOException {
    outputStreams.each {
      it.close();
    }
  }
}
