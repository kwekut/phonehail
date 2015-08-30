name := """play-silhouette-postgres-async-seed"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += Resolver.jcenterRepo
resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"


libraryDependencies ++= Seq(
  cache,
  ws,
  filters,
  "com.mohiva" %% "play-silhouette" % "3.0.0-RC2",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.kyleu" %% "jdub-async" % "1.0",
  "com.twilio.sdk" % "twilio-java-sdk" % "3.4.5",
  "com.stripe" % "stripe-java" % "1.33.0",
  "org.webjars" % "bootstrap" % "3.3.5"
)

routesGenerator := InjectedRoutesGenerator


fork in run := false