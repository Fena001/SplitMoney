package com.example.splitmoney.Calculation

/**
 * Calculates each user's share for an equally split expense.
 *
 * @param selectedMembers Map of userId to whether they are included in the split.
 * @param totalAmount The total amount of the expense to be split.
 * @return A map of userId to their share (Double).
 */
fun calculateEqualSplit(
    selectedMembers: Map<String, Boolean>,
    totalAmount: Double
): Map<String, Double> {
    val includedUserIds = selectedMembers.filterValues { it }.keys
    val splitCount = includedUserIds.size

    if (splitCount == 0) return emptyMap()

    val share = totalAmount / splitCount

    return includedUserIds.associateWith { share }
}
