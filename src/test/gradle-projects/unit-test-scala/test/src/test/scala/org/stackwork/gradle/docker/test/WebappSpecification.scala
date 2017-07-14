package org.stackwork.gradle.docker.test

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.twitter.finagle.Http
import com.twitter.finagle.http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}

@RunWith(classOf[JUnitRunner])
class WebappSpecification extends FunSuite {

  test("We can make a request to a web service running in Docker") {

    val host = System.getProperty("stackwork.service.host")
    val port = System.getProperty("stackwork.service.port")

    val client = Http.client.newService(s"$host:$port")
    val request = http.Request(http.Method.Get, "/")
    val response = client(request)

    Await.result(response.onSuccess { rep: http.Response =>
      assert(rep.status === 200)
    })
  }

}
