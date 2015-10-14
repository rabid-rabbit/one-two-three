name := "one-two-three"

organization := "com.sungevity"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

val geoToolsVersion = "13.2"

resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.geotools" % "gt-main" % geoToolsVersion,
  "org.geotools" % "gt-render" % geoToolsVersion,
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

mainClass in Compile := Some("com.sungevity.Main")
