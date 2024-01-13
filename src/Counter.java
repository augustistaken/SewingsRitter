import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Counter extends MyBot implements Strategy {
    private Map<Integer, Integer> specialMyCards = new HashMap<>();
    public Counter(List<Integer> myNewCards, List<Integer> specialCardsPlayed) {
        for(int i = 0; i < 15 ; i++) {
            specialMyCards.put(specialCardsPlayed.get(i), myNewCards.get(i));
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public int giveCard(int nextCard) {
        return specialMyCards.get(nextCard);
    }

    @Override
    public int gibKarte(int naechsteKarte) {
        return giveCard(naechsteKarte);
    }

}

