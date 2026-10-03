import java.time.LocalDateTime;

public class Starter {

    public static class pet {
        public String petName;
        public int petId;
        public boolean running = true;
        public boolean ispetexisting;
        public int hunger;
        public int happiness;
        public LocalDateTime lastSeen;
        public String ownerName;
        public String petType;
        public int hearts;
        public int correctAnswers;
        public String stage;
    }

    public static void main(String[] args) {
        if (SystemCheck.run()) {
            MainPetmenu.main(args);
        }
    }
}