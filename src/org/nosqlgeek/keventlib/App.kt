package org.nosqlgeek.keventlib


fun main(args: Array<String>) {

    var loop = EventLoop(1000)

    for (i in (0..100000)) {

        val e = Event(currentTime(), EventType.SET, "hello ${i}".toByteArray(), false)

        val submitted = loop.submit(e);


        if (!submitted) {

            println("The loop is too busy. Retrying to submit ...")
            sleep(100)
            val resubmitted = loop.submit(e)


            if (!resubmitted) {
                println("Oh, oh!")
                break;
            }
        }


    }

    Thread.sleep(5000)
}

