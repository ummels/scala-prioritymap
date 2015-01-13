package de.ummels.prioritymap

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable._

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
  * Concrete classes may also override other methods for efficiency.
  */
trait PriorityMap[A, B] extends Map[A, B] with PriorityMapLike[A, B, PriorityMap[A, B]] {

  import PriorityMap._

  /** An empty priority map of the same type as this priority map. */
  override def empty = PriorityMap.empty

  override def seq: PriorityMap[A, B] = this

  /** The same priority map with a given default function.
    * Note: `get`, `contains`, `iterator`, `keys`, etc. are not affected by `withDefault`.
    *
    * Invoking transformer methods (e.g. `map`) will not preserve the default value.
    *
    * @param d the function mapping keys to values, used for non-present keys
    * @return  a wrapper of this priority map with a default function
    */
  def withDefault(d: A => B): PriorityMap[A, B] = new WithDefault(this, d)

  /** The same priority map with a given default value.
    * Note: `get`, `contains`, `iterator`, `keys`, etc. are not affected by `withDefaultValue`.
    *
    * Invoking transformer methods (e.g. `map`) will not preserve the default value.
    *
    * @param d default value used for non-present keys
    * @return  a wrapper of this priority map with a default value
    */
  def withDefaultValue(d: B): PriorityMap[A, B] = new WithDefault(this, x => d)

  override def stringPrefix = "PriorityMap"
}

/** This object provides a set of operations needed to create priority maps. */
object PriorityMap extends PriorityMapFactory[PriorityMap] {

  import scala.language.implicitConversions

  def empty[A, B](implicit ord: Ordering[B]): PriorityMap[A, B] = DefaultPriorityMap.empty[A, B]

  implicit def canBuildFrom[A, B](implicit ord: Ordering[B]): CanBuildFrom[Coll, (A, B), PriorityMap[A, B]] =
    new PriorityMapCanBuildFrom[A, B]

  private[prioritymap] trait Default[A, B] extends DefaultMap[A, B] with PriorityMap[A, B] {
    override def +(kv: (A, B)): PriorityMap[A, B] = {
      val b = newBuilder
      b ++= this
      b += kv
      b.result()
    }

    override def -(key: A): PriorityMap[A, B] = {
      val b = newBuilder
      for (kv <- this; if kv._1 != key) b += kv
      b.result()
    }
  }

  private[prioritymap] class WithDefault[A, B](underlying: PriorityMap[A, B], d: A => B) extends
  Map.WithDefault[A, B](underlying, d) with PriorityMap[A, B] {

    def ordering = underlying.ordering

    override def empty = new WithDefault(underlying.empty, d)

    def +(kv: (A, B)): WithDefault[A, B] = new WithDefault(underlying + kv, d)

    override def -(key: A): WithDefault[A, B] = new WithDefault(underlying - key, d)

    def rangeImpl(from: Option[B], until: Option[B]): WithDefault[A, B] =
      new WithDefault(underlying.rangeImpl(from, until), d)

    override def withDefault(d: A => B): PriorityMap[A, B] = new WithDefault(underlying, d)

    override def withDefaultValue(d: B): PriorityMap[A, B] = new WithDefault(underlying, x => d)
  }

}
