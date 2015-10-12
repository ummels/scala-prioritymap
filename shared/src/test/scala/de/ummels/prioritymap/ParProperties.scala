package de.ummels.prioritymap

/** Properties for immutable priority maps that support the `par` operation */
trait ParProperties extends Properties {
  property("par should return an equivalent map") {
    forAll(genPriorityMap) { m =>
      m.par shouldEqual m
    }
  }
}
