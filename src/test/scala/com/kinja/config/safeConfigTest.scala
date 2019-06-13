package com.kinja.config

import org.scalatest._

import scala.concurrent.duration.Duration

@SuppressWarnings(Array(
  "org.wartremover.warts.NonUnitStatements",
  "org.wartremover.warts.Throw"
))
class safeConfigTest extends FlatSpec with Matchers {
  "safeConfig" should "handle getBoolean" in {
    TestConfig.getBoolean1 should be(true)
    TestConfig.getBoolean2 should be(true)
  }

  it should "handle getBooleanList" in {
    TestConfig.getBooleanList1 should be(List(true, false))
    TestConfig.getBooleanList2 should be(List(true, false))
  }

  it should "handle getDouble" in {
    TestConfig.getDouble1 should be(3.4)
    TestConfig.getDouble2 should be(3.4)
  }

  it should "handle getDoubleList" in {
    TestConfig.getDoubleList1 should be(List(1.0, 2.0, 3.0))
    TestConfig.getDoubleList2 should be(List(1.0, 2.0, 3.0))
  }

  it should "handle getDuration" in {
    TestConfig.getDuration1.toMillis should be(120000)
    TestConfig.getDuration2.toMillis should be(120000)
  }

  it should "handle getDurationList" in {
    TestConfig.getDurationList1.map(_.toMillis) should be(List(86400000, 120000, 2500))
    TestConfig.getDurationList2.map(_.toMillis) should be(List(86400000, 120000, 2500))
  }

  it should "handle getInt" in {
    TestConfig.getInt1 should be(1)
    TestConfig.getInt2 should be(1)
  }

  it should "handle getIntList" in {
    TestConfig.getIntList1 should be(List(1, 2, 3, 4))
    TestConfig.getIntList2 should be(List(1, 2, 3, 4))
  }

  it should "handle getLong" in {
    TestConfig.getLong1 should be(2147483648L)
    TestConfig.getLong2 should be(2147483648L)
  }

  it should "handle getLongList" in {
    TestConfig.getLongList1 should be(List(2147483648L, 2147483649L, 2147483650L))
    TestConfig.getLongList2 should be(List(2147483648L, 2147483649L, 2147483650L))
  }

  it should "handle getObject" in {
    TestConfig.getObject1.get("string").unwrapped should be("This is a string.")
    TestConfig.getObject2.get("string").unwrapped should be("This is a string.")

    TestConfig.getObject1.get("int").unwrapped should be(1)
    TestConfig.getObject2.get("int").unwrapped should be(1)
  }

  it should "handle getObjectList" in {
    val objects1 = TestConfig.getObjectList1
    val objects2 = TestConfig.getObjectList2

    objects1.map(_.get("a").unwrapped) should be(List(1, 4))
    objects2.map(_.get("b").unwrapped) should be(List(2, 5))
    objects1.map(_.get("c").unwrapped) should be(List(3, 6))
  }

  it should "handle getString" in {
    TestConfig.getString1 should be("This is a string.")
    TestConfig.getString2 should be("This is a string.")
  }

  it should "handle getStringList" in {
    TestConfig.getStringList1 should be(List("a", "b", "c"))
    TestConfig.getStringList2 should be(List("a", "b", "c"))
  }

  it should "handle getRawConfig" in {
    TestConfig.getRawConfig1.getString("string") should be("This is a string.")
    TestConfig.getRawConfig2.getString("string") should be("This is a string.")
  }

  it should "handle missing values" in {
    val errorMessage = try {
      @safeConfig(testConf)
      object MissingConf {
        val foo : BootupErrors[String] = getString("does-not-exist")
        val bar : BootupErrors[Int] = getInt("also-does-not-exist")
      }
      MissingConf
      ""
    } catch {
      case e : BootupConfigurationException => e.getMessage
    }
    errorMessage should be("The following Bootup configuration errors were found: \n\tCould not find key `does-not-exist` in configuration `root`.\n\tCould not find key `also-does-not-exist` in configuration `root`.")
  }

  it should "handle wrong types" in {
    val errorMessage = try {
      @safeConfig(testConf)
      object WrongTypeConf {
        val foo : BootupErrors[Int] = getInt("string")
        val bar : BootupErrors[Duration] = getDuration("object-list")
      }
      WrongTypeConf
      ""
    } catch {
      case e : BootupConfigurationException => e.getMessage
    }
    errorMessage should be("The following Bootup configuration errors were found: \n\tIncorrect type for `string` in configuration `root`. Expected Int.\n\tIncorrect type for `object-list` in configuration `root`. Expected Long.")
  }

  it should "handle classes" in {
    val concreteConf1 = new DepInjTest1
    val concreteConf2 = new DepInjTest2
    val concreteConf3 = new DepInjTest3
    concreteConf1.getBoolean1 should be(true)
    concreteConf2.getBoolean1 should be(true)
    concreteConf3.getBoolean1 should be(true)
  }

  it should "handle classes with arguments" in {
    val concreteConf1 = new ClassArgTest1("bool")
    val concreteConf2 = new ClassArgTest2(testConf)
    concreteConf1.getBoolean1 should be(true)
    concreteConf2.secret should be("This is a string.")
  }

  it should "handle objects with protected mixins" in {
    ProtectedMemberTest.getBoolean1 should be(true)
  }

  it should "BootupErrors.sequence should work" in {
    BootupErrors.sequence(List(BootupErrors(1), BootupErrors(2), BootupErrors(3))).toOption should be(Some(List(1, 2, 3)))
  }
}
