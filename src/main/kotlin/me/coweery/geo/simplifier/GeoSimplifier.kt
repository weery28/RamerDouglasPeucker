package me.coweery.geo.simplifier

import kotlin.math.sqrt

class GeoSimplifier<T>(
    private val xySelector: (T) -> Pair<Double, Double>,
    private val distanceBetweenPoints: (Double, Double, Double, Double) -> Double
) {
    private fun distanceToSegmentSquared(
        px: Double,
        py: Double,
        vx: Double,
        vy: Double,
        wx: Double,
        wy: Double
    ): Double {
        val l2: Double = distanceBetweenPoints(vx, vy, wx, wy)
        if (l2 == 0.0) return distanceBetweenPoints(px, py, vx, vy)
        val t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2
        if (t < 0) return distanceBetweenPoints(px, py, vx, vy)
        return if (t > 1) distanceBetweenPoints(px, py, wx, wy) else distanceBetweenPoints(
            px, py, vx + t * (wx - vx),
            vy + t * (wy - vy)
        )
    }

    private fun perpendicularDistance(px: Double, py: Double, vx: Double, vy: Double, wx: Double, wy: Double): Double {
        return sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy))
    }

    private fun douglasPeucker(
        list: List<T>,
        s: Int,
        e: Int,
        epsilon: Double,
        resultList: MutableList<T>
    ) {
        // Find the point with the maximum distance
        var dmax = 0.0
        var index = 0
        val end = e - 1
        for (i in s + 1 until end) {
            // Point
            val (px, py) = xySelector(list[i])
            // Start
            val (vx, vy) = xySelector(list[s])
            // End
            val (wx, wy) = xySelector(list[end])
            val d = perpendicularDistance(px, py, vx, vy, wx, wy)
            if (d > dmax) {
                index = i
                dmax = d
            }
        }
        // If max distance is greater than epsilon, recursively simplify
        if (dmax > epsilon) {
            // Recursive call
            douglasPeucker(list, s, index, epsilon, resultList)
            douglasPeucker(list, index, e, epsilon, resultList)
        } else {
            if (end - s > 0) {
                resultList.add(list[s])
                resultList.add(list[end])
            } else {
                resultList.add(list[s])
            }
        }
    }

    fun execute(line: List<T>, maxDeviation: Double): List<T> {
        return mutableListOf<T>().apply {
            douglasPeucker(line, 0, line.size, maxDeviation, this)
        }
    }
}