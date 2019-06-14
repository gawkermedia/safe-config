import xerial.sbt.Sonatype._

sonatypeProfileName := "com.kinja"

publishMavenStyle := true
useGpg := true

description := "Safe Config provides a safe and convenient wrapper around Typesafe's Config library."
homepage := Some(url("https://github.com/gawkermedia/safe-config"))
licenses := Seq("BSD 3-Clause" -> url("https://github.com/gawkermedia/safe-config/blob/master/LICENSE"))

sonatypeProjectHosting := Some(GitHubHosting("gawkermedia", "safe-config", ""))

developers := List(
  Developer(id = "ClaireNeveu", name = "Claire Neveu", email = "", url = url("https://github.com/ClaireNeveu")),
  Developer(id = "pjrt", name = "Pedro Rodriguez", email = "", url = url("https://github.com/pjrt"))
)

credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype")
pgpSecretRing := file(System.getProperty("SEC_RING", ""))
pgpPublicRing := file(System.getProperty("PUB_RING", ""))
pgpPassphrase := Some(Array(System.getProperty("PGP_PASS", ""): _*))
