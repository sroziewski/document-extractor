package com.opi.lil.actors

import java.io.StringReader
import java.nio.file.{Path, Paths, Files}
import java.text.BreakIterator
import java.util
import java.util.Locale

import akka.actor.SupervisorStrategy.Restart
import akka.util.Timeout
import com.datastax.driver.core.ResultSet
import com.google.common.util.concurrent.{ListenableFuture, FutureCallback, Futures}
import com.opi.lil.actors.WebsiteProtocol._
import com.opi.lil.core.{DatabaseHandler}
import com.opi.lil.gz.{GzFileIterator}
import com.opi.lil.utils.Iterator.transform
import com.opi.lil.utils.SpellCorrector
import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.pipeline.CoreNLPProtos
import edu.stanford.nlp.pipeline.CoreNLPProtos.{Sentence, Document}
import edu.stanford.nlp.process.DocumentPreprocessor
import org.elasticsearch.common.settings.Settings
import wabisabi.Client
import com.ning.http.client.Response

import scala.concurrent.{Await, Promise, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import akka.actor.Status._
import com.sksamuel.elastic4s.ElasticClient

class SentenceWorker(dbaseHandler: DatabaseHandler, checker: ActorRef) extends Actor with ActorLogging {
  import CheckerProtocol._
  import scala.concurrent.duration._

  implicit val askTimeout = Timeout(5 seconds)

  private var counterOfWrittenChunks: Int = 0
  val corpus = "/home/szymon/data/sets/nkjp/nkjp-corpus.txt"
//  val corpus = "D:\\data\\corpus\\nkjp-corpus.txt"
  val sp = new SpellCorrector(corpus)
  val wordIterator: BreakIterator = BreakIterator.getSentenceInstance(Locale.US)
  val client = new Client("http://10.20.20.213:9200")
  val result: Future[Response] = client.search(
    index = "foo",
    query = "{\"query\": { \"match_all\": {} }"
  )
//  val client = ElasticClient.remote("10.20.20.213", 9200)

  def receive = {
    case ProcessWebsite(website:String) =>
      processWebsite(website)
    case NumberOfWrittenChunks(count: Int) =>
      log.info(s"FileWorker ${self} got the number of found chunks: ${count}")
      counterOfWrittenChunks = count
    case Success(value) =>
      log.info( s"Record ${value} has been written to DB")
  }

  private def processWebsite(website: String) = {
    import ExecutionContext.Implicits.global


    log.info(s"Processing website ${website} ...")

    val originalSender = sender

//    val sentences: Array[String] = website.split("(?<=[.?!])\\s+(?=[a-zA-Z])").filter(_.split("\\s+").size>2)


//    log.info(sentences.toString)
//    sentences.foreach(checker ! CheckMe(_, dbaseHandler, self, sp))
    checker ! CheckMe(website, dbaseHandler, self, sp)
//    sentences.par.foreach(checker ! CheckMe(_, dbaseHandler, self, sp))

//    if (isNotProcessed(url)) {
//      log.info(s"FileWorker ${self} is processing url: ${url}")
//      val fileName = "/tmp/cc/"+url.split("/").last
//      downloadFile(url, fileName)
//      val it = GzFileIterator(fileName)
//      val fs: List[Future[(WARCMessage, Status)]] = transform(it){
//        line => line.contains("WARC/1.0")}{
//        lines =>
//          val msg = BouncerProtocol.buildWARCMessage(lines)
//          val me = self
//          bouncerRouter ? PleaseLetMeIn(msg, dbaseHandler, url, me) map {
//            case _ => (msg, Success())
//          } recover {
//            case e: Exception => (msg, Failure(e))
//          }
//      }
//
//      Thread sleep 200000
//
//      val f: Future[List[(WARCMessage, Status)]] = Future.sequence(fs)
//
//      f onSuccess  {
//        case result => {
////          Thread sleep 200000
////          var completed: Int = 0
//          val completed = countCompleted(result)
//          log.info(s"[Success] List size: ${result.size}, successCount: $completed")
//          originalSender ! ProcessingFinished(url, Data(url, Success(), completed, result.size))
//        }
//      }
//
//      f onFailure{
//        case e => {
//          log.info(s"[Failure] exception: $e)")
//          originalSender ! ProcessingFinished(url, Data(url, Failure(e), 0,0))
//        }
//      }
//    }
  }

  //TODO: Make it non-blocking
//  private def isNotProcessed(url: String) = {
//    val results = dbaseHandler.getURLData(url)
//    results.isEmpty || results.head.status.isInstanceOf[Failure]
//  }

  private def countCompleted(result:List[(WARCMessage, Status)]): Int = {
    result.map(_._2 match {
      case Success(_) => true
      case _ => false
    }).filter(x=>true).size
  }

  override def postStop(): Unit = {
    log.info(s"Worker actor is stopped: ${self}")
  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10,
    withinTimeRange = 10 seconds) {
    case _: Exception =>
      log.info(s"FileWorker actor is retrying: ${self}")
      Restart
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    import com.opi.lil.utils.Bool._

    log.error(reason, "FileWorker restarting due to [{}] when processing [{}]",
      reason.getMessage(), message.isDefined ?> message.get | "")

    context.children foreach { child =>
      context.unwatch(child)
      context.stop(child)
    }
    postStop()
  }

  private[this] def downloadFile(url: String, destination: String) = {
    import sys.process._
    import java.net.URL
    import java.io.File

    new URL(url) #> new File(destination)!!
  }

}
