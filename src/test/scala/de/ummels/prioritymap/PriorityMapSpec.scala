package de.ummels.prioritymap

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{Matchers, PropSpec, prop}

/** Spec for immutable priority maps */
class PriorityMapSpec extends PropSpec with prop.PropertyChecks with Matchers {

  type Keys = Int
  type Values = (Int, Int)

  property("size should return the number of elements") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.size shouldBe m.toSeq.size
    }
  }

  property("iterator should return items in priority order") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      val values = m.values.toSeq
      values shouldBe values.sorted
    }
  }

  property("head and tail should match") {
    forAll { (m: PriorityMap[Keys, Values]) =>
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
    forAll { (m: PriorityMap[Keys, Values]) =>
      if (m.nonEmpty) {
        val (l, i) = (m.last, m.init)
        (i.toSeq :+ l) shouldBe m.toSeq
      } else {
        an [NoSuchElementException] should be thrownBy m.last
        an [UnsupportedOperationException] should be thrownBy m.init
      }
    }
  }

  property("+ and ++ should add and replace elements") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      i <- Gen.choose(0, m.size)
    } yield (m, i)) { case (m, i) =>
      val kvs = m.toSeq
      val (xs1, xs2) = kvs.splitAt(i)
      val xs = xs2 ++ xs1
      (m.empty /: xs)(_ + _) shouldBe m
      (m.empty ++ xs) shouldBe m
      (m /: xs)(_ + _) shouldBe m
      (m ++ xs) shouldBe m
    }
  }

  property("updated should behave like +") {
    forAll { (m: PriorityMap[Keys, Values], key: Keys, value: Values) =>
      m updated (key, value) shouldBe m + (key -> value)
    }
  }

  property("merged should merge old and new values") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      keys <- Gen.listOfN(m.size, Arbitrary.arbitrary[Keys])
      vals <- Gen.listOfN(m.size, Arbitrary.arbitrary[Values])
    } yield (m, keys, vals)) { case (m, keys, vals) =>
      val f: (Values, Values) => Values = (v1, v2) => (v1._1 + v2._1, v1._2 min v2._2)
      val kvs = keys zip vals
      val m1 = (m merged kvs)(f)
      val all = m.toSeq ++ kvs
      val m2 = all groupBy (_._1) mapValues (xs => xs map (_._2) reduceLeft f)
      m1 shouldBe m2
    }
  }

  property("- should remove elements") {
    forAll { (m: PriorityMap[Keys, Values], key: Keys, value: Values) =>
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
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      s <- Gen.choose(0, m.size)
      p <- Gen.containerOfN[Set, Keys](s, Gen.oneOf(m.keys.toSeq))
    } yield (m, p)) { case (m, p) =>
      m.filterKeys(p) shouldBe m.filter{ case (k, _) => p(k) }
    }
  }

  property("mapValues should behave like map") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      vals <- Gen.listOfN(m.size, Arbitrary.arbitrary[Int])
      f = Map.empty[Values, Int] ++ (m.values zip vals)
    } yield (m, f)) { case (m, f) =>
      m.mapValues(f) shouldBe m.map{ case (k, v) => (k, f(v)) }
    }
  }

  property("firstKey should optionally return the first key") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.firstKey shouldBe m.keys.headOption
    }
  }

  property("lastKey should optionally return the last key") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.lastKey shouldBe m.keys.lastOption
    }
  }

  property("firstValue should optionally return the first value") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.firstValue shouldBe m.values.headOption
    }
  }

  property("lastValue should optionally return the last value") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.lastValue shouldBe m.values.lastOption
    }
  }

  property("valueSet should return the set of value") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      m.valueSet shouldBe m.values.toSet
    }
  }

  property("range should filter out entries with values outside the given bounds") {
    forAll { (m: PriorityMap[Keys, Values], from: Option[Values], until: Option[Values]) =>
      def isInRange(v: Values): Boolean =
        from.map(f => m.ordering.lteq(f, v)).getOrElse(true) &&
          until.map(u => m.ordering.lt(v, u)).getOrElse(true)
      m.rangeImpl(from, until) shouldBe m.filter { case (_, v) => isInRange(v) }
    }
  }

  property("withDefault should use the default function for keys not in the map") {
    forAll { (m: PriorityMap[Keys, Values],
              defaultMap: Map[Keys, Values],
              defaultValue: Values,
              key: Keys) =>
      val d = defaultMap.withDefaultValue(defaultValue)
      val m1 = m.withDefault(d)
      m1(key) shouldBe m.getOrElse(key, d(key))
      (m1 - key)(key) shouldBe d(key)
    }
  }

  property("withDefaultValue should use the default function for keys not in the map") {
    forAll { (m: PriorityMap[Keys, Values], d: Values, key: Keys, other: Keys) =>
      val m1 = m.withDefaultValue(d)
      m1(key) shouldBe m.getOrElse(key, d)
      (m1 - key)(key) shouldBe d
    }
  }
}
