RxBroadcast
===========

[![Download from jcenter](https://api.bintray.com/packages/whymarrh/maven/RxBroadcast/images/download.svg)](https://bintray.com/whymarrh/maven/RxBroadcast/_latestVersion)

RxBroadcast is a small distributed event library for Java and the JVM.

See [rxbroadcast.website](http://rxbroadcast.website) for more information.

Quick start
-----------

Gradle dependency:

```groovy
compile 'rxbroadcast:rxbroadcast:1.0.0'
```

Project status
--------------

This library is under development.

Known issues:

- Broadcast implementations are not fault-tolerant
- Broadcast ordering uses unbounded pending queues
- Causal order queues duplicate messages in perpetuity
