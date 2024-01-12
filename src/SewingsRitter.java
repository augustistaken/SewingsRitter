import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private Strategy myStrategy;
    private int numOfRandomGames = 10000;


    public SewingsRitter() {
        super();
    }
    @Override
    public void reset() {
        if(turnNumber == 15) {
            turnNumber = 0;

            System.out.println("Game " + gameNumber + ": " + myPoints + " - " + hisPoints);
            if(myPoints > hisPoints) {
                wins++;
            }
            gameNumber++;
            System.out.println("Wins: " + wins + " out of "+ gameNumber);
            if(gameNumber == 1) {
                evaluation();
            } else {
                ((MyBot) myStrategy).reset();
            }
        }
        super.reset();
    }
    @Override
    public int gibKarte(int nextCard) {
        int myCard;
        if(gameNumber == 0)
            myCard = firstRun(nextCard);
        else
            myCard = myStrategy.giveCard(nextCard);
        giveCard(nextCard, myCard);
        return myCard;
    }

        public void evaluation() {
        int attempts = 1;
        List<int[]> attemptArray = new ArrayList<>();
            int[] permArray;
            while(attempts< 16) {
                permArray = new int[15];
                for (int i = 0; i < 15; i++)
                    permArray[i] = -1;
                int[] testArray = new int[15];
                int index = 0;
                for (int i = attempts % 15; i < 16; i++) {
                    int[] winPoints = new int[15];
                    while (index < 15) {
                        for (int l = 0; l < 15; l++) {
                            if (permArray[l] == -1)
                                testArray[l] = -1;
                            else
                                testArray[l] = permArray[l];
                        }
                        testArray[index] = i;
                        for (int k = 0; k < numOfRandomGames; k++) {
                            for (int j = 0; j < 15; j++) {
                                if (testArray[j] == -1) {
                                    boolean inTestArray;
                                    do {
                                        int number = (int) (Math.random() * 15) + 1;
                                        inTestArray = false;
                                        for (int element : testArray) {
                                            if (element == number) {
                                                inTestArray = true;
                                                break;
                                            }
                                        }
                                        if (!inTestArray) {
                                            testArray[j] = number;
                                        }
                                    } while (inTestArray);
                                }
                            }

                            Simulation simulation = new Simulation(specialCardsPlayed, new Last(Arrays.stream(testArray).boxed().collect(Collectors.toList())), new Last(hisCardsPlayed));
                            winPoints[index] = winPoints[index] + simulation.playGame();
                            testArray[index] = i;
                        }
                        index++;
                    }
                    int j = 0;
                    int[] winnerIndices = sortIndicesByArrayValues(winPoints);
                    int winnerIndex = -1;
                    for (int pos : winnerIndices) {
                        if (permArray[pos] == -1) {
                            winnerIndex = pos;
                            break;
                        }

                    }
                    permArray[winnerIndex] = i;
                    index = 0;
                }
                for(int i = 1; i< attempts % 15; i++) {
                    int[] winPoints = new int[15];
                    while (index < 15) {
                        for (int l = 0; l < 15; l++) {
                            if (permArray[l] == -1)
                                testArray[l] = -1;
                            else
                                testArray[l] = permArray[l];
                        }
                        testArray[index] = i;
                        for (int k = 0; k < numOfRandomGames; k++) {
                            for (int j = 0; j < 15; j++) {
                                if (testArray[j] == -1) {
                                    boolean inTestArray;
                                    do {
                                        int number = (int) (Math.random() * 15) + 1;
                                        inTestArray = false;
                                        for (int element : testArray) {
                                            if (element == number) {
                                                inTestArray = true;
                                                break;
                                            }
                                        }
                                        if (!inTestArray) {
                                            testArray[j] = number;
                                        }
                                    } while (inTestArray);
                                }
                            }

                            Simulation simulation = new Simulation(specialCardsPlayed, new Last(Arrays.stream(testArray).boxed().collect(Collectors.toList())), new Last(hisCardsPlayed));
                            winPoints[index] = winPoints[index] + simulation.playGame();
                            testArray[index] = i;
                        }
                        index++;
                    }
                    int j = 0;
                    int[] winnerIndices = sortIndicesByArrayValues(winPoints);
                    int winnerIndex = -1;
                    for (int pos : winnerIndices) {
                        if (permArray[pos] == -1) {
                            winnerIndex = pos;
                            break;
                        }

                    }
                    permArray[winnerIndex] = i;
                    index = 0;
                }
                attemptArray.add(permArray);
                attempts++;
            }
            permArray = compareArrayPositions(attemptArray);
            System.out.println(Arrays.toString(permArray));
            System.out.println(Arrays.toString(specialCardsPlayed.toArray()));
            myStrategy = new Counter(Arrays.stream(permArray).boxed().collect(Collectors.toList()), specialCardsPlayed);
        }

    private int[] compareArrayPositions(List<int[]> grandArray) {
        int[] permArray = new int[15];
        int[][] frequencyCounter = new int[15][16]; // 15 positions, numbers 1 to 15

        // Counting frequencies
        for (int[] singleArray : grandArray) {
            for (int i = 0; i < singleArray.length; i++) {
                int number = singleArray[i];
                if (number >= 1 && number <= 15) {
                    frequencyCounter[i][number]++;
                }
            }
        }

        // Finding most frequent number for each position
        for (int i = 0; i < 15; i++) {
            int maxFrequency = 0;
            int mostFrequentNumber = 0;
            for (int j = 1; j <= 15; j++) {
                if (frequencyCounter[i][j] > maxFrequency) {
                    maxFrequency = frequencyCounter[i][j];
                    mostFrequentNumber = j;
                }
            }
            permArray[i] = mostFrequentNumber;
        }

        return permArray;
    }

    private int[] sortIndicesByArrayValues(int[] array) {
        Integer[] indices = IntStream.range(0, array.length).boxed().toArray(Integer[]::new);

        Arrays.sort(indices, Comparator.comparing(i -> array[i]));
        return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
    }


    public int firstRun(int nextCard) {
        int myCard = myCards.get(Utils.random.nextInt(myCards.size()));
        return myCard;
    }
}