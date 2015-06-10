package com.kinja.config

import com.typesafe.config._

import scala.concurrent.duration.Duration

@safeConfig(testConf)
object TestConfig {

  private val subConfig = getConfig("sub-config")

  // root tests
  val root1 = root.flatMap(_.getInt("int"))
  val root2 : BootupErrors[Int] = root.flatMap(_.getInt("int"))

  // getString tests
  val getString1 = getString("string")
  val getString2 : BootupErrors[String] = getString("string")

  // getInt tests
  val getInt1 = getInt("int")
  val getInt2 : BootupErrors[Int] = getInt("int")

  // getLong tests
  val getLong1 = getLong("long")
  val getLong2 : BootupErrors[Long] = getLong("long")

  // getBoolean tests
  val getBoolean1 = getBoolean("bool")
  val getBoolean2 : BootupErrors[Boolean] = getBoolean("bool")

  // getDouble tests
  val getDouble1 = getDouble("double")
  val getDouble2 : BootupErrors[Double] = getDouble("double")

  // getDuration tests
  val getDuration1 = getDuration("duration")
  val getDuration2 : BootupErrors[Duration] = getDuration("duration")

  // getObject tests
  val getObject1 = getObject("sub-config")
  val getObject2 : BootupErrors[ConfigObject] = getObject("sub-config")

  // getConfig tests
  val getConfig1 = getConfig("sub-config")
  val getConfig2 : BootupErrors[LiftedTypesafeConfig] = getConfig("sub-config")

  // getStringList tests
  val getStringList1 = getStringList("string-list")
  val getStringList2 : BootupErrors[List[String]] = getStringList("string-list")

  // getRawConfig tests
  val getRawConfig1 = getRawConfig("sub-config")
  val getRawConfig2 : BootupErrors[Config] = getRawConfig("sub-config")

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

@safeConfig(testConf)
object EmptyConfig {
}

final case class SomethingConfig(foo : Int, bar : String)
