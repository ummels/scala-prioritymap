Immutable prioirty maps for Scala
=================================

Priority maps are similar to sorted maps, but while for sorted maps `iterator` returns an iterator that
produces entries sorted by their keys, calling `iterator` on a priority map returns an iterator that
produces entries sorted by their ''values''. Priority maps also offer several `range` methods, which
return a submap with values inside a given range.

Since calling `head` on a priority map returns a key-value pair with minimal value, priority
maps can also be thought of as a more versatile variant of ''priority queues''.

Usage
-----

The easiest way to instantiate a new priority map is to use the `apply` method in the
PriorityMap companion object.

    scala> val m = PriorityMap('a' -> 1, 'b' -> 2, 'c' -> 0)
    m: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1, b -> 2)

Since priority maps are immutable, updating a key/value pair returns a new map and does
not modify the old map.

    scala> m + ('b' -> -1)
    res1: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(b -> -1, c -> 0, a -> 1)
    
    scala> m
    res2: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1, b -> 2)

In addition to the methods available for maps, priority maps offer methods for obtaining
a submap whose values lie inside a given range.

    scala> m.from(1)
    res3: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(a -> 1, b -> 2)
    
    scala> m.until(2)
    res4: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1)
    
    scala> m.range(1, 2)
    res5: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(a -> 1)
