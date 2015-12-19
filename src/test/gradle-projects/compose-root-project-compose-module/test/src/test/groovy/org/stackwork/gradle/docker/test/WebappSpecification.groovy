package org.stackwork.gradle.docker.test

import spock.lang.Specification

import javax.ws.rs.client.ClientBuilder

class WebappSpecification extends Specification {

  def dockerContainerHost = System.properties.getProperty('docker.service.host')
  def dockerContainerPort = System.properties.getProperty('docker.service.port')

  def service = ClientBuilder.newClient().target("http://$dockerContainerHost:$dockerContainerPort")

  def "We can make a request to a web service running in Docker"() {
    when:
    def response = service.path('/').request().buildGet().invoke()

    then:
    response.status == 200
    response.readEntity(String.class).trim() == 'Hello world'
  }

}
