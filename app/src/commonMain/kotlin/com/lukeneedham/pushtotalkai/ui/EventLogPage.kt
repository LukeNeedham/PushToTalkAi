package com.lukeneedham.pushtotalkai.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/** The page: connects to the ViewModel and the navigation, and delegates all UI. No preview. */
@Composable
fun EventLogPage(onBack: () -> Unit) {
    val viewModel = koinViewModel<EventLogViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventLogPageContent(state = state, onBack = onBack, onClear = viewModel::onClear)
}
