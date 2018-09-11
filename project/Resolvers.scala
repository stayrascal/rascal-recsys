import sbt._

object Resolvers {
  lazy val resolverSetting = Seq(
    Resolver.mavenLocal,
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
  )
}
