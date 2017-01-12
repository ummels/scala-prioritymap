package de.ummels.prioritymap

import scala.collection.breakOut
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{Inspectors, Matchers, PropSpec, prop}

import scala.collection.immutable.SortedSet

/** Properties for immutable priority maps */
trait Properties extends PropertySpec {
  property("apply should create a priority map with the given entries") {
    forAll(genOrd, Gen.listOf(genKeyValue)) { (ord, kvs) =>
      PriorityMap(kvs:_*)(ord) shouldEqual Map(kvs:_*)
      StandardPriorityMap(kvs:_*)(ord) shouldEqual Map(kvs:_*)
    }
  }

  property("size should return the number of elements") {
    forAll(genPriorityMap) { m =>
      m.size shouldBe m.toSeq.size
    }
  }

  property("iterator should return items in priority order") {
    forAll(genPriorityMap) { m =>
      val values = m.values.toSeq
      whenever(values.length > 1) {
        Inspectors.forAll(values zip values.tail) { case (v1, v2) =>
          m.ordering.lteq(v1, v2) shouldBe true
        }
      }
    }
  }

  property("head and tail should match") {
    forAll(genPriorityMap) { m =>
      if (m.nonEmpty) {
        val (h, t) = (m.head, m.tail)
        (h +: t.toSeq) shouldBe m.toSeq
      } else {
        an [NoSuchElementException] should be thrownBy m.head
        an [UnsupportedOperationException] should be thrownBy m.tail
      }
    }
  }

  property("last and init should match") {
    forAll(genPriorityMap) { m =>
      if (m.nonEmpty) {
        val (l, i) = (m.last, m.init)
        (i.toSeq :+ l) shouldBe m.toSeq
      } else {
        an [NoSuchElementException] should be thrownBy m.last
        an [UnsupportedOperationException] should be thrownBy m.init
      }
    }
  }

  property("+ should behave like ++") {
    forAll(genPriorityMap, genKeyValue, Gen.listOf(genKeyValue)) { (m, kv, kvs) =>
      m + kv shouldBe m ++ Seq(kv)
      (m /: kvs)(_ + _) shouldBe m ++ kvs
      m + (kv, kv, kvs:_*) shouldBe m ++ (kv :: kvs)
    }
  }

  property("+ should add or replace entries") {
    forAll(genPriorityMap, genKey, genValue) { (m, k, v) =>
      val m1 = m + (k -> v)
      m1(k) shouldBe v
      m1 - k shouldBe m - k
    }
  }

  property("+ should fall back to Map's + for other value types") {
    forAll(genPriorityMap, genKey, genValue) { (m, k, v) =>
      m + (k -> v._1) shouldBe Map(m.toSeq:_*) + (k -> v._1)
    }
  }

  property("updated should behave like +") {
    forAll(genPriorityMap, genKey, genValue) { (m, key, value) =>
      m updated(key, value) shouldBe m + (key -> value)
    }
  }

  property("merged should merge old and new values") {
    forAll(for {
      m <- genPriorityMap
      keys <- Gen.listOfN(m.size, genKey)
      vals <- Gen.listOfN(m.size, genValue)
      f <- Arbitrary.arbitrary[(Value, Value) => Value]
    } yield (m, keys, vals, f)) { case (m, keys, vals, f) =>
      val kvs = keys zip vals
      val m1 = (m merged kvs)(f)
      val all = m.toSeq ++ kvs
      val m2 = all groupBy (_._1) mapValues (kvs => kvs map (_._2) reduceLeft f)
      m1 shouldEqual m2
    }
  }

  property("- should remove elements") {
    forAll(genPriorityMap, genKey, genValue) { (m, key, value) =>
      val m1 = m - key
      if (!(m contains key)) {
        (m + (key -> value) - key) shouldBe m
        m1 shouldBe m
      }
      else {
        m1 should not contain key
        m1 + (key -> m(key)) shouldBe m
      }
    }
  }

  property("filterKeys should behave like filter") {
    forAll(genPriorityMap, Arbitrary.arbitrary[Key => Boolean]) { (m, p) =>
      m.filterKeys(p) shouldBe m.filter { case (k, _) => p(k) }
    }
  }

  property("mapValues should behave like map") {
    forAll(genPriorityMap, Arbitrary.arbitrary[Value => Int]) { (m, f) =>
      m.mapValues(f) shouldBe m.map { case (k, v) => (k, f(v)) }
    }
  }

  property("firstKey should optionally return the first key") {
    forAll(genPriorityMap) { m =>
      m.firstKey shouldBe m.keys.headOption
    }
  }

  property("lastKey should optionally return the last key") {
    forAll(genPriorityMap) { m =>
      m.lastKey shouldBe m.keys.lastOption
    }
  }

  property("firstValue should optionally return the first value") {
    forAll(genPriorityMap) { m =>
      m.firstValue shouldBe m.values.headOption
    }
  }

  property("lastValue should optionally return the last value") {
    forAll(genPriorityMap) { m =>
      m.lastValue shouldBe m.values.lastOption
    }
  }

  property("valueSet should return the set of values") {
    forAll(genPriorityMap) { m =>
      val s = m.valueSet
      val ord = m.ordering
      s shouldBe SortedSet.empty(ord) ++ m.values
      s.ordering shouldBe ord
    }
  }

  property("range should filter out entries with values outside the given bounds") {
    forAll(genPriorityMap, genValue, genValue) { (m, from, until) =>
      val ord = m.ordering
      m.range(from, until) shouldBe m.filter { case (_, v) => ord.lteq(from, v) && ord.lt(v, until) }
    }
  }

  property("from should filter out entries with small values") {
    forAll(genPriorityMap, genValue) { (m, from) =>
      m.from(from) shouldBe m.filter { case (_, v) => m.ordering.lteq(from, v) }
    }
  }

  property("until should filter out entries with big values") {
    forAll(genPriorityMap, genValue) { (m, until) =>
      m.until(until) shouldBe m.filter { case (_, v) => m.ordering.lt(v, until) }
    }
  }

  property("withDefault should use the default function for keys not in the map") {
    forAll(genPriorityMap, genKey, Arbitrary.arbitrary[Key => Value]) { (m, key, d) =>
      val m1 = m.withDefault(d)
      m1(key) shouldBe m.getOrElse(key, d(key))
      (m1 - key)(key) shouldBe d(key)
      m1.empty(key) shouldBe d(key)
      m1.filterKeys(_ != key)(key) shouldBe d(key)
    }
  }

  property("withDefaultValue should use the default function for keys not in the map") {
    forAll(genPriorityMap, genKey, genValue) { (m, key, d) =>
      val m1 = m.withDefaultValue(d)
      m1(key) shouldBe m.getOrElse(key, d)
      (m1 - key)(key) shouldBe d
      m1.empty(key) shouldBe d
      m1.filterKeys(_ != key)(key) shouldBe d
    }
  }

  property("breakOut should be able to yield a builder for priority maps") {
    forAll(Gen.listOf(genKey)) { keys =>
      val m1: PriorityMap[Key, Key] = keys.map(k => k -> k)(breakOut)
      m1.keys.toSet shouldBe keys.toSet
      val m2: StandardPriorityMap[Key, Key] = keys.map(k => k -> k)(breakOut)
      m2.keys.toSet shouldBe keys.toSet
    }
  }
}
