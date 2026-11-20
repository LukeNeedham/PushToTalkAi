package com.lukeneedham.pushtotalkai.di

import com.lukeneedham.pushtotalkai.network.HttpClientFactory
import org.koin.dsl.module

object NetworkModule {
    val bindings = module { single { HttpClientFactory.create() } }
}
