val scalaV = "2.13.5"

val catsV = "2.6.1"
val catsEffectV = "2.5.4"
val fs2V = "3.1.5"
val munitV = "0.7.29"
val munitCatsEffectV = "0.3.0"
val log4catsV = "1.3.1"

val kindProjectorV = "0.13.2"
val betterMonadicForV = "0.3.1"

// Projects
lazy val `cats-process` = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(
    scalaVersion := scalaV
  )
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "cats-process",
    scalaVersion := scalaV
  )

lazy val Core = config("core")
lazy val Root = config(".")

lazy val site = project
  .in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(ParadoxSitePlugin)
  .enablePlugins(ParadoxMaterialThemePlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .enablePlugins(PreprocessPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(
    scalaVersion := scalaV,
    makeSite := makeSite.dependsOn(mdoc.toTask("")).value,
    mdocIn := baseDirectory.value / "docs",
    sourceDirectory in (Compile, paradox) := mdocOut.value,
    Compile / paradoxMaterialTheme ~= {
      _.withRepository(uri("https://github.com/cats4scala/cats-process"))
    },
    paradoxProperties += ("project.version.stable" -> previousStableVersion.value.get),
    siteSubdirName in SiteScaladoc := "api"
  )
  .settings(
    SiteScaladocPlugin.scaladocSettings(Root, mappings in (Compile, packageDoc) in core, "api"),
    SiteScaladocPlugin.scaladocSettings(Core, mappings in (Compile, packageDoc) in core, "api/core")
  )
  .dependsOn(core)

// General Settings
lazy val commonSettings = Seq(
  scalaVersion := scalaV,
  crossScalaVersions := Seq(scalaV, "2.12.15"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % kindProjectorV cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % betterMonadicForV),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"         % catsV,
    "org.typelevel" %% "cats-effect"       % catsEffectV,
    "co.fs2"        %% "fs2-core"          % fs2V,
    "co.fs2"        %% "fs2-io"            % fs2V,
    "org.typelevel" %% "log4cats-core"     % log4catsV,
    "org.typelevel" %% "log4cats-slf4j"    % log4catsV,
    "org.typelevel" %% "log4cats-testing"  % log4catsV        % Test,
    "org.scalameta" %% "munit"             % munitV           % Test,
    "org.typelevel" %% "munit-cats-effect" % munitCatsEffectV % Test
  )
)

// General Settings
inThisBuild(
  List(
    organization := "io.github.cats4scala",
    developers := List(
      Developer("JesusMtnez", "Jesús Martínez", "jesusmartinez93@gmail.com", url("https://github.com/JesusMtnez")),
      Developer(
        "pirita",
        "Ignacio Navarro Martín",
        "ignacio.navarro.martin@gmail.com",
        url("https://github.com/pirita")
      ),
      Developer("BeniVF", "Benigno Villa Fernández", "beni.villa@gmail.com", url("https://github.com/BeniVF"))
    ),
    homepage := Some(url("https://github.com/cats4scala/cats-process")),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    pomIncludeRepository := { _ => false },
    testFrameworks += new TestFramework("munit.Framework"),
    scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-sourcepath",
      (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url",
      "https://github.com/cats4scala/cats-process/blob/v" + version.value + "€{FILE_PATH}.scala"
    )
  )
)

addCommandAlias("fmt", """scalafmtSbt;scalafmtAll""")
addCommandAlias("fmtCheck", """scalafmtSbtCheck;scalafmtCheckAll""")
