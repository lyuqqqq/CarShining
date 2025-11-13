package net.uniquecomputer.houseguru.Model

data class AllServicesModel(
    val image: Int,
    val title: String,
    val desc: String = "",
    val duration: String = "",
    val price: String = ""
)
