import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private List<Integer> specialCards;
    private int index;

    private int enemyPoints;
    private int myPoints;
    private int points;

    private Strategy myPlayer;
    private Last enemyPlayer;

    public Simulation(List<Integer> specialCards, Strategy myStrategy, Last enemyStrategy) {
        this.specialCards = new ArrayList<>(specialCards);
        myPlayer = myStrategy;
        enemyPlayer = enemyStrategy;
    }

    private int generateSpecificSpecialCard() {
        return specialCards.get(index);
    }

    public void nextTurn() {

        int cardPlayedFor = generateSpecificSpecialCard();
        index++;
        points = points + cardPlayedFor;

        int enemyTurn = enemyPlayer.giveCard(cardPlayedFor);

        int myTurn = myPlayer.giveCard(cardPlayedFor);

        if (enemyTurn != myTurn) {
            if (enemyTurn > myTurn)
                enemyPoints = enemyPoints + points;
            else
                myPoints = myPoints + points;
            points = 0;
        }
    }

    public int playGame() {
        while (index < 15) {
            nextTurn();
        }
        return myPoints;
    }

    public void resetGame() {
        index = 0;
        ((MyBot) myPlayer).reset();
        ((MyBot) enemyPlayer).reset();
        enemyPoints = 0;
        myPoints = 0;
        points = 0;
    }

}
