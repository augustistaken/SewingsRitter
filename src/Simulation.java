import java.util.List;

public class Simulation {

    private List<Integer> specialCards;
    private int index;

    private int myPoints;
    private int points;

    private Strategy myPlayer;
    private Strategy enemyPlayer;

    private int generateSpecificSpecialCard() {
        return specialCards.get(index);
    }

    public void nextTurn() {

        int cardPlayedFor = generateSpecificSpecialCard();
        index++;

        points = points + cardPlayedFor;

        int enemyTurn = enemyPlayer.giveCard(cardPlayedFor);

        int myTurn = myPlayer.giveCard(cardPlayedFor);

        if (myTurn != enemyTurn) {
            if(points > 0) {
                if (myTurn > enemyTurn) {
                    myPoints = myPoints + points;
                }
            } else {
                if (myTurn < enemyTurn) {
                    myPoints = myPoints + points;
                }
            }
            points = 0;
        }
    }

    public int playGame() {
        while (index < 15) {
            nextTurn();
        }
        return myPoints;
    }

    public void resetSimulation() {
        index = 0;
        myPoints = 0;
        points = 0;
    }

    public void setSpecialCards(List<Integer> specialCards) {
        this.specialCards = specialCards;
    }

    public void setStrategies(Strategy myStrategy, Strategy enemyStrategy) {
        myPlayer = myStrategy;
        enemyPlayer = enemyStrategy;
    }

}
