import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private Strategy myStrategy;
    private int numOfRandomGames = 1000;

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
            gameNumber++;
            System.out.println("Wins: " + wins + " out of " + gameNumber);
            if (gameNumber == 1) {
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
        if (gameNumber == 0)
            myCard = firstRun(nextCard);
        else
            myCard = myStrategy.giveCard(nextCard);
        giveCard(nextCard, myCard);
        return myCard;
    }

    public void evaluation() {
        int attempts = 1;
        int[] permArray;
        first:
        while (attempts < 16) {
            permArray = new int[15];
            Arrays.fill(permArray, -1); // Fill permArray with -1
            int[] testArray = new int[15];
            int index = 0;
            int start = attempts % 15;
            if (start == 0) start = 15;
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
                        int pointsWon = simulation.playGame();
                        if (pointsWon > 35) {
                            System.out.println(Arrays.toString(testArray));
                            myStrategy = new Counter(Arrays.stream(testArray).boxed().collect(Collectors.toList()), specialCardsPlayed);
                            break first;
                        }
                        winPoints[index] += pointsWon;
                        for (int m = 0; m < 15; m++) {
                            if (permArray[m] != testArray[m])
                                testArray[m] = -1;
                        }
                        testArray[index] = i;
                    }
                    index++;
                }
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
            attempts++;
        }
        System.out.println(Arrays.toString(specialCardsPlayed.toArray()));
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