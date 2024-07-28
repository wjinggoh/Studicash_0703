package my.edu.tarc.studicash_0703.Models

import java.util.*

fun String.findFloat(): ArrayList<Float> {
    if (this.isEmpty()) return ArrayList()
    val results = ArrayList<Float>()
    val matchedResults = Regex("[+-]?([0-9]*[.])?[0-9]+").findAll(this)
    for (match in matchedResults) {
        if (match.value.isFloatAndWhole()) results.add(match.value.toFloat())
    }
    return results
}

fun String.firstLine(): String {
    return this.split("\n").firstOrNull() ?: ""
}

private fun String.isFloatAndWhole() = this.matches("\\d*\\.\\d+".toRegex())
