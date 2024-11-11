package de.ams.techday.aionmobilelitert.reinforcementlearning

import androidx.compose.runtime.Immutable
import kotlin.random.Random

const val NUMBER_OF_CELLS = 8

@Immutable
data class UiState(
    val displayPlayerBoard: List<List<Int>> = placeShip(board = EMPTY_BOARD()),
    val displayAgentBoard: List<List<Int>> = placeShip(board = EMPTY_BOARD()),
    val hiddenPlayerBoard: List<List<Int>> = EMPTY_BOARD(),
    val agentHits: Int = 0,
    val playerHits: Int = 0,
    val numberCells: Int = NUMBER_OF_CELLS,
    // a plane occupies 8 cells - each player needs 8 hits to eliminate the plane and win the game
    val theEnd: Boolean = agentHits == numberCells || playerHits == numberCells
)

// represents each cell on the board
internal data class Cell(val value: Int) {
    companion object {
        const val EMPTY = 0
        const val PLANE = 2
        const val HIT = 1
        const val MISS = -1
    }
}

internal enum class Orientation {
    HeadingRight,
    HeadingUp,
    HeadingLeft,
    HeadingDown
}

// Create the empty board(8x8) using a 2D array
internal val EMPTY_BOARD = {
    List(NUMBER_OF_CELLS) {
        List(NUMBER_OF_CELLS) { Cell.EMPTY }
    }
}

internal fun placeShip(
    board: List<List<Int>>
): List<List<Int>> {

    val resultBoard = board.map { it.toMutableList() }
    val orientation = Orientation.entries.random()
    val boardSize = resultBoard.size

    val planeCoreX: Int
    val planeCoreY: Int

    when (orientation) {
        Orientation.HeadingRight -> {
            planeCoreX = Random.nextInt(boardSize - 2) + 1
            planeCoreY = Random.nextInt(boardSize - 3) + 2
            resultBoard[planeCoreX][planeCoreY - 2] = Cell.PLANE
            resultBoard[planeCoreX][planeCoreY - 2] = Cell.PLANE
            resultBoard[planeCoreX - 1][planeCoreY - 2] = Cell.PLANE
            resultBoard[planeCoreX + 1][planeCoreY - 2] = Cell.PLANE
        }

        Orientation.HeadingUp -> {
            planeCoreX = Random.nextInt(boardSize - 3) + 1
            planeCoreY = Random.nextInt(boardSize - 2) + 1
            resultBoard[planeCoreX + 2][planeCoreY] = Cell.PLANE
            resultBoard[planeCoreX + 2][planeCoreY + 1] = Cell.PLANE
            resultBoard[planeCoreX + 2][planeCoreY - 1] = Cell.PLANE
        }

        Orientation.HeadingLeft -> {
            planeCoreX = Random.nextInt(boardSize - 2) + 1
            planeCoreY = Random.nextInt(boardSize - 3) + 1
            resultBoard[planeCoreX][planeCoreY + 2] = Cell.PLANE
            resultBoard[planeCoreX - 1][planeCoreY + 2] = Cell.PLANE
            resultBoard[planeCoreX + 1][planeCoreY + 2] = Cell.PLANE
        }

        Orientation.HeadingDown -> {
            planeCoreX = Random.nextInt(boardSize - 3) + 2
            planeCoreY = Random.nextInt(boardSize - 2) + 1
            resultBoard[planeCoreX - 2][planeCoreY] = Cell.PLANE
            resultBoard[planeCoreX - 2][planeCoreY + 1] = Cell.PLANE
            resultBoard[planeCoreX - 2][planeCoreY - 1] = Cell.PLANE
        }
    }
    // populate the cross in the plane
    resultBoard[planeCoreX][planeCoreY] = Cell.PLANE
    resultBoard[planeCoreX + 1][planeCoreY] = Cell.PLANE
    resultBoard[planeCoreX - 1][planeCoreY] = Cell.PLANE
    resultBoard[planeCoreX][planeCoreY + 1] = Cell.PLANE
    resultBoard[planeCoreX][planeCoreY - 1] = Cell.PLANE

    return resultBoard
}
