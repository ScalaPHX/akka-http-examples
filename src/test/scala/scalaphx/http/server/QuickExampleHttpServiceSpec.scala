package scalaphx.http.server

import akka.http.scaladsl.model.{ContentTypes, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

/**
  * Unit test for basic server sample
  *
  * User: terry
  * Date: 6/8/16
  * Time: 3:17 PM
  *
  */
class QuickExampleHttpServiceSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val route = Route.seal(QuickExampleHttpService.route)

  "The service" should {
    "return valid html for GET /hello" in {
      Get("/hello") ~> route ~> check {
        info(s"response => ${responseAs[String]}")
        status should be (StatusCodes.OK)
        contentType should be (ContentTypes.`text/html(UTF-8)`)
        responseAs[String] should be ("<h1>Hello ScalaPHX!</h1>")
      }
    }

    "return a 404 for a GET /hello/world" in {
      Get("/hello/world") ~> route ~> check {
        status should be (StatusCodes.NotFound)
      }
    }
  }
}
