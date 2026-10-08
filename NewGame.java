import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public class NewGame {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 15;

    public static Starter.Pet createNewPet(Scanner scanner, SavingSystem savingSystem) {
        printCreationHeader();

        Starter.Pet pet = new Starter.Pet();
        pet.ownerName = askForOwnerName(scanner);
        pet.petType = askForPetType(scanner, savingSystem);
        printGreeting(savingSystem, pet.petType);
        pet.petName = askForPetName(scanner, savingSystem);
        initializePetStats(pet, savingSystem);

        printPetCreatedBanner();
        showPetStatus(pet, savingSystem.extractPetPersonality(pet.petType));

        if (!askForRulesAgreement(scanner)) {
            return null;
        }



        savingSystem.savePet(pet);
        return pet;
    }

    private static boolean askForRulesAgreement(Scanner scanner) {
        String answer;
        printRules();
        System.out.print("  Do you agree to the rules? (yes/no): ");
        answer = null;
        answer = scanner.nextLine().trim().toLowerCase();
        scanner.next();
        switch(answer) {
            case "y":
                System.out.println("  Great! Let's get started.");
                return true;
            case "yes":
                System.out.println("  Great! Let's get started.");
                return true;
            case "n":
                System.out.println("  Okay, no hard feelings. Goodbye!");
                return false;
            case "no":
                System.out.println("  Okay, no hard feelings. Goodbye!");
                return false;
            default:
                printRules();
                askForRulesAgreement(scanner);
                return false;




        }

          }






    private static void printRules() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("            GAME RULES");
        System.out.println("  ==========================================");
        System.out.println("  1. Answer trivia questions to feed your pet.");
        System.out.println("  2. Correct answer: your pet gets fed and grows.");
        System.out.println("  3. Wrong answer: you lose 1 heart.");
        System.out.println("  4. Reach 10 correct answers to evolve your");
        System.out.println("     pet to Adult!");
        System.out.println("  5. Lose all 5 hearts and you must wait 10");
        System.out.println("     minutes before playing again.");
        System.out.println("  ==========================================");
        System.out.println();
    }

    private static void printCreationHeader() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("         CREATE NEW PET");
        System.out.println("  ==========================================");
        System.out.println("  Welcome to Tomogotchi! Let's create your new friend.");
        System.out.println();
    }

    private static String askForOwnerName(Scanner scanner) {
        System.out.print("  What's your name, caretaker? ");
        String ownerName = scanner.nextLine();
        System.out.println("  Nice to meet you, " + ownerName + "!");
        return ownerName;
    }

    private static String askForPetType(Scanner scanner, SavingSystem savingSystem) {
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
                    String petType = validPets.get(idx);
                    System.out.println("  You chose: " + petType + "!");
                    return petType;
                }
                System.out.print("  Oops! Not appropriate. Please try again (1-" + validPets.size() + "): ");
            } catch (NumberFormatException e) {
                System.out.print("  Oops! Not appropriate. Please enter a number: ");
            }
        }
    }

    private static void printGreeting(SavingSystem savingSystem, String petType) {
        Map<String, Object> personality = savingSystem.extractPetPersonality(petType);
        String greeting = (String) personality.get("greeting");
        if (greeting != null && !greeting.isEmpty()) {
            System.out.println();
            System.out.println("  " + greeting);
        }
    }

    private static String askForPetName(Scanner scanner, SavingSystem savingSystem) {
        String namesDbJson = savingSystem.loadConfig("namesdb.json");
        List<String> blockedWords = savingSystem.extractJsonStringArray(namesDbJson, "blockedNames");

        System.out.println();
        System.out.print("  What would you like to name your pet? ");
        while (true) {
            String petName = scanner.nextLine();
            if (isNameValid(petName, blockedWords)) {
                System.out.println("  Great name: " + petName + "!");
                return petName;
            }
            System.out.print("  Oops! Not appropriate. Please try again: ");
        }
    }

    private static boolean isNameValid(String name, List<String> blockedWords) {
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            return false;
        }
        for (String blocked : blockedWords) {
            if (name.toLowerCase().contains(blocked.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static void initializePetStats(Starter.Pet pet, SavingSystem savingSystem) {
        pet.hunger = (int) (Math.random() * 31) + 70;
        pet.happiness = (int) (Math.random() * 31) + 70;
        pet.petId = (int) (Math.random() * 99999) + 1;
        pet.lastSeen = LocalDateTime.now();
        pet.isPetExisting = true;
        pet.hearts = 5;
        pet.correctAnswers = 0;
        pet.xp = 0;
        pet.stage = savingSystem.getInitialStage();
    }

    private static void printPetCreatedBanner() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("           PET CREATED!");
        System.out.println("  ==========================================");
    }

    public static void showPetStatus(Starter.Pet pet, Map<String, Object> personality) {
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
}
