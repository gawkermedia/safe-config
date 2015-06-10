package com.kinja.config

import com.typesafe.config._

import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

/**
 * A wrapper around TypesafeConfig's Config that lifts Missing and WrongType errors into BootupErrors
 */
final case class LiftedTypesafeConfig private[LiftedTypesafeConfig] (configAndName : (Config, String)) extends AnyVal {

  private def lift[A](name : String, f : String ⇒ A)(implicit ev : reflect.Manifest[A]) : BootupErrors[A] = try {
    BootupErrors(f(name))
  } catch {
    case _ : ConfigException.WrongType ⇒
      BootupErrors.failed(s"Incorrect type for `$name`. Expected ${ev.toString}.")
    case _ : ConfigException.Missing ⇒
      BootupErrors.failed(s"Could not find key `$name` in configuration `${configAndName._2}`.")
  }

  /**
   * The underlying TypesafeConfig.
   */
  def underlying : Config = configAndName._1

  /**
   * Retrieves the value found at the given name as a String.
   */
  def getString(name : String) : BootupErrors[String] = lift(name, underlying.getString)

  /**
   * Retrieves the value found at the given name as an Int.
   */
  def getInt(name : String) : BootupErrors[Int] = lift(name, underlying.getInt)

  /**
   * Retrieves the value found at the given name as a Long.
   */
  def getLong(name : String) : BootupErrors[Long] = lift(name, underlying.getLong)

  /**
   * Retrieves the value found at the given name as a Boolean.
   */
  def getBoolean(name : String) : BootupErrors[Boolean] = lift(name, underlying.getBoolean)

  /**
   * Retrieves the value found at the given name as a Double.
   */
  def getDouble(name : String) : BootupErrors[Double] = lift(name, underlying.getDouble)

  /**
   * Retrieves the value found at the given name as a Duration.
   */
  def getDuration(name : String) : BootupErrors[Duration] =
    lift(name, (s ⇒ underlying.getDuration(s, TimeUnit.MILLISECONDS)))
      .map(ms ⇒ Duration(ms, TimeUnit.MILLISECONDS))

  /**
   * Retrieves the value found at the given name as a ConfigObject.
   */
  def getObject(name : String) : BootupErrors[ConfigObject] = lift(name, underlying.getObject)

  /**
   * Retrieves a sub-config from the values with the given prefix.
   */
  def getConfig(name : String) : BootupErrors[LiftedTypesafeConfig] =
    this.getRawConfig(name).map(c ⇒ LiftedTypesafeConfig(c, name))

  /**
   * Retrieves the value found at the given name as a List of Strings.
   */
  def getStringList(name : String) : BootupErrors[List[String]] =
    lift(name, underlying.getStringList).map(_.asScala.toList)

  /**
   * As `getConfig` but this returns the underyling TypesafeConfig.
   * Missing configuration values will not be correctly handled by the returned config.
   */
  def getRawConfig(name : String) : BootupErrors[Config] = lift(name, underlying.getConfig)
}

object LiftedTypesafeConfig {
  def apply(config : Config, name : String) : LiftedTypesafeConfig =
    LiftedTypesafeConfig((config, name))
}
