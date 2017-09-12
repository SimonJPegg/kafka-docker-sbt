package com.kainos.token

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object TokenServer extends App {

  private implicit val system: ActorSystem = ActorSystem("my-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  private implicit val executionContext = system.dispatcher

  private val route: Route =
    pathPrefix("token" / Segment) { pan =>
      get {
        // dumb implementation for demonstration
        val firstSix = pan.substring(0, 6)
        val lastFour = pan.substring(pan.length - 4)
        val responseString = s"$firstSix******$lastFour"
        println(s"Request for PAN: $pan, Token is: $responseString")
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, responseString))
      }
    }

  private val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(route, "tokenserver", 8888)

  println(s"Server online at http://tokenserver:8888/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
