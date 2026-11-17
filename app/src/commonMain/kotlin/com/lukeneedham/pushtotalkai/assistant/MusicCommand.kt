package com.lukeneedham.pushtotalkai.assistant

enum class MusicCommand(val argument: String) {
    Play("play"),
    Pause("pause"),
    PlayPause("play_pause"),
    Next("next"),
    Previous("previous");

    companion object {
        fun fromArgument(argument: String?): MusicCommand? = entries.firstOrNull { it.argument == argument }
    }
}
