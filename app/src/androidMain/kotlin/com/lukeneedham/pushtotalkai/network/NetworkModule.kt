package com.lukeneedham.pushtotalkai.network

import org.koin.dsl.module

object NetworkModule {
    val bindings = module { single { HttpClientFactory.create() } }
}
