package de.ummels.prioritymap

import scala.collection.parallel.immutable.ParMap
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable._

/** Default implementation of immutable priority maps using a pair of maps. */
final class StandardPriorityMap[A, B] private (map: Map[A, B], bags: SortedMap[B, Set[A]])
                                             (implicit val ordering: Ordering[B])
  extends PriorityMap[A, B]
  with PriorityMapLike[A, B, StandardPriorityMap[A, B]]
  with Serializable {

  def this()(implicit ordering: Ordering[B]) =
    this(Map.empty[A, B], SortedMap.empty[B, Set[A]])

  private def insert(key: A, value: B): StandardPriorityMap[A, B] = {
    //require(!map.contains(key))
    val bags1 = bags + (value -> (bags.getOrElse(value, Set.empty) + key))
    new StandardPriorityMap(map.updated(key, value), bags1)
  }

  private def delete(key: A, value: B): StandardPriorityMap[A, B] = {
    //require(map(key) == value)
    val bag = bags(value) - key
    val bags1 = if (bag.isEmpty) bags - value else bags updated(value, bag)
    new StandardPriorityMap(map - key, bags1)
  }

  def +(kv: (A, B)): StandardPriorityMap[A, B] = {
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
    bag <- bags.valuesIterator
    a <- bag.iterator
  } yield (a, map(a))

  def -(key: A): StandardPriorityMap[A, B] = get(key) match {
    case None => this
    case Some(v) => delete(key, v)
  }

  override def empty: StandardPriorityMap[A, B] = StandardPriorityMap.empty

  override def size: Int = map.size

  override def last: (A, B) = {
    val key = bags.last._2.last
    (key, map(key))
  }

  override def valueSet: SortedSet[B] = bags.keySet

  def rangeImpl(from: Option[B], until: Option[B]): StandardPriorityMap[A, B] = {
    val bags1 = bags.rangeImpl(from, until)
    val map1 = map.filterKeys(k => (map.get(k) map bags1.contains) getOrElse true)
    new StandardPriorityMap[A, B](map1, bags1)
  }

  override def tail: StandardPriorityMap[A, B] = headOption match {
    case None => throw new UnsupportedOperationException("tail of empty map")
    case Some((k, v)) => delete(k, v)
  }

  override def init: StandardPriorityMap[A, B] = lastOption match {
    case None => throw new UnsupportedOperationException("init of empty map")
    case Some((k, v)) => delete(k, v)
  }

  override def par: ParMap[A, B] = map.par
}

/** This object provides a set of operations needed to create priority maps. */
object StandardPriorityMap extends PriorityMapFactory[StandardPriorityMap] {

  import language.implicitConversions

  def empty[A, B](implicit ord: Ordering[B]): StandardPriorityMap[A, B] =
    new StandardPriorityMap[A, B]

  implicit def canBuildFrom[A, B](implicit ord: Ordering[B]):
  CanBuildFrom[Coll, (A, B), StandardPriorityMap[A, B]] =
    new PriorityMapCanBuildFrom[A, B]
}
