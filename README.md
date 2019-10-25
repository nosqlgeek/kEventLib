# kEventLib

kEventLib is a light-weighted event loop implementation in  Kotlin. This project has more academical character. The idea is to illustrate how an event loop works. This project gave me also a chance to play a bit around with Kotlin.

## Event

We are defining a generic event as something which can happen at a specific time, has a type and a payload:

```Kotlin
Event(val time : Long, val type : EventType, val data : ByteArray, var processed : Boolean = false)
```

More specific events were derived from `Event`:

* **SimpleEvent**: An event without a specific time. It doesn't matter exactly when such an event should be executed
* **TimedEvent**: An event which allows passing a delay, which means that the event should not be executed before this time is over

Timed events are having a lower priority than non-timed events. So we will execute non-timed events first, but we are considering that timed events are deferred to be executed in the future. 

An excellent example for timed events would be 'disk write' events in Redis or async calls in Node.js. Both projects (Node.js and Redis) are based on event loops.

## Event Queue and Event Buffer

I decided to implement two different structures, dependent on if it is about a non-timed event or a timed event.

* **EventQueue**: We are using the event queue to process the events in the order of their appearance. Node.js is using a stack instead of a queue because calls can be nested, and so a call-stack makes more sense.
* **EventBuffer**: This structure is used to buffer timed events. My naive event-loop works in a way that timed events are only processed after all non-timed events are processed. You could indeed think of more sophisticated scheduling approaches.


## Event Loop

The event loop is a ... loop which runs a function call in a single thread:

```Kotlin
private fun loop() {

   while (true) {
      processNext()
   }
}
```

Processing an event means to check first if the event queue is empty. If not, then we are processing one of the queued events. If it is empty, then we start processing the buffered events. All buffered events that are in the past will be processed, whereby the event with the minimum timestamp (the one which happened earliest) will be processed first.

It can happen that no event can be processed. Then an `EmptyEvent` is returned. It can also happen that an error occurs when submitting an event to the loop. This will return an `ErrorEvent`. Such an error is caused by the fact that either the event loop's queue or buffer is fully utilized. The 'submitter' would then need to implement a back-pressure mechanism.


## Example

Here some example code:

```Kotlin
/**
 * Runs the event loop with some events
 */
fun testRunLoop() {

    val loop = EventLoop(10000)


    for (i in 0 .. 100) {

        loop.submit(SimpleEvent(EventType.SET, "event:1:${i}"))
    }

    for (i in 0 .. 100) {

        //Submit for in 5 seconds
        loop.submit(TimedEvent(5000, EventType.SET, "event:timed:${i}".toByteArray()))
        sleep(100)
    }

    for (i in 0 .. 2000) {

        val e = loop.submit(SimpleEvent(EventType.SET, "event:2:${i}"))

        //Back-preassure
        if (e is ErrorEvent) {
            println(String(e.data))
            sleep(100)
        }
    }

}
```

The execution output (handling an event just prints some details about it) looks like:

```
SET : -1:event:1:0
...
SET : -1:event:1:100
SET : 1572008568056:event:timed:0
...
SET : 1572008573427:event:timed:52
SET : -1:event:2:0
...
SET : -1:event:2:2000
SET : 1572008573530:event:timed:53
...
SET : 1572008578398:event:timed:100

```

Events that are printed with the prefix '-1' are non-timed events. Otherwise, the prefix is the timestamp (to which the event was deferred to).

The source code can be found here: https://github.com/nosqlgeek/kEventLib .
