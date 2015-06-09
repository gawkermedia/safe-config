package com.kinja.config

import com.typesafe.config.Config

trait ConfigApi {
  protected def root : BootupErrors[LiftedTypesafeConfig]

  /**
   * Creates a new, smaller config from the section available at $name.
   */
  protected def nested(name : String) : BootupErrors[LiftedTypesafeConfig] =
    root.flatMap(_.getConfig(name))
}
