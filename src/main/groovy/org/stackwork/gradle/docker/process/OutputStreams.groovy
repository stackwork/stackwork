package org.stackwork.gradle.docker.process

import groovy.util.logging.Log

@Log
class OutputStreams extends OutputStream {

  private final List<OutputStream> outputStreams;

  public OutputStreams(List<OutputStream> outputStreams) {
    for (OutputStream outputStream : outputStreams) {
      if (out == null)
        throw new NullPointerException();
    }
    else if (tee == null)
      throw new NullPointerException();

    this.out = out;
    this.tee = tee;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    tee.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
    tee.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    tee.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    out.flush();
    tee.flush();
  }

  @Override
  public void close() throws IOException {
    out.close();
    tee.close();
  }
}
