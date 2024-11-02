/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

//Taryn and Tynan
//12.14.21
//Yahtzee - This program plays a full game of Yahtzee
//We added the extension of keeping track of the top 10 high scores

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import acm.io.*;
import acm.program.*;
import acm.util.*;
import acmx.export.java.util.Scanner;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] diceRoll = new int[N_DICE];
	private int[] totalPlayerScores;
	private int player = 1;
	private int [][] categoryScore;
	private int[][] categoriesSelected;
	private Scanner scan;
	private BufferedWriter bufferedWriter;
	private ArrayList<Integer> scoreList;
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run(){
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		totalPlayerScores = new int [nPlayers];
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		categoriesSelected = new int [nPlayers][YahtzeeConstants.N_CATEGORIES];
		categoryScore = new int [nPlayers][YahtzeeConstants.N_CATEGORIES];
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		//creates file and scanner for storing top 10 high scores
		File scores = new File("scores.txt");
		scan = new Scanner(scores);
		scoreList = new ArrayList<Integer>();
		while(scan.hasNextInt()) {
			scoreList.add(scan.nextInt());
		}
		playGame();
	}

	private void playGame() {
		//number of rounds
		int i = 0;
		while(i < 13) {
			//switch between players
			player = 1;
			while(player <= nPlayers) {
				playerTurn();
				player++;
			}
			i++;
		}
		endGame();
	}
	
	private void endGame() {
		for(int i = 0; i < nPlayers; i++) {
			//calculate upper score for each player
			int upperScore = 0;
			for(int j = 0; j < YahtzeeConstants.UPPER_SCORE; j++) {
				upperScore += categoryScore[i][j];
			}
			display.updateScorecard(YahtzeeConstants.UPPER_SCORE, i+1, upperScore);
			//decide if player gets an upperBonus or not
			if(upperScore > 64) {
				int upperBonus = 35;
				display.updateScorecard(YahtzeeConstants.UPPER_BONUS, i +1, upperBonus);
				totalPlayerScores[i] += upperBonus;
				display.updateScorecard(YahtzeeConstants.TOTAL, i+1, totalPlayerScores[i]);
			} else {
				display.updateScorecard(YahtzeeConstants.UPPER_BONUS, i +1, 0);
			}
		}
		//calculate lower score for each player
		for(int i = 0; i < nPlayers; i++) {
			int lowerScore = 0;
			for(int j = 8; j < YahtzeeConstants.LOWER_SCORE; j++) {
				lowerScore += categoryScore[i][j];
			}
			display.updateScorecard(YahtzeeConstants.LOWER_SCORE, i +1, lowerScore);
		}
		//if player got a high score congratulate them
		for(int i = 0; i < nPlayers; i++) {
			boolean highScore = inputScore(totalPlayerScores[i]);
			if(highScore) {
				display.printMessage(playerNames[i] + " got a high score of " + totalPlayerScores[i]);
			}
		}
	}
	
	//put score into high score list if there are less than 10 scores in list
	//or if it is bigger than one of the previous high scores
	private boolean inputScore(int score) {
		if(scoreList.size() < 10) {
			scoreList.add(score);
		} else {
			for(int i = 0 ; i < scoreList.size(); i++) {
				if(score > scoreList.get(i)) {
					scoreList.add(score);
					break;
				}
			}
		}
		//sort scores from high to low
		for(int i = 0; i < scoreList.size(); i++) {
			for(int j = 0; j < scoreList.size() -1; j++) {
				if(scoreList.get(j) < scoreList.get(j+1)){
					int temp = scoreList.get(j);
					scoreList.set(j, scoreList.get(j+1));
					scoreList.set(j+1, temp);
				}
			}
		}
		//put high score into file
		try {
			FileWriter fileWriter;
			File scores = new File("scores.txt");
			fileWriter = new FileWriter(scores);
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(int i = 0; i < 10; i++) {
				bufferedWriter.write(scoreList.get(i).toString() + " ");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return scoreList.subList(0,10).contains(score);
	}
	
	//randomly roll the dice
	private void rollDice() {
		display.printMessage(playerNames[player-1] + "'s turn. Click 'Roll Dice' to roll the dice");
		display.waitForPlayerToClickRoll(player);
		for(int i = 0; i < diceRoll.length; i++) {
			if(!display.isDieSelected(i)) {
				diceRoll[i] = rgen.nextInt(1,6);
			}
		}
		display.displayDice(diceRoll);
	}
	
	//allow player to select the dice they want to keep
	private boolean selectDice() {
		display.printMessage("Select the dice you want to keep. Then press reroll");
		display.waitForPlayerToSelectDice();
		return false;
	}
	
	//check which dice to reroll
	private void reRoll() {
		if(selectDice() == false) {
			rollDice();
		}
	}
	
	//for the ones, twos, threes, etc. category
	private void sameNumber(int category) {
		int score = 0;
		//checks to see if dice numbers are the same as category selected
		for(int i = 0; i < diceRoll.length; i++) {
			if(diceRoll[i] == category) {
				score  += diceRoll[i];
			}
		}
		//update scorecard
		categoryScore[player -1][category - 1] = score;
	}
	
	//for the chance category
	private void chance(int category) {
		int score = 0;
			//add up all the values on the dice
			for(int i = 0; i < diceRoll.length; i++) {
					score += diceRoll[i];
			}
		//update scorecard
		categoryScore[player-1][category - 1] = score;
	}
	
	//for the three of a kind and four of a kind category
	private void ofAKind(int category) {
		int score = 0;
		int maxIndex = 0;
		int valueAtIndex = 0;
		//make a new array that counts the amount of times of each dice value
		//shows up in the diceRoll array
		int[] diceCount = new int [6];
		for(int i = 0; i < diceRoll.length; i++) {
			diceCount[diceRoll[i]-1]++;
		}
		//compare how many times each value shows up
		for(int j = 0; j < diceCount.length; j++) {
			if(diceCount[j] > valueAtIndex) {
				maxIndex = j;
				valueAtIndex = diceCount[j];
			}
		}
		score = valueAtIndex * (maxIndex + 1);
		categoryScore[player-1][category -1] = score;
	}
	
	//checks for fullHouse and Yahtzee categories
	private void multiples(int category) {
		int score = 0;
		boolean haveTriple = false;
		boolean haveDouble = false;
		boolean haveFive = false;
		//make a new array that counts the amount of times of each dice value
		//shows up in the diceRoll array
		int[] diceCount = new int [6];
		for(int i = 0; i < diceRoll.length; i++) {
			diceCount[diceRoll[i]-1]++;
		}
		for(int j = 0; j < diceCount.length; j++) {
			//if 3 of the dice are the same
			if(diceCount[j] == 3) {
				haveTriple = true;
				//if 2 of the dice are the same
			} else if (diceCount[j] == 2) {
				haveDouble = true;
			}
			//if 5 of the dice are the same
			if (diceCount[j] == 5) {
				haveFive = true;
			}
		}
		//fullHouse
		if(haveTriple && haveDouble) {
			score = 25;
		}
		//Yahtzee
		if(haveFive) {
			score = 50;
		}
		categoryScore[player-1][category -1] = score;
	}
	
	//checks for small straight and large straight
	private void straight(int straightSize, int category) {
		int score = 0;
		int consecutive = 0;
		boolean haveStraight = false;
		//make a new array that counts the amount of times of each dice value
		//shows up in the diceRoll array
		int[] diceCount = new int [6];
		for(int i = 0; i < diceRoll.length; i++) {
			diceCount[diceRoll[i]-1]++;
		}
		//checks for consecutive values
		for(int j = 0; j < diceCount.length; j++) {
			if(diceCount[j] > 0) {
				consecutive++;
				if(consecutive >= straightSize) {
					haveStraight = true;
				}
				//if you find a 0
			} else {
				consecutive = 0;
			}
		}
		//checks size of straight
		if(haveStraight) {
			if(straightSize == 4) {
				score = 30;
			} else {
				score = 40;
			}
		}
		categoryScore[player-1][category -1] = score;
	}
	
	//each player's turn
	private void playerTurn() {
		rollDice();
		reRoll();
		reRoll();
		int category = display.waitForPlayerToSelectCategory();
		display.printMessage("Select the category.");
		while(categoriesSelected[player - 1][category] == 1) {
			category = display.waitForPlayerToSelectCategory();
		}
		//check which category the player selected
		if(category < 7) {
			sameNumber(category);
		} else if(category == YahtzeeConstants.CHANCE) {
			chance(category);
		} else if(category == YahtzeeConstants.THREE_OF_A_KIND || category == YahtzeeConstants.FOUR_OF_A_KIND) {
			ofAKind(category);
		} else if( category == YahtzeeConstants.FULL_HOUSE) {
			multiples(category);
		} else if (category == YahtzeeConstants.YAHTZEE) {
			multiples(category);
		} else if(category == YahtzeeConstants.LARGE_STRAIGHT) {
			straight(5, category);
		} else if(category == YahtzeeConstants.SMALL_STRAIGHT) {
			straight(4, category);	
		}
		//update arrays and scorecards
		display.updateScorecard(category, player, categoryScore[player-1][category-1]);
		totalPlayerScores[player - 1] += categoryScore[player-1][category-1];
		display.updateScorecard(YahtzeeConstants.TOTAL, player, totalPlayerScores[player -1]);
		categoriesSelected[player - 1][category] = 1;
	}
}
