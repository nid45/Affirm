package com.affirm.takehome.data

import kotlinx.serialization.Serializable

data class Restaurant(
        open val id: String,
        open val name: String,
        open val image: String,
        open val rating: String
)