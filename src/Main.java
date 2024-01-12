public class Main {
    public static void main(String[] args) throws Exception {
        HolsDerGeier holsDerGeier = new HolsDerGeier();
        holsDerGeier.neueSpieler(new FiveOrRandom(), new SewingsRitter());
        for(int i=0;i<10;i++) {
            holsDerGeier.ganzesSpiel();
            holsDerGeier.naechstesSpiel();
        }
    }
}