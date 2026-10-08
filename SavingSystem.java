import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.LinkedHashMap;

public class SavingSystem {

    private static final String SAVE_FOLDER = "saves";

    private String osName;
    private Path saveDirectory;
    private Path configsDirectory;

    public String detectOperatingSystem() {
        osName = System.getProperty("os.name");
        osName = osName.replaceAll("\\s+", "").toLowerCase();
        getSaveDirectory();
        return osName;
    }

    public Path getSaveDirectory() {
        if (osName == null) detectOperatingSystem();
        if (osName.contains("linux") || osName.contains("mac") || osName.contains("win")) {
            saveDirectory = Paths.get(System.getProperty("user.dir"), SAVE_FOLDER);
            try {
                Files.createDirectories(saveDirectory);
                return saveDirectory;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return saveDirectory;
    }

    public Path getConfigsDir() {
        if (osName == null) detectOperatingSystem();
        configsDirectory = Paths.get(System.getProperty("user.dir"), "configs");
        try {
            Files.createDirectories(configsDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configsDirectory;
    }

    public String loadConfig(String filename) {
        Path configDir = getConfigsDir();
        Path configFile = configDir.resolve(filename);
        if (!Files.exists(configFile)) {
            System.out.println("Config file not found: " + filename);
            return null;
        }
        try {
            return new String(Files.readAllBytes(configFile));
        } catch (IOException e) {
            System.out.println("Error loading config: " + e.getMessage());
            return null;
        }
    }

    public List<String> extractJsonStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String search = "\"" + key + "\": [";
        int start = json.indexOf(search);
        if (start == -1) return result;
        start += search.length();
        int end = json.indexOf("]", start);
        if (end == -1) return result;
        String arrayContent = json.substring(start, end);
        String[] items = arrayContent.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"") && item.endsWith("\"")) {
                result.add(item.substring(1, item.length() - 1));
            }
        }
        return result;
    }

