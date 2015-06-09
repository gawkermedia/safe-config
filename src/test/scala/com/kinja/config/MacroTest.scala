package com.kinja.config

import com.typesafe.config._

@config(testConf)
object TestConfig {

  val subConfig = nested("sub-config")

  val levelOne = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne

  val otherLevelOne = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne

  val somethingConfig = for {
    conf ← subConfig
    foo ← conf.getInt("foo")
    bar ← conf.getString("bar")
  } yield SomethingConfig(foo, bar)
}

final case class SomethingConfig(foo : Int, bar : String)
