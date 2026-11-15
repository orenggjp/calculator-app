package org.example.app

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.SwingUtilities
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import org.example.calculator.CalculatorEngine
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import javax.swing.JOptionPane

class CalculatorWindow {

    private val calculator = CalculatorEngine()

    fun show() {
        SwingUtilities.invokeLater {
            val frame = JFrame("Calculator")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(380,520)
            frame.layout = BorderLayout(10,10)
            frame.contentPane.background = Color(30,30,30)

            val display = JTextField("0")
            display.isEditable = false
            display.horizontalAlignment = JTextField.RIGHT
            display.font = Font("Arial", Font.PLAIN, 28)
            display.background = Color(45,45,45)
            display.foreground = Color.WHITE
            display.caretColor = Color.WHITE
            display.border = javax.swing.BorderFactory.createEmptyBorder(15,15,15,15)
            display.preferredSize = Dimension(0, 85)

            frame.add(display, BorderLayout.NORTH)

            val buttonPanel = JPanel()
            buttonPanel.layout = GridLayout(5,4,5,5)
            buttonPanel.background = Color(30,30,30)
            buttonPanel.border = javax.swing.BorderFactory.createEmptyBorder(5,5,5,5)

            val buttons = listOf(
                "7", "8", "9", "÷",
                "4", "5", "6", "×",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "C", "⌫", "", ""
            )

            var firstOperand = 0.0
            var pendingOperator: String? = null
            var startNewNumber = true
            var errorState = false

            val calculationHistory = mutableListOf<String>()

            // History button goes here
            val historyButton = JButton("History")

            historyButton.addActionListener {
                val historyText = if (calculationHistory.isEmpty()) {
                    "No calculations yet."
                } else {
                    calculationHistory.joinToString("\n")
                }

                JOptionPane.showMessageDialog(
                    frame,
                    historyText,
                    "Calculation History",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }

            val clearHistoryButton = JButton("Clear History")

            clearHistoryButton.addActionListener {
                calculationHistory.clear()

                JOptionPane.showMessageDialog(
                    frame,
                    "Calculation history cleared.",
                    "Clear History",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }

            fun clear() {
                display.text = "0"
                firstOperand = 0.0
                pendingOperator = null
                startNewNumber = true
                errorState = false
            }

            fun enterDigit(digit: String) {
                if(errorState) clear()

                if(startNewNumber || display.text == "0") {
                    display.text = digit
                    startNewNumber = false
                } else {
                    display.text += digit
                }
            }

            fun enterDecimal() {
                if (errorState) clear()

                if (startNewNumber) {
                    display.text = "0."
                    startNewNumber = false
                } else if (!display.text.contains(".")) {
                    display.text += "."
                }
            }

            fun calculateResult() {
                val operation = pendingOperator ?: return

                val secondOperand = display.text.toDoubleOrNull() ?: return

                try {
                    val result = when (operation) {
                        "+" -> calculator.add(firstOperand, secondOperand)
                        "-" -> calculator.subtract(firstOperand, secondOperand)
                        "×" -> calculator.multiply(firstOperand, secondOperand)
                        "÷" -> calculator.divide(firstOperand, secondOperand)
                        else -> return
                    }

                    display.text = formatResult(result)
                    calculationHistory.add(
                        "${formatResult(firstOperand)} $operation ${formatResult(secondOperand)} = ${formatResult(result)}"
                    )
                } catch (e: ArithmeticException) {
                    display.text = "Error"
                    errorState = true
                }

                pendingOperator = null
                startNewNumber = true
            }

            fun chooseOperator(operation: String) {
                if(errorState) return

                if(pendingOperator != null && !startNewNumber) {
                    calculateResult()
                    if (errorState) return
                }

                firstOperand = display.text.toDoubleOrNull() ?: 0.0
                pendingOperator = operation
                startNewNumber = true
            }

            fun backspace() {
                if(errorState) {
                    clear()
                    return
                }

                if(!startNewNumber) {
                    display.text = if (display.text.length <= 1) {
                        "0"
                    } else {
                        display.text.dropLast(1)
                    }
                }
            }

            fun handleInput(input: String) {
                when (input) {
                    in "0".."9" -> enterDigit(input)
                    "." -> enterDecimal()
                    "+", "-", "×", "÷" -> chooseOperator(input)
                    "=" -> calculateResult()
                    "C" -> clear()
                    "⌫" -> backspace()
                }
            }

            for (text in buttons) {
                val button = JButton(text)

                if (text.isNotEmpty()) {
                    button.font = Font("Arial", Font.BOLD, 20)
                    button.isFocusPainted = false
                    button.margin = Insets(10,10,10,10)

                    button.background = when (text) {
                        "=" -> Color(40,120,220)
                        "+", "-", "×", "÷" -> Color(70,70,90)
                        "C", "⌫" -> Color(165,65,65)
                        else -> Color(60,60,60)
                    }

                    button.foreground = Color.WHITE
                    button.isOpaque = true
                    button.border = javax.swing.BorderFactory.createEmptyBorder(10,10,10,10)
                    button.addActionListener {
                        handleInput(text)
                    }
                } else {
                    button.isEnabled = false
                }

                buttonPanel.add(button)
            }

            val historyPanel = JPanel(GridLayout(1, 2, 5, 5))

            historyPanel.add(historyButton)
            historyPanel.add(clearHistoryButton)

            historyPanel.background = Color(30, 30, 30)
            historyPanel.border = javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)

            frame.add(buttonPanel, BorderLayout.CENTER)

            frame.add(historyPanel, BorderLayout.SOUTH)

            frame.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_ENTER -> handleInput("=")
                        KeyEvent.VK_ESCAPE -> handleInput("C")
                        KeyEvent.VK_BACK_SPACE -> handleInput("⌫")
                        KeyEvent.VK_ADD -> handleInput("+")
                        KeyEvent.VK_SUBTRACT -> handleInput("-")
                        KeyEvent.VK_MULTIPLY -> handleInput("×")
                        KeyEvent.VK_DIVIDE -> handleInput("÷")
                    }

                    when (e.keyChar) {
                        in '0'..'9' -> handleInput(e.keyChar.toString())
                        '.' -> handleInput(".")
                        '+' -> handleInput("+")
                        '-' -> handleInput("-")
                        '*' -> handleInput("×")
                        '/' -> handleInput("÷")
                        '=' -> handleInput("=")
                    }
                }
            })

            frame.isFocusable = true
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
            frame.requestFocusInWindow()
        }
    }

    private fun formatResult(value: Double): String {
        if (!value.isFinite()) return "Error"

        return if (value % 1.0 == 0.0 &&
            value >= Long.MIN_VALUE.toDouble() &&
            value < Long.MAX_VALUE.toDouble()
            ) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}