import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public class MainPetmenu {
    private static Scanner scanner = new Scanner(System.in);
    private static SavingSystem savingSystem = new SavingSystem();
    private static Starter.pet currentPet;
    private static Map<String, Object> currentPersonality;

    public static void main(String[] args) {
        System.out.println("=== Welcome to Tomogotchi Pet Game ===");

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Create New Pet");
            System.out.println("2. Load Pet");
            if (currentPet != null) {
                System.out.println("3. Show Pet Status");
                System.out.println("4. Exit");
            } else {
                System.out.println("3. Exit");
            }
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createNewPet();
                    break;
                case "2":
                    loadPet();
                    break;
                case "3":
                    if (currentPet != null) {
                        showPetStatus();
                    } else {
                        System.out.println("Goodbye! Thanks for playing!");
                        return;
                    }
                    break;
                case "4":
                    if (currentPet != null) {
                        System.out.println("Goodbye! Thanks for playing!");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createNewPet() {
        System.out.println("\n=== CREATE NEW PET ===");
        System.out.println("Welcome to Tomogotchi! Let's create your new friend.");

        Starter.pet pet = new Starter.pet();

        System.out.print("What's your name, caretaker? ");
        pet.ownerName = scanner.nextLine();
        System.out.println("Nice to meet you, " + pet.ownerName + "!");

        String quizJson = savingSystem.loadConfig("quiz.json");
        List<String> validPets = savingSystem.extractJsonStringArray(quizJson, "validPets");

        System.out.println("\nChoose your pet type:");
        for (int i = 0; i < validPets.size(); i++) {
            System.out.println((i + 1) + ". " + validPets.get(i));
        }
        System.out.print("Enter pet type (1-" + validPets.size() + "): ");

        while (true) {
            try {
                int idx = Integer.parseInt(scanner.nextLine()) - 1;
                if (idx >= 0 && idx < validPets.size()) {
                    pet.petType = validPets.get(idx);
                    System.out.println("You chose: " + pet.petType + "!");
                    break;
                } else {
                    System.out.print("Oops! Not appropriate. Please try again (1-" + validPets.size() + "): ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Oops! Not appropriate. Please enter a number: ");
            }
        }

        currentPersonality = savingSystem.extractPetPersonality(pet.petType);
        
        String greeting = (String) currentPersonality.get("greeting");
        if (greeting != null && !greeting.isEmpty()) {
            System.out.println("\n" + greeting);
        }

        String namesDbJson = savingSystem.loadConfig("namesdb.json");
        List<String> blockedWords = savingSystem.extractJsonStringArray(namesDbJson, "blockedNames");
        int minLen = 2;
        int maxLen = 15;

        System.out.print("\nWhat would you like to name your pet? ");
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
                System.out.println("Great name: " + pet.petName + "!");
                break;
            } else {
                System.out.print("Oops! Not appropriate. Please try again: ");
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

        System.out.println("\n=== PET CREATED ===");
        showPetStatus(pet);

        savingSystem.savePet(pet);
        currentPet = pet;

        gameLoop();
    }

    private static void loadPet() {
        System.out.println("\n=== LOAD PET ===");

        List<String> saves = savingSystem.listSaveFiles();

        if (saves.isEmpty()) {
            System.out.println("No save files found. Create a new pet first.");
            return;
        }

        System.out.println("Available saves:");
        for (int i = 0; i < saves.size(); i++) {
            System.out.println((i + 1) + ". " + saves.get(i));
        }

        System.out.print("Choose a save (number): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < saves.size()) {
                Starter.pet loadedPet = savingSystem.loadPet(saves.get(idx));
                if (loadedPet != null) {
                    System.out.println("Pet loaded successfully!");
                    currentPet = loadedPet;
                    currentPersonality = savingSystem.extractPetPersonality(loadedPet.petType);
                    gameLoop();
                }
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private static void gameLoop() {
        System.out.println("\n=== GAME LOOP ===");
        System.out.println("Your pet " + currentPet.petName + " is ready for adventure!");

        while (true) {
            showPetStatus(currentPet);

            System.out.println("\n=== ACTIONS ===");
            System.out.println("1. Feed");
            System.out.println("2. Play");
            System.out.println("3. Save and Quit");
            System.out.println("4. Return to Main Menu");
            System.out.print("Choose an action: ");

            String action = scanner.nextLine();

            switch (action) {
                case "1":
                    feed();
                    break;
                case "2":
                    play();
                    break;
                case "3":
                    saveAndQuit();
                    return;
                case "4":
                    System.out.println("Returning to main menu...");
                    return;
                default:
                    System.out.println("Invalid action. Please try again.");
            }
        }
    }

    private static void showPetStatus() {
        if (currentPet == null) {
            System.out.println("No pet loaded.");
            return;
        }
        showPetStatus(currentPet);
    }

    private static void showPetStatus(Starter.pet pet) {
        System.out.println();
        System.out.println("--- PET STATUS ---");
        System.out.println("Name: " + pet.petName);
        System.out.println("Owner: " + pet.ownerName);
        System.out.println("Type: " + pet.petType);
        System.out.println("Happiness: " + pet.happiness + "/100");
        System.out.println("Hunger: " + pet.hunger + "/100");
        System.out.println("Hearts: " + pet.hearts);
        System.out.println("Correct Answers: " + pet.correctAnswers);
        System.out.println("Stage: " + pet.stage);
        System.out.println("Last seen: " + pet.lastSeen);
        
        if (currentPersonality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) currentPersonality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                System.out.println("Personality: " + String.join(", ", traits));
            }
        }
    }

    private static void feed() {
        currentPet.hunger = Math.min(100, currentPet.hunger + 20);
        currentPet.happiness = Math.min(100, currentPet.happiness + 5);
        
        String reaction = (String) currentPersonality.get("feedReaction");
        if (reaction != null && !reaction.isEmpty()) {
            System.out.println(currentPet.petName + ": " + reaction);
        } else {
            System.out.println(currentPet.petName + " munches happily! Hunger: " + currentPet.hunger);
        }
    }

    private static void play() {
        currentPet.happiness = Math.min(100, currentPet.happiness + 15);
        currentPet.hunger = Math.max(0, currentPet.hunger - 10);
        
        String reaction = (String) currentPersonality.get("playReaction");
        if (reaction != null && !reaction.isEmpty()) {
            System.out.println(currentPet.petName + ": " + reaction);
        } else {
            System.out.println(currentPet.petName + " plays excitedly! Happiness: " + currentPet.happiness);
        }
    }

    private static void saveAndQuit() {
        savingSystem.savePet(currentPet);
        System.out.println("Game saved successfully! See you next time!");
    }
}