    public Map<String, Object> extractPetPersonality(String petType) {
        String personalitiesJson = loadConfig("personalities.json");
        if (personalitiesJson == null) return new HashMap<>();

        Map<String, Object> personality = new HashMap<>();
        String petSearch = "\"" + petType + "\": {";
        int petStart = personalitiesJson.indexOf(petSearch);
        if (petStart == -1) return personality;
        petStart += petSearch.length();

        int braceCount = 1;
        int i = petStart;
        while (i < personalitiesJson.length() && braceCount > 0) {
            char c = personalitiesJson.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            i++;
        }
        if (braceCount != 0) return personality;

        String petJson = personalitiesJson.substring(petStart, i - 1);

        personality.put("greeting", extractJsonString(petJson, "greeting"));
        personality.put("personality", extractJsonString(petJson, "personality"));
        personality.put("feedReaction", extractJsonString(petJson, "feedReaction"));
        personality.put("playReaction", extractJsonString(petJson, "playReaction"));
        personality.put("evolutionMessage", extractJsonString(petJson, "evolutionMessage"));

        personality.put("traits", extractJsonStringArray(petJson, "traits"));
        personality.put("likes", extractJsonStringArray(petJson, "likes"));
        personality.put("dislikes", extractJsonStringArray(petJson, "dislikes"));

        return personality;
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\": ";
        int start = json.indexOf(search);
        if (start == -1) return 0;
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("\n", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private boolean extractJsonBoolean(String json, String key) {
        String search = "\"" + key + "\": ";
        int start = json.indexOf(search);
        if (start == -1) return false;
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("\n", start);
        if (end == -1) end = json.indexOf("}", start);
        return Boolean.parseBoolean(json.substring(start, end).trim());
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public void savePet(Starter.Pet pet) {
        detectOperatingSystem();
        Path saveDir = getSaveDirectory();
        String safeOwner = pet.ownerName != null ? pet.ownerName.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
        String safePet = pet.petName != null ? pet.petName.replaceAll("[^a-zA-Z0-9]", "_") : "pet";
        Path saveFile = saveDir.resolve(safeOwner + "_" + safePet + ".json");

        try {
            Files.write(saveFile, buildPetJson(pet).getBytes());
            System.out.println("Pet saved successfully! File: " + saveFile.getFileName());
        } catch (IOException e) {
            System.out.println("Error saving pet: " + e.getMessage());
        }
    }

    private String buildPetJson(Starter.Pet pet) {
        return "{\n" +
            "  \"petName\": \"" + escapeJson(pet.petName) + "\",\n" +
            "  \"petId\": " + pet.petId + ",\n" +
            "  \"hunger\": " + pet.hunger + ",\n" +
            "  \"happiness\": " + pet.happiness + ",\n" +
            "  \"lastSeen\": \"" + pet.lastSeen + "\",\n" +
            "  \"ispetexisting\": " + pet.isPetExisting + ",\n" +
            "  \"ownerName\": \"" + escapeJson(pet.ownerName != null ? pet.ownerName : "") + "\",\n" +
            "  \"petType\": \"" + escapeJson(pet.petType != null ? pet.petType : "") + "\",\n" +
            "  \"hearts\": " + pet.hearts + ",\n" +
            "  \"correctAnswers\": " + pet.correctAnswers + ",\n" +
            "  \"xp\": " + pet.xp + ",\n" +
            "  \"stage\": \"" + escapeJson(pet.stage != null ? pet.stage : "egg") + "\"\n" +
            "}";
    }

    public Starter.Pet loadPet(String filename) {
        detectOperatingSystem();
        Path saveDir = getSaveDirectory();
        Path saveFile = saveDir.resolve(filename);

        if (!Files.exists(saveFile)) {
            System.out.println("Save file not found: " + filename);
            return null;
        }

        try {
            String content = new String(Files.readAllBytes(saveFile));
            Starter.Pet pet = parsePetFromJson(content);
            System.out.println("Pet loaded: " + pet.petName + " (ID: " + pet.petId + ")");
            return pet;
        } catch (Exception e) {
            System.out.println("Error loading pet: " + e.getMessage());
            return null;
        }
    }

    private Starter.Pet parsePetFromJson(String content) {
        Starter.Pet pet = new Starter.Pet();
        pet.petName = extractJsonString(content, "petName");
        pet.petId = extractJsonInt(content, "petId");
        pet.hunger = extractJsonInt(content, "hunger");
        pet.happiness = extractJsonInt(content, "happiness");
        pet.lastSeen = LocalDateTime.parse(extractJsonString(content, "lastSeen"));
        pet.isPetExisting = extractJsonBoolean(content, "ispetexisting");
        pet.ownerName = extractJsonString(content, "ownerName");
        pet.petType = extractJsonString(content, "petType");
        pet.hearts = extractJsonInt(content, "hearts");
        pet.correctAnswers = extractJsonInt(content, "correctAnswers");
        pet.xp = extractJsonInt(content, "xp");
        pet.stage = extractJsonString(content, "stage");
        return pet;
    }

    public List<String> listSaveFiles() {
        detectOperatingSystem();
        Path saveDir = getSaveDirectory();
        List<String> saves = new ArrayList<>();
        try {
            if (Files.exists(saveDir)) {
                Files.list(saveDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> saves.add(p.getFileName().toString()));
            }
        } catch (IOException e) {
            System.out.println("Error listing saves: " + e.getMessage());
        }
        return saves;
    }

    // ---- Stages ----

    public List<Map<String, Object>> loadStages() {
        String stagesJson = loadConfig("stages.json");
        if (stagesJson == null) return new ArrayList<>();
        return parseStagesJson(stagesJson);
    }

    private List<Map<String, Object>> parseStagesJson(String json) {
        List<Map<String, Object>> stages = new ArrayList<>();
        String search = "\"stages\": [";
        int start = json.indexOf(search);
        if (start == -1) return stages;
        start += search.length();
        int end = json.indexOf("]", start);
        if (end == -1) return stages;
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
                    stages.add(parseStageObject(obj));
                    objStart = -1;
                }
            }
        }
        return stages;
    }

    private Map<String, Object> parseStageObject(String obj) {
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("id", extractJsonInt(obj, "id"));
        stage.put("name", extractJsonString(obj, "name"));
        stage.put("minCorrectAnswers", extractJsonInt(obj, "minCorrectAnswers"));
        stage.put("xpToNext", extractJsonInt(obj, "xpToNext"));
        stage.put("description", extractJsonString(obj, "description"));
        return stage;
    }

    public String getInitialStage() {
        List<Map<String, Object>> stages = loadStages();
        if (stages.isEmpty()) return "egg";
        return (String) stages.get(0).get("name");
    }

    public String getStageForCorrectAnswers(int correctAnswers) {
        List<Map<String, Object>> stages = loadStages();
        String currentStage = stages.get(0).get("name").toString();
        for (Map<String, Object> stage : stages) {
            int min = (Integer) stage.get("minCorrectAnswers");
            if (correctAnswers >= min) {
                currentStage = (String) stage.get("name");
            } else {
                break;
            }
        }
        return currentStage;
    }

