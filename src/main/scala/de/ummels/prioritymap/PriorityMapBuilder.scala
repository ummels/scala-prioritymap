package de.ummels.prioritymap

import collection.mutable

/** The canonical builder for priority maps, working with the `+` method
  * to add new elements.
  */
class PriorityMapBuilder[A, B, Coll <: PriorityMap[A, B] with PriorityMapLike[A, B, Coll]](empty: Coll)
extends mutable.MapBuilder[A, B, Coll](empty) {
  override def +=(x: (A, B)): this.type = {
    elems = elems + x
    this
  }
}
