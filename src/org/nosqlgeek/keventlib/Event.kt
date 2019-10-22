package org.nosqlgeek.keventlib

enum class EventType {
    EMPTY,
    GET,
    SET
}

open class Event(val time : Long, val type : EventType, val data : ByteArray, var processed : Boolean = false)

class EmptyEvent() : Event(-1, EventType.EMPTY, ByteArray(0), true )

