package scalaphx.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Simple Web server example taken from:
  *
  * http://doc.akka.io/docs/akka/2.4.7/scala/http/introduction.html
  *
  * User: terry
  * Date: 6/8/16
  * Time: 11:49 AM
  *
  */
object QuickExampleHttpService extends App {
  // define necessary implicits to setup the actor system & stream materializer
  implicit val system = ActorSystem("QuickExampleHttpService")
  implicit val materializer = ActorMaterializer()
  // use the execution context from the actor system above
  implicit val executionContext = system.dispatcher
  // define a simple route: GET /hello
  // NOTE - this is a def (method) vs a val (value) because this class extends App, which extends DelayedInit, which causes issues with unit testing.
  //        in production - you would not handle it this way, see the WeatherService example for a better structured 'real life' example.
  def route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello ScalaPHX!</h1>"))
      }
    }
  // setup the Http server...
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println("Server up: http://localhost:8080/\nPress return to stop...")

  StdIn.readLine()
  // clean things up nicely..
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
