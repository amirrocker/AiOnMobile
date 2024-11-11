package de.ams.techday.aionmobilelitert.reinforcementlearning

import android.content.Context
import android.graphics.Point
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReinforcementLearningViewModel(private val helper: ReinforcementLearningHelper) : ViewModel() {

    private val playerBoard = MutableSharedFlow<List<List<Int>>>()
    private val agentBoard = MutableSharedFlow<List<List<Int>>>()
    private val agentHit = MutableStateFlow(0)
    private val playerhit = MutableStateFlow(0)

    val uiState: StateFlow<UiState> = combine(
        playerBoard,
        agentBoard,
        agentHit,
        playerhit,
    ) { playerBoard, agentBoard, agentHit, playerHit ->

        UiState(
            displayAgentBoard = agentBoard,
            displayPlayerBoard = playerBoard,
            hiddenPlayerBoard = playerBoard.map {
                it.map { cell ->
                    if(cell == Cell.PLANE) Cell.EMPTY else cell
                }
            },
            agentHits = agentHit,
            playerHits = playerHit,
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState()
    )

    fun hitAgent(hitPoint: Point) {
        viewModelScope.launch {
            val displayAgentBoard = uiState.value.displayAgentBoard
            val newAgentBoard = displayAgentBoard.map { it.toMutableList() }

            val row = hitPoint.x
            val col = hitPoint.y

            if(newAgentBoard[row][col] == Cell.PLANE) {
                newAgentBoard[row][col] = Cell.HIT
                agentHit.update { it + 1 }
            } else {
                newAgentBoard[row][col] = Cell.MISS
            }

            // player attack
            agentBoard.emit(newAgentBoard)

            // agent attack if game is not over yet
            if(agentHit.value <= 8) {
                predict()
            }
        }
    }

    // e.g. position = 4
    // row = 4 / 8
    // col = 4 % 8
    private fun agentHit(position: Int): Point {
        val row = position / uiState.value.displayPlayerBoard.size
        val col = position % uiState.value.displayPlayerBoard.size
        // row major format
        return Point(row, col)
    }

    // the magic
    private fun predict() {

        viewModelScope.launch {
            val displayPlayerBoard = uiState.value.displayPlayerBoard.map {
                it.toMutableList()
            }
            val hiddenPlayerBoard = uiState.value.hiddenPlayerBoard
            val hitPosition = helper.predict(hiddenPlayerBoard)
            val agentHit = agentHit(hitPosition)
            val row = agentHit.x
            val col = agentHit.y
            if(displayPlayerBoard[row][col] == Cell.PLANE) {
                displayPlayerBoard[row][col] = Cell.HIT
                playerhit.update { it +1 }
            } else {
                displayPlayerBoard[row][col] = Cell.MISS
            }
            playerBoard.emit(displayPlayerBoard)
        }
    }

    // reset the game
    fun reset() {
        viewModelScope.launch {
            agentBoard.emit(placeShip(board = EMPTY_BOARD()))
            playerBoard.emit(placeShip(board = EMPTY_BOARD()))
            agentHit.emit(0)
            playerhit.emit(0)
        }
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val helper = ReinforcementLearningHelper(context)
                return ReinforcementLearningViewModel(helper) as T
            }
        }
    }
}