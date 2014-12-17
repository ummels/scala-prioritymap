package de.ummels.prioritymap

import scala.collection.{mutable, GenTraversableOnce}
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable._

/**
 * Default implementation of immutable priority maps using two maps: a regular map from
 * keys to values and a sorted map from values to sets of keys.
 */
final class DefaultPriorityMap[A, B] private (map: Map[A, B], bags: SortedMap[B, Set[A]])
                                             (implicit val ordering: Ordering[B])
  extends PriorityMap[A, B]
  with PriorityMapLike[A, B, DefaultPriorityMap[A, B]]
  with Serializable {

  def this()(implicit ordering: Ordering[B]) =
    this(Map.empty[A, B], SortedMap.empty[B, Set[A]])

  override def empty = DefaultPriorityMap.empty

  override protected[this] def newBuilder = DefaultPriorityMap.newBuilder

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

  def range(from: Option[B], until: Option[B]): DefaultPriorityMap[A, B]  = {
    val bags1 = bags.rangeImpl(from, until)
    val map1 = map.filterKeys(k => (map.get(k) map bags1.contains) getOrElse true)
    new DefaultPriorityMap[A, B](map1, bags1)
  }

  override def tail = headOption match {
    case None => throw new UnsupportedOperationException("tail of empty map")
    case Some((k, _)) => this - k
  }

  override def init = lastOption match {
    case None => throw new UnsupportedOperationException("init of empty map")
    case Some((k, _)) => this - k
  }

  override def drop(n: Int) = {
    if (n <= 0) this
    else if (n >= size) empty
    else this -- keys.take(n)
  }

  override def take(n: Int) = {
    if (n <= 0) empty
    else if (n >= size) this
    else empty ++ super.take(n)
  }

  override def slice(from: Int, until: Int) = {
    if (until <= from) empty
    else if (from <= 0) take(until)
    else if (until >= size) drop(from)
    else drop(from).take(until - from)
  }

  override def dropRight(n: Int) = take(size - n)

  override def takeRight(n: Int) = drop(size - n)

  override def splitAt(n: Int) = (take(n), drop(n))

  private[this] def countWhile(p: ((A, B)) => Boolean): Int = {
    var result = 0
    val it = iterator
    while (it.hasNext && p(it.next())) result += 1
    result
  }

  override def dropWhile(p: ((A, B)) => Boolean) = drop(countWhile(p))

  override def takeWhile(p: ((A, B)) => Boolean) = take(countWhile(p))

  override def span(p: ((A, B)) => Boolean) = splitAt(countWhile(p))

  private def insert(key: A, value: B): DefaultPriorityMap[A, B] = {
    require(!map.contains(key))
    val bags1 = bags + (value -> (bags.getOrElse(value, Set.empty) + key))
    new DefaultPriorityMap(map updated(key, value), bags1)
  }

  def +(kv: (A, B)): DefaultPriorityMap[A, B] = {
    val (k, v) = kv
    get(k) match {
      case None => insert(k, v)
      case Some(`v`) => this
      case Some(_) => (this - k).insert(k, v)
    }
  }

  override def updated(key: A, value: B): DefaultPriorityMap[A, B] = this + (key -> value)

  override def +(kv1: (A, B), kv2: (A, B), kvs: (A, B)*): DefaultPriorityMap[A, B] =
    this + kv1 + kv2 ++ kvs

  override def ++(kvs: GenTraversableOnce[(A, B)]): DefaultPriorityMap[A, B] =
    (this /: kvs.seq)(_ + _)

  def +[B1 >: B](kv: (A, B1)): Map[A, B1] = map + kv

  def get(key: A): Option[B] = map get key

  def iterator: Iterator[(A, B)] = for {
    (b, bag) <- bags.iterator
    a <- bag.iterator
  } yield (a, b)

  def -(key: A): DefaultPriorityMap[A, B] = get(key) match {
    case None => this
    case Some(v) =>
      val bag = bags(v) - key
      val bags1 = if (bag.isEmpty) bags - v else bags updated(v, bag)
      new DefaultPriorityMap(map - key, bags1)
  }
}

/** This object provides a set of operations needed to create priority maps. */
object DefaultPriorityMap {

  import language.implicitConversions

  type Coll = DefaultPriorityMap[_, _]

  /** An empty priority map. */
  def empty[A, B](implicit ord: Ordering[B]): DefaultPriorityMap[A, B] =
    new DefaultPriorityMap(Map.empty, SortedMap.empty)

  /** A priority map that contains the given key/value bindings.
    *
    * @tparam A the key type
    * @tparam B the value type
    * @param kvs the key/value pairs that make up the map
    * @param ord the implicit ordering on values
    * @return a new priority map with the given bindings
    */
  def apply[A, B](kvs: (A, B)*)(implicit ord: Ordering[B]): DefaultPriorityMap[A, B] =
    DefaultPriorityMap.empty[A, B] ++ kvs

  /** A priority map that contains with the bindings from the given map.
    *
    * @tparam A the key type
    * @tparam B the value type
    * @param m the map
    * @param ord the implicit ordering on values
    * @return a new priority map with the bindings form the given map
    */
  def fromMap[A, B](m: Map[A, B])(implicit ord: Ordering[B]): DefaultPriorityMap[A, B] = {
    val empty = SortedMap.empty[B, Set[A]]
    val bags = (empty /: m)((bs, kv) =>
      bs updated (kv._2, bs.getOrElse(kv._2, Set.empty[A]) + kv._1))
    new DefaultPriorityMap[A, B](m, bags)
  }

  def newBuilder[A, B](implicit ord: Ordering[B]): mutable.Builder[(A, B), DefaultPriorityMap[A, B]] =
    new mutable.MapBuilder[A, B, Map[A, B]](Map.empty) mapResult (fromMap(_))

  implicit def canBuildFrom[A, B](implicit ord: Ordering[B]): CanBuildFrom[Coll, (A, B), DefaultPriorityMap[A, B]] =
    new CanBuildFrom[Coll, (A, B), DefaultPriorityMap[A, B]] {
      def apply(from: Coll) = newBuilder[A, B]

      def apply() = newBuilder[A, B]
    }
}
