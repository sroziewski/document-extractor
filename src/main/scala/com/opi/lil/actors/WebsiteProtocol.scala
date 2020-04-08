/**
 * Created by Stokowiec on 2015-04-02.
 */
package com.opi.lil.actors

import akka.actor.Status._

object WebsiteProtocol {

  case class StartIteratingOverWebsites(numActors: Int)
  case class ProcessWebsite(website: String)
  case class NumberOfWrittenChunks(count: Int)
  case class ProcessingFinished(url: String, data: Data)
  case class Data(url: String, status: Status, completed: Int, all: Int)
  case class Website(key: String, content: String)

}
