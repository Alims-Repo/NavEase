package com.alim.navease

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform