package com.kinja.config

import com.typesafe.config._

@safeConfig(testConf)
object TestConfig {

  private val subConfig = getConfig("sub-config")

  val levelOne = for {
    conf ← root
    levelOne ← conf.getInt("levelone")
  } yield levelOne

  val bar : BootupErrors[String] = root.flatMap(_.getString("sub-config.bar"))

  val _levelOne = getInt("levelone")

  val otherLevelOne : BootupErrors[Int] = for {
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
