# Immutable priority maps for Scala

[![Build Status](https://travis-ci.org/ummels/scala-prioritymap.svg?branch=master)](https://travis-ci.org/ummels/scala-prioritymap)
[![Coverage Status](https://coveralls.io/repos/ummels/scala-prioritymap/badge.svg?branch=master)](https://coveralls.io/r/ummels/scala-prioritymap?branch=master)

Priority maps are similar to sorted maps, but while for sorted maps `iterator` returns an iterator that
produces entries sorted by their keys, calling `iterator` on a priority map returns an iterator that
produces entries sorted by their *values*. Priority maps also offer several `range` methods, which
return a submap with values inside a given range.

Since calling `head` on a priority map returns a key-value pair with minimal value, priority
maps can also be thought of as a more versatile variant of *priority queues*.

This implementation of priority maps has been inspired by
[Mark Engelberg's implentation for Clojure](https://github.com/clojure/data.priority-map).

## Setup

The latest version is 0.3.0 and supports Scala 2.10 and 2.11.

Releases are available from [Maven Central](http://search.maven.org/#search|ga|1|scala-prioritymap).
If you use [sbt](http://www.scala-sbt.org/), simply add the following dependency to your build file:

```scala
libraryDependencies += "de.ummels" %% "scala-prioritymap" % "0.3.0"
```

See the [release notes](RELEASE.md) if you upgrade from an earlier release.

## Usage

The easiest way to instantiate a new priority map is to use the `apply` method in the
PriorityMap companion object.

```scala
scala> import de.ummels.prioritymap.PriorityMap
import de.ummels.prioritymap.PriorityMap

scala> val m = PriorityMap('a' -> 1, 'b' -> 2, 'c' -> 0)
m: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1, b -> 2)
```

Since priority maps are immutable, updating a key/value pair returns a new map and does
not modify the old map.

```scala
scala> m + ('b' -> -1)
res0: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(b -> -1, c -> 0, a -> 1)

scala> m
res1: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1, b -> 2)
```

In addition to the methods available for maps, priority maps offer methods for obtaining
a submap whose values lie inside a given range.

```scala
scala> m.from(1)
res2: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(a -> 1, b -> 2)

scala> m.until(2)
res3: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(c -> 0, a -> 1)

scala> m.range(1, 2)
res4: de.ummels.prioritymap.PriorityMap[Char,Int] = PriorityMap(a -> 1)
```

The full API docs are available [here](http://ummels.github.io/scala-prioritymap/api/current/).
