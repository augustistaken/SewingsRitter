import java.util.ArrayList;
import java.util.List;

public abstract class MyBot extends HolsDerGeierSpieler {
    protected List<Integer> myCards = new ArrayList<>();
    protected List<Integer> hisCards = new ArrayList<>();
    protected List<Integer> specialCards = new ArrayList<>();

    protected List<Integer> myCardsPlayed = new ArrayList<>();
    protected List<Integer> hisCardsPlayed = new ArrayList<>();
    protected List<Integer> specialCardsPlayed = new ArrayList<>();

    protected int myPoints;
    protected int hisPoints;
    protected int pointsToWin;
    protected int hisGameNumber;
    protected int turnNumber;

    public MyBot() {
    }

    @Override
    public void reset() {
        if(getNummer() == 1)
            hisGameNumber = 1;
        else
            hisGameNumber = 0;

        for (int i = 1; i <= 15; i++) {
            myCards.add(i);
            hisCards.add(i);
        }

        for (int i = -5; i <= 10; i++) {
            if (i != 0) {
                specialCards.add(i);
            }
        }

        myCardsPlayed.clear();
        hisCardsPlayed.clear();
        specialCardsPlayed.clear();

        myPoints = 0;
        hisPoints = 0;
        pointsToWin = 0;
        turnNumber = 0;
    }


    protected int giveCard(int nextCard, int myCard) {
        if(turnNumber == 0) {
            specialCards.remove((Integer) nextCard);
            specialCardsPlayed.add(nextCard);
            myCards.remove((Integer) myCard);
            myCardsPlayed.add(myCard);
            pointsToWin = pointsToWin + nextCard;
            turnNumber++;
            return myCard;
        }
        int myLastCard = myCardsPlayed.get(myCardsPlayed.size() - 1);
        int hisLastCard = getHdg().letzterZug(hisGameNumber);
        hisCards.remove((Integer) hisLastCard);
        hisCardsPlayed.add(hisLastCard);
        givePoints(myLastCard, hisLastCard);

        specialCards.remove((Integer) nextCard);
        specialCardsPlayed.add(nextCard);
        myCards.remove((Integer) myCard);
        myCardsPlayed.add(myCard);
        pointsToWin = pointsToWin + nextCard;

        if (turnNumber == 14) {
            myLastCard = myCardsPlayed.get(myCardsPlayed.size() - 1);
            myCardsPlayed.add(myLastCard);
            hisLastCard = hisCards.remove(0);
            hisCardsPlayed.add(hisLastCard);
            givePoints(myLastCard, hisLastCard);
        }
        turnNumber++;
        return myCard;
    }


    private void givePoints(int myLastCard, int hisLastCard) {
        if (myLastCard != hisLastCard) {
            if(pointsToWin > 0) {
                if (myLastCard > hisLastCard) {
                    myPoints = myPoints + pointsToWin;
                } else {
                    hisPoints = hisPoints + pointsToWin;
                }
            } else {
                if (myLastCard < hisLastCard) {
                    myPoints = myPoints + pointsToWin;
                } else {
                    hisPoints = hisPoints + pointsToWin;
                }
            }
            pointsToWin = 0;
        }
    }


}
