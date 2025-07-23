package org.tinya.relicroute

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform