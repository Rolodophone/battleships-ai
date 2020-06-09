package net.rolodophone.battleshipsai

import android.graphics.Point
import android.util.Log
import androidx.core.graphics.plus
import kotlin.math.abs

class Ai {
    companion object {
        private const val BOARD_WIDTH = 10
        private const val BOARD_HEIGHT = 10
        private const val NUM_SHIPS = 5
        private const val NUM_LONG_SHIPS = 2
    }

    enum class FireResult {MISS, HIT, SINK}

    private val unknownPoints = mutableSetOf<Point>()

    init {
        for (y in 0 until BOARD_HEIGHT) {
            for (x in 0 until BOARD_WIDTH) {
                if (abs(x - y) % 4 == 0) unknownPoints.add(Point(x, y))
            }
        }
    }

    // > 0 means part of a ship is located there
    private val aiShipsBoard = List(BOARD_HEIGHT) { MutableList(BOARD_WIDTH) { 0 } }

    init {
        Log.d("AI", "Spawning AI's ships...")

        val shipLengths = listOf(5, 4, 3, 3, 2)

        for ((shipIndex, shipLength) in shipLengths.withIndex()) {

            Log.d("AI", "Spawning ship ${shipIndex+1} (length: $shipLength)...")

            lookForShip@ while (true) {

                val x = (0..9).random()
                val y = (0..9).random()
                val (directionX, directionY) = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)).random()

                val shipCoords = mutableListOf<Point>()

                for (i in 0 until shipLength) {
                    val shipCoord = Point(x + (directionX * i), y + (directionY * i))

                    Log.d("AI", "Trying (${shipCoord.x}, ${shipCoord.y})")

                    if (shipCoord.y in aiShipsBoard.indices && shipCoord.x in aiShipsBoard[shipCoord.y].indices // checks that this part of the ship is inside the board
                        && aiShipsBoard[shipCoord.y][shipCoord.x] == 0) { // checks that this part of the ship doesn't overlap with another ship

                        shipCoords.add(shipCoord)
                    }

                    else {
                        continue@lookForShip
                    }
                }

                //at this point shipCoords is filled with valid ship coordinates so I add them to the board
                shipCoords.forEach {
                    aiShipsBoard[it.y][it.x] = shipIndex + 1
                }

                break@lookForShip
            }

            Log.d("AI", "Ship ${shipIndex+1} spawned")
        }

        Log.d("AI", "AI ship configuration:\n${aiShipsBoard.joinToString(separator = "\n") { it.joinToString(separator = "") }}")
        Log.i("AI", "Spawned AI's ships")
    }

    private val whereAiHasFired = mutableSetOf<Point>()

    var targetedShip = mutableSetOf<Point>()
    var shipsSunk = 0
    var longShipsSunk = 0
    lateinit var previousShot: Point
    lateinit var previousOffset: Point

    lateinit var availableXOffsets: MutableSet<Point>
    lateinit var availableYOffsets: MutableSet<Point>

    init { stopTargetingShip() }

    var shipsPlayerHasSunk = 0

    var gameOver = false


    fun receiveShot(x: Int, y: Int): FireResult {
        val shipIndex = aiShipsBoard[y][x]

        return if (shipIndex == 0) FireResult.MISS
        else {
            aiShipsBoard[y][x] = 0

            if (aiShipsBoard.any { it.contains(shipIndex) }) FireResult.HIT
            else {
                shipsPlayerHasSunk++
                if (shipsPlayerHasSunk == NUM_SHIPS) {
                    gameOver = true
                }
                FireResult.SINK
            }
        }
    }


    fun fire(): Point {
        previousShot = when {
            targetedShip.isEmpty() -> fireRandomlyInAPattern()

            targetedShip.size == 1 -> // shoot randomly adjacent to last shot
                generateShotBasedOnOffset((availableXOffsets + availableYOffsets) as MutableSet<Point>) { offset: Point -> targetedShip.single().plus(offset) }

            else -> { // shoot randomly in line with the targeted ship
                val shipIsVertical = targetedShip.first().x == targetedShip.last().x

                generateShotBasedOnOffset(if (shipIsVertical) availableYOffsets else availableXOffsets) { offset: Point ->

                    if (shipIsVertical) {
                        if (previousOffset == Point(0, -1)) targetedShip.minBy { it.y }
                        else targetedShip.maxBy { it.y }
                    } else {
                        if (previousOffset == Point(-1, 0)) targetedShip.minBy { it.x }
                        else targetedShip.maxBy { it.x }
                    }!!
                        .plus(offset)
                }
            }
        }

        whereAiHasFired.add(previousShot)
        return previousShot
    }


    fun observeResult(result: FireResult) {
        when (result) {
            FireResult.MISS -> {
                //if attacking ship and missed remove that offset
                if (targetedShip.isNotEmpty()) {
                    availableXOffsets.remove(previousOffset)
                    availableYOffsets.remove(previousOffset)
                }
            }

            FireResult.HIT -> {
                targetedShip.add(previousShot)
            }

            FireResult.SINK -> {
                if (targetedShip.size >= 4) longShipsSunk++
                if (longShipsSunk == NUM_LONG_SHIPS) {
                    // add other lines so AI can cover the whole board to find the ship of length 2
                    for (y in 0 until BOARD_HEIGHT) {
                        for (x in 0 until BOARD_WIDTH) {
                            if (abs(x - y) % 4 == 2) unknownPoints.add(Point(x, y))
                        }
                    }
                }

                shipsSunk++
                if (shipsSunk >= NUM_SHIPS) {
                    gameOver = true
                }

                stopTargetingShip()
            }
        }
    }


    private fun stopTargetingShip() {
        availableXOffsets = mutableSetOf(Point(-1, 0), Point(1, 0))
        availableYOffsets = mutableSetOf(Point(0, -1), Point(0, 1))
        targetedShip.clear()
    }
    
    
    private fun validateShot(shot: Point): Boolean = shot.y in aiShipsBoard.indices && shot.x in aiShipsBoard[shot.y].indices && shot !in whereAiHasFired


    private fun generateShotBasedOnOffset(availableOffsets: MutableSet<Point>, shotGenerator: (offset: Point) -> Point): Point {
        do {
            if (availableOffsets.isEmpty()) { // this happens if the user makes a mistake
                stopTargetingShip()
                return fireRandomlyInAPattern()
            }

            previousOffset = availableOffsets.random()
            availableOffsets.remove(previousOffset)
            previousShot = shotGenerator(previousOffset)
        }
        while (!validateShot(previousShot))

        return previousShot
    }


    private fun fireRandomlyInAPattern(): Point {
        previousShot = unknownPoints.random()
        unknownPoints.remove(previousShot)

        return previousShot
    }
}