package com.example.wecon_jetpackcompose

import androidx.compose.runtime.MutableState


data class Message(
    var message: String?,
    var sender: User?, // have to use User object here instead
    var datetime: String?
)
{
    constructor(): this(null, null, null)
// empty constructor  is important to add  because firebase need it
}