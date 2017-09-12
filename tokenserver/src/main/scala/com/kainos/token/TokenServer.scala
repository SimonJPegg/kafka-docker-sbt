package com.kainos.token

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object TokenServer extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    pathPrefix("token" / Segment) { pan =>
      get {
        // dumb implementation for demonstration
        val firstSix = pan.substring(0, 6)
        val lastFour = pan.substring(pan.length - 4)
        val responseString = s"$firstSix******$lastFour"
        println(s"Got: $pan, replying with: $responseString")
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, responseString))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "tokenserver", 8888)

  println(s"Server online at http://tokenserver:8888/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
