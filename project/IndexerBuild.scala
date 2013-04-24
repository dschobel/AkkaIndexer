import sbt._
import sbt.Keys._

object IndexerBuild extends Build {

  lazy val indexer = Project(
    id = "indexer",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Indexer",
      organization := "org.example",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.1",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies ++= Seq("com.typesafe.akka" % "akka-actor_2.10" % "2.1.2",
        "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test")
    )
  )
}
