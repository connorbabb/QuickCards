package com.example.quickcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.quickcards.ui.theme.QuickCardsTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField

// Data class that holds the term and definition of a flashcard
data class Flashcard(val term: String, val value: String)

class MainActivity : ComponentActivity() {
    // list of cards and a message that is displayed below the options set to default here
    private val cards = mutableStateOf(listOf<Flashcard>())
    private val message = mutableStateOf("No Cards Created.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // main column for app
            QuickCardsTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Static message at the top
                    Text(
                        text = "Select any option below:",
                        modifier = Modifier.padding(top = 16.dp) // Padding in order to be on phone notification bar
                    )

                    // States to track if Add Card or Remove Card button has been clicked
                    // 3rd State to set cards to invisible if user is testing
                    val isAddCardClicked = remember { mutableStateOf(false) }
                    val isRemoveCardClicked = remember { mutableStateOf(false) }
                    val displayCards = remember { mutableStateOf(true) }

                    // States to hold the term and value for Add/Remove operations
                    val term = remember { mutableStateOf("") }
                    val value = remember { mutableStateOf("") }

                    // Add Card Button
                    Button(
                        onClick = {
                            isAddCardClicked.value = !isAddCardClicked.value // Toggle visibility
                            isRemoveCardClicked.value = false // Ensure only one mode is active
                            message.value = if (isAddCardClicked.value) "Enter the Term and Definition" else ""
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Add Card")
                    }

                    // Conditionally display the TextFields if Add Card is clicked
                    if (isAddCardClicked.value) {
                        TextField(
                            value = term.value,
                            onValueChange = { term.value = it },
                            label = { Text("Enter Term") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TextField(
                            value = value.value,
                            onValueChange = { value.value = it },
                            label = { Text("Enter Definition") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Submit button for adding a card
                        Button(
                            onClick = {
                                addCard(term.value, value.value)
                                isAddCardClicked.value = false // Hide the TextFields after submitting
                                term.value = ""
                                value.value = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B0000), // Dark red
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Submit Card")
                        }
                    }

                    // Remove Card Button
                    Button(
                        onClick = {
                            isRemoveCardClicked.value = !isRemoveCardClicked.value // Toggle visibility
                            isAddCardClicked.value = false // Ensure only one mode is active
                            message.value = if (isRemoveCardClicked.value) "Enter the Term to Remove" else ""
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Remove Card")
                    }

                    // Conditionally display the TextField and Submit button for Remove Card
                    if (isRemoveCardClicked.value) {
                        TextField(
                            value = term.value,
                            onValueChange = { term.value = it },
                            label = { Text("Enter Term") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Submit button for removign a card
                        Button(
                            onClick = {
                                removeCard(term.value)
                                isRemoveCardClicked.value = false// Hide the TextField after submitting
                                term.value = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B0000), // Dark red
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Submit")
                        }
                    }

                    // Values used for testing and shuffling
                    val isTestMode = remember { mutableStateOf(false) }
                    val shuffledCards = remember { mutableStateOf(emptyList<Flashcard>()) }
                    val currentCardIndex = remember { mutableStateOf(0) }
                    val isAnswerShown = remember { mutableStateOf(false) }

                    if (!isTestMode.value) {
                        Button(
                            onClick = {
                                if (cards.value.isNotEmpty()) {
                                    displayCards.value = !displayCards.value
                                    shuffledCards.value = cards.value.shuffled()
                                    currentCardIndex.value = 0
                                    isAnswerShown.value = false
                                    isTestMode.value = true
                                    message.value = ""
                                } else {
                                    message.value = "No cards available to test."
                                }
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Test")
                        }
                    } else {
                        TestMode(
                            shuffledCards = shuffledCards.value,
                            currentCardIndex = currentCardIndex.value,
                            isAnswerShown = isAnswerShown.value,
                            onShowAnswer = { isAnswerShown.value = true },
                            onNextCard = {
                                if (currentCardIndex.value + 1 < shuffledCards.value.size) {
                                    currentCardIndex.value += 1
                                    isAnswerShown.value = false
                                } else {
                                    isTestMode.value = false
                                    message.value = "Test completed."
                                    displayCards.value = !displayCards.value
                                }
                            }
                        )
                    }


                    // Display the dynamic message below the buttons
                    Text(
                        text = message.value,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Display list of cards
                    if (displayCards.value) {
                        Text(
                            text = "Cards: ${cards.value.joinToString(", ") { "${it.term}: ${it.value}" }}",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Function to add a new card with a term and value
    private fun addCard(term: String, value: String) {
        if (term.isNotEmpty() && value.isNotEmpty()) {
            val newCard = Flashcard(term = term, value = value)
            cards.value = cards.value + newCard
            message.value = "Card Added. Total Cards: ${cards.value.size}"
        } else {
            message.value = "Please enter both term and value."
        }
    }

    // Function to remove a card by its term
    private fun removeCard(term: String) {
        if (term.isNotEmpty()) {
            val cardToRemove = cards.value.find { it.term == term }
            if (cardToRemove != null) {
                cards.value = cards.value.filter { it.term != term }
                message.value = "Card with term '$term' removed."
            } else {
                message.value = "No card found with term '$term'."
            }
        } else {
            message.value = "Please enter a term to remove."
        }
    }
}

// Test function to randomly select card and display
@Composable
fun TestMode(
    shuffledCards: List<Flashcard>,
    currentCardIndex: Int,
    isAnswerShown: Boolean,
    onShowAnswer: () -> Unit,
    onNextCard: () -> Unit
) {
    val currentCard = shuffledCards.getOrNull(currentCardIndex)

    if (currentCard != null) {
        Text(
            text = "Term: ${currentCard.term}",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isAnswerShown) {
            Text(
                text = "Definition: ${currentCard.value}",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onNextCard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Next Flashcard")
            }
        } else {
            Button(
                onClick = onShowAnswer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Show Answer")
            }
        }
    } else {
        Text("No more flashcards available.")
    }
}
