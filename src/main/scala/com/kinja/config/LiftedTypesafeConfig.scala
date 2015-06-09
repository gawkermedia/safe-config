package com.kinja.config

import com.typesafe.config._

/**
 * A wrapper around TypesafeConfig's Config that lifts Missing and WrongType errors into BootupErrors
 */
final case class LiftedTypesafeConfig private[LiftedTypesafeConfig] (configAndName : (Config, String)) extends AnyVal {

  private def lift[A](name : String, f : String ⇒ A)(implicit ev : reflect.Manifest[A]) : BootupErrors[A] = try {
    BootupErrors(f(name))
  } catch {
    case _ : ConfigException.WrongType ⇒ BootupErrors.failed(s"Incorrect type for `$name`. Expected ${ev.toString}.")
    case _ : ConfigException.Missing   ⇒ BootupErrors.failed(s"Could not find key `$name` in configuration `${configAndName._2}`.")
  }

  def underlying = configAndName._1

  def getString(name : String) = lift(name, underlying.getString)
  def getInt(name : String) = lift(name, underlying.getInt)
  def getLong(name : String) = lift(name, underlying.getLong)
  def getBoolean(name : String) = lift(name, underlying.getBoolean)
  def getDouble(name : String) = lift(name, underlying.getDouble)
  def getObject(name : String) = lift(name, underlying.getObject)
  def getConfig(name : String) = lift(name, underlying.getConfig)
  def getStringList(name : String) = lift(name, underlying.getStringList)
  def getLiftedConfig(name : String) : BootupErrors[LiftedTypesafeConfig] = this.getConfig(name).map(c ⇒ LiftedTypesafeConfig(c, name))
}

object LiftedTypesafeConfig {
  def apply(config : Config, name : String) : LiftedTypesafeConfig = LiftedTypesafeConfig((config, name))
}
