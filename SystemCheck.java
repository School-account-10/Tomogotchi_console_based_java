import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
// for the future developers DO NOT MODIFY THIS
// this is the SELF CHECK SYSTEM
// this checks the underlying code of the system to make sure if modified we will know
public class SystemCheck {

    private static final String[] REQUIRED_CONFIGS = {"personalities.json", "quiz.json", "namesdb.json"};
    private static final String[] REQUIRED_DIRS = {"configs", "saves"};
    private static final String BACKUP_DIR = "configs/codebackup";
    private static final String BACKUP_FILE = "code_backup.json";

    public static final Scanner SCANNER = new Scanner(System.in);

    // Chat config
    public static boolean chatEnabled = false;
    public static String chatModel = null;
    public static boolean chatUseOllama = false;

    public static boolean run() {
        printCheckHeader();

        // Chat setup
        setupChat();

        boolean allGood = checkRequiredDirectories() && checkRequiredConfigFiles();

        System.out.println();
        if (!allGood) {
            System.out.println("  WARNING: Some required files are missing!");
            System.out.println("  The game may not work correctly.");
        }

        if (!runCodeBackupCheck(SCANNER)) {
            System.out.println();
            System.out.println("  Game not started. Goodbye!");
            return false;
        }

        return askForPermissionToRun();
    }

