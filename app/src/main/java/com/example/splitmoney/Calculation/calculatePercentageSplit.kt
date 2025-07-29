package com.example.splitmoney.Calculation

/**
 * Takes a map of uid -> percentage string
 * Returns a map of uid -> actual amount (based on total)
 */
fun parsePercentageSplit(userPercentages: Map<String, String>, totalAmount: Double): Map<String, Double> {
    return userPercentages.mapValues { (it.value.toDoubleOrNull() ?: 0.0) * totalAmount / 100.0 }
}

/**
 * Returns true if total percentage == 100.0 (rounded to 2 decimals)
 */
fun isPercentageValid(userPercentages: Map<String, String>): Boolean {
    val totalPercentage = userPercentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    return kotlin.math.abs(totalPercentage - 100.0) < 0.01
}

/**
 * Returns total of parsed percentage values (for display)
 */
fun calculateTotalPercentage(userPercentages: Map<String, String>): Double {
    return userPercentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
}
