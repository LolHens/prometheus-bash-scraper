name := "prometheus-bash-exporter"
version := {
  val Tag = "refs/tags/(.*)".r
  sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
    .getOrElse("0.0.1-SNAPSHOT")
}

scalaVersion := "2.13.6"

val http4sVersion = "0.22.0"

libraryDependencies ++= Seq(
  "org.graalvm.nativeimage" % "svm" % "21.1.0" % Provided,
  "ch.qos.logback" % "logback-classic" % "1.2.5",
  "io.monix" %% "monix" % "3.4.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "de.lolhens" %% "http4s-monix" % "0.0.1",
  "io.github.vigoo" %% "prox-fs2" % "0.7.1",
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

Compile / doc / sources := Seq.empty

assembly / assemblyJarName := s"${name.value}-${version.value}.sh.bat"

assembly / assemblyOption := (assembly / assemblyOption).value
  .copy(prependShellScript = Some(AssemblyPlugin.defaultUniversalScript(shebang = false)))

assembly / assemblyMergeStrategy := {
  case PathList(paths@_*) if paths.last == "module-info.class" =>
    MergeStrategy.discard

  case PathList("META-INF", "jpms.args") =>
    MergeStrategy.discard

  case PathList("META-INF", "io.netty.versions.properties") =>
    MergeStrategy.first

  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

enablePlugins(
  GraalVMNativeImagePlugin
)

GraalVMNativeImage / name := (GraalVMNativeImage / name).value + "-" + (GraalVMNativeImage / version).value
graalVMNativeImageOptions ++= Seq(
  //"--static",
  "--no-server",
  "--no-fallback",
  "--initialize-at-build-time",
  "--install-exit-handlers",
  "--enable-url-protocols=http,https",
  "--allow-incomplete-classpath" /*logback-classic*/
)
