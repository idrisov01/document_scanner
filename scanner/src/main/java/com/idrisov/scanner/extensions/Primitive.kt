package com.idrisov.scanner.extensions


fun Int.percentOf(percent: Int): Double = (this / 100 * percent).toDouble()
