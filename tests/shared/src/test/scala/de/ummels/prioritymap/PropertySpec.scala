package de.ummels.prioritymap

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{PropSpecLike, prop, Matchers}

trait PropertySpec extends PropSpecLike with prop.PropertyChecks with Matchers with prop.Configuration {
  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  private val ord1 = Ordering.Tuple2(Ordering.Int, Ordering.Int)
  private val ord2 = Ordering.by[(Int, Int), Int](x => x._1)

  type Key = Int
  type Value = (Int, Int)

  def genOrd: Gen[Ordering[Value]] = Gen.oneOf(ord1, ord2)

  def genKey: Gen[Key] = Gen.choose(-10, 10)

  def genValue: Gen[Value] = Gen.zip(genKey, genKey)

  def genKeyValue: Gen[(Key, Value)] = Gen.zip(genKey, genValue)

  def genPriorityMap: Gen[PriorityMap[Key, Value]] = for {
    ord <- genOrd
    kvs <- Gen.listOf(genKeyValue)
    m = PriorityMap.empty(ord) ++ kvs
    default <- Arbitrary.arbitrary[Option[Value]]
    pred <- Arbitrary.arbitrary[Option[Key => Boolean]]
  } yield (default, pred) match {
      case (None, None) => m
      case (Some(d), None) => m withDefaultValue d
      case (None, Some(p)) => m filterKeys p
      case (Some(d), Some(p)) => (m withDefaultValue d) filterKeys p
    }
}
