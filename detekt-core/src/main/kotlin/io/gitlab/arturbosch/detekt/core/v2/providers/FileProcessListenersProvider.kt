package io.gitlab.arturbosch.detekt.core.v2.providers

import io.gitlab.arturbosch.detekt.api.v2.FileProcessListener
import io.gitlab.arturbosch.detekt.api.v2.ResolvedContext
import io.gitlab.arturbosch.detekt.api.v2.providers.CollectionFileProcessListenerProvider
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import java.util.ServiceLoader

fun interface FileProcessListenersProvider {
    fun get(resolvedContext: Deferred<ResolvedContext>): Flow<FileProcessListener>
}

@OptIn(FlowPreview::class)
class FileProcessListenersProviderImpl(
    private val settings: ProcessingSettings,
) : FileProcessListenersProvider {

    override fun get(resolvedContext: Deferred<ResolvedContext>): Flow<FileProcessListener> {
        return flow {
            emitAll(
                ServiceLoader.load(CollectionFileProcessListenerProvider::class.java, settings.pluginLoader)
                    .asFlow()
                    .flatMapMerge { ruleProvider -> ruleProvider.get(resolvedContext) }
                // TODO I think that we need to sort this list. I'll check it later
            )
        }
    }
}
