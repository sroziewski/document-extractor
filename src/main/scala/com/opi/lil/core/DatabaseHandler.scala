package com.opi.lil.core

import akka.actor.Status.{Failure, Success}
import com.datastax.driver.core._
import core.{Keyspaces}

/**
 * Created by sroziewski 19/10/1025
 */
class DatabaseHandler(dbaseHandler: Cluster) {

  import com.opi.lil.actors.WebsiteProtocol.Website
  import scala.collection.JavaConversions._

  def parseRow(r: Row): Website = {
    val key = r.getString("key")
    val content = r.getString("content")
    Website(key, content)
  }

  val session: Session = dbaseHandler.connect(Keyspaces.ngramKeyspace)
  val documentsQuery: PreparedStatement = session.prepare("select * from text limit 10;")

  def getWebsites: Iterable[Website] = {
    session.execute(documentsQuery.bind()).map(row => parseRow(row))
  }

}
