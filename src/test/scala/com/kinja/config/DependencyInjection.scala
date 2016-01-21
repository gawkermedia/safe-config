package com.kinja.config

import com.typesafe.config._

abstract class DependencyInjection {
  lazy val injectedConfig = ConfigFactory.load()
}
