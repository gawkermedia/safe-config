package com.kinja.config

import org.scalatest._

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
}
