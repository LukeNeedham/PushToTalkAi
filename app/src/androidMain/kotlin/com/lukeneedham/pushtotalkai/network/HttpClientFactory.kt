package com.lukeneedham.pushtotalkai.network

import com.lukeneedham.common.network.NetworkLogInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets

object HttpClientFactory {
    fun create(): HttpClient = HttpClient(OkHttp) {
        install(WebSockets)
        engine { addInterceptor(NetworkLogInterceptor()) }
    }
}
