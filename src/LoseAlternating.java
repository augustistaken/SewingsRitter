import java.util.List;
import java.util.stream.Collectors;

public class LoseAlternating extends MyBot implements Strategy {
    private int currentStrategy = 0;
    private int gameNumber;
    private int wins;

    @Override
    public void reset() {
        if (turnNumber == 15) {
            gameNumber++;
            if(myPoints > hisPoints)
                wins++;
        }
        if((gameNumber - wins) % 10 == 0) {
            currentStrategy++;
        }
        super.reset();
    }
    @Override
    public int gibKarte(int naechsteKarte) {
        return giveCard(naechsteKarte);
    }

    @Override
    public int giveCard(int nextCard) {
        int myCard = 0;
        switch(currentStrategy % 5) {
            case 0:
                switch (nextCard) {
                    case -5: case -4: case -3:
                        myCard = findLowestCardInRange(1, 3);
                        break;
                    case -2: case -1: case 1:
                        myCard = findHighestCardInRange(4, 6);
                        break;
                    case 2: case 3: case 4:
                        myCard = findMiddleCardInRange(7, 9);
                        break;
                    case 5: case 6: case 7:
                        myCard = findRandomCardInRange(10, 12);
                        break;
                    case 8: case 9: case 10:
                        myCard = findLowestCardInRange(13, 15);
                        break;
                }
                break;
            case 1:
                switch (nextCard) {
                    case -5: case -4: case -3:
                        myCard = findHighestCardInRange(1, 3);
                        break;
                    case -2: case -1: case 1:
                        myCard = findLowestCardInRange(4, 6);
                        break;
                    case 2: case 3: case 4:
                        myCard = findRandomCardInRange(7, 9);
                        break;
                    case 5: case 6: case 7:
                        myCard = findMiddleCardInRange(10, 12);
                        break;
                    case 8: case 9: case 10:
                        myCard = findHighestCardInRange(13, 15);
                        break;
                }
                break;
            case 2:
                switch (nextCard) {
                    case -5: case -4: case -3:
                        myCard = findMiddleCardInRange(1, 3);
                        break;
                    case -2: case -1: case 1:
                        myCard = findRandomCardInRange(4, 6);
                        break;
                    case 2: case 3: case 4:
                        myCard = findLowestCardInRange(7, 9);
                        break;
                    case 5: case 6: case 7:
                        myCard = findHighestCardInRange(10, 12);
                        break;
                    case 8: case 9: case 10:
                        myCard = findMiddleCardInRange(13, 15);
                        break;
                }
                break;
            case 3:
                switch (nextCard) {
                    case -5: case -4: case -3:
                        myCard = findRandomCardInRange(1, 3);
                        break;
                    case -2: case -1: case 1:
                        myCard = findMiddleCardInRange(4, 6);
                        break;
                    case 2: case 3: case 4:
                        myCard = findHighestCardInRange(7, 9);
                        break;
                    case 5: case 6: case 7:
                        myCard = findLowestCardInRange(10, 12);
                        break;
                    case 8: case 9: case 10:
                        myCard = findRandomCardInRange(13, 15);
                        break;
                }
                break;
            case 4:
                switch (nextCard) {
                    case -5: case -4: case -3:
                        myCard = findHighestCardInRange(1, 3);
                        break;
                    case -2: case -1: case 1:
                        myCard = findMiddleCardInRange(4, 6);
                        break;
                    case 2: case 3: case 4:
                        myCard = findRandomCardInRange(7, 9);
                        break;
                    case 5: case 6: case 7:
                        myCard = findRandomCardInRange(10, 12);
                        break;
                    case 8: case 9: case 10:
                        myCard = findLowestCardInRange(13, 15);
                        break;
                }
                break;
        }
        return super.giveCard(nextCard, myCard);
    }

    private int findLowestCardInRange(int start, int end) {
        return myCards.stream()
                .filter(card -> card >= start && card <= end)
                .min(Integer::compare)
                .orElse(-1);
    }

    private int findHighestCardInRange(int start, int end) {
        return myCards.stream()
                .filter(card -> card >= start && card <= end)
                .max(Integer::compare)
                .orElse(-1);
    }

    private int findMiddleCardInRange(int start, int end) {
        List<Integer> cardsInRange = myCards.stream()
                .filter(card -> card >= start && card <= end)
                .sorted()
                .collect(Collectors.toList());
        if (!cardsInRange.isEmpty()) {
            return cardsInRange.get(cardsInRange.size() / 2);
        }
        return -1;
    }

    private int findRandomCardInRange(int start, int end) {
        List<Integer> cardsInRange = myCards.stream()
                .filter(card -> card >= start && card <= end)
                .collect(Collectors.toList());
        if (!cardsInRange.isEmpty()) {
            int randomIndex = Utils.random.nextInt(cardsInRange.size());
            return cardsInRange.get(randomIndex);
        }
        return -1;
    }


}


