package com.kinja.config

import com.typesafe.config._

@config(testConf)
object MacroTest {
  val levelOne = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne

  val otherLevelOne = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne
}

