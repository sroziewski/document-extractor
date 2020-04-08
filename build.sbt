import TaskProvider.{defaultClass, deploy, deployImpl, remote, remoteFolder, submit}
import sbt._
import sbt.complete.DefaultParsers.spaceDelimited


lazy val commonSettings = Seq(
  organization := "com.opi.lil",
  version := "1.0",
  scalaVersion := "2.11.6",               // desired scala version
  remote := "spark@10.20.20.213",         // host and user name
  remoteFolder := "/home/spark/dev/cc/",  // dest directory of jar files
  defaultClass := "MainApp",
  deploy := deployImpl.value,
  submit := {
    val args: Seq[String] = spaceDelimited("<arg>").parsed
    val className = if (args==Nil) defaultClass.value else args.head
    val jar = new JarData(name.value, version.value, scalaVersion.value)
    Process(s"cmd /C script\\run-java ${remote.value} ${remoteFolder.value} ${jar.fileName()} $className").!
  }
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "sentence-extractor",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.6",
      "com.typesafe.akka" %% "akka-slf4j" % "2.3.6",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "com.sun.jna" % "jna" % "3.0.9",
      "com.datastax.cassandra"  % "cassandra-driver-core" % "2.0.1"  exclude("org.xerial.snappy", "snappy-java"),
      "org.xerial.snappy"       % "snappy-java"           % "1.0.5", //https://github.com/ptaoussanis/carmine/issues/5
      "org.carrot2" % "morfologik-polish" % "1.9.0",
      "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
      "org.apache.spark" %% "spark-core" % "1.6.1" % "provided",
      "org.apache.spark" %% "spark-mllib" % "1.6.1" % "provided",
      "org.apache.spark" %% "spark-sql" % "1.6.1" % "provided",
      "com.sksamuel.elastic4s" % "elastic4s-core_2.11" % "1.7.4",
      "wabisabi" %% "wabisabi" % "2.0.14"
      ),
      resolvers += "gphat" at "https://raw.github.com/gphat/mvn-repo/master/releases/"
  )

mainClass in assembly := Some("com.opi.lil.MainApp")
