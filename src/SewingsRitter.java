import java.util.*;
import java.util.stream.Collectors;

public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private Strategy currentStrategy;
    private List<Strategy> futureStrategies = new ArrayList<>(990);
    private List<Strategy> strategies = new ArrayList<>(1000);
    private List<Counter> pastGames = new ArrayList<>();
    private int numOfTestGames = 100;
    private int numOfAnalysisGames = 15;
    private int analysisOn = 10;

    public SewingsRitter() {
        super();
    }

    @Override
    public void reset() {
        if (turnNumber == 15) {
            turnNumber = 0;

            System.out.println("Game " + gameNumber + ": " + myPoints + " - " + hisPoints);
            if (myPoints > hisPoints) {
                wins++;
            }
            pastGames.add(new Counter(hisCardsPlayed, specialCardsPlayed));
            gameNumber++;
            if(gameNumber % analysisOn == 0) {
                patternAnalysis();
            }
            System.out.println("Wins: " + wins + " out of " + gameNumber);
            if (gameNumber == 1) {
                evaluation();
            } else {
                ((MyBot) currentStrategy).reset();
            }
        }
        super.reset();
    }

    @Override
    public int gibKarte(int nextCard) {
        int myCard = 0;
        if(strategies.isEmpty())
            myCard = firstRun(nextCard);
        else
            myCard = currentStrategy.giveCard(nextCard);
        giveCard(nextCard, myCard);
        return myCard;
    }

    private void patternAnalysis() {
        int[][] points = new int[strategies.size()][pastGames.size()];
        for(int i=0;i<strategies.size();i++) {
            Strategy strategy = strategies.get(i);
            for(int j=0;j<pastGames.size();j++) { //Runs a strategy against all past games
                Counter pastStrategy = pastGames.get(j);
                for(int k = 0; k<numOfAnalysisGames;k++) {
                    Collections.shuffle(specialCardsPlayed);
                    points[i][j] += (new Simulation(specialCardsPlayed, strategy, pastStrategy)).playGame();
                }
            }

        }

    }

    private void evaluation() {
        int attempts = 1;
        int[] permArray; // Array to store successful permutations
        Set<int[]> winningStrategies = new HashSet<>(10000);

        while (attempts < 15) {
            permArray = new int[15];
            Arrays.fill(permArray, -1);

            for (int i = 1; i < 16; i++) { // Iterate over numbers 1 to 15
                int numberToPlace = (i + attempts - 2) % 15 + 1;
                int[] winPoints = new int[15];

                for (int position = 0; position < 15; position++) { // Iterate over positions
                    if (permArray[position] != -1) {
                        continue; // Skip locked positions
                    }

                    // Run the simulation for each permutation
                    for (int k = 0; k < numOfTestGames; k++) {
                        int[] testArray = new int[15];
                        Arrays.fill(testArray, -1); // Reset testArray for each game

                        // Copy locked values from permArray to testArray
                        for (int l = 0; l < 15; l++) {
                            if (permArray[l] != -1)
                                testArray[l] = permArray[l];
                        }

                        testArray[position] = numberToPlace; // Place number i in the current position

                        // Fill the rest of testArray with unique random numbers
                        ArrayList<Integer> availableNumbers = new ArrayList<>();
                        for (int num = 1; num <= 15; num++) {
                            int finalNum = num;
                            if (!Arrays.stream(testArray).anyMatch(x -> x == finalNum)) {
                                availableNumbers.add(num);
                            }
                        }
                        Collections.shuffle(availableNumbers); // Randomize the list of available numbers
                        int availIndex = 0;
                        for (int j = 0; j < 15; j++) {
                            if (testArray[j] == -1) {
                                testArray[j] = availableNumbers.get(availIndex++);
                            }
                        }

                        // Run the simulation with the current permutation
                        int pointsWon = (new Simulation(specialCardsPlayed, new Last(Arrays.stream(testArray).boxed().collect(Collectors.toList())), new Last(hisCardsPlayed))).playGame();
                        if (pointsWon > 35) {
                            winningStrategies.add(testArray.clone()); // Clone the array before adding
                        }
                        winPoints[position] += pointsWon;
                    }
                }

                // Find the most successful position for number i
                int winnerIndex = -1;
                int maxPoints = -1;
                for (int j = 0; j < winPoints.length; j++) {
                    if (winPoints[j] > maxPoints && permArray[j] == -1) {
                        winnerIndex = j;
                        maxPoints = winPoints[j];
                    }
                }

                if (winnerIndex != -1) {
                    permArray[winnerIndex] = numberToPlace;
                }
            }

            attempts++;
        }

        int mostPoints = 0;
        //Final Test
        Strategy finalStrategy = null;
        Strategy hisStrategy = new Counter(hisCardsPlayed, specialCardsPlayed);
        for(int[] strategy : winningStrategies) {
            Strategy theStrategy = new Counter(Arrays.stream(strategy).boxed().collect(Collectors.toList()), specialCardsPlayed);
            int points = 0;
            for(int i = 0; i<15; i++) {
                Collections.shuffle(specialCardsPlayed);
                points += (new Simulation(specialCardsPlayed, theStrategy, hisStrategy)).playGame();
            }
            if(points > mostPoints) {
                finalStrategy = theStrategy;
                mostPoints = points;
            }
        }
        if(finalStrategy != null)
            strategies.add(finalStrategy);
    }

    private int firstRun(int nextCard) {
        int myCard = 0;
        switch(nextCard) {
            case -5: case -4: case -3:
                do {
                    myCard = Utils.random.nextInt(4 - 1) + 1;
                } while(!myCards.contains(myCard));
                break;
            case -2: case -1: case 1:
                do {
                    myCard = Utils.random.nextInt(7 - 4) + 4;
                } while(!myCards.contains(myCard));
                break;
            case 2: case 3: case 4:
                do {
                    myCard = Utils.random.nextInt(10 - 7) + 7;
                } while(!myCards.contains(myCard));
                break;
            case 5: case 6: case 7:
                do {
                    myCard = Utils.random.nextInt(13 - 10) + 10;
                } while(!myCards.contains(myCard));
                break;
            case 8: case 9: case 10:
                do {
                    myCard = Utils.random.nextInt(16 - 13) + 13;
                } while(!myCards.contains(myCard));
                break;
        }
        return myCard;
    }
}