import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class MainPetmenu {
    private static Scanner scanner = new Scanner(System.in);
    private static SavingSystem savingSystem = new SavingSystem();
    private static Starter.pet currentPet;
    private static Map<String, Object> currentPersonality;

    public static void main(String[] args) {
        printBanner();

        while (true) {
            printMenuBox();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    currentPet = NewGame.createNewPet(scanner, savingSystem);
                    if (currentPet != null) {
                        currentPersonality = savingSystem.extractPetPersonality(currentPet.petType);
                        gameLoop();
                    }
                    break;
                case "2":
                    currentPet = loadPet();
                    if (currentPet != null) {
                        currentPersonality = savingSystem.extractPetPersonality(currentPet.petType);
                        gameLoop();
                    }
                    break;
                case "3":
                    if (currentPet != null) {
                        showPetStatus(currentPet);
                    } else {
                        printGoodbye();
                        return;
                    }
                    break;
                case "4":
                    printGoodbye();
                    return;
                default:
                    System.out.println("║  ⚠ Invalid choice. Please try again.              ║");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       🐾  TOMOGOTCHI  🐾                        ║");
        System.out.println("║   Your Console Pet Adventure Starts Here!        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printMenuBox() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                 🏠 MAIN MENU                     ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1. 🆕 Create New Pet                            ║");
        System.out.println("║  2. 📂 Load Pet                                  ║");
        if (currentPet != null) {
            System.out.println("║  3. 📊 Show Pet Status                           ║");
            System.out.println("║  4. ❌ Exit                                      ║");
        } else {
            System.out.println("║  3. ❌ Exit                                      ║");
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.print("▶ ");
    }

    private static void printGoodbye() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  👋 Goodbye! Thanks for playing!  ♡              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private static void printActionMenu() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              🎮 ACTIONS                          ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1. 🍖 Feed                                      ║");
        System.out.println("║  2. 🎾 Play                                      ║");
        System.out.println("║  3. 📊 View Stats                                ║");
        System.out.println("║  4. 💾 Save & Quit                               ║");
        System.out.println("║  5. 🏠 Return to Main Menu                       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.print("▶ ");
    }

    private static void printStatusHeader(Starter.pet pet) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              📊 PET STATUS                       ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  Name:    %-38s║%n", pet.petName);
        System.out.printf("║  Owner:   %-38s║%n", pet.ownerName);
        System.out.printf("║  Type:    %-38s║%n", pet.petType);
        System.out.printf("║  Stage:   %-38s║%n", pet.stage);
        System.out.printf("║  ID:      %-38d║%n", pet.petId);
        System.out.println("╠══════════════════════════════════════════════════╣");
    }

    private static void printStatusBar(String label, int value, String emoji) {
        int bars = value / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < bars) bar.append("█");
            else bar.append("░");
        }
        System.out.printf("║  %s %s %-3d/100 [%-10s]║%n", emoji, label, value, bar.toString());
    }

    private static void printStatusFooter(Starter.pet pet) {
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  Hearts: %-2d    Correct Answers: %-3d            ║%n", pet.hearts, pet.correctAnswers);
        System.out.printf("║  Last seen: %-34s║%n", pet.lastSeen);
        if (currentPersonality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) currentPersonality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                System.out.printf("║  Personality: %-32s║%n", String.join(", ", traits));
            }
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private static Starter.pet loadPet() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                📂 LOAD PET                       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        List<String> saves = savingSystem.listSaveFiles();

        if (saves.isEmpty()) {
            System.out.println("  ⚠ No save files found. Create a new pet first.");
            return null;
        }

        System.out.println("  Available saves:");
        for (int i = 0; i < saves.size(); i++) {
            System.out.println("    " + (i + 1) + ". " + saves.get(i));
        }

        System.out.print("  Choose a save (number): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < saves.size()) {
                Starter.pet loadedPet = savingSystem.loadPet(saves.get(idx));
                if (loadedPet != null) {
                    System.out.println("  ✅ Pet loaded successfully!");
                    return loadedPet;
                }
            } else {
                System.out.println("  ⚠ Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠ Invalid input. Please enter a number.");
        }
        return null;
    }

    private static void gameLoop() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║          🎮 GAME LOOP STARTED                    ║");
        System.out.println("║  Your pet " + currentPet.petName + " is ready for adventure!        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        while (true) {
            showPetStatus(currentPet);
            printActionMenu();

            String action = scanner.nextLine();

            switch (action) {
                case "1":
                    feed();
                    break;
                case "2":
                    play();
                    break;
                case "3":
                    showPetStatus(currentPet);
                    break;
                case "4":
                    saveAndQuit();
                    return;
                case "5":
                    System.out.println("  ↩ Returning to main menu...");
                    return;
                default:
                    System.out.println("  ⚠ Invalid action. Please try again.");
            }
        }
    }

    private static void feed() {
        currentPet.hunger = Math.min(100, currentPet.hunger + 20);
        currentPet.happiness = Math.min(100, currentPet.happiness + 5);

        String reaction = (String) currentPersonality.get("feedReaction");
        if (reaction != null && !reaction.isEmpty()) {
            System.out.println("  " + currentPet.petName + ": " + reaction);
        } else {
            System.out.println("  " + currentPet.petName + " munches happily! Hunger: " + currentPet.hunger);
        }
    }

    private static void play() {
        currentPet.happiness = Math.min(100, currentPet.happiness + 15);
        currentPet.hunger = Math.max(0, currentPet.hunger - 10);

        String reaction = (String) currentPersonality.get("playReaction");
        if (reaction != null && !reaction.isEmpty()) {
            System.out.println("  " + currentPet.petName + ": " + reaction);
        } else {
            System.out.println("  " + currentPet.petName + " plays excitedly! Happiness: " + currentPet.happiness);
        }
    }

    private static void saveAndQuit() {
        savingSystem.savePet(currentPet);
        System.out.println("  💾 Game saved successfully! See you next time!");
    }

    private static void showPetStatus(Starter.pet pet) {
        printStatusHeader(pet);
        printStatusBar("😋 Hunger", pet.hunger, "🍖");
        printStatusBar("😊 Happiness", pet.happiness, "♡");
        printStatusFooter(pet);
    }
}