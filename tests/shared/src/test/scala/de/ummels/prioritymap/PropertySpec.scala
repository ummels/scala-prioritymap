package de.ummels.prioritymap

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{PropSpecLike, prop, Matchers}

trait PropertySpec extends PropSpecLike with prop.PropertyChecks with Matchers with prop.Configuration {
  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  private val ord1 = Ordering.Tuple2(Ordering.Int, Ordering.Int)
  private val ord2 = Ordering.by[(Int, Int), Int](x => x._1)

  type Keys = Int
  type Values = (Int, Int)

  def genOrd: Gen[Ordering[Values]] = Gen.oneOf(ord1, ord2)

  def genKey: Gen[Keys] = Gen.choose(-10, 10)

  def genValue: Gen[Values] = Gen.zip(genKey, genKey)

  def genKeyValue: Gen[(Keys, Values)] = Gen.zip(genKey, genValue)

  def genPriorityMap: Gen[PriorityMap[Keys, Values]] = for {
    ord <- genOrd
    kvs <- Gen.listOf(genKeyValue)
    m = PriorityMap.empty(ord) ++ kvs
    default <- Arbitrary.arbitrary[Option[Values]]
    pred <- Arbitrary.arbitrary[Option[Keys => Boolean]]
  } yield (default, pred) match {
      case (None, None) => m
      case (Some(d), None) => m withDefaultValue d
      case (None, Some(p)) => m filterKeys p
      case (Some(d), Some(p)) => (m withDefaultValue d) filterKeys p
    }
}
