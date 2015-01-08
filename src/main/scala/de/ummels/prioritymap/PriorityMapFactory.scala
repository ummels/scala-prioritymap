package de.ummels.prioritymap

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

/** A template for companion objects of PriorityMap and subclasses thereof. */
abstract class PriorityMapFactory[CC[A, B] <: PriorityMap[A, B] with PriorityMapLike[A, B, CC[A, B]]] {

  type Coll = CC[_, _]

  /** An empty priority map. */
  def empty[A, B](implicit ord: Ordering[B]): CC[A, B]

  /** The standard builder for priority maps. */
  def newBuilder[A, B](implicit ord: Ordering[B]): mutable.Builder[(A, B), CC[A, B]] =
    new mutable.MapBuilder[A, B, CC[A, B]](empty) {
      override def +=(x: (A, B)): this.type = {
        elems = elems + x
        this
      }
    }

  /** A priority map that contains the given key/value bindings.
    *
    * @tparam A the key type
    * @tparam B the value type
    * @param kvs the key/value pairs that make up the map
    * @param ord the implicit ordering on values
    * @return a new priority map with the given bindings
    */
  def apply[A, B](kvs: (A, B)*)(implicit ord: Ordering[B]): CC[A, B] =
    (newBuilder[A, B](ord) ++= kvs).result()

  class PriorityMapCanBuildFrom[A, B](implicit ord: Ordering[B]) extends
  CanBuildFrom[Coll, (A, B), CC[A, B]] {
    def apply(from: Coll) = newBuilder[A, B]

    def apply() = newBuilder[A, B]
  }

}