    private static void printCheckHeader() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("         SYSTEM CHECK");
        System.out.println("  ==========================================");
        System.out.println();
    }

    private static boolean checkRequiredDirectories() {
        boolean allGood = true;
        for (String dir : REQUIRED_DIRS) {
            Path dirPath = Paths.get(System.getProperty("user.dir"), dir);
            if (!Files.exists(dirPath)) {
                System.out.println("  [MISSING] Directory: " + dir + " -- creating it.");
                try {
                    Files.createDirectories(dirPath);
                } catch (Exception e) {
                    System.out.println("  [ERROR] Could not create directory: " + dir);
                    allGood = false;
                }
            } else {
                System.out.println("  [OK] Directory: " + dir);
            }
        }
        return allGood;
    }

    private static boolean checkRequiredConfigFiles() {
        boolean allGood = true;
        Path configsDir = Paths.get(System.getProperty("user.dir"), "configs");
        for (String config : REQUIRED_CONFIGS) {
            Path configPath = configsDir.resolve(config);
            if (!Files.exists(configPath)) {
                System.out.println("  [MISSING] Config file: " + config);
                allGood = false;
            } else {
                System.out.println("  [OK] Config: " + config);
            }
        }
        return allGood;
    }

    private static boolean askForPermissionToRun() {
        System.out.print("  Do you agree to run? (yes/no): ");
        String answer = SCANNER.nextLine().trim().toLowerCase();

        if (answer.equals("yes") || answer.equals("y")) {
            System.out.println("  System check passed. Starting game...");
            System.out.println();
            return true;
        }

        System.out.println("  Game not started. Goodbye!");
        return false;
    }

    // ---- Chat Setup ----

    private static void setupChat() {
        // Check if Ollama is installed first
        boolean ollamaInstalled = false;
        ProcessBuilder pb;
        Process p;
        try {
            // Try which ollama first
            pb = new ProcessBuilder("which", "ollama");
            p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                ollamaInstalled = true;
            } else {
                // Fallback: check common installation paths
                String[] ollamaPaths = {
                    "/usr/local/bin/ollama",
                    "/usr/bin/ollama",
                    "/opt/homebrew/bin/ollama",
                    System.getProperty("user.home") + "/.local/bin/ollama"
                };
                for (String path : ollamaPaths) {
                    java.io.File file = new java.io.File(path);
                    if (file.exists() && file.canExecute()) {
                        ollamaInstalled = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // If all else fails, assume not installed
            ollamaInstalled = false;
        }

        if (!ollamaInstalled) {
            System.out.println("  Ollama not found. Chat requires Ollama installed.");
            System.out.println("  Install Ollama: https://ollama.ai");
            chatEnabled = false;
            chatUseOllama = false;
            return;
        }

        // List available models first
        List<String> models = new ArrayList<>();
        try {
            pb = new ProcessBuilder("ollama", "list");
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            p.waitFor();
            String ollamaOutput = output.toString();
            for (String l : ollamaOutput.split("\s+")) {
                l = l.trim();
                if (l.isEmpty() || l.startsWith("NAME") || l.startsWith("---")) continue;
                String[] parts = l.split("\s+");
                if (parts.length > 0) models.add(parts[0]);
            }
        } catch (Exception e) {
            System.out.println("  Error listing models: " + e.getMessage());
        }

        // Filter to only chat-capable models (exclude embedding/OCR)
        List<String> chatModels = new ArrayList<>();
        for (String m : models) {
            if (isChatCapableModel(m)) {
                chatModels.add(m);
            }
        }

        // Show model options
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("           AVAILABLE MODELS");
        System.out.println("  ==========================================");
        System.out.println();
        if (chatModels.isEmpty()) {
            System.out.println("  No chat-capable models found. Chat will use JSON fallback.");
            chatUseOllama = false;
        } else {
            java.util.Map<String, String> modelDesc = new java.util.HashMap<>();
            modelDesc.put("qwen3-embedding:4b", "General purpose, balanced");
            modelDesc.put("qwen3-embedding:0.6b", "Fast, lightweight");
            modelDesc.put("nomic-embed-text:latest", "Text embeddings");
            modelDesc.put("glm-ocr:q8_0", "OCR / image text");
            modelDesc.put("nomic-embed-text-v2-moe:latest", "Advanced embeddings");
            for (int i = 0; i < chatModels.size(); i++) {
                String m = chatModels.get(i);
                String desc = modelDesc.getOrDefault(m, "Chat model");
                System.out.println("    " + (i + 1) + ". " + m + " - " + desc);
            }
            System.out.println();
            System.out.print("  Choose a model (1-" + chatModels.size() + "): ");
            while (true) {
                try {
                    int idx = Integer.parseInt(SCANNER.nextLine().trim()) - 1;
                    if (idx >= 0 && idx < chatModels.size()) {
                        chatModel = chatModels.get(idx);
                        System.out.println("  Using model: " + chatModel);
                        // Preload model into memory
                        System.out.print("  Loading model into memory...");
                        try {
                            ProcessBuilder preloadPb = new ProcessBuilder("ollama", "run", chatModel, "hi");
                            preloadPb.redirectErrorStream(true);
                            Process preloadP = preloadPb.start();
                            preloadP.waitFor();
                            System.out.println(" done!");
                        } catch (Exception e) {
                            System.out.println(" (will load on first chat)");
                        }
                        break;
                    }
                    System.out.print("  Invalid choice. Try again (1-" + chatModels.size() + "): ");
                } catch (NumberFormatException e) {
                    System.out.print("  Invalid choice. Try again (1-" + chatModels.size() + "): ");
                }
            }
        }

        System.out.println();
        System.out.print("  Enable chat? (yes/no): ");
        String answer = SCANNER.nextLine().trim().toLowerCase();
        if (!answer.equals("yes") && !answer.equals("y")) {
            System.out.println("  Chat disabled.");
            chatEnabled = false;
            return;
        }
        chatEnabled = true;
        chatUseOllama = !chatModels.isEmpty();

        System.out.println("  Chat enabled with " + chatModel + "!");
    }

    /**
     * Checks if a model can generate text (excludes embedding/OCR models).
     */
    public static boolean isChatCapableModel(String modelName) {
        if (modelName == null) return false;
        String lower = modelName.toLowerCase();
        // Exclude known non-chat model families
        if (lower.contains("embedding") || lower.contains("ocr")) return false;
        return true;
    }

    private static boolean runCodeBackupCheck(Scanner scanner) {
        Path backupDir = Paths.get(System.getProperty("user.dir"), BACKUP_DIR);
        Path backupPath = backupDir.resolve(BACKUP_FILE);

        printBackupHeader();

        List<String> currentFiles = listJavaFiles();
        if (currentFiles.isEmpty()) {
            System.out.println("  [ERROR] No .java files found. Cannot back up code.");
            return false;
        }

        if (!Files.exists(backupPath)) {
            System.out.println("  Hi, so you currently don't have a code backup.");
            System.out.println("  Let's create one (copying all code into " + BACKUP_DIR + "/" + BACKUP_FILE + ")...");
            if (!writeBackup(backupDir, backupPath, currentFiles, null)) {
                return false;
            }
            System.out.println();
            System.out.println("  Great, now that you have a file, let's do a final");
            System.out.println("  vanity check against the current code.");
            System.out.println();
        }

        String backupJson = readBackupJson(backupPath, currentFiles);
        if (backupJson == null) {
            return false;
        }

        BackupComparison comparison = compareBackupWithCurrent(backupJson, currentFiles);
        if (comparison.issues.isEmpty()) {
            System.out.println("  Vanity check successful, proceeding with program.");
            System.out.println();
            return true;
        }

        printComparisonIssues(comparison);
        return promptForBackupUpdate(scanner, backupDir, backupPath, backupJson, currentFiles);
    }

    private static void printBackupHeader() {
        System.out.println("  ==========================================");
        System.out.println("          CODE BACKUP");
        System.out.println("  ==========================================");
        System.out.println();
    }

    private static String readBackupJson(Path backupPath, List<String> currentFiles) {
        String backupJson;
        try {
            backupJson = new String(Files.readAllBytes(backupPath));
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not read backup file: " + e.getMessage());
            return null;
        }

        if (backupJson.indexOf("\"files\": {") == -1) {
            System.out.println("  [ERROR] Backup file is corrupted. Recreating it...");
            if (!writeBackup(backupPath.getParent(), backupPath, currentFiles, null)) {
                return null;
            }
            try {
                backupJson = new String(Files.readAllBytes(backupPath));
            } catch (IOException e) {
                System.out.println("  [ERROR] Could not read backup file: " + e.getMessage());
                return null;
            }
        }

        return backupJson;
    }

    private static class BackupComparison {
        List<String> issues = new ArrayList<>();
        StringBuilder diffReport = new StringBuilder();
    }

    private static BackupComparison compareBackupWithCurrent(String backupJson, List<String> currentFiles) {
        BackupComparison comparison = new BackupComparison();

        for (String file : currentFiles) {
            String backedUp = extractJsonFileContent(backupJson, file);
            if (backedUp == null) {
                comparison.issues.add(file + " (NEW - not in backup)");
                comparison.diffReport.append("  ").append(file).append(": new file, not in backup\n");
                continue;
            }

            String current = readFileToString(Paths.get(System.getProperty("user.dir"), file));
            if (current == null) {
                comparison.issues.add(file + " (UNREADABLE)");
                continue;
            }

            if (!backedUp.equals(current)) {
                comparison.issues.add(file + " (MODIFIED)");
                comparison.diffReport.append("  ").append(file).append(":\n");
                comparison.diffReport.append(diffLines(backedUp, current));
            }
        }

        for (String backedFileName : extractBackupFileNames(backupJson)) {
            if (!currentFiles.contains(backedFileName)) {
                comparison.issues.add(backedFileName + " (MISSING - erased from disk)");
                comparison.diffReport.append("  ").append(backedFileName).append(": MISSING (file was erased)\n");
            }
        }

        return comparison;
    }

    private static void printComparisonIssues(BackupComparison comparison) {
        System.out.println("  CODE CHANGES: backup does not match current code!");
        System.out.println();
        for (String issue : comparison.issues) {
            System.out.println("  [DIFF] " + issue);
        }
        System.out.println();
        System.out.println("  --- what changed (- = erased/old, + = added/new) ---");
        System.out.print(comparison.diffReport.toString());
        System.out.println("  ------------------------------------------------------");
        System.out.println();
    }

    private static boolean promptForBackupUpdate(Scanner scanner, Path backupDir, Path backupPath,
                                                 String backupJson, List<String> currentFiles) {
        System.out.print("  Is this code changed by you? (yes/no/bypass): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (answer.equals("bypass") || answer.equals("b") || answer.equals("skip")) {
            System.out.println();
            System.out.println("  Bypassing code backup check - the game will run,");
            System.out.println("  but the backup JSON will NOT be updated.");
            System.out.println();
            return true;
        }

        if (!answer.equals("yes") && !answer.equals("y")) {
            printCheckFailed();
            return false;
        }

        if (!confirmWithRandomCode(scanner,
                "Enter this randomly generated code below if you wanna",
                "modify the backup:")) {
            return false;
        }

        if (!confirmWithRandomCode(scanner,
                "Code accepted. Are you sure you wanna modify the backup?",
                "Enter this randomly generated code below to confirm:")) {
            return false;
        }

        String created = extractJsonStringField(backupJson, "created");
        if (!writeBackup(backupDir, backupPath, currentFiles, created)) {
            return false;
        }

        System.out.println();
        System.out.println("  Backup updated with the new code. Proceeding with program.");
        System.out.println();
        return true;
    }

    private static boolean confirmWithRandomCode(Scanner scanner, String promptLine1, String promptLine2) {
        String code = generateCode();
        System.out.println();
        System.out.println("  " + promptLine1);
        System.out.println("  " + promptLine2);
        System.out.println("  " + code);
        System.out.print("  > ");

        String entry = scanner.nextLine().trim();
        if (!entry.equalsIgnoreCase(code)) {
            System.out.println("  Wrong code. Backup was NOT modified.");
            printCheckFailed();
            return false;
        }
        return true;
    }

    private static void printCheckFailed() {
        System.out.println();
        System.out.println("  Vanity check failed. Code will not run unless you:");
        System.out.println("    1. restore the original code, or");
        System.out.println("    2. override the backup (run again and confirm with the codes).");
        System.out.println("  Backup location: " + BACKUP_DIR + "/" + BACKUP_FILE);
    }

    // ------------------------------------------------------------------
    //  BACKUP FILE HELPERS
    // ------------------------------------------------------------------

    private static List<String> listJavaFiles() {
        List<String> files = new ArrayList<>();
        try {
            Files.list(Paths.get(System.getProperty("user.dir")))
                .filter(p -> p.getFileName().toString().endsWith(".java"))
                .forEach(p -> files.add(p.getFileName().toString()));
        } catch (IOException e) {
            return files;
        }
        files.sort(Comparator.naturalOrder());
        return files;
    }

    private static boolean writeBackup(Path backupDir, Path backupPath, List<String> files, String created) {
        try {
            Files.createDirectories(backupDir);
            String createdValue = (created != null) ? created : LocalDateTime.now().toString();
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"version\": 1,\n");
            json.append("  \"created\": \"").append(createdValue).append("\",\n");
            json.append("  \"updated\": \"").append(LocalDateTime.now()).append("\",\n");
            json.append("  \"files\": {\n");
            for (int i = 0; i < files.size(); i++) {
                String file = files.get(i);
                String content = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), file)));
                json.append("    \"").append(escapeJson(file)).append("\": \"")
                    .append(escapeJson(content)).append("\"");
                if (i < files.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  }\n");
            json.append("}");
            Files.write(backupPath, json.toString().getBytes());
            System.out.println("  [OK] Code backup written: " + BACKUP_DIR + "/" + BACKUP_FILE + " (" + files.size() + " files)");
            return true;
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not write code backup: " + e.getMessage());
            return false;
        }
    }

    private static String readFileToString(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return null;
        }
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String extractJsonStringField(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private static String extractJsonFileContent(String json, String filename) {
        String search = "\"" + filename + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private static List<String> extractBackupFileNames(String json) {
        List<String> files = new ArrayList<>();
        String search = "\"files\": {";
        int start = json.indexOf(search);
        if (start == -1) return files;
        start += search.length();
        int end = json.indexOf("}", start);
        if (end == -1) return files;
        String obj = json.substring(start, end);
        String[] pairs = obj.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");
                files.add(key);
            }
        }
        return files;
    }

    private static String diffLines(String oldContent, String newContent) {
        StringBuilder diff = new StringBuilder();
        String[] oldLines = oldContent.split("\n");
        String[] newLines = newContent.split("\n");
        int maxLines = Math.max(oldLines.length, newLines.length);
        for (int i = 0; i < maxLines; i++) {
            String oldLine = i < oldLines.length ? oldLines[i] : "";
            String newLine = i < newLines.length ? newLines[i] : "";
            if (!oldLine.equals(newLine)) {
                diff.append("  - ").append(oldLine).append("\n");
                diff.append("  + ").append(newLine).append("\n");
            }
        }
        return diff.toString();
    }

    private static String generateCode() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append((char) ('A' + rnd.nextInt(26)));
        }
        return sb.toString();
    }
}
