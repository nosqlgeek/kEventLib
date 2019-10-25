package org.nosqlgeek.keventlib

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * An event queue allows to process the events in the same order as they are coming in.
 * We are pushing to the tail of the queue and we are pulling from the head: F(irst) I(n) F(irst) O(ut)
 *
 */
class EventQueue(val size : Int) {


    //Error message in case of an overflow
    private val ERR_MSG = "Queue overflow!"

    //Let's allocate some memory for the queue
    private val queue = arrayOfNulls<Event>(size)

    //Pointers
    private var pushPointer = 0
    private var pullPointer = 0

    //A mutual exclusive lock
    private val mutex = ReentrantLock()


    /**
     * Adds an event to queue of events
     *
     * Returns the event itself if the event could be added to the stack.
     * The only reason why it can't be added to the stack is a stack overflow
     */
    fun push(event : Event) : Event {

        mutex.withLock {

            if (pushPointer < size ) {

                queue[pushPointer] = event
                pushPointer++
                return event
            }
        }

        //Stack overflow
        return ErrorEvent(ERR_MSG)
    }

    /**
     * Get an event from the queue
     *
     * Returns the event that was pulled or an empty event if no event could be pulled from the stack because it was
     * empty
     */
    fun pop() : Event {


        mutex.withLock {

            val event = queue[pullPointer]

            if (event != null) {

                queue[pullPointer] = null
                pullPointer++


                //The queue is empty again
                if (pullPointer == pushPointer) {
                    pushPointer = 0;
                    pullPointer = 0;
                }

                return event

            } else {

                //Kotlin doesn't like null values. The language seems to enforce to not run into null pointer exceptions
                //So we are dealing with empty events instead of null values
                return EmptyEvent()
            }
        }

    }


    /**
     * Checks if the queue is empty
     */
    fun isEmpty() : Boolean {

        return pushPointer == pullPointer
    }

}