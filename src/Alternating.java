import java.util.List;
import java.util.stream.Collectors;

public class Alternating extends MyBot implements Strategy {
    private int currentStrategy = 0;
    @Override
    public void reset() {
        if (turnNumber == 15) {
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
        switch(currentStrategy % 3) {
            case 0:
                if((nextCard + 5) != 0 && myCards.contains((Integer) nextCard + 5))
                    myCard = nextCard + 5;
                else
                    myCard = myCards.get(Utils.random.nextInt(myCards.size()));
                break;
            case 1: case 2:
               myCard = myCards.get(Utils.random.nextInt(myCards.size()));
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

