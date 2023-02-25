package com.example.aplicacionrest

import com.example.aplicacionrest.clases.Neko
import retrofit2.Call
import retrofit2.http.GET

interface NekoWaifuAPIInterface {
    @GET("neko")
    fun obtenerNekos(): Call<Neko>
}