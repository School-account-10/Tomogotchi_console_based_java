import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class MainPetmenu {
    private static Scanner scanner = SystemCheck.SCANNER;
    private static SavingSystem savingSystem = new SavingSystem();
    private static Starter.pet currentPet;
    private static Map<String, Object> currentPersonality;

    private static final int W = 50;

    private static int displayWidth(String s) {
        int w = 0;
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == 0xFE0F || cp == 0x200D)
                continue;
            if (cp >= 0x1F000 || cp == 0x2705 || cp == 0x274C)
                w += 2;
            else
                w += 1;
        }
        return w;
    }

    private static String fit(String s, int width) {
        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            int cw = displayWidth(new String(Character.toChars(cp)));
            if (w + cw > width)
                break;
            sb.appendCodePoint(cp);
            w += cw;
        }
        while (w < width) {
            sb.append(' ');
            w++;
        }
        return sb.toString();
    }

    private static void top() {
        System.out.println("╔" + "═".repeat(W) + "╗");
    }

    private static void divider() {
        System.out.println("╠" + "═".repeat(W) + "╣");
    }

    private static void bottom() {
        System.out.println("╚" + "═".repeat(W) + "╝");
    }

    private static void line(String text) {
        System.out.println("║" + fit("  " + text, W) + "║");
    }

    private static void centered(String text) {
        int total = Math.max(0, W - displayWidth(text));
        int left = total / 2;
        System.out.println("║" + fit(" ".repeat(left) + text, W) + "║");
    }

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
                    printGoodbye();
                    return;
                default:
                    System.out.println("  ⚠ Invalid choice. Please try again.");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        top();
        centered("🐾  TOMOGOTCHI  🐾");
        centered("Your Console Pet Adventure Starts Here!");
        bottom();
        System.out.println();
    }

    private static void printMenuBox() {
        top();
        centered("🏠 MAIN MENU");
        divider();
        line("1. 🆕 Create New Pet");
        line("2. 📂 Load Pet");
        line("3. ❌ Exit");
        bottom();
        System.out.print("▶ ");
    }

    private static void printGoodbye() {
        System.out.println();
        top();
        centered("👋 Goodbye! Thanks for playing! ♡");
        bottom();
    }

    private static void printActionMenu() {
        top();
        centered("🎮 ACTIONS");
        divider();
        line("1. 🍖 Feed");
        line("2. 🎾 Play");
        line("3. 📊 View Stats");
        line("4. 💾 Save & Quit");
        line("5. 🏠 Return to Main Menu");
        bottom();
        System.out.print("▶ ");
    }
    private static void currentPet(){

    }

    private static void printStatusHeader(Starter.pet pet) {
        System.out.println();
        top();
        centered("PET");
        divider();
        line("  (sprite display here)");
        divider();
        centered("📊 PET STATUS");
        divider();
        line(String.format("%-8s %s", "Name:", pet.petName));
        line(String.format("%-8s %s", "Owner:", pet.ownerName));
        line(String.format("%-8s %s", "Type:", pet.petType));
        line(String.format("%-8s %s", "Stage:", pet.stage));
        line(String.format("%-8s %d", "ID:", pet.petId));
        divider();
    }

    private static void printStatusBar(String label, int value, String emoji) {
        int bars = value / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < bars ? "█" : "░");
        }
        line(emoji + " " + String.format("%-10s %3d/100 [%s]", label, value, bar));
    }

    private static void printStatusFooter(Starter.pet pet) {
        divider();
        line("Hearts: " + pet.hearts + "    Correct Answers: " + pet.correctAnswers);
        line("Last seen: " + pet.lastSeen);
        if (currentPersonality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) currentPersonality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                line("Personality: " + String.join(", ", traits));
            }
        }
        bottom();
    }

    private static Starter.pet loadPet() {
        System.out.println();
        top();
        centered("📂 LOAD PET");
        bottom();

        List<String> saves = savingSystem.listSaveFiles();

        if (saves.isEmpty()) {
            System.out.println("  ⚠ No save files found. Create a new pet first.");
            return null;
        }

        System.out.println("  Available saves:");
        for (int i = 0; i < saves.size(); i++) {
            System.out.println("    " + (i + 1) + ". " + saves.get(i));
        }
        System.out.println("    " + (saves.size() + 1) + ". 🏠 Back to Main Menu");

        System.out.print("  Choose a save (number): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < saves.size()) {
                Starter.pet loadedPet = savingSystem.loadPet(saves.get(idx));
                if (loadedPet != null) {
                    System.out.println("  ✅ Pet loaded successfully!");
                    return loadedPet;
                }
            } else if (idx == saves.size()) {
                System.out.println("  ↩ Returning to main menu...");
                return null;
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
        top();

        centered("Your pet " + currentPet.petName + " is ready for adventure!");
        bottom();

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
        printStatusBar("Hunger", pet.hunger, "🍖");
        printStatusBar("Happiness", pet.happiness, "♡");
        printStatusFooter(pet);
    }
}