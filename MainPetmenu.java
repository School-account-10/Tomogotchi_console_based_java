import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainPetmenu {
    private static final Scanner scanner = SystemCheck.SCANNER;
    private static final SavingSystem savingSystem = new SavingSystem();
    private static Starter.Pet currentPet;
    private static Map<String, Object> currentPersonality;

    private static final int BOX_WIDTH = 50;

    private static int getDisplayWidth(String s) {
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

    private static String fitToWidth(String s, int width) {
        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            int cw = getDisplayWidth(new String(Character.toChars(cp)));
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

    private static void printBoxTop() {
        System.out.println("╔" + "═".repeat(BOX_WIDTH) + "╗");
    }

    private static void printBoxDivider() {
        System.out.println("╠" + "═".repeat(BOX_WIDTH) + "╣");
    }

    private static void printBoxBottom() {
        System.out.println("╚" + "═".repeat(BOX_WIDTH) + "╝");
    }

    private static void printBoxLine(String text) {
        System.out.println("║" + fitToWidth("  " + text, BOX_WIDTH) + "║");
    }

    private static void printBoxCentered(String text) {
        int total = Math.max(0, BOX_WIDTH - getDisplayWidth(text));
        int left = total / 2;
        System.out.println("║" + fitToWidth(" ".repeat(left) + text, BOX_WIDTH) + "║");
    }

    public static void main(String[] args) {
        printBanner();

        while (true) {
            printMainMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            if (handleMainMenuChoice(choice)) {
                return;
            }
        }
    }

    private static boolean handleMainMenuChoice(String choice) {
        switch (choice) {
            case "1":
                currentPet = NewGame.createNewPet(scanner, savingSystem);
                if (currentPet != null) {
                    currentPersonality = savingSystem.extractPetPersonality(currentPet.petType);
                    gameLoop();
                } else {
                    printGoodbye();
                    return true;
                }
                return false;
            case "2":
                currentPet = loadPet();
                if (currentPet != null) {
                    currentPersonality = savingSystem.extractPetPersonality(currentPet.petType);
                    gameLoop();
                }
                return false;
            case "3":
                printGoodbye();
                return true;
            default:
                System.out.println("  ⚠ Invalid choice. Please try again.");
                return false;
        }
    }

    private static void printBanner() {
        System.out.println();
        printBoxTop();
        printBoxCentered("🐾  TOMOGOTCHI  🐾");
        printBoxCentered("Your Console Pet Adventure Starts Here!");
        printBoxBottom();
        System.out.println();
    }

    private static void printMainMenu() {
        printBoxTop();
        printBoxCentered("🏠 MAIN MENU");
        printBoxDivider();
        printBoxLine("1. 🆕 Create New Pet");
        printBoxLine("2. 📂 Load Pet");
        printBoxLine("3. ❌ Exit");
        printBoxBottom();
        System.out.print("▶ ");
    }

    private static void printGoodbye() {
        System.out.println();
        printBoxTop();
        printBoxCentered("👋 Goodbye! Thanks for playing! ♡");
        printBoxBottom();
    }

    private static void printActionMenu() {
        printBoxTop();
        printBoxCentered("🎮 ACTIONS");
        printBoxDivider();
        printBoxLine("1. 🍖 Feed");
        printBoxLine("2. 🎾 Play");
        printBoxLine("3. 📊 View Stats");
        printBoxLine("4. 💾 Save & Quit");
        printBoxLine("5. 🏠 Return to Main Menu");
        printBoxBottom();
        System.out.print("▶ ");
    }

    private static void printStatusHeader(Starter.Pet pet) {
        System.out.println();
        printBoxTop();
        printBoxCentered("PET");
        printBoxDivider();
        printBoxLine("  (sprite display here)");
        printBoxDivider();
        printBoxCentered("📊 PET STATUS");
        printBoxDivider();
        printBoxLine(String.format("%-8s %s", "Name:", pet.petName));
        printBoxLine(String.format("%-8s %s", "Owner:", pet.ownerName));
        printBoxLine(String.format("%-8s %s", "Type:", pet.petType));
        printBoxLine(String.format("%-8s %s", "Stage:", pet.stage));
        printBoxLine(String.format("%-8s %d", "ID:", pet.petId));
        printBoxLine(String.format("%-8s %s", "Time:", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        printBoxDivider();
    }

    private static void printStatusBar(String label, int value, String emoji) {
        int bars = value / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < bars ? "█" : "░");
        }
        printBoxLine(emoji + " " + String.format("%-10s %3d/100 [%s]", label, value, bar));
    }

    private static void printStatusFooter(Starter.Pet pet) {
        printBoxDivider();
        printBoxLine("Hearts: " + pet.hearts + "    Correct Answers: " + pet.correctAnswers);
        printBoxLine("Last seen: " + pet.lastSeen);
        if (currentPersonality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) currentPersonality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                printBoxLine("Personality: " + String.join(", ", traits));
            }
        }
        printBoxBottom();
    }

    private static Starter.Pet loadPet() {
        System.out.println();
        printBoxTop();
        printBoxCentered("📂 LOAD PET");
        printBoxBottom();

        List<String> saves = savingSystem.listSaveFiles();

        if (saves.isEmpty()) {
            System.out.println("  ⚠ No save files found. Create a new pet first.");
            return null;
        }

        printSaveOptions(saves);
        System.out.print("  Choose a save (number): ");
        int selection = parseSaveSelection(scanner.nextLine(), saves.size());

        if (selection >= 0 && selection < saves.size()) {
            Starter.Pet loadedPet = savingSystem.loadPet(saves.get(selection));
            if (loadedPet != null) {
                System.out.println("  ✅ Pet loaded successfully!");
                return loadedPet;
            }
        } else if (selection == saves.size()) {
            System.out.println("  ↩ Returning to main menu...");
        } else {
            System.out.println("  ⚠ Invalid selection.");
        }
        return null;
    }

    private static void printSaveOptions(List<String> saves) {
        System.out.println("  Available saves:");
        for (int i = 0; i < saves.size(); i++) {
            System.out.println("    " + (i + 1) + ". " + saves.get(i));
        }
        System.out.println("    " + (saves.size() + 1) + ". 🏠 Back to Main Menu");
    }

    private static int parseSaveSelection(String input, int saveCount) {
        try {
            return Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void gameLoop() {
        System.out.println();
        printBoxTop();
        printBoxCentered("Your pet " + currentPet.petName + " is ready for adventure!");
        printBoxBottom();

        while (true) {
            showPetStatus(currentPet);
            printActionMenu();

            String action = scanner.nextLine();

            if (handleAction(action)) {
                return;
            }
        }
    }

    private static boolean handleAction(String action) {
        switch (action) {
            case "1":
                feed();
                return false;
            case "2":
                play();
                return false;
            case "3":
                showPetStatus(currentPet);
                return false;
            case "4":
                saveAndQuit();
                return true;
            case "5":
                System.out.println("  ↩ Returning to main menu...");
                return true;
            default:
                System.out.println("  ⚠ Invalid action. Please try again.");
                return false;
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

    private static void clearConsole() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    private static void showPetStatus(Starter.Pet pet) {
        clearConsole();
        printStatusHeader(pet);
        printStatusBar("Hunger", pet.hunger, "🍖");
        printStatusBar("Happiness", pet.happiness, "♡");
        printStatusFooter(pet);
    }
}
