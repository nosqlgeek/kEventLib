package org.nosqlgeek.keventlib

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


/**
 * A very simple event loop implementation
 */
class EventLoop(var size : Int ) {

    //Threshold when the buffer should be optimized
    val OPT_THREASHOLD = 0.8
    val RESIZE_ENABLED = false
    val RESIZE_SCALE_FACTOR = 10
    val RESIZE_TIME_MS = 100
    val RESIZE_MAX_COUNT = 1

    // The events of the event loop
    private var eventBuffer = arrayOfNulls<Event>(size)

    //The pointer to the next free slot in the event buffer
    private var eventPointer : Int = 0

    //Using a lock for thread synchronization
    private val mutex = ReentrantLock()

    //Count how often we needed to optimize
    private var lastOpt = currentTime()

    //Number of buffer resizings
    private var numResizes = 0

    //The event loop's worker thread
    private  val worker = thread { loop() }


    /**
     * Resizes the size of the event loop
     */
    fun resetBufferSize( newSize : Int) {

        if (newSize > size) {

            mutex.withLock {
                eventBuffer = eventBuffer.copyOf(newSize)
                size = newSize
            }
        }
    }


    /**
     * Try to submit the event
     * It can happen that we can't submit as the buffer is full.
     * The processing of events implies to optimize the buffer from time to time.
     */
    fun submit(event : Event) : Boolean {


        try {

            //Submitting an event in a threadsafe way
            mutex.withLock {

                eventBuffer[eventPointer] = event;
                eventPointer++;
            }

            return true

        } catch (e : ArrayIndexOutOfBoundsException) {

            return false
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
     * This gives us more flexibility than a simple queue
     *
     * Returns an empty event if no event was processed
     */
    private fun processNext() : Event {

        var result : Event = EmptyEvent()
        var min = -1L


        //Scanning the queue in a threadsafe way
        mutex.withLock {

            //Find the event with the minimum timestamp
            for (e in eventBuffer) {

                if (e != null && !e.processed ) {

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

            //Dynamically resize the buffer
            if (currentTime() - lastOpt >= RESIZE_TIME_MS && numResizes <= RESIZE_MAX_COUNT && RESIZE_ENABLED) {

                println("Resizing buffer ...")

                resetBufferSize(size * RESIZE_SCALE_FACTOR)
                numResizes++;

            }

            lastOpt = currentTime();

        }

        process(result);
        return result
    }


    /**
     * Process an event
     *
     * Returns either the processed event or an empty event
     */
    private fun process(e : Event) : Event {

        if ( e.type != EventType.EMPTY) {
            println(e.type.toString() + " : " + e.time + ":" + String(e.data))
            e.processed = true
        }

        return e

    }

}