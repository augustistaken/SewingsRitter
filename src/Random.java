
public class Random extends MyBot implements Strategy {

    @Override
    public int gibKarte(int nextCard) {
        return giveCard(nextCard);
    }

    @Override
    public int giveCard(int nextCard) {
        int myCard = myCards.get(Utils.random.nextInt(myCards.size()));
        return super.giveCard(nextCard, myCard);
    }
}