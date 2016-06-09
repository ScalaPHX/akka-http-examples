package scalaphx.http.server

import akka.actor.{Actor, Props}

import scalaphx.http.server.WeatherCacheActor.Protocol.{AddReport, FindByZip}

/**
  * Sample actor.
  *
  * Uses the WeatherCache from the WeatherService example as the basis for its business logic
  *
  * User: terry
  * Date: 6/9/16
  * Time: 2:02 PM
  *
  */
object WeatherCacheActor {
  def props = Props[WeatherCacheActor]

  object Protocol {
    case class FindByZip(zip : String)
    case class AddReport(report : WeatherReport)
  }
}

class WeatherCacheActor extends Actor {
  def receive = {
    case FindByZip(zip) => sender() ! WeatherCache.findByZip(zip)
    case AddReport(report) => sender() ! WeatherCache.addReport(report)
  }
}
