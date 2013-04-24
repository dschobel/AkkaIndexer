package org.example

import akka.actor._
import akka.pattern.ask
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout


case class NewContent(urls: Seq[String], content: String)
case class UrlDiscovered(url: String)
case class Crawl(url: String)
case object Done


class Listener()  extends Actor with ActorLogging{
  def receive ={
    case Done => {println("saw 10 messages");  context.system.shutdown()}
  }
}

class CrawlerDispatcher(numCrawlers: Int = 1, seedURL: String, listener: ActorRef, contentSink: ActorRef)  extends Actor with ActorLogging{
  //import collection.mutable.Queue[String]

  //val urlQueue = Queue(seed)
//Props(classOf[ActorWithArgs], "arg")
  //val crawlerRouter = context.actorOf(Props(classOf[Crawler],contentSink).withRouter(RoundRobinRouter(numCrawlers)), name = "crawlerRouter")
  val crawlerRouter = context.actorOf(Props(classOf[Crawler]).withRouter(RoundRobinRouter(numCrawlers)), name = "crawlerRouter")

  var count = 0
  def receive ={
    case UrlDiscovered(url) => {
      crawlerRouter ! Crawl(url)

      count += 1//add it the the processing queue
      if(count == 2){
        listener ! Done
      }
    }
  }
}

object FakeHTML {
  val HTML = "<html><body><p>hi</p></body></html>"
  val discoverURLS = List("http://www.google.com/","http://www.yahoo.com")
}

class ContentAggregator() extends Actor with ActorLogging{
  def receive = {
    case NewContent(urls,content) =>  {println("content aggregator got content"); log.debug(s"got content: $content")}
  }
}

class Crawler()  extends Actor with ActorLogging {

  private def parsePage(url: String): (String,Seq[String]) = (FakeHTML.HTML,FakeHTML.discoverURLS)
  
  def crawl(url: String): Unit = {
    val (pageContent, newURLS) = parsePage(url)
    Indexer.GlobalContentSink ! NewContent(newURLS,pageContent)
  }
  
  def receive = {
    case Crawl(url) => {log.info(s"crawler received crawl($url) message");crawl(url)}
  }
}

object Indexer {

  lazy val system = ActorSystem("Indexer")
  lazy val GlobalContentSink = system.actorOf(Props(new ContentAggregator()),name="contentSink")

  def main(args: Array[String]) ={
      val listener = system.actorOf(Props(new Listener()),name="listener")
      val dispatcher = system.actorOf(Props(new CrawlerDispatcher(1,"http://www.example.com",listener, GlobalContentSink)),name="dispatcher")

        println("sent first message")
        dispatcher ! UrlDiscovered("http://www.google.com")
        Thread.sleep(5000)
        println("sent second message")
        dispatcher ! UrlDiscovered("http://www.yahoo.com")
      }
  }
