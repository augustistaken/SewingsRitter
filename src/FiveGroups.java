import java.util.Random;

public class FiveGroups extends MyBot implements Strategy {
    @Override
    public int gibKarte(int naechsteKarte) {
        return giveCard(naechsteKarte);
    }

    @Override
    public int giveCard(int nextCard) {
        int myCard = 0;
        Random random = new Random();

        switch(nextCard) {
            case -5: case -4: case -3:
                do {
                    myCard = random.nextInt(4 - 1) + 1;
                } while(!myCards.contains(myCard));
                break;
            case -2: case -1: case 1:
                do {
                    myCard = random.nextInt(7 - 4) + 4;
                } while(!myCards.contains(myCard));
                break;
            case 2: case 3: case 4:
                do {
                    myCard = random.nextInt(10 - 7) + 7;
                } while(!myCards.contains(myCard));
                break;
            case 5: case 6: case 7:
                do {
                    myCard = random.nextInt(13 - 10) + 10;
                } while(!myCards.contains(myCard));
                break;
            case 8: case 9: case 10:
                do {
                    myCard = random.nextInt(16 - 13) + 13;
                } while(!myCards.contains(myCard));
                break;
        }

        return super.giveCard(nextCard, myCard);
    }

}
