name := "Parser OnAndroid GitHub repository"

version := "0.0.1"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.2.0"

ideaExcludeFolders ++= Seq (
  ".idea",
  ".idea_modules",
  "target"
)

