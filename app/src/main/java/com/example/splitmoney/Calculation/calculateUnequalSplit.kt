package com.example.splitmoney.Calculation

/**
 * Takes a map of uid -> string amount input
 * Returns map of uid -> parsed amount (Double)
 */
fun parseUnequalSplit(userAmounts: Map<String, String>): Map<String, Double> {
    return userAmounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
}

/**
 * Returns sum of all valid entered amounts
 */
fun calculateTotalEntered(userAmounts: Map<String, String>): Double {
    return userAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
}

/**
 * Returns true if total entered equals the total expense (rounded to 2 decimals)
 */
fun isTotalValid(userAmounts: Map<String, String>, totalAmount: Double): Boolean {
    val totalEntered = calculateTotalEntered(userAmounts)
    return "%.2f".format(totalEntered) == "%.2f".format(totalAmount)
}