    public Map<String, Object> getStageInfo(String stageName) {
        List<Map<String, Object>> stages = loadStages();
        for (Map<String, Object> stage : stages) {
            if (stage.get("name").equals(stageName)) {
                return stage;
            }
        }
        return new HashMap<>();
    }

    public boolean isFinalStage(String stageName) {
        List<Map<String, Object>> stages = loadStages();
        if (stages.isEmpty()) return false;
        return stages.get(stages.size() - 1).get("name").equals(stageName);
    }

    public String getNextStage(String currentStage) {
        List<Map<String, Object>> stages = loadStages();
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).get("name").equals(currentStage)) {
                if (i + 1 < stages.size()) {
                    return (String) stages.get(i + 1).get("name");
                }
                return currentStage;
            }
        }
        return currentStage;
    }

    public int getXpToNext(String stageName) {
        List<Map<String, Object>> stages = loadStages();
        for (Map<String, Object> stage : stages) {
            if (stage.get("name").equals(stageName)) {
                return (Integer) stage.getOrDefault("xpToNext", 0);
            }
        }
        return 0;
    }

    // Returns perfect scores needed for next stage (1 for most stages)
    public int getPerfectScoresNeeded(String currentStage) {
        List<Map<String, Object>> stages = loadStages();
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).get("name").equals(currentStage)) {
                if (i + 1 < stages.size()) {
                    int currentMin = (Integer) stages.get(i).get("minCorrectAnswers");
                    int nextMin = (Integer) stages.get(i + 1).get("minCorrectAnswers");
                    return Math.max(1, nextMin - currentMin);
                }
                return 1;
            }
        }
        return 1;
    }

    public Map<String, Integer> getXpRewards() {
        String stagesJson = loadConfig("stages.json");
        if (stagesJson == null) return new HashMap<>();
        Map<String, Integer> rewards = new HashMap<>();
        String search = "\"xpRewards\": {";
        int start = stagesJson.indexOf(search);
        if (start == -1) return rewards;
        start += search.length();
        int end = stagesJson.indexOf("}", start);
        if (end == -1) return rewards;
        String obj = stagesJson.substring(start, end);
        String[] pairs = obj.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");


                int val = Integer.parseInt(kv[1].trim());
                rewards.put(key, val);
            }
        }
        return rewards;
    }

    public int getXpReward(String action) {
        Map<String, Integer> rewards = getXpRewards();
        return rewards.getOrDefault(action, 0);
    }

    // XP = military_time (HHMM) - 0.06 * stage_id
    public int calculateXpReward(int stageId) {
        LocalTime now = LocalTime.now();
        int militaryTime = now.getHour() * 100 + now.getMinute();
        double xp = militaryTime - (0.06 * stageId);
        return (int) Math.max(1, Math.round(xp));
    }

    // Chat XP: random, ~50-70% of regular XP
    public int getChatXpReward(int stageId) {
        int base = calculateXpReward(stageId);
        Random rnd = new Random();
        double multiplier = 0.5 + rnd.nextDouble() * 0.2; // 0.5 to 0.7
        return (int) Math.max(1, Math.round(base * multiplier));
    }

    // ---- Chat Rate Limiting ----
    private static final int MAX_CHAT_INTERACTIONS = 6;
    private static final long CHAT_COOLDOWN_MS = 4 * 60 * 60 * 1000; // 4 hours

    public boolean canChat() {
        List<Long> timestamps = loadChatTimestamps();
        long now = System.currentTimeMillis();
        // Remove timestamps older than 4 hours
        timestamps.removeIf(t -> (now - t) > CHAT_COOLDOWN_MS);
        saveChatTimestamps(timestamps);
        return timestamps.size() < MAX_CHAT_INTERACTIONS;
    }

    public int getRemainingChatSlots() {
        List<Long> timestamps = loadChatTimestamps();
        long now = System.currentTimeMillis();
        timestamps.removeIf(t -> (now - t) > CHAT_COOLDOWN_MS);
        saveChatTimestamps(timestamps);
        return Math.max(0, MAX_CHAT_INTERACTIONS - timestamps.size());
    }

    public void recordChatInteraction() {
        List<Long> timestamps = loadChatTimestamps();
        long now = System.currentTimeMillis();
        timestamps.add(now);
        saveChatTimestamps(timestamps);
    }

    private List<Long> loadChatTimestamps() {
        Path chatFile = Paths.get(System.getProperty("user.dir"), "saves", "chat_timestamps.json");
        List<Long> timestamps = new ArrayList<>();
        try {
            if (Files.exists(chatFile)) {
                String content = new String(Files.readAllBytes(chatFile));
                // Simple parsing: ["timestamp1","timestamp2",...]
                content = content.trim();
                if (content.startsWith("[") && content.endsWith("]")) {
                    content = content.substring(1, content.length() - 1);
                    for (String s : content.split(",")) {
                        s = s.trim();
                        if (!s.isEmpty()) {
                            timestamps.add(Long.parseLong(s));
                        }
                    }
                }
            }
        } catch (Exception e) {
            timestamps.clear();
        }
        return timestamps;
    }

    private void saveChatTimestamps(List<Long> timestamps) {
        try {
            Path chatFile = Paths.get(System.getProperty("user.dir"), "saves", "chat_timestamps.json");
            Files.createDirectories(chatFile.getParent());
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < timestamps.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(timestamps.get(i));
            }
            sb.append("]");
            Files.writeString(chatFile, sb.toString());
        } catch (Exception e) {
            // silently fail
        }
    }

    // ---- Sprites ----

    public List<String> getSprite(String petType) {
        String spritesJson = loadConfig("sprites.json");
        if (spritesJson == null) return new ArrayList<>();
        return parseSprite(spritesJson, petType);
    }

    private List<String> parseSprite(String json, String petType) {
        List<String> lines = new ArrayList<>();
        String search = "\"" + petType + "\": [";
        int start = json.indexOf(search);
        if (start == -1) return lines;
        start += search.length();
        int end = json.indexOf("]", start);
        if (end == -1) return lines;
        String arrayContent = json.substring(start, end);

        String[] items = arrayContent.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"") && item.endsWith("\"")) {
                String line = item.substring(1, item.length() - 1);
                line = unescapeJson(line);
                lines.add(line);
            }
        }
        return lines;
    }

    public String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                i++;
                char e = s.charAt(i);
                switch (e) {
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                        } else {
                            sb.append('u');
                        }
                        break;
                    default: sb.append(e);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---- Chat ----

    public String getChatResponse(Starter.Pet pet, String userMessage) {
        if (SystemCheck.chatUseOllama && SystemCheck.chatModel != null) {
            return getOllamaResponse(pet, userMessage);
        } else {
            return getFallbackResponse(pet, userMessage);
        }
    }

    private String getOllamaResponse(Starter.Pet pet, String userMessage) {
        try {
            String personality = (String) extractPetPersonality(pet.petType).getOrDefault("personality", "friendly");
            String prompt = "You are " + pet.petName + ", a " + personality + " " + pet.petType + ". Do not talk about complicated topics. Respond to: " + userMessage;
            ProcessBuilder pb = new ProcessBuilder("ollama", "run", SystemCheck.chatModel, prompt);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            p.waitFor();
            String result = output.toString().trim();
            // Limit to 600 tokens (~2400 chars)
            if (result.length() > 2400) {
                result = result.substring(0, 2400);
                int lastSpace = result.lastIndexOf(' ');
                if (lastSpace > 2000) result = result.substring(0, lastSpace);
            }
            return result;
        } catch (Exception e) {
            return "Sorry, I couldn't process that.";
        }
    }

    private String getFallbackResponse(Starter.Pet pet, String userMessage) {
        String responsesJson = loadConfig("chat_responses.json");
        if (responsesJson == null) return "Hmm...";

        String lowerMsg = userMessage.toLowerCase();
        List<String> responses = new ArrayList<>();

        if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            responses = extractJsonStringArray(responsesJson, "greetings");
        } else if (lowerMsg.contains("good") || lowerMsg.contains("great") || lowerMsg.contains("awesome")) {
            responses = extractJsonStringArray(responsesJson, "happy");
        } else if (lowerMsg.contains("sad") || lowerMsg.contains("bad") || lowerMsg.contains("sorry")) {
            responses = extractJsonStringArray(responsesJson, "sad");
        } else if (lowerMsg.contains("bye") || lowerMsg.contains("goodbye")) {
            responses = extractJsonStringArray(responsesJson, "goodbye");
        } else {
            responses = extractJsonStringArray(responsesJson, "default");
        }

        if (responses.isEmpty()) return "Hmm...";
        Random rnd = new Random();
        return responses.get(rnd.nextInt(responses.size()));
    }
}
