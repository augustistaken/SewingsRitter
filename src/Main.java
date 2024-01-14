public class Main {
    public static void main(String[] args) throws Exception {
        HolsDerGeier holsDerGeier = new HolsDerGeier();
        holsDerGeier.neueSpieler(new SewingsRitter(), new Random());
        for(int i=0;i<1000;i++) {
            holsDerGeier.ganzesSpiel();
            holsDerGeier.naechstesSpiel();
        }
    }
}