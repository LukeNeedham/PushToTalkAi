package com.lukeneedham.pushtotalkai.assistant

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/** The scope of everything the assistant does. One thread, so the assistant's state needs no locks. */
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default.limitedParallelism(1)
}
