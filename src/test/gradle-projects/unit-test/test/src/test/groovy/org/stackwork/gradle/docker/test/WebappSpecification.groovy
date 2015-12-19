package org.stackwork.gradle.docker.test

import spock.lang.Specification

import javax.ws.rs.client.ClientBuilder

class WebappSpecification extends Specification {

  def containerHost = System.properties.getProperty('stackwork.service.host')
  def containerPort = System.properties.getProperty('stackwork.service.port')

  def service = ClientBuilder.newClient().target("http://$containerHost:$containerPort")

  def "We can make a request to a web service running in Docker"() {
    when:
    def response = service.path('/').request().buildGet().invoke()

    then:
    response.status == 200
    response.readEntity(String.class).trim() == 'Serving frontend'
  }

}
