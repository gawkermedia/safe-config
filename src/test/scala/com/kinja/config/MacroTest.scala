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

  val pureValue = 5

  val somethingConfig : BootupErrors[SomethingConfig] = for {
    conf ← subConfig
    foo ← conf.getInt("foo")
    bar ← conf.getString("bar")
  } yield SomethingConfig(foo, bar)

  val blockTest1 = {
    val foo = levelOne
    val somethingConfig = 5
    val bar = otherLevelOne
    levelOne.map(_ + somethingConfig)
  }

  val thirdLevelOne : BootupErrors[Int] = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne

  val otherPureValue : Int = 7
  val somePureValue : Option[Int] = Some(8)
}

final case class SomethingConfig(foo : Int, bar : String)
