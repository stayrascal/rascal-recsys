
import sbt._

object Dependencies {

  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0-SNAP9"
  val mockito = "org.mockito" % "mockito-core" % "2.9.0"

  val commonDeps = Seq(scalaTest % Test, mockito % Test)
}
