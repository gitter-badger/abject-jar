sbtPlugin := true

name := "abject-jar"

organization := "au.com.cba.omnia"

version := "0.0.3"

description := "sbt plugin to create a single fat jar, in the legacy hadoop format"

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions := Seq("-unchecked")

publishMavenStyle := false

scalaVersion := "2.10.3"
