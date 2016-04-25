
/**
  *  This is a slightly more advanced sbt setup using two projects.
  *  The first one, "codegen" a customized version of Slick's
  *  code-generator. The second one "main" depends on "codegen", which means
  *  it is compiled after "codegen". "main" uses the customized
  *  code-generator from project "codegen" as a sourceGenerator, which is run
  *  to generate Slick code, before the code in project "main" is compiled.
  */

/** main project containing main source code depending on slick and codegen project */
lazy val root = (project in file("."))
    .settings(sharedSettings)
    .settings(slick := slickCodeGenTask.value) // register manual sbt command)
    .settings(sourceGenerators in Compile += slickCodeGenTask.taskValue) // register automatic code generation on every compile, remove for only manual use)
    .dependsOn(codegen)


/** codegen project containing the customized code generator */
lazy val codegen = project
    .settings(sharedSettings)
    .settings(libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.1.1")


// shared sbt config between main project and codegen project
lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-feature", "-unchecked", "-deprecation"),
  libraryDependencies ++= List(
    "com.typesafe.slick" %% "slick" % "3.1.1",
    "org.slf4j" % "slf4j-nop" % "1.7.10",
    "com.h2database" % "h2" % "1.4.187"
  )
)


// code generation task that calls the customized code generator
lazy val slick = taskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = Def.task {
  val dir = sourceManaged.value
  val cp = (dependencyClasspath in Compile).value
  val r = (runner in Compile).value
  val s = streams.value
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  toError(r.run("demo.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
  val fname = outputDir + "/demo/Tables.scala"
  Seq(file(fname))
}

