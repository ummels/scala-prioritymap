package de.ummels.prioritymap

import scala.collection.mutable

/** The canonical builder for immutable priority maps, using the `+` method
  * to add new elements to an empty priority map.
  */
class PriorityMapBuilder[A, B, Coll <: PriorityMap[A, B] with PriorityMapLike[A, B, Coll]](empty: Coll)
extends mutable.MapBuilder[A, B, Coll](empty) {
  override def +=(x: (A, B)): this.type = {
    elems = elems + x
    this
  }
}
