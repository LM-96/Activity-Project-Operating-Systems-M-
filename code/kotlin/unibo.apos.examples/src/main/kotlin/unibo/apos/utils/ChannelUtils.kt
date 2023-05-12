package unibo.apos.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> ReceiveChannel<T>.consumeUntilClosed(block: suspend (T) -> Unit) {
    while(!this.isClosedForReceive) {
        try {
            block(receive())
        } catch (_: ClosedReceiveChannelException) {
            return
        }
    }
}