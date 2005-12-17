QU8AX - Quick UTF-8 API for XML
===============================

This API seeks to make XML processing faster. Two major concepts support
this goal: Handling all character data directly in UTF-8, and modeling
attributes as single events.

Modeling attributes as events avoids the container classes needed to pass
a set of attributes for an element. Furthermore, it makes the combination
of multiple attribute filters usually easier.
