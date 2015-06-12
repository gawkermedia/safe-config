package com.kinja.config

/**
 * The types of errors that a BootupErrors can catch
 */
sealed trait ConfigError extends Product with Serializable {

  /** The name of the configuration where the error was found. */
  val configName : String

  /** Name of the value that was being accessed when the error was encountered. */
  val valueName : String
}

object ConfigError {

  /** Indicates that the value is simply missing from the configuration. */
  final case class MissingValue(configName : String, valueName : String) extends ConfigError {
    override def toString =
      s"Could not find key `$valueName` in configuration `$configName`."
  }

  /** Indicates that the type expected did not match the actual type of the value. */
  final case class WrongType(
      configName :   String,
      valueName :    String,
      expectedType : String
  ) extends ConfigError {

    override def toString =
      s"Incorrect type for `$valueName` in configuration `$configName`. Expected $expectedType."
  }
}
