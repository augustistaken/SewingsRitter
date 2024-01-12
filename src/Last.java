import java.util.List;

public class Last extends MyBot implements Strategy {
    private List<Integer> hisLastCards;
    private int index;
    public Last(List<Integer> hisLastCards) {
        this.hisLastCards = hisLastCards;
    }

    @Override
    public void reset() {
        super.reset();
        index = 0;
    }
    @Override
    public int giveCard(int nextCard) {
        index++;
        return hisLastCards.get(index-1);
    }

    @Override
    public int gibKarte(int naechsteKarte) {
        return giveCard(naechsteKarte);
    }

}
