package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityMainBinding
import android.content.res.ColorStateList


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private val boardButtons = Array<android.widget.Button?>(9) { null }

    private var isPlayerXTurn = true
    private var movesCount = 0
    private var isGameActive = true
    private var playerGoesFirst = true
    private var yourScore = 0
    private var computerScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeBoardButtons()

        binding.btnPlayAsX.text = "Go First"
        binding.btnPlayAsO.text = "Go Second"

        binding.btnPlayAsX.setOnClickListener {
            playerGoesFirst = true
            startGame()
        }
        binding.btnPlayAsO.setOnClickListener {
            playerGoesFirst = false
            startGame()
        }
        binding.btnNewGame.setOnClickListener {
            showChoiceScreen()
        }
        binding.btnResetRound.setOnClickListener {
            resetGame()
        }
    }

    private fun initializeBoardButtons() {
        for (i in 0..8) {
            val buttonID = "btn_${i / 3}${i % 3}"
            val resID = resources.getIdentifier(buttonID, "id", packageName)
            boardButtons[i] = findViewById(resID)
            boardButtons[i]?.setOnClickListener(this)
        }
    }

    private fun startGame() {
        binding.llPlayerChoice.visibility = View.GONE
        binding.clGameArea.visibility = View.VISIBLE
        yourScore = 0
        computerScore = 0
        updateScore()
        resetGame()
    }

    private fun showChoiceScreen() {
        binding.llPlayerChoice.visibility = View.VISIBLE
        binding.clGameArea.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        val isMyTurn = (isPlayerXTurn && playerGoesFirst) || (!isPlayerXTurn && !playerGoesFirst)

        if (!isGameActive || view !is Button || view.text.isNotEmpty() || !isMyTurn) {
            return
        }
        playMove(view, isPlayerXTurn)

        if (isGameActive) {
            Handler(Looper.getMainLooper()).postDelayed({ computerTurn() }, 500)
        }
    }

    private fun computerTurn() {
        if (!isGameActive) return

        val bestMoveIndex = findBestMove()
        if (bestMoveIndex != -1) {
            playMove(boardButtons[bestMoveIndex], isPlayerXTurn)
        }
    }

    private fun playMove(button: Button?, isX: Boolean) {
        if (button == null || !isGameActive || button.text.isNotEmpty()) return

        val symbol = if (isX) "X" else "O"
        val color = if (isX) Color.RED else Color.BLUE

        button.text = symbol
        button.setTextColor(color)
        movesCount++

        val winningCombo = checkForWin()
        if (winningCombo != null) {
            isGameActive = false
            highlightWinningButtons(winningCombo)
            Handler(Looper.getMainLooper()).postDelayed({
                endGame(isX)
            }, 1000)
        } else if (movesCount == 9) {
            isGameActive = false
            binding.tvStatus.text = "It's a Draw!"
            binding.btnResetRound.visibility = View.VISIBLE
        } else {
            isPlayerXTurn = !isPlayerXTurn
            updateStatusText()
        }
    }

    private fun findBestMove(): Int {
        return boardButtons.indexOfFirst { it?.text.isNullOrEmpty() }
    }

    private fun updateStatusText() {
        val isMyTurnNow = (isPlayerXTurn && playerGoesFirst) || (!isPlayerXTurn && !playerGoesFirst)
        binding.tvStatus.text = if (isMyTurnNow) "Your Turn" else "Computer's Turn"
    }

    private fun highlightWinningButtons(winningCombo: IntArray) {
        // Create the color state list once, outside the loop.
        val greenColor = ColorStateList.valueOf(Color.parseColor("#90EE90")) // Light Green

        for (index in winningCombo) {
            // Apply the same color object to each of the three winning buttons.
            boardButtons[index]?.backgroundTintList = greenColor
        }
    }


    private fun endGame(didXWin: Boolean) {
        val winnerSymbol = if (didXWin) "X" else "O"
        val playerSymbol = if (playerGoesFirst) "X" else "O"

        if (winnerSymbol == playerSymbol) {
            yourScore++
            binding.tvStatus.text = "You Won!"
        } else {
            computerScore++
            binding.tvStatus.text = "Computer Won!"
        }
        updateScore()
        binding.btnResetRound.visibility = View.VISIBLE
    }

    private fun updateScore() {
        binding.tvScorePlayerX.text = "You: $yourScore"
        binding.tvScorePlayerO.text = "Computer: $computerScore"
    }

    private fun checkForWin(): IntArray? {
        val winningCombos = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        for (combo in winningCombos) {
            val first = boardButtons[combo[0]]?.text
            if (first?.isNotEmpty() == true && first == boardButtons[combo[1]]?.text && first == boardButtons[combo[2]]?.text) {
                return combo
            }
        }
        return null
    }

    private fun resetGame() {
        movesCount = 0
        isGameActive = true
        isPlayerXTurn = true

        binding.btnResetRound.visibility = View.GONE

        for (button in boardButtons) {
            button?.text = ""
            // Change this line
            button?.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
        }

        updateStatusText()

        if (!playerGoesFirst) {
            Handler(Looper.getMainLooper()).postDelayed({ computerTurn() }, 500)
        }
    }
}
