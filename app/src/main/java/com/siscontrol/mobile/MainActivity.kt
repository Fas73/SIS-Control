package com.siscontrol.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.siscontrol.mobile.core.NfcManager
import com.siscontrol.mobile.presentation.AppNavigation
import com.siscontrol.mobile.presentation.theme.SISControlTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager

    companion object {
        private val _nfcTagFlow = MutableSharedFlow<String>()
        val nfcTagFlow = _nfcTagFlow.asSharedFlow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcManager = NfcManager(this)

        lifecycleScope.launch {
            nfcManager.tagData.collect { tagId ->
                _nfcTagFlow.emit(tagId)
            }
        }

        setContent {
            SISControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            nfcManager.onNewIntent(intent)
        }
    }
}
