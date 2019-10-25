package org.nosqlgeek.keventlib

import kotlin.concurrent.thread


/**
 * A very simple event loop implementation
 */
class EventLoop(var size : Int ) {

    //Threshold when the buffer should be optimized
    val OPT_THREASHOLD = 0.8


    // We are using 2 queues here
    // -- The event queue is used for non-timed events
    private var eventQueue = EventQueue(size)

    // -- The event buffer is used for events that are deferred to be executed in the future
    private var eventBuffer = EventBuffer(size)


    //The event loop's worker thread
    private  val worker = thread { loop() }



    /**
     * Try to submit the event
     * It can happen that we can't submit as the buffer or queue is full.
     * Then an error event is returned
     */
    fun submit(event : Event) : Event {

        //If this is not a timed event
        return if (event.time <= 0) {

            eventQueue.push(event)

        } else {

            eventBuffer.add(event)
        }
    }



    /**
     * The actual event loop
     *
     */
    private fun loop() {

        while (true) {
            processNext()
        }
    }


    /**
     * Find the next event to process and process it.
     *
     * Returns an empty event if no event could be found or
     * an error event if an error occurred
     */
    private fun processNext() : Event {


        return if (!eventQueue.isEmpty()) {

            process(eventQueue.pop())

        } else {

            process(eventBuffer.findNext())
        }

    }


    /**
     * Process an event
     *
     * Returns either the processed event or an empty event
     */
    private fun process(e : Event) : Event {

        if ( e.type != EventType.EMPTY && e.type != EventType.ERROR) {
            println(e.type.toString() + " : " + e.time + ":" + String(e.data))
            e.processed = true
        }

        return e
    }

}