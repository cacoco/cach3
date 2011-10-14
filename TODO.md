In Progress
===========
* Cache Activity Observers [to address multi-datacenter and/or multi-object updates]
* Pluggable/Overrideable MemcachedClient Factory

To Do / For Consideration
=========================
These are the items that should be considered for future inclusion in the project.

* Add an annotation field for prefixing an id.
* Remove the keyIndex pattern, in favor of in-line parameter annotations? (As per ssm2?)
* User Velocity (or other) templating language for generating keys. So, order of ops would be Velocity, @CacheKeyMethod, toString()
* Internal / Memcached stats; Opening up notifications to Observers
* Multiple LOGICAL Nodes
* Multiple Operations Annotation/Plugin; accept a Bean name adhering to an Interface
* "Large List" support. (Is this even really needed?)
* JMX Management Interface?

Finished
========
* Cache Disabling
