

lazy val root = (project in file("."))
    .settings(sharedSettings)
    .settings(
      Seq(
        slick <<= slickCodeGenTask, // register manual sbt command
        sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
      )
    )
    .dependsOn(codegen)


/** codegen project containing the customized code generator */
lazy val codegen = project
    .settings(sharedSettings)
    .settings( libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.1.1" )


// shared sbt config between main project and codegen project
lazy val sharedSettings = Seq(
  scalaVersion := "2.11.7",
  libraryDependencies ++= List(
    "com.typesafe.slick" %% "slick" % "3.1.1",
    "org.slf4j" % "slf4j-nop" % "1.7.10",
    "com.h2database" % "h2" % "1.4.187"
  )
)

// code generation task that calls the customized code generator
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  toError(r.run("demo.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
  val fname = outputDir + "/demo/Tables.scala"
  Seq(file(fname))
}