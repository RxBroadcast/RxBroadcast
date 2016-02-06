RxBroadcast
===========

RxBroadcast is a distributed event library for Java, built on top of [observable sequences](http://reactivex.io/intro.html) (via [RxJava](https://github.com/ReactiveX/RxJava)).

API
---

By example:

```java
class Bar {
    public int value;

    public Bar(final int value) {
        this.value = value;
    }
}

class Foo {
    public int value;

    public Foo(final int value) {
        this.value = value;
    }
}

class Main {
    public static void main(String[] args) {
        final Broadcast broadcast = new UdpBroadcast("192.168.0.255", 8000);
        broadcast.valuesOfType(Foo.class).subscribe(System.out::println);

        broadcast.send(new Foo(42));

        broadcast.await();
    }
}
```

Quick start
-----------

For Gradle, add the following to your dependencies:

```
compile 'com.whymarrh:rxbroadcast:1.0.0'
```
