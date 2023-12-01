package com.kinja.config

import scala.util.control.NoStackTrace

/**
 * Error thrown by safeConfig when any errors are detected.
 */
class BootupConfigurationException(errors: Seq[ConfigError])
    extends RuntimeException("The following Bootup configuration errors were found: \n\t" + errors.mkString("\n\t"))
    with NoStackTrace
