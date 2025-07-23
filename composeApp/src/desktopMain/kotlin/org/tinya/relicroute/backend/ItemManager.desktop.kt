package org.tinya.relicroute.backend

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.tinya.relicroute.Relic
import java.io.InputStream
