package de.ummels.prioritymap

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{PropSpec, Matchers, prop}

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

  property("contained and definedAt should return true/false for keys in/not in the map") {
    forAll { (m: PriorityMap[Keys, Values], key: Keys, v: Values) =>
      (m + (key -> v)).contains(key) shouldBe true
      (m + (key -> v)).isDefinedAt(key) shouldBe true
      (m - key).contains(key) shouldBe false
      (m - key).isDefinedAt(key) shouldBe false
    }
  }

  property("head and tail should match") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      whenever (m.nonEmpty) {
        val (h, t) = (m.head, m.tail)
        (h +: t.toSeq) shouldBe m.toSeq
      }
    }
  }

  property("last and init should match") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      whenever (m.nonEmpty) {
        val (l, i) = (m.last, m.init)
        (i.toSeq :+ l) shouldBe m.toSeq
      }
    }
  }

  property("slice should respect order") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      i <- Gen.choose(0, m.size)
    } yield (m, i)) { case (m, i) =>
      m.slice(0, i).toSeq ++ m.slice(i, m.size).toSeq shouldBe m.toSeq
    }
  }

  property("splitAt should respect order") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      i <- Gen.choose(0, m.size)
    } yield (m, i)) { case (m, i) =>
      val (m1, m2) = m.splitAt(i)
      m1.toSeq ++ m2.toSeq shouldBe m.toSeq
    }
  }

  property("span should respect order") {
    forAll (for {
      m <- Arbitrary.arbitrary[PriorityMap[Keys, Values]]
      i <- Gen.choose(0, m.size)
      p <- Gen.containerOfN[Set, (Keys, Values)](i, Gen.oneOf(m.toSeq))
    } yield (m, p)) { case (m, p) =>
      val (m1, m2) = m.span(p)
      m1.toSeq ++ m2.toSeq shouldBe m.toSeq
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

  property("firstValue should return the minimal value") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      if (m.isEmpty)
        m.firstValue shouldBe None
      else {
        m.values.min shouldBe m.firstValue.get
      }
    }
  }

  property("lastValue should return the maximal value") {
    forAll { (m: PriorityMap[Keys, Values]) =>
      if (m.isEmpty)
        m.lastValue shouldBe None
      else {
        m.values.max shouldBe m.lastValue.get
      }
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
      m.range(from, until) shouldBe m.filter { case (_, v) => isInRange(v) }
    }
  }
}
