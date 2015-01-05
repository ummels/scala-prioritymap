package de.ummels.prioritymap

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable._
import scala.collection.mutable

/** A generic trait for immutable priority maps. Concrete classes have to provide
  * functionality for the abstract methods in `PriorityMap`:
  *
  * {{{
  * def get(key: A): Option[B]
  * def iterator: Iterator[(A, B)]
  * def +(kv: (A, B): PriorityMap[A, B]
  * def +[B1 >: B](kv: (A, B1)): Map[A, B1]
  * def -(key: A): PriorityMap[A, B]
  * def rangeImpl(from: Option[B], until: Option[B]): PriorityMap[A, B]
  * implicit def ordering: Ordering[B]
  * }}}
  *
  * The iterator returned by `iterator` should generate key/value pairs in the
  * order specified by the implicit ordering on values.
  *
  * Concrete classes may also override `valueSet`, whose default implementation
  * builds up a new SortedSet from the map's values.
  */
trait PriorityMap[A, B] extends Map[A, B] with PriorityMapLike[A, B, PriorityMap[A, B]] {

  /** An empty priority map of the same type as this priority map. */
  override def empty = PriorityMap.empty

  override def seq: PriorityMap[A, B] = this

  override protected[this] def newBuilder = PriorityMap.newBuilder

  override def stringPrefix = "PriorityMap"
}

/** This object provides a set of operations needed to create priority maps. */
object PriorityMap {

  import scala.language.implicitConversions

  type Coll = PriorityMap[_, _]

  /** An empty priority map. */
  def empty[A, B](implicit ord: Ordering[B]): PriorityMap[A, B] =
    DefaultPriorityMap.empty[A, B]

  /** A priority map that contains the given key/value bindings.
    *
    * @tparam A the key type
    * @tparam B the value type
    * @param kvs the key/value pairs that make up the map
    * @param ord the implicit ordering on values
    * @return a new priority map with the given bindings
    */
  def apply[A, B](kvs: (A, B)*)(implicit ord: Ordering[B]): PriorityMap[A, B] =
    empty[A, B] ++ kvs

  /** A priority map that contains all bindings from the given map.
    *
    * @tparam A the key type
    * @tparam B the value type
    * @param m the map
    * @param ord the implicit ordering on values
    * @return a new priority map with the bindings form the given map
    */
  def fromMap[A, B](m: Map[A, B])(implicit ord: Ordering[B]): PriorityMap[A, B] =
    DefaultPriorityMap.fromMap[A, B](m)

  /** Default Builder for [[PriorityMap]] objects. */
  def newBuilder[A, B](implicit ord: Ordering[B]): mutable.Builder[(A, B), PriorityMap[A, B]] =
    new mutable.MapBuilder[A, B, Map[A, B]](Map.empty) mapResult (fromMap(_))

  implicit def canBuildFrom[A, B](implicit ord: Ordering[B]): CanBuildFrom[Coll, (A, B), PriorityMap[A, B]] =
    new CanBuildFrom[Coll, (A, B), PriorityMap[A, B]] {
      def apply(from: Coll) = newBuilder[A, B]

      def apply() = newBuilder[A, B]
    }

  private[prioritymap] trait Default[A, B] extends DefaultMap[A, B] with PriorityMap[A, B] {
    override def +(kv: (A, B)): PriorityMap[A, B] = {
      val b = newBuilder
      b ++= this
      b += ((kv._1, kv._2))
      b.result()
    }

    override def -(key: A): PriorityMap[A, B] = {
      val b = newBuilder
      for (kv <- this; if kv._1 != key) b += kv
      b.result()
    }
  }
}
