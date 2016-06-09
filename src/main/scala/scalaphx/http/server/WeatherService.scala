package scalaphx.http.server

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import scala.collection.mutable.ListBuffer
import scala.io.StdIn
import scalaphx.http.server.WeatherCacheActor.Protocol.{AddReport, FindByZip}

/**
  * More of a real-life example of akka-http used to build a http based micro-service
  *
  * User: terry
  * Date: 6/8/16
  * Time: 11:58 AM
  *
  */

case class WeatherReport(zip : String, highTemp : Float, lowTemp : Float, current : Float, conditions : String)
case class Error(status : Int, message : String)

object Protocols extends DefaultJsonProtocol {
  implicit val weatherReportFormat = jsonFormat5(WeatherReport.apply)
  implicit val errorFormat = jsonFormat2(Error.apply)
}

object WeatherCache {
  val cache = new ListBuffer[WeatherReport]

  cache += WeatherReport("85021", 112.3f, 85.6f, 100.8f, "clear")
  cache += WeatherReport("85085", 110.8f, 75.6f, 99.3f, "mostly cloudy")
  cache += WeatherReport("98101", 68.3f, 59.9f, 70.0f, "rainy - again")

  def findByZip(zip : String) = cache.find(_.zip == zip)
  def addReport(report : WeatherReport) = cache += report

}

trait WeatherServiceRoutes extends SprayJsonSupport {
  import Protocols._

  val routes = {
    logRequestResult("weather-api") {
        pathPrefix("weather" / "current") {
          (get & path(Segment)) {
            zip =>
              val badRequestError = NotFound -> Error(status = 404, s"Zip $zip was not found!")
              // not sure what's going on here - this compiles cleanly but IntelliJ reports as error...
              complete {
                WeatherCache.findByZip(zip).fold[ToResponseMarshallable](badRequestError)(report => OK -> report)
              }
          } ~
            (post & entity(as[WeatherReport])) { report =>
              complete {
                WeatherCache.addReport(report)
                Created -> s"Report added for ${report.zip}"
              }
            }
        }
      }
    }
}

trait WeatherServiceSupport {
  implicit val system = ActorSystem("WeatherService")
  implicit val materializer = ActorMaterializer()
  implicit def executor = system.dispatcher

  val logger = Logging(system, getClass)

}


object WeatherServiceServer extends App with WeatherServiceRoutes with WeatherServiceSupport {

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println("Server up: http://localhost:8080/\nPress return to stop...")

  StdIn.readLine()
  // clean things up nicely..
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}