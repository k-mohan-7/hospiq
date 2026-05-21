package com.simats.hospiq.network

object ApiConfig {
    private const val BASE_IP = "192.168.1.35"
    const val BASE_URL = "http://$BASE_IP/hospiq/"
    const val IMAGE_BASE_URL = "http://$BASE_IP/hospiq/"
    const val TIMEOUT_SECONDS = 30L
}
