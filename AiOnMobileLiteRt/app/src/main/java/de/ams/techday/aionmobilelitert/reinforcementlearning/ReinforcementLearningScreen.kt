package de.ams.techday.aionmobilelitert.reinforcementlearning

import android.graphics.Point
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobilelitert.R
import de.ams.techday.aionmobilelitert.ui.theme.darkBlue

@Composable
fun ReinforcementLearningScreen(
    reinforcementLearningViewModel: ReinforcementLearningViewModel = viewModel(
        factory = ReinforcementLearningViewModel.getFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by reinforcementLearningViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Header()
        }
        ) { innerPadding ->
        ReinforcementLearningContent(
            innerPadding,
            uiState,
            reinforcementLearningViewModel::hitAgent,
            reinforcementLearningViewModel::reset
        )
    }
}

@Composable
fun ReinforcementLearningContent(
    innerPadding: PaddingValues,
    uiState: UiState,
    onHitAgent: (Point) -> Unit = {},
    onReset: () -> Unit = {},
) {

            Column(
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = 10.dp,
                        end = 10.dp
                    )
            ) {

                val agentBoard = uiState.displayAgentBoard
                val playerBoard = uiState.displayPlayerBoard
                val theEnd = uiState.theEnd

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = GAME_INSTRUCTIONS
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row {
                    Board(user = User.Agent, board = agentBoard) { row, col ->
                        if (theEnd) return@Board
                        onHitAgent(Point(row, col))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Agent board:\n${uiState.agentHits} hits")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))

                Row {
                    Board(user = User.Player, board = playerBoard) { _, _ -> }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Your board:\n${uiState.playerHits} hits")
                }
                if (theEnd) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val youWin = uiState.agentHits == 8

                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = if (youWin) "You win!!!" else "Agent win!!!"
                    )
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            onReset()
                        }) {
                        Text(text = "Reset")
                    }
                }

            }
        }


@Composable
fun Board(
    modifier: Modifier = Modifier,
    board: List<List<Int>>,
    user: User,
    onHit: (row: Int, col: Int) -> Unit
) {
    Column(modifier = modifier) {
        for (row in board.indices) {
            Row {
                for (col in board[row].indices) {
                    val cellValue = board[row][col]
                    val color = when (cellValue) {
                        Cell.EMPTY -> Color.White
                        Cell.HIT -> Color.Red
                        Cell.PLANE -> if (user == User.Agent) Color.White else darkBlue
                        Cell.MISS -> Color.Yellow
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = color)
                            .border(1.dp, color = Color.Gray)
                            .clickable {
                                if (cellValue == Cell.EMPTY || cellValue == Cell.PLANE) {
                                    onHit(row, col)
                                }
                            },
                    )
                }
            }
        }
    }
}

enum class User {
    Player, Agent
}

private const val GAME_INSTRUCTIONS = "Tap any cell in the agent board as your strike. Red-hit, Yellow-miss"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        title = {
            Image(
                modifier = Modifier.size(120.dp),
                alignment = Alignment.CenterStart,
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
            )
        },
    )
}

