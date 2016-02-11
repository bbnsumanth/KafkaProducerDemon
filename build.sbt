name := "KafkaProducerDemon"

version := "1.0"

scalaVersion := "2.11.7"

seq( sbtavro.SbtAvro.avroSettings : _*)

//E.g. put the source where IntelliJ can see it 'src/main/java' instead of 'targe/scr_managed'.
javaSource in sbtavro.SbtAvro.avroConfig <<= (sourceDirectory in Compile)(_ / "java")

resolvers ++=Seq(
  "confluent" at "http://packages.confluent.io/maven/"
)

libraryDependencies ++= Seq(
  "org.apache.avro" % "avro" % "1.8.0",
  "io.confluent" % "kafka-avro-serializer" % "2.0.0",
  "org.apache.kafka" % "kafka_2.11" % "0.9.0.0",
  "org.apache.kafka" % "kafka-clients" % "0.8.2.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "com.typesafe.play" %% "play-ws" % "2.4.3"
)


    