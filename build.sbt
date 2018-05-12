name := "slick-ddd-repository"

version := "0.1"

scalaVersion := "2.12.6"

lazy val core = RootProject(uri("https://github.com/noukenolife/ddd-core.git"))
lazy val root = (project in file("."))
  .dependsOn(core)
  .dependsOn(core % "test->test")
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.2.3",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      "com.h2database" % "h2" % "1.4.197" % Test
    )
  )
