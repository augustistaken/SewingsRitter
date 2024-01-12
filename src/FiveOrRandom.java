
public class FiveOrRandom extends MyBot implements Strategy {

    @Override
    public int gibKarte(int nextCard) {
        return giveCard(nextCard);
    }

    @Override
    public int giveCard(int nextCard) {
        int myCard;
        if((nextCard + 5) != 0 && myCards.contains((Integer) nextCard + 5))
            myCard = nextCard + 5;
        else
            myCard = myCards.get(Utils.random.nextInt(myCards.size()));
        return super.giveCard(nextCard, myCard);
    }
}