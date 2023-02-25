package com.example.aplicacionrest.clases

import com.example.aplicacionrest.clases.Results
import com.google.gson.annotations.SerializedName


data class Neko (

  @SerializedName("results" ) var results : ArrayList<Results> = arrayListOf()

)