/**
 * We are just using a single Kotlin script for our Unit tests. There are indeed more fancy ways, but Kotlin is coming
 * out of the box with assertions
 */
package org.nosqlgeek.keventlib

import kotlin.test.assertTrue



/**
 * Test if it's possible to add events to the queue
 */
fun testAddToEventQueue() {

    println("-- testAddToEventQueue")

    val queue = EventQueue(1000)


    for (i in 0..100) {
        assertTrue {
            queue.push(SimpleEvent(EventType.SET, "testAddToEventQueue ${i}")) != EmptyEvent()
        }
    }
}


/**
 * Test to add events at the max. border
 */
fun testAddWithSizeToEventQueue() {

    println("-- testAddWithSizeToEventQueue")

    val queue = EventQueue(1000)


    for (i in 0 .. queue.size-1 ) {


        val e : Event = SimpleEvent(EventType.SET, "testAddWithSizeToEventQueue ${i}")

        assertTrue {
            queue.push(e) == e
        }
    }

    assertTrue {

        val e2 : Event = SimpleEvent(EventType.SET, "More than queue size")
        val eErr = queue.push(e2)

        println(String(eErr.data))

        e2 != eErr
    }
}


/**
 * Test to add and remove items from the queue
 */
fun testAddRemoveToFromQueue() {

    println("-- testAddRemoveToFromQueue")

    val queue = EventQueue(10)

    queue.push(SimpleEvent(EventType.SET, "first event"))
    queue.push(SimpleEvent(EventType.SET, "second event"))
    queue.push(SimpleEvent(EventType.SET, "third event"))


    assertTrue { String(queue.pop().data) == "first event" }
    assertTrue { String(queue.pop().data) == "second event"}
    assertTrue { String(queue.pop().data) == "third event"}
}


/**
 * Runs the event loop with some events
 */
fun testRunLoop() {

    val loop = EventLoop(10000)


    for (i in 0 .. 100) {

        //Submit for in 5 seconds
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



//-- Test suites

/**
 * The event queue test suite
 */
fun eventQueueTestSuite() {

    testAddToEventQueue()
    testAddWithSizeToEventQueue()
    testAddRemoveToFromQueue()
}


/**
 * The event buffer test suite
 */
fun eventBufferTestSuite() {
    //TODO
}


fun eventLoopTestSuite() {

    testRunLoop()
}


