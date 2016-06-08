package scalaphx.streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.util.ByteString

import scala.concurrent.duration._

/**
  * Getting Started akka-streams example, taken from:
  *
  * http://doc.akka.io/docs/akka/2.4.7/scala/stream/stream-quickstart.html
  *
  * User: terry
  * Date: 6/8/16
  * Time: 11:28 AM
  *
  */
object SimpleStreamExample extends App {

    implicit val system = ActorSystem("SimpleStreamExample")
    implicit val materializer = ActorMaterializer()

    val source = Source(1 to 100)

    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

    println("calculating factorials & writing to disk")

    val result = factorials.map(num => ByteString(s"$num\n")).runWith(FileIO.toPath(Paths.get("factorials.txt")))

    println(s"Writing factorials to console, throttled...")

    val done = factorials.zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num").throttle(1, 250.millisecond, 1, ThrottleMode.shaping).runForeach(println)

    println("Fini!")



}
