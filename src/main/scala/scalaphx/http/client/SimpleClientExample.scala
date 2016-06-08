package scalaphx.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.CompactByteString

/**
  * akka-http client examples
  *
  * User: terry
  * Date: 6/8/16
  * Time: 12:31 PM
  *
  */
object SimpleClientExample extends App {

  implicit val system = ActorSystem("ClientExample")
  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val futureResults = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/weather/current/85085").withHeaders(Accept(MediaTypes.`application/json`)))

  futureResults.flatMap {
    response =>
      println(s"Status: ${response.status.intValue()}")
      println(s"Content-Type: ${response.entity.contentType}")
      Unmarshal(response.entity).to[String].map {
        responseString => println(s"Response:\n$responseString")
      }
  }
}

object SimpleClientPostExample extends App {

  import spray.json._

  import scalaphx.http.server.Protocols._
  import scalaphx.http.server.WeatherReport

  implicit val system = ActorSystem("ClientPostExample")
  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val flow = Http().outgoingConnection(host = "localhost", port = 8080)

  // create our report to submit & convert to Json
  val report = WeatherReport(zip = "85001", highTemp = 115.0f, lowTemp = 89.3f, current = 113.3f, conditions = "damn hot!")
  val reportJson = report.toJson
  // create the HttpEntity
  val entity = Strict(contentType = ContentTypes.`application/json`, CompactByteString(reportJson.toString()))
  val postRequest = HttpRequest(method = HttpMethods.POST, uri = "/weather/current").withEntity(entity)
  // wire up a stream that uses the post request and the above flow we defined above, and run it!
  val futureResults = Source.single(postRequest).via(flow).runWith(Sink.head)
  // show the results
  futureResults.flatMap(printResults)

  // you wouldn't really do this in production...sleeping to make sure above thread finishes for this example
  Thread.sleep(500)

  // create our GET request for the item we just posted
  val getRequest = HttpRequest(uri = s"/weather/current/${report.zip}")
  // re-use the previously defined flow and execute the stream
  val futureGetResults = Source.single(getRequest).via(flow).runWith(Sink.head)
  // show the results
  futureGetResults.flatMap(printResults)


  private def printResults(response: HttpResponse) = {
    println(s"Status: ${response.status.intValue()}")
    println(s"Content-Type: ${response.entity.contentType}")
    Unmarshal(response.entity).to[String].map {
      entity => println(s"Response:\n$entity\n")
    }
  }

}