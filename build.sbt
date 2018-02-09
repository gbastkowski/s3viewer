name := "s3viewer"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaV       = "2.5.9"
  val akkaHttpV   = "10.0.11"
  val log4jV      = "2.10.0"
  val scalaTestV  = "3.0.5"
  Seq(
    "com.github.seratch"       %% "awscala"              % "0.6.2",
    "com.typesafe.akka"        %% "akka-actor"           % akkaV,
    "com.typesafe.akka"        %% "akka-stream"          % akkaV,
    "com.typesafe.akka"        %% "akka-testkit"         % akkaV,
    "com.typesafe.akka"        %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"        %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka"        %% "akka-http-testkit"    % akkaHttpV,
    "org.apache.logging.log4j"  % "log4j-api"            % log4jV,
    "org.apache.logging.log4j"  % "log4j-core"           % log4jV,
    "org.apache.logging.log4j"  % "log4j-slf4j-impl"     % log4jV,
    "org.scalatra.scalate"     %% "scalate-core"         % "1.8.0",
    "org.webjars"               % "bootstrap"            % "4.0.0"    % Runtime,
    "org.webjars"               % "jquery"               % "3.3.1"    % Runtime,
    "org.webjars.npm"           % "popper.js"            % "1.13.0"   % Runtime,
    "org.scalatest"            %% "scalatest"            % scalaTestV % Test
  )
}

enablePlugins(DockerPlugin)
mainClass in assembly := Some("net.bastkowski.s3viewer.S3Viewer")

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java"                   ,
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1"            ,
      "-XshowSettings:vm"               ,
      "-Dlog4j2.debug=true"             ,
      "-jar"                            ,
      artifactTargetPath)
    cmd("n4-cd-n4os-storage")
  }
}
