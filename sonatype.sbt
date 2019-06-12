import xerial.sbt.Sonatype._

sonatypeProfileName := "com.kinja"

publishMavenStyle := true

licenses := Seq("BSD 3-Clause" -> url("https://github.com/gawkermedia/safe-config/blob/master/LICENSE"))

sonatypeProjectHosting := Some(GitHubHosting("gawkermedia", "safe-config", ""))

developers := List(
  Developer(id = "ClaireNeveu", name = "Claire Neveu", email = "", url = url("https://github.com/ClaireNeveu")),
  Developer(id = "pjrt", name = "Pedro Rodriguez", email = "", url = url("https://github.com/pjrt"))
)

lazy val secRing: String = System.getProperty("SEC_RING", "")
lazy val pubRing: String = System.getProperty("PUB_RING", "")
lazy val pgpPass: String = System.getProperty("PGP_PASS", "")

credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype")
pgpSecretRing := file(secRing)
pgpPublicRing := file(pubRing)
pgpPassphrase := Some(Array(pgpPass: _*))
