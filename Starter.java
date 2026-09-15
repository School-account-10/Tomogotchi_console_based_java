import java.time.LocalDateTime;


public class Starter {

    public static class pet { //
        // make pet variables // add more here
        public String petName;
        public int petId;
        public boolean running = true;
        public boolean ispetexisting;
        public int hunger;
        public int happiness;
        public LocalDateTime lastSeen;

    }

    public static void main(String[] args) {
        Menu.show();
    }

}
