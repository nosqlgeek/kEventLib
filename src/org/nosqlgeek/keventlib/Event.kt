package org.nosqlgeek.keventlib

enum class EventType {
    EMPTY,
    ERROR,
    GET,
    SET,
    IO
}

open class Event(val time : Long, val type : EventType, val data : ByteArray, var processed : Boolean = false)

class EmptyEvent() :
        Event(-1, EventType.EMPTY, ByteArray(0), true )

class ErrorEvent(msg : String ) :
        Event (currentTime(), EventType.ERROR, msg.toByteArray(), true)

class SimpleEvent(type : EventType, msg : String ) :
        Event(-1, type, msg.toByteArray())

class TimedEvent(deferredMs : Long,  type : EventType, data : ByteArray) :
        Event(currentTime() + deferredMs, type, data)
