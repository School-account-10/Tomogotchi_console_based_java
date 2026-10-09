import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Duration;

public class MainPetmenu {
    private static final Scanner scanner = SystemCheck.SCANNER;
    private static final SavingSystem savingSystem = new SavingSystem();
    private static Starter.Pet currentPet;
    private static Map<String, Object> currentPersonality;
    private static boolean renderWindows = true;

    private static int BOX_WIDTH = 50;

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
        printBoxLine("2. 🎾 Play (Trivia)");
        printBoxLine("3. 💬 Chat");
        printBoxLine("4. 📊 View Stats");
        printBoxLine("5. 💾 Save & Quit");

        printBoxBottom();
        System.out.print("▶ ");
    }

    private static void printStatusHeader(Starter.Pet pet) {
        System.out.println();
        printBoxTop();
        printBoxCentered("PET");
        printBoxDivider();
        printSprite(pet.petType, pet.stage);
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

    private static void printPetStatus(Starter.Pet pet) {
        // ─── Pet window (top): sprite + identity ───
        System.out.println();
        printBoxTop();
        printBoxCentered("🐾 " + pet.petName + " 🐾");
        List<String> sprite = savingSystem.getSprite(pet.petType.toLowerCase(), pet.stage);
        if (!sprite.isEmpty()) {
            for (String s : sprite) {
                printBoxCentered(s);
            }
        }
        printBoxDivider();
        printBoxLine(String.format("%-8s %s", "Name:", pet.petName));
        printBoxLine(String.format("%-8s %s", "Owner:", pet.ownerName));
        printBoxLine(String.format("%-8s %s", "Type:", pet.petType));
        printBoxLine(String.format("%-8s %s", "Stage:", pet.stage));
        printBoxBottom();

        // ─── Status window (below): live stats ───
        System.out.println();
        printBoxTop();
        printBoxCentered("📊 STATUS");
        printBoxDivider();
        printBoxLine("Hearts: " + pet.hearts);
        printXpBar(pet);
        int neededPerfect = savingSystem.getPerfectScoresNeeded(pet.stage);
        printBoxLine("⭐ " + String.format("%-10s %d/%d perfect quizzes", "Progress:", pet.perfectScoresInStage, neededPerfect));
        if (currentPersonality != null) {
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) currentPersonality.get("traits");
            if (traits != null && !traits.isEmpty()) {
                printBoxLine("Personality: " + String.join(", ", traits));
            }
        }
        printBoxBottom();
    }

    private static void printSprite(String petType, String stage) {
        List<String> sprite = savingSystem.getSprite(petType.toLowerCase(), stage);
        if (sprite.isEmpty()) {
            printBoxLine("  (no sprite for " + petType + ")");
            return;
        }
        for (String line : sprite) {
            printBoxLine("  " + line);
        }
    }

    private static void printStatusBar(String label, int value, String emoji) {
        int bars = value / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < bars ? "█" : "░");
        }
        printBoxLine(emoji + " " + String.format("%-10s %3d/100 [%s]", label, value, bar));
    }

    private static void printXpBar(Starter.Pet pet) {
        int xpToNext = savingSystem.getXpToNext(pet.stage);

        // Egg stage: show hatch progress bar
        if ("egg".equals(pet.stage)) {
            int neededPerfect = savingSystem.getPerfectScoresNeeded(pet.stage);
            int pct = neededPerfect > 0 ? (pet.perfectScoresInStage * 100) / neededPerfect : 0;
            int bars = pct / 10;
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                bar.append(i < bars ? "█" : "░");
            }
            printBoxLine("⭐ " + String.format("%-10s %d/%d [%s]", "HATCH:", pet.perfectScoresInStage, neededPerfect, bar));
            return;
        }

        // Adult stage: max level
        if (savingSystem.isFinalStage(pet.stage)) {
            printBoxLine("⭐ " + String.format("%-10s MAX LEVEL", "XP:"));
            return;
        }

        // Other stages: show XP progress
        if (xpToNext <= 0) {
            printBoxLine("⭐ " + String.format("%-10s MAX LEVEL", "XP:"));
            return;
        }
        int xp = pet.xp;
        int pct = Math.min(100, (xp * 100) / xpToNext);
        int bars = pct / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < bars ? "█" : "░");
        }
        printBoxLine("⭐ " + String.format("%-10s %4d/%4d [%s]", "XP:", xp, xpToNext, bar));
    }

    private static void printStatusFooter(Starter.Pet pet) {
        printBoxDivider();
        // Perfect quiz counter (always shown for all stages)
        int neededPerfect = savingSystem.getPerfectScoresNeeded(pet.stage);
        printBoxLine("⭐ " + String.format("%-10s %d/%d perfect quizzes", "Progress:", pet.perfectScoresInStage, neededPerfect));
        printBoxLine("Hearts: " + pet.hearts);
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
                // Initialize perfectScoresInStage for old save files that don't have it
                if (loadedPet.perfectScoresInStage == 0 && loadedPet.correctAnswers > 0) {
                    loadedPet.perfectScoresInStage = Math.min(loadedPet.correctAnswers,
                        savingSystem.getPerfectScoresNeeded(loadedPet.stage));
                }
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
        while (true) {
            renderWindows = true; // reset so pet/status windows return after View Stats suppressed them
            if (renderWindows) {
                printPetStatus(currentPet);
            }
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
                playTrivia();
                return false;
            case "3":
                chat();
                return false;
            case "4":
                showPetStatus(currentPet);
                renderWindows = false;
                return false;
            case "5":
                saveAndQuit();
                return true;
            default:
                System.out.println("  ⚠ Invalid action. Please try again.");
                return false;
        }
    }

    // ---- Chat ----

    private static void chat() {
        if (!SystemCheck.chatEnabled) {
            System.out.println("  Chat is disabled. Enable it in System Check.");
            System.out.println("  Press Enter to continue...");
            SystemCheck.SCANNER.nextLine();
            return;
        }

        int remaining = savingSystem.getRemainingChatSlots(currentPet);
        if (remaining <= 0) {
            System.out.println("  " + currentPet.petName + " is tired of chatting! Come back later.");
            System.out.println("  Press Enter to continue...");
            SystemCheck.SCANNER.nextLine();
            return;
        }

        System.out.println();
        printBoxTop();
        printBoxCentered("💬 CHAT");
        printBoxDivider();
        printBoxLine("  Talk to your pet! Type 'exit' to quit.");
        printBoxLine("  Remaining chats: " + remaining + "/6");
        printBoxBottom();
        System.out.println();

        while (true) {
            System.out.print("  You: ");
            String input = SystemCheck.SCANNER.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("  " + currentPet.petName + " says goodbye!");
                break;
            }
            if (input.isEmpty()) continue;

            remaining = savingSystem.getRemainingChatSlots(currentPet);
            if (remaining <= 0) {
                System.out.println();
                printBoxTop();
                printBoxCentered("💬 " + currentPet.petName);
                printBoxDivider();
                printBoxLine("  I'm all chatted out for now. See you later!");
                printBoxBottom();
                System.out.println();
                break;
            }

            String response = savingSystem.getChatResponse(currentPet, input);
            int chatXp = savingSystem.getChatXpReward(getStageId(currentPet.stage));
            currentPet.xp += chatXp;
            savingSystem.recordChatInteraction(currentPet);

            System.out.println();
            printBoxTop();
            printBoxCentered("💬 " + currentPet.petName);
            printBoxDivider();
            printBoxLine("  " + response);
            printBoxLine("  ⭐ +" + chatXp + " XP (chat)");
            printBoxBottom();
            System.out.println();

            checkLevelUp();
        }
    }

    private static void feed() {
        currentPet.hunger = Math.min(100, currentPet.hunger + 20);
        currentPet.happiness = Math.min(100, currentPet.happiness + 5);
        awardXp("feed");

        String reaction = (String) currentPersonality.get("feedReaction");
        if (reaction != null && !reaction.isEmpty()) {
            System.out.println("  " + currentPet.petName + ": " + reaction);
        } else {
            System.out.println("  " + currentPet.petName + " munches happily! Hunger: " + currentPet.hunger);
        }
    }

    private static void saveAndQuit() {
        savingSystem.savePet(currentPet);
        System.out.println("  💾 Game saved successfully! See you next time!");
    }

    // ---- XP System ----

    private static void awardXp(String action) {
        int stageId = getStageId(currentPet.stage);
        int xp = savingSystem.calculateXpReward(stageId);
        currentPet.xp += xp;
        System.out.println("  ⭐ +" + xp + " XP (" + currentPet.xp + "/" + savingSystem.getXpToNext(currentPet.stage) + ")");
        checkLevelUp();
    }

    private static int getStageId(String stageName) {
        List<Map<String, Object>> stages = savingSystem.loadStages();
        for (Map<String, Object> stage : stages) {
            if (stage.get("name").equals(stageName)) {
                return (Integer) stage.get("id");
            }
        }
        return 1;
    }

    private static void checkLevelUp() {
        boolean leveledUp = false;
        String oldStage = currentPet.stage;

        // Check XP track
        int xpToNext = savingSystem.getXpToNext(currentPet.stage);
        if (xpToNext > 0 && currentPet.xp >= xpToNext) {
            String nextStage = savingSystem.getNextStage(currentPet.stage);
            if (!nextStage.equals(currentPet.stage)) {
                currentPet.stage = nextStage;
                leveledUp = true;
            }
        }

        // Check perfect scores track (only if not already leveled up via XP)
        if (!leveledUp) {
            int neededPerfect = savingSystem.getPerfectScoresNeeded(currentPet.stage);
            if (neededPerfect > 0 && currentPet.perfectScoresInStage >= neededPerfect) {
                String nextStage = savingSystem.getNextStage(currentPet.stage);
                if (!nextStage.equals(currentPet.stage)) {
                    currentPet.stage = nextStage;
                    leveledUp = true;
                }
            }
        }

        if (leveledUp) {
            currentPet.xp = 0;
            currentPet.perfectScoresInStage = 0;
            showEvolutionMessage(oldStage, currentPet.stage);
        }
    }

    // ---- Trivia ----

    private static void playTrivia() {
        clearConsole();
        printBoxTop();
        printBoxCentered("🧠 TRIVIA");
        printBoxDivider();
        printBoxLine("Type 'exit' at any time to return to the menu.");
        printBoxBottom();
        System.out.println("  Let's play trivia! Press Enter to start.");
        scanner.nextLine();

        while (currentPet.hearts > 0) {
            Map<String, Object> question = getRandomQuestion();
            if (question == null) {
                clearConsole();
                printBoxTop();
                printBoxCentered("🧠 SESSION ENDED");
                printBoxDivider();
                printBoxLine("No more trivia questions available.");
                printBoxBottom();
                System.out.println("  Press Enter to return to the menu.");
                scanner.nextLine();
                return;
            }

            // Adaptive width: long code snippets/questions can exceed 50 chars, so size the
            // trivia box to fit the widest rendered line: the question (with the "Question: "
            // prefix) or any choice. We use display width so emoji / wide characters are counted
            // correctly, and include the 2-space prefix that printBoxLine adds.
            int originalBoxWidth = BOX_WIDTH;
            int widest = 0;
            String[] qLines = question.get("question").toString().split("\n", -1);
            for (int i = 0; i < qLines.length; i++) {
                String line = (i == 0) ? "Question: " + qLines[i] : qLines[i];
                widest = Math.max(widest, getDisplayWidth("  " + line) + 2);
            }
            BOX_WIDTH = Math.max(originalBoxWidth, widest);

            clearConsole();
            printBoxTop();
            printBoxCentered("🧠 TRIVIA TIME!");
            printBoxDivider();

            // Split multi-line questions (question + choices) so each line gets its own box borders
            for (int i = 0; i < qLines.length; i++) {
                if (i == 0) {
                    printBoxLine("Question: " + qLines[i]);
                } else {
                    printBoxLine(qLines[i]);
                }
            }

            printBoxDivider();
            printBoxLine("Hearts: " + currentPet.hearts + "/5");
            printBoxLine("Correct Answers: " + currentPet.correctAnswers);
            printBoxBottom();
            System.out.println();
            System.out.print("  Your answer (type 'exit' to quit trivia): ");
            String answer = scanner.nextLine().trim();

            BOX_WIDTH = originalBoxWidth; // revert width for pet/status windows

            if (answer.equalsIgnoreCase("exit")) {
                System.out.println("  Exiting trivia session...");
                return;
            }

            // Process the answer
            String correctAnswer = (String) question.get("answer");
            String oldStage = currentPet.stage;
            if (answer.equalsIgnoreCase(correctAnswer)) {
                handleCorrectAnswer();
            } else {
                System.out.println();
                System.out.println("  ❌ Wrong! The answer was: " + correctAnswer);
                currentPet.hearts--;
                awardXp("triviaWrong");

                if (currentPet.hearts <= 0) {
                    handleNoHearts();
                    return;
                }

                // If the pet evolved this round, the evolution message is the culminating event —
                // no conflicting heart-loss line should follow it. Otherwise show the normal
                // heart loss feedback.
                if (!oldStage.equals(currentPet.stage)) {
                    System.out.println("  " + currentPet.petName + " grew despite the mistake!");
                } else {
                    System.out.println("  " + currentPet.petName + " loses a heart! Hearts left: " + currentPet.hearts);
                }
            }

            // Let the user read the feedback before continuing to the next question
            System.out.println();
            System.out.println("  Press Enter for the next question...");
            scanner.nextLine();
        }
        // loop ends because the pet ran out of hearts; handleNoHearts() already handled game over
    }

    private static Map<String, Object> getRandomQuestion() {
        String quizJson = savingSystem.loadConfig("quiz.json");
        if (quizJson == null) return null;

        List<Map<String, Object>> questions = parseQuestionsJson(quizJson);
        if (questions.isEmpty()) return null;

        Random rnd = new Random();
        return questions.get(rnd.nextInt(questions.size()));
    }

    private static List<Map<String, Object>> parseQuestionsJson(String json) {
        List<Map<String, Object>> questions = new ArrayList<>();
        String search = "\"questions\": [";
        int start = json.indexOf(search);
        if (start == -1) return questions;
        start += search.length();
        int end = json.indexOf("]", start);
        if (end == -1) return questions;
        String arrayContent = json.substring(start, end);

        int braceCount = 0;
        int objStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (braceCount == 0) objStart = i;
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && objStart != -1) {
                    String obj = arrayContent.substring(objStart, i + 1);
                    questions.add(parseQuestionObject(obj));
                    objStart = -1;
                }
            }
        }
        return questions;
    }

    private static Map<String, Object> parseQuestionObject(String obj) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("question", extractJsonString(obj, "question"));
        q.put("answer", extractJsonString(obj, "answer"));
        return q;
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private static void handleCorrectAnswer() {
        System.out.println();
        System.out.println("  ✅ Correct! " + currentPet.petName + " is happy!");
        currentPet.correctAnswers++;
        currentPet.perfectScoresInStage++;
        currentPet.hunger = Math.min(100, currentPet.hunger + 15);
        currentPet.happiness = Math.min(100, currentPet.happiness + 10);
        awardXp("triviaCorrect");
        // checkLevelUp() is called inside awardXp() — checks both tracks

        if (savingSystem.isFinalStage(currentPet.stage)) {
            handleGameWon();
            return;
        }

        // No per-round pause here — playTrivia prints "Press Enter for the next question..."
        // between rounds so the trivia session keeps flowing.
    }


    private static void showEvolutionMessage(String oldStage, String newStage) {
        Map<String, Object> stageInfo = savingSystem.getStageInfo(newStage);
        String description = (String) stageInfo.getOrDefault("description", "");
        String personalityMsg = (String) currentPersonality.getOrDefault("evolutionMessage", "");

        System.out.println();
        printBoxTop();
        printBoxCentered("✨ EVOLUTION! ✨");
        printBoxDivider();
        printBoxLine(currentPet.petName + " evolved from " + oldStage + " to " + newStage + "!");
        if (!description.isEmpty()) {
            printBoxLine(description);
        }
        if (!personalityMsg.isEmpty()) {
            printBoxLine(personalityMsg);
        }
        printBoxBottom();
    }

    private static void handleGameWon() {
        System.out.println();
        printBoxTop();
        printBoxCentered("🏆 CONGRATULATIONS! 🏆");
        printBoxDivider();
        printBoxLine(currentPet.petName + " has reached ADULT stage!");
        printBoxLine("Total correct answers: " + currentPet.correctAnswers);
        printBoxDivider();
        printBoxLine("Thanks for playing! Your pet is now fully grown.");
        printBoxBottom();
        System.out.println();
        System.out.println("  Press Enter to return to main menu...");
        scanner.nextLine();
        savingSystem.savePet(currentPet);
        System.out.println("  💾 Final save complete.");
        System.exit(0);
    }

    private static void handleNoHearts() {
        System.out.println();
        printBoxTop();
        printBoxCentered("💔 NO HEARTS LEFT");
        printBoxDivider();
        printBoxLine(currentPet.petName + " is too tired to continue.");
        printBoxLine("You must wait 10 minutes before playing again.");
        printBoxBottom();
        System.out.println();

        currentPet.lastSeen = LocalDateTime.now();
        currentPet.hearts = 5; // reset for next session
        savingSystem.savePet(currentPet);

        System.out.println("  Game saved. Please wait 10 minutes before starting again.");
        System.out.println("  Press Enter to exit...");
        scanner.nextLine();
        System.exit(0);
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
        printXpBar(pet);
        printStatusFooter(pet);
    }
}
