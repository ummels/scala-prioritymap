Release Notes
=============

Version 0.2.0
-------------

- Renamed abstract `range` method to `rangeImpl` (BREAKING CHANGE).
- Adapted static return types in trait `PriorityMapLike` (BREAKING CHANGE).
- Made `newBuilder` protected in both `PriorityMap` and `DefaultPriorityMap` (BREAKING CHANGE).
- Changed static return type of `seq` to `PriorityMap[A, B]` (BREAKING CHANGE).
- Added methods `firstKey`, `lastKey`, and `merged`.
- Added variants of methods `withDefault` and `withDefaultValue` that return priority maps.
- Optimized performance.

Version 0.1.0
-------------

- Initial release.
