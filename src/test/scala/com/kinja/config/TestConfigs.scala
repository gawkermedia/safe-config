package com.kinja.config

import com.typesafe.config._

import scala.concurrent.duration.Duration

@safeConfig(testConf)
object TestConfig {

  private val subConfig = getConfig("sub-config")

  // root tests
  val root1 = root.flatMap(_.getInt("int"))
  val root2 : BootupErrors[Int] = root.flatMap(_.getInt("int"))

  // getBoolean tests
  val getBoolean1 = getBoolean("bool")
  val getBoolean2 : BootupErrors[Boolean] = getBoolean("bool")

  // getBooleanList tests
  val getBooleanList1 = getBooleanList("bool-list")
  val getBooleanList2 : BootupErrors[List[Boolean]] = getBooleanList("bool-list")

  // getDouble tests
  val getDouble1 = getDouble("double")
  val getDouble2 : BootupErrors[Double] = getDouble("double")

  // getDoubleList tests
  val getDoubleList1 = getDoubleList("double-list")
  val getDoubleList2 : BootupErrors[List[Double]] = getDoubleList("double-list")

  // getDuration tests
  val getDuration1 = getDuration("duration")
  val getDuration2 : BootupErrors[Duration] = getDuration("duration")

  // getDurationList tests
  val getDurationList1 = getDurationList("duration-list")
  val getDurationList2 : BootupErrors[List[Duration]] = getDurationList("duration-list")

  // getInt tests
  val getInt1 = getInt("int")
  val getInt2 : BootupErrors[Int] = getInt("int")

  // getIntList tests
  val getIntList1 = getIntList("int-list")
  val getIntList2 : BootupErrors[List[Int]] = getIntList("int-list")

  // getLong tests
  val getLong1 = getLong("long")
  val getLong2 : BootupErrors[Long] = getLong("long")

  // getLongList tests
  val getLongList1 = getLongList("long-list")
  val getLongList2 : BootupErrors[List[Long]] = getLongList("long-list")

  // getObject tests
  val getObject1 = getObject("sub-config")
  val getObject2 : BootupErrors[ConfigObject] = getObject("sub-config")

  // getObjectList tests
  val getObjectList1 = getObjectList("object-list")
  val getObjectList2 : BootupErrors[List[ConfigObject]] = getObjectList("object-list")

  // getConfig tests
  val getConfig1 = getConfig("sub-config")
  val getConfig2 : BootupErrors[LiftedTypesafeConfig] = getConfig("sub-config")

  // getString tests
  val getString1 = getString("string")
  val getString2 : BootupErrors[String] = getString("string")

  // getStringList tests
  val getStringList1 = getStringList("string-list")
  val getStringList2 : BootupErrors[List[String]] = getStringList("string-list")

  // getRawConfig tests
  val getRawConfig1 = getRawConfig("sub-config")
  val getRawConfig2 : BootupErrors[Config] = getRawConfig("sub-config")

  val levelOne = for {
    conf <- root
    levelOne <- conf.getInt("levelone")
  } yield levelOne

  val bar : BootupErrors[String] = root.flatMap(_.getString("sub-config.string"))

  val _levelOne = getInt("levelone")

  val otherLevelOne : BootupErrors[Int] = for {
    conf <- root
    levelOne <- conf.getInt("levelone")
  } yield levelOne

  val pureValue = 5

  val somethingConfig : BootupErrors[SomethingConfig] = for {
    conf <- subConfig
    foo <- conf.getInt("int")
    bar <- conf.getString("string")
  } yield SomethingConfig(foo, bar)

  val blockTest1 = {
    val somethingConfig = 5
    levelOne.map(_ + somethingConfig)
  }

  val thirdLevelOne : BootupErrors[Int] = for {
    conf <- root
    levelOne <- conf.getInt("levelone")
  } yield levelOne

  val otherPureValue : Int = 7
  val somePureValue : Option[Int] = Some(8)
}

@safeConfig(testConf)
object EmptyConfig {
}

final case class SomethingConfig(foo : Int, bar : String)

@safeConfig("injectedConfig")
class DepInjTest1() extends DependencyInjection {
  val getBoolean1 = getBoolean("bool")
}

@safeConfig("rawConfig")
class DepInjTest2() extends DependencyInjection {
  private val rawConfig = injectedConfig
  val getBoolean1 = getBoolean("bool")
}

@safeConfig("rawConfig")
class DepInjTest3() extends DependencyInjection {
  private val rawConfig : com.typesafe.config.Config = injectedConfig
  val getBoolean1 = getBoolean("bool")
}

@safeConfig(testConf)
object ProtectedMemberTest extends ProtectedMember {
  val getBoolean1 = getBoolean(someString)
}

@safeConfig(testConf)
class ClassArgTest1(private val someString : String) {
  val getBoolean1 = getBoolean(someString)
}

@safeConfig("rawConfig")
class ClassArgTest2(private val rawConfig : Config) {

  val secret = getString("string")
}
