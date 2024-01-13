import java.util.*;
import java.util.stream.Collectors;

public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private Strategy currentStrategy;
    private List<Integer> pastWinPoints = new ArrayList<>(1000);
    private List<Strategy> futureStrategies = new ArrayList<>(1000);
    private List<Strategy> strategies = new ArrayList<>(1000);
    private List<Counter> pastGames = new ArrayList<>(1000);
    private int numOfTestGames = 100;
    private int numOfAnalysisGames = 5;
    private Simulation simulation;

    public SewingsRitter() {
        super();
        for(int i = 0;i<1000;i++) {
            futureStrategies.add(null);
        }
        simulation = new Simulation();
    }

    @Override
    public void reset() {
        if (turnNumber == 15) {
            turnNumber = 0;

            if (myPoints > hisPoints) {
                wins++;
            }
            pastGames.add(new Counter(hisCardsPlayed, specialCardsPlayed));
            pastWinPoints.add(myPoints);
            gameNumber++;
            System.out.println("Wins: " + wins + " out of " + gameNumber);
            evaluation();
            patternAnalysis();
            if(futureStrategies.get(gameNumber) == null) {
                currentStrategy = strategies.get(gameNumber - 1);
            } else {
                currentStrategy = futureStrategies.get(gameNumber);
            }
            ((MyBot) currentStrategy).reset();
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
        int strategiesSize = strategies.size();
        int pastGamesSize = pastGames.size();
        int[][] points = new int[strategiesSize][pastGamesSize];
        for(int i=0;i<strategiesSize;i++) {
            Strategy strategy = strategies.get(i);
            for (int j = 0; j < pastGamesSize; j++) { //Runs a strategy against all past games
                Counter pastStrategy = pastGames.get(j);
                for (int k = 0; k < numOfAnalysisGames; k++) {
                    Collections.shuffle(specialCardsPlayed);
                    simulation.resetSimulation();
                    simulation.setSpecialCards(specialCardsPlayed);
                    simulation.setStrategies(strategy, pastStrategy);
                    points[i][j] += simulation.playGame();
                }
            }
            int winnerIndex = -1;
            for (int j = 0; j < pastGamesSize; j++) { //Runs through the results of the games
                int pointsWon = pastWinPoints.get(i);
                winnerIndex = j;
                if (points[i][j] > (pointsWon * 1.125 * numOfAnalysisGames)) {
                    pastWinPoints.set(i, points[i][j] / numOfAnalysisGames);
                    break;
                }
            }
            if(winnerIndex != -1)
                futureStrategies.set(winnerIndex, strategy);
            int firstIndex = -1;
            for (int j = 0; j < strategiesSize; j++) {
                if (strategy.equals(futureStrategies.get(j))) {
                    if (firstIndex == -1) {
                        firstIndex = j; // Store the first index where the strategy is found.
                    } else {
                        int secondIndex = j; // Store the second index where the strategy is found.
                        int distance = secondIndex - firstIndex;
                        for (int k = secondIndex + distance; k < futureStrategies.size(); k += distance) {
                            futureStrategies.set(k, strategy); // Update futureStrategies based on the calculated distance.
                        }
                        break; // Exit the loop after updating futureStrategies.
                    }
                }
            }
        }
    }

    public void evaluation() {
        int[] permArray; // Array to store successful permutations
        Set<int[]> winningStrategies = new HashSet<>();

        while (winningStrategies.isEmpty()) {
            permArray = new int[15];
            Arrays.fill(permArray, -1);

            for (int i = 1; i < 16; i++) { // Iterate over numbers 1 to 15
                int[] winPoints = new int[15];

                for (int position = 0; position < 15; position++) { // Iterate over positions
                    if (permArray[position] != -1) {
                        continue; // Skip locked positions
                    }

                    // Run the simulation for each permutation
                    for (int k = 0; k < numOfTestGames; k++) {
                        int[] testArray = new int[15];
                        for (int l = 0; l < 15; l++) {
                            if (permArray[l] != -1) {
                                testArray[l] = permArray[l];
                            } else {
                                testArray[l] = -1; // Reset only the positions that are not locked
                            }
                        }

                        testArray[position] = i; // Place number i in the current position

                        // Fill the rest of testArray with unique random numbers
                        ArrayList<Integer> availableNumbers = new ArrayList<>(15);
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
                        List<Integer> testArrayList = new ArrayList<>(testArray.length);
                        for (int value : testArray) {
                            testArrayList.add(value);
                        }
                        simulation.resetSimulation();
                        simulation.setSpecialCards(specialCardsPlayed);
                        simulation.setStrategies(new Last(testArrayList), new Last(hisCardsPlayed));
                        int pointsWon = simulation.playGame();
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
                    permArray[winnerIndex] = i;
                }
            }
        }

        int mostPoints = 0;
        //Final Test
        Strategy finalStrategy = null;
        Strategy hisStrategy = new Counter(hisCardsPlayed, specialCardsPlayed);
        for(int[] strategy : winningStrategies) {
            Strategy theStrategy = new Counter(Arrays.stream(strategy).boxed().collect(Collectors.toList()), specialCardsPlayed);
            int points = 0;
            for(int i = 0; i<5; i++) {
                Collections.shuffle(specialCardsPlayed);
                simulation.resetSimulation();
                simulation.setSpecialCards(specialCardsPlayed);
                simulation.setStrategies(theStrategy, hisStrategy);
                points += simulation.playGame();
            }
            if(points > mostPoints) {
                finalStrategy = theStrategy;
                mostPoints = points;
            }
        }
        if(finalStrategy != null) {
            strategies.add(finalStrategy);
        }
    }

    private int firstRun(int nextCard) {
        return myCards.get(Utils.random.nextInt(myCards.size()));
    }
}