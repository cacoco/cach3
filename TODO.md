These are the items that should be considered for future inclusion in the project.

* Remove the keyIndex pattern, in favor of in-line parameter annotations? (As per ssm?)
* User Velocity (or other) templating language for generating keys. So, order of ops would be Velocity, @CacheKeyMethod, toString()
* Definable Memcached node provider (Enables runtime node switching?)
* Cache Disabling (via JMX Management Interface?)
* Internal / Memcached stats; Opening up notifications to Observers
* Multiple LOGICAL Nodes
* Multiple Operations Annotation/Plugin; accept a Bean name adhering to an Interface
* "Large List" support. (Is this even really needed?)

