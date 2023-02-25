package com.example.aplicacionrest.clases

import com.google.gson.annotations.SerializedName


data class Results (

  @SerializedName("artist_href" ) var artistHref : String? = null,
  @SerializedName("artist_name" ) var artistName : String? = null,
  @SerializedName("source_url"  ) var sourceUrl  : String? = null,
  @SerializedName("url"         ) var url        : String? = null

)