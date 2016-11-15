# Release Notes

#### Version 1.0.0 (15 Nov 2016)

- Add support for Scala 2.12

#### Version 0.5.0 (18 Oct 2015)

- Add Scala.js support.
- Rename `DefaultPriorityMap` to `StandardPriorityMap`.

#### Version 0.3.0 (24 Jan 2015)

- Fixed behavior of default implementation for custom orderings.
- Fixed preservation of default function after calling `withDefault` or `withDefaultValue`.
- Added builder and factory classes.
- Removed `fromMap` method from companion objects.
- Mader inner class `PriorityMap.WithDefault` private.

#### Version 0.2.0 (06 Jan 2015)

- Renamed abstract `range` method to `rangeImpl`.
- Adapted static return types in trait `PriorityMapLike`.
- Made `newBuilder` protected in both `PriorityMap` and `DefaultPriorityMap`.
- Changed static return type of `seq` to `PriorityMap[A, B]`.
- Added methods `firstKey`, `lastKey`, and `merged`.
- Added variants of methods `withDefault` and `withDefaultValue` that return priority maps.
- Optimized performance.

#### Version 0.1.0 (09 Dec 2014)

- Initial release.
