package com.kinja.config

import com.typesafe.config.{ Config, ConfigObject }

import scala.concurrent.duration.Duration

trait ConfigApi {

  /** The full configuration as given to the `safeConfig` macro (but lifted). */
  protected def root : BootupErrors[LiftedTypesafeConfig]

  /** Creates a String from the value found at the given name. */
  protected def getString(name : String) : BootupErrors[String] =
    root.flatMap(_.getString(name))

  /** Creates an Int from the value found at the given name. */
  protected def getInt(name : String) : BootupErrors[Int] =
    root.flatMap(_.getInt(name))

  /** Creates n Long from the value found at the given name. */
  protected def getLong(name : String) : BootupErrors[Long] =
    root.flatMap(_.getLong(name))

  /** Creates a Boolean from the value found at the given name. */
  protected def getBoolean(name : String) : BootupErrors[Boolean] =
    root.flatMap(_.getBoolean(name))

  /** Creates a Double from the value found at the given name. */
  protected def getDouble(name : String) : BootupErrors[Double] =
    root.flatMap(_.getDouble(name))

  /** Creates a Duration from the value found at the given name. */
  protected def getDuration(name : String) : BootupErrors[Duration] =
    root.flatMap(_.getDuration(name))

  /** Creates a ConfigObject from the value found at the given name. */
  protected def getObject(name : String) : BootupErrors[ConfigObject] =
    root.flatMap(_.getObject(name))

  /** Creates a sub-configuration from all keys starting with the given prefix. */
  protected def getConfig(name : String) : BootupErrors[LiftedTypesafeConfig] =
    root.flatMap(_.getConfig(name))

  /** Creates a List of Strings from the value found at the given name. */
  protected def getStringList(name : String) : BootupErrors[List[String]] =
    root.flatMap(_.getStringList(name))

  /**
   * As `getConfig` but this returns the underyling Typesafe Config.
   * Missing configuration values will not be correctly handled by the returned config.
   */
  protected def getRawConfig(name : String) : BootupErrors[Config] =
    root.flatMap(_.getRawConfig(name))
}
