package de.ummels.prioritymap

import scala.collection.parallel.immutable.ParMap
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable._

/**
 * Default implementation of immutable priority maps using two maps: a regular map from
 * keys to values and a sorted map from values to sets of keys.
 */
final class DefaultPriorityMap[A, B] private(map: Map[A, B], bags: SortedMap[B, Set[A]])
                                            (implicit val ordering: Ordering[B])
  extends PriorityMap[A, B]
  with PriorityMapLike[A, B, DefaultPriorityMap[A, B]]
  with Serializable {

  def this()(implicit ordering: Ordering[B]) =
    this(Map.empty[A, B], SortedMap.empty[B, Set[A]])

  private def insert(key: A, value: B): DefaultPriorityMap[A, B] = {
    //require(!map.contains(key))
    val bags1 = bags + (value -> (bags.getOrElse(value, Set.empty) + key))
    new DefaultPriorityMap(map.updated(key, value), bags1)
  }

  private def delete(key: A, value: B): DefaultPriorityMap[A, B] = {
    //require(map(key) == value)
    val bag = bags(value) - key
    val bags1 = if (bag.isEmpty) bags - value else bags updated(value, bag)
    new DefaultPriorityMap(map - key, bags1)
  }

  def +(kv: (A, B)): DefaultPriorityMap[A, B] = {
    val (key, value) = kv
    get(key) match {
      case None => insert(key, value)
      case Some(`value`) => this
      case Some(v) => delete(key, v).insert(key, value)
    }
  }

  def +[B1 >: B](kv: (A, B1)): Map[A, B1] = map + kv

  def get(key: A): Option[B] = map get key

  def iterator: Iterator[(A, B)] = for {
    (b, bag) <- bags.iterator
    a <- bag.iterator
  } yield (a, b)

  def -(key: A): DefaultPriorityMap[A, B] = get(key) match {
    case None => this
    case Some(v) => delete(key, v)
  }

  override def empty = DefaultPriorityMap.empty

  override def size = map.size

  override def contains(key: A) = map contains key

  override def isDefinedAt(key: A) = map contains key

  override def last = {
    val greatestVal = bags.lastKey
    (bags(greatestVal).last, greatestVal)
  }

  override def lastOption = if (map.isEmpty) None else Some(last)

  override def lastValue = if (bags.nonEmpty) Some(bags.lastKey) else None

  override def valueSet = bags.keySet

  def rangeImpl(from: Option[B], until: Option[B]): DefaultPriorityMap[A, B] = {
    val bags1 = bags.rangeImpl(from, until)
    val map1 = map.filterKeys(k => (map.get(k) map bags1.contains) getOrElse true)
    new DefaultPriorityMap[A, B](map1, bags1)
  }

  override def tail = headOption match {
    case None => throw new UnsupportedOperationException("tail of empty map")
    case Some((k, v)) => delete(k, v)
  }

  override def init = lastOption match {
    case None => throw new UnsupportedOperationException("init of empty map")
    case Some((k, v)) => delete(k, v)
  }

  override def par: ParMap[A, B] = map.par
}

/** This object provides a set of operations needed to create priority maps. */
object DefaultPriorityMap extends PriorityMapFactory[DefaultPriorityMap] {

  import language.implicitConversions

  def empty[A, B](implicit ord: Ordering[B]): DefaultPriorityMap[A, B] =
    new DefaultPriorityMap[A, B]

  implicit def canBuildFrom[A, B](implicit ord: Ordering[B]): CanBuildFrom[Coll, (A, B), DefaultPriorityMap[A, B]] =
    new PriorityMapCanBuildFrom[A, B]
}
