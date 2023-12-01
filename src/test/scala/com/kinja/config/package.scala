package com.kinja

import com.typesafe.config._

package object config {
  val testConf: Config = ConfigFactory.load()
}
