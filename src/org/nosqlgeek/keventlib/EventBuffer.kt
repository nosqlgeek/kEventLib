package org.nosqlgeek.keventlib


import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * The difference between the buffer and the queue is that the buffer is not accessed via a FIFO pattern but every
 * event has a dedicated timestamp which is indicating when it should be executed
 */
class EventBuffer(val size : Int) {


    //Error message in case of an overflow
    private val ERR_MSG = "Buffer overflow!"

    //Threshold when the buffer should be optimized
    private val OPT_THREASHOLD = 0.8

    // The event buffer is used for events that are deferred to be executed in the future
    private var eventBuffer = arrayOfNulls<Event>(size)

    //The pointer to the next free slot in the event buffer
    private var eventPointer : Int = 0

    //Using a lock for thread synchronization
    private val mutex = ReentrantLock()


    /**
     * Adds an event to the buffer
     */
    fun add(event : Event) : Event {

        try {

            //Add an event in a threadsafe way
            mutex.withLock {
                eventBuffer[eventPointer] = event
                eventPointer++
            }


            return event

        } catch (e : ArrayIndexOutOfBoundsException) {

            return ErrorEvent(ERR_MSG)
        }
    }

    /**
     * Optimizes the buffer by freeing up space
     */
    private fun optimizeBuffer() {

        mutex.withLock {

            println("Optimizing buffer ... ")
            var tmp = ArrayList<Event>();

            //Remove processed events from the buffer
            for (i in (0..size-1)) {

                var e = eventBuffer[i];


                //If there are non-processed events then remember them
                if (e != null && !e.processed) {

                    tmp.add(e)
                }

                //Empty the buffer slot
                eventBuffer[i] = null;

            }

            //Reference the not yet processed events again
            eventPointer = 0;

            for (j in (0..tmp.size-1)) {

                eventBuffer[j] = tmp[j];
                eventPointer++;
            }
        }
    }

    /**
     * Find the next event which is overdue
     *
     * Returns an empty event if no event was processed
     */
     fun findNext() : Event {

        var result : Event = EmptyEvent()
        var min = -1L


        //Scanning the queue in a threadsafe way
        mutex.withLock {

            //Find the event with the minimum timestamp
            for (e in eventBuffer) {

                //Only process not yet processed events and events that are not in the future
                if (e != null && !e.processed && e.time <= currentTime() ) {

                    if (min == -1L || min > e.time) {
                        min = e.time
                        result = e;
                    }
                }
            }
        }

        //Optimize the buffer from time to time when processing items
        if (eventPointer >= size * OPT_THREASHOLD ) {

            optimizeBuffer();

        }

        return result
    }

}