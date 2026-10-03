import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public class NewGame {

    public static Starter.pet createNewPet(Scanner scanner, SavingSystem savingSystem) {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("         CREATE NEW PET");
        System.out.println("  ==========================================");
        System.out.println("  Welcome to Tomogotchi! Let's create your new friend.");
        System.out.println();

        Starter.pet pet = new Starter.pet();

        System.out.print("  What's your name, caretaker? ");
        pet.ownerName = scanner.nextLine();
        System.out.println("  Nice to meet you, " + pet.ownerName + "!");

        String quizJson = savingSystem.loadConfig("quiz.json");
        List<String> validPets = savingSystem.extractJsonStringArray(quizJson, "validPets");

        System.out.println();
        System.out.println("  Choose your pet type:");
        for (int i = 0; i < validPets.size(); i++) {
            System.out.println("    " + (i + 1) + ". " + validPets.get(i));
        }
        System.out.print("  Enter pet type (1-" + validPets.size() + "): ");

        while (true) {
            try {
                int idx = Integer.parseInt(scanner.nextLine()) - 1;
                if (idx >= 0 && idx < validPets.size()) {
                    pet.petType = validPets.get(idx);
                    System.out.println("  You chose: " + pet.petType + "!");
                    break;
                } else {
                    System.out.print("  Oops! Not appropriate. Please try again (1-" + validPets.size() + "): ");
                }
            } catch (NumberFormatException e) {
                System.out.print("  Oops! Not appropriate. Please enter a number: ");
            }
        }

        Map<String, Object> personality = savingSystem.extractPetPersonality(pet.petType);

        String greeting = (String) personality.get("greeting");
        if (greeting != null && !greeting.isEmpty()) {
            System.out.println();
            System.out.println("  " + greeting);
        }

        String namesDbJson = savingSystem.loadConfig("namesdb.json");
        List<String> blockedWords = savingSystem.extractJsonStringArray(namesDbJson, "blockedNames");
        int minLen = 2;
        int maxLen = 15;

        System.out.println();
        System.out.print("  What would you like to name your pet? ");
        while (true) {
            pet.petName = scanner.nextLine();
            boolean valid = true;

            if (pet.petName.length() < minLen || pet.petName.length() > maxLen) {
                valid = false;
            } else {
                for (String blocked : blockedWords) {
                    if (pet.petName.toLowerCase().contains(blocked.toLowerCase())) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                System.out.println("  Great name: " + pet.petName + "!");
                break;
            } else {
                System.out.print("  Oops! Not appropriate. Please try again: ");
            }
        }

        pet.hunger = (int) (Math.random() * 31) + 70;
        pet.happiness = (int) (Math.random() * 31) + 70;
        pet.petId = (int) (Math.random() * 99999) + 1;
        pet.lastSeen = LocalDateTime.now();
        pet.ispetexisting = true;
        pet.hearts = 5;
        pet.correctAnswers = 0;
        pet.stage = "Baby";

        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("           PET CREATED!");
        System.out.println("  ==========================================");
        showPetStatus(pet, personality);

        savingSystem.savePet(pet);
        return pet;
    }

    public static void showPetStatus(Starter.pet pet, Map<String, Object> personality) {
        System.out.println();
        System.out.println("  --- PET STATUS ---");
        System.out.println("  Name: " + pet.petName);
        System.out.println("  Owner: " + pet.ownerName);
        System.out.println("  Type: " + pet.petType);
        System.out.println("  Happiness: " + pet.happiness + "/100");
        System.out.println("  Hunger: " + pet.hunger + "/100");
        System.out.println("  Hearts: " + pet.hearts);
        System.out.println("  Correct Answers: " + pet.correctAnswers);
        System.out.println("  Stage: " + pet.stage);
        System.out.println("  Last seen: " + pet.lastSeen);

        if (personality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) personality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                System.out.println("  Personality: " + String.join(", ", traits));
            }
        }
    }
}// tampered line
