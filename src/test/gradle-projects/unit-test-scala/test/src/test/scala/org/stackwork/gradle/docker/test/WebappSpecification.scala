package org.stackwork.gradle.docker.test

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.Await
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WebappSpecification extends FunSuite with ScalaFutures {

  test("We can make a request to a web service running in Docker, using an assigned port") {

    val host = System.getProperty("stackwork.service.host")
    val port = System.getProperty("stackwork.service.port")

    val endpoint: Service[Request, Response] = Http.client.newService(s"$host:$port")
    val request = http.Request("/")
    request.host = s"$host:$port"
    val response = Await.result(endpoint(request))
    assert(response.status.code === 200)
  }

  test("We can make a request to a web service running in Docker, using port nr. 1 specifically") {

    val host = System.getProperty("stackwork.service.host")
    val port = System.getProperty("stackwork.service.port_80")

    val endpoint: Service[Request, Response] = Http.client.newService(s"$host:$port")
    val request = http.Request("/")
    request.host = s"$host:$port"
    val response = Await.result(endpoint(request))
    assert(response.status.code === 200)
  }

  test("We can make a request to a web service running in Docker, using port nr. 2 specifically") {

    val host = System.getProperty("stackwork.service.host")
    val port = System.getProperty("stackwork.service.port_81")

    val endpoint: Service[Request, Response] = Http.client.newService(s"$host:$port")
    val request = http.Request("/")
    request.host = s"$host:$port"
    val response = Await.result(endpoint(request))
    assert(response.status.code === 200)
  }

}
