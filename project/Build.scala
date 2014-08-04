import sbt._
import Keys._
import Tests._

/** 
 *  This is a slightly more advanced sbt setup using two projects.
 *  The first one, "codegen" a customized version of Slick's
 *  code-generator. The second one "main" depends on "codegen", which means
 *  it is compiled after "codegen". "main" uses the customized
 *  code-generator from project "codegen" as a sourceGenerator, which is run
 *  to generate Slick code, before the code in project "main" is compiled.
 */
object stagedBuild extends Build {
  /** main project containing main source code depending on slick and codegen project */
  lazy val mainProject = Project(
    id="main",
    base=file("."),
    settings = sharedSettings ++ Seq(
      slick <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
    )
  ).dependsOn( codegenProject )
  /** codegen project containing the customized code generator */
  lazy val codegenProject = Project(
    id="codegen",
    base=file("codegen"),
    settings = sharedSettings ++ Seq(
      libraryDependencies ++= List(
        "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3"
      )
    )
  )
  
  // shared sbt config between main project and codegen project
  val sharedSettings = Project.defaultSettings ++ Seq(
    scalaVersion := "2.10.3",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.3.170"
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
}
