
lazy val secRing: String = System.getProperty("SEC_RING", "")
lazy val pubRing: String = System.getProperty("PUB_RING", "")
lazy val pgpPass: String = System.getProperty("PGP_PASS", "")

pgpSecretRing := file(secRing)
pgpPublicRing := file(pubRing)
pgpPassphrase := Some(Array(pgpPass: _*))
