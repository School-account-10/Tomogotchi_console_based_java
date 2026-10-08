import java.time.LocalDateTime;

public class Starter {

    public static class Pet {
        public String petName;
        public int petId;
        public boolean isPetExisting;
        public int hunger;
        public int happiness;
        public LocalDateTime lastSeen;
        public String ownerName;
        public String petType;
        public int hearts;
        public int correctAnswers;
        public int perfectScoresInStage;
        public int xp;
        public String stage;
    }

    public static void main(String[] args) {
        if (SystemCheck.run()) {
            MainPetmenu.main(args);
        }
    }
}
