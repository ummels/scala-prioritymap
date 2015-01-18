package de.ummels.prioritymap

import scala.collection.GenTraversableOnce
import scala.collection.immutable._

/** A template trait for immutable priority maps.
  * To create a concrete priority map, you need to implement the following methods
  * in addition to those of `MapLike`:
  *
  * {{{
  * def +(kv: (A, B): This
  * def rangeImpl(from: Option[B], until: Option[B]): This
  * }}}
  *
  * The iterator returned by `iterator` should generate key/value pairs in the
  * order specified by the implicit ordering on values.
  *
  * Concrete classes may also override other methods for efficiency.
  */
trait PriorityMapLike[A, B, +This <: PriorityMapLike[A, B, This] with PriorityMap[A, B]]
  extends MapLike[A, B, This] { self =>
  implicit def ordering: Ordering[B]

  /** Adds a key/value binding to this priority map.
    *
    * @param kv the key/value pair
    * @return a new priority map with the new binding added to this priority map
    */
  def +(kv: (A, B)): This

  /** Adds a key/value binding to this priority map.
    *
    * @param key the key
    * @param value the value
    * @return a new priority map with the new binding added to this map
    */
  def updated(key: A, value: B): This = this + (key -> value)

  /** Adds two or more key/value bindings to this priority map.
    *
    * @param kv1 the first key/value pair to add
    * @param kv2 the second key/value pair to add
    * @param kvs the remaining key/value pairs to add
    * @return a new priority map with the new bindings added to this map
    */
  def +(kv1: (A, B), kv2: (A, B), kvs: (A, B)*): This =
    this + kv1 + kv2 ++ kvs

  /** Adds a number of key/value bindings to this priority map.
    *
    * @param kvs a traversable object consisting of key/value pairs
    * @return a new priority map with the new bindings added to this map
    */
  def ++(kvs: GenTraversableOnce[(A, B)]): This =
    ((repr: This) /: kvs)(_ + _)

  /** Merges a number of key/value bindings into this priority map.
    *
    * If a key is contained in both this map and the given bindings, computes
    * the new value by applying the given merge function to the existing value
    * and the new value.
    *
    * @param kvs a traversable object consisting of key/value pairs
    * @param f the merge function
    * @return a new priority map with the new bindings merged into this map
    */
  def merged(kvs: GenTraversableOnce[(A, B)])(f: (B, B) => B): This =
    ((repr: This) /: kvs) { (m, kv) =>
      val (k, v2) = kv
      m.get(k) match {
        case None => m + kv
        case Some(v1) => m + (k -> f(v1, v2))
      }
    }

  /** Transforms this map by applying a function to every retrieved value.
    *
    *  @param  f the function used to transform values of this map
    *  @return a new priority map that maps every key of this map
    *          to `f(this(key))`
    */
  def mapValues[C](f: B => C)(implicit ord: Ordering[C]): PriorityMap[A, C] =
    map { case (a, b) => (a, f(b)) }

  /** Returns a new priority map of the same type as this priority map that
    * only contains values between the given optional bounds.
    *
    *  @param from  the lower-bound (inclusive) on values or
    *               `None` if there is no lower bound
    *  @param until the upper-bound (exclusive) on values or
    *               `None` if there is no upper bound
    */
  def rangeImpl(from: Option[B], until: Option[B]): This

  /** Returns a new priority map of the same type as this priority map that
    * only contains values greater than or equal to the given lower bound.
    *
    *  @param from the lower-bound (inclusive) on values
    */
  def from(from: B): This = rangeImpl(Some(from), None)

  /** Returns a new priority map of the same type as this priority map that
    * only contains values smaller than the given upper bound.
    *
    *  @param until the upper-bound (exclusive) on values
    */
  def until(until: B): This = rangeImpl(None, Some(until))

  /** Returns a new priority map of the same type as this priority map that
    * only contains values between the given bounds.
    *
    *  @param from  the lower-bound (inclusive) on values
    *  @param until the upper-bound (exclusive) on values
    */
  def range(from: B, until: B): This = rangeImpl(Some(from), Some(until))

  /** Optionally returns the first key of this priority map. */
  def firstKey: Option[A] = headOption map (_._1)

  /** Optionally returns the last key of this priority map. */
  def lastKey: Option[A] = lastOption map (_._1)

  /** Optionally returns the first value of this priority map. */
  def firstValue: Option[B] = headOption map (_._2)

  /** Optionally returns the last value of this priority map. */
  def lastValue: Option[B] = lastOption map (_._2)

  /** Returns the values of this priority map as a sorted set. */
  def valueSet: SortedSet[B] = SortedSet.empty[B] ++ values

  override protected[this] def newBuilder = new PriorityMapBuilder[A, B, This](empty)

  override def filterKeys(p: A => Boolean): PriorityMap[A, B] =
    new FilteredKeys(p) with PriorityMap.Default[A, B] {
      implicit def ordering: Ordering[B] = self.ordering

      def rangeImpl(from: Option[B], until: Option[B]) =
        self.rangeImpl(from, until).filterKeys(p)
    }
}
