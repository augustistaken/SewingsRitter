import java.util.*;
import java.util.stream.Collectors;

public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private Strategy nextStrategy = null;
    private List<Strategy> strategies = new ArrayList<>(1000);
    private List<Counter> pastGames = new ArrayList<>(1000);
    private int numOfTestGames = 100;
    private int numOfAnalysisGames = 15;
    private Simulation simulation;
    List<int[]> strategyPerformanceData = new ArrayList<>();
    private Strategy currentStrategy;

    public SewingsRitter() {
        super();
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
            System.out.println("Wins: " + wins + " out of " + (gameNumber + 1));
            evaluation();
            patternAnalysis();
            if(nextStrategy != null) {
                currentStrategy = nextStrategy;
                ((MyBot) currentStrategy).reset();
            }
            gameNumber++;
        }
        super.reset();
    }

    @Override
    public int gibKarte(int nextCard) {
        int myCard = 0;
        if(nextStrategy == null)
            myCard = firstRun(nextCard);
        else
            myCard = currentStrategy.giveCard(nextCard);
        giveCard(nextCard, myCard);
        return myCard;
    }

    private void patternAnalysis() {
        int pastGamesSize = pastGames.size();
        Strategy newStrategy = strategies.get(strategies.size() - 1); // Get the latest strategy
        int[] newStrategyPoints = new int[pastGamesSize]; // Array to store points for the new strategy

        // Evaluate the new strategy against all past games
        for (int j = 0; j < pastGamesSize; j++) {
            Counter pastStrategy = pastGames.get(j);
            for (int k = 0; k < numOfAnalysisGames; k++) {
                Collections.shuffle(specialCardsPlayed);
                simulation.resetSimulation();
                simulation.setSpecialCards(specialCardsPlayed);
                simulation.setStrategies(newStrategy, pastStrategy);
                newStrategyPoints[j] += simulation.playGame();
            }
        }
        applyDecayToPerformanceData();
        // Update the performance data for the new strategy
        strategyPerformanceData.add(newStrategyPoints);

        // Update performance data for past strategies to align with the new strategy
        updatePastStrategyPerformanceData(pastGamesSize);

        // Analyze the performance and update future strategies
        Map<Strategy, Double> performanceResults = analyzeStrategyPerformance();
        updateFutureStrategies(performanceResults);
    }

    private void updatePastStrategyPerformanceData(int pastGamesSize) {
        for (int i = 0; i < strategyPerformanceData.size() - 1; i++) {
            int[] currentData = strategyPerformanceData.get(i);
                int[] extendedData = new int[pastGamesSize];
                System.arraycopy(currentData, 0, extendedData, 0, currentData.length);
                Arrays.fill(extendedData, currentData.length, extendedData.length, -1);
                strategyPerformanceData.set(i, extendedData);
        }
    }

    private void applyDecayToPerformanceData() {
        for (int i = 0; i < strategyPerformanceData.size(); i++) {
            int[] data = strategyPerformanceData.get(i);
            for (int j = 0; j < data.length; j++) {
                if(data[j] == -1)
                    continue;
                data[j] = (int) (data[j] * Math.pow(0.99,(j + 1)));
            }
            strategyPerformanceData.set(i, data);
        }
    }


    private Map<Strategy, Double> analyzeStrategyPerformance() {
        Map<Strategy, Double> strategyPerformance = new HashMap<>();
        for (int i = 0; i < strategies.size(); i++) {
            double ema = calculateExponentialMovingAverage(strategyPerformanceData.get(i));
            strategyPerformance.put(strategies.get(i), ema);
        }
        return strategyPerformance;
    }

    private double calculateExponentialMovingAverage(int[] points) {
        double ema = 0.0;
        double smoothingFactor = 0.2; // You can adjust this factor as needed

        int validDataCount = 0; // Keep track of the number of valid data points

        if (points.length > 0) {
            for (int i = 0; i < points.length; i++) {
                if (points[i] != -1) {
                    if (validDataCount == 0) {
                        ema = points[i]; // Initialize EMA with the first valid data point
                    } else {
                        ema = smoothingFactor * points[i] + (1 - smoothingFactor) * ema;
                    }
                    validDataCount++;
                }
            }
        }

        return ema;
    }


    private void updateFutureStrategies(Map<Strategy, Double> performanceResults) {
        double epsilon = 0.1; // Probability of choosing a random strategy

        if (Math.random() < epsilon) {
            // Exploration: Choose a random strategy
            int randomIndex = Utils.random.nextInt(strategies.size());
            nextStrategy = strategies.get(randomIndex);
        } else {
            // Exploitation: Choose the best-performing strategy based on EMA scores
            List<Strategy> sortedStrategies = performanceResults.entrySet().stream()
                    .sorted(Map.Entry.<Strategy, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!sortedStrategies.isEmpty()) {
                nextStrategy = sortedStrategies.get(0);
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