package com.opi.lil

import java.nio.file.{Paths, Files}

import com.opi.lil.actors.{SentenceMaster}
import com.opi.lil.actors.WebsiteProtocol.StartIteratingOverWebsites
import com.datastax.driver.core.{ProtocolOptions, Cluster}
import akka.actor.{Props, ActorSystem}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scala.collection.JavaConversions._
import org.apache.spark.streaming.dstream.ConstantInputDStream

object MainApp extends App{

    val system = ActorSystem("Sentence-Extractor-Manager")

    private def config = system.settings.config

    private val cassandraConfig = config.getConfig("akka.main.db.cassandra")
    private val port = cassandraConfig.getInt("port")
    private val hosts = cassandraConfig.getStringList("hosts").toList

//    val conf: SparkConf = new SparkConf().setAppName("scala_streaming_test").set("spark.cassandra.connection.host", "127.0.0.1")
//    val ssc: StreamingContext = new StreamingContext(conf, Seconds(10))

    lazy val dbaseHandler: Cluster =
      Cluster.builder().
        addContactPoints(hosts: _*).
        withCompression(ProtocolOptions.Compression.SNAPPY).
        withPort(port).
        build()

    val m = system.actorOf(Props(new SentenceMaster(dbaseHandler)), name = "Sentence-Master")
    val numberOfActors = 1 //8 //24 // 3 times more than #cores

    m ! StartIteratingOverWebsites(numberOfActors)

    system.awaitTermination()
}
