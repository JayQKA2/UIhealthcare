package vn.edu.usth.uihealthcare.ui.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun launchIO(scope: CoroutineScope, code: () -> Unit) {
    scope.launch {
        withContext(Dispatchers.IO) {
            code()
        }
    }
}