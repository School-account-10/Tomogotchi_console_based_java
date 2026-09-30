import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SavingSystem {
    Starter.pet iniVariables = new Starter.pet();
    Path AppdataDirectory;
    String COS;
    String name = "saves";
    Path configsDirectory;

    public String OSDetection() {
        COS = System.getProperty("os.name");
        COS = COS.replaceAll("\\s+", "").toLowerCase();
        FileSYS();
        return COS;
    }

    public Path FileSYS() {
        if (COS == null) OSDetection();
        if (COS.contains("linux") || COS.contains("mac") || COS.contains("win")) {
            AppdataDirectory = Paths.get(System.getProperty("user.dir"), name);
            try {
                Files.createDirectories(AppdataDirectory);
                return AppdataDirectory;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AppdataDirectory;
    }

    public Path getConfigsDir() {
        if (COS == null) OSDetection();
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

    public void savePet(Starter.pet pet) {
        OSDetection();
        Path saveDir = FileSYS();
        String safeOwner = pet.ownerName != null ? pet.ownerName.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
        String safePet = pet.petName != null ? pet.petName.replaceAll("[^a-zA-Z0-9]", "_") : "pet";
        Path saveFile = saveDir.resolve(safeOwner + "_" + safePet + ".json");

        String json = "{\n" +
            "  \"petName\": \"" + escapeJson(pet.petName) + "\",\n" +
            "  \"petId\": " + pet.petId + ",\n" +
            "  \"hunger\": " + pet.hunger + ",\n" +
            "  \"happiness\": " + pet.happiness + ",\n" +
            "  \"lastSeen\": \"" + pet.lastSeen + "\",\n" +
            "  \"ispetexisting\": " + pet.ispetexisting + ",\n" +
            "  \"ownerName\": \"" + escapeJson(pet.ownerName != null ? pet.ownerName : "") + "\",\n" +
            "  \"petType\": \"" + escapeJson(pet.petType != null ? pet.petType : "") + "\",\n" +
            "  \"hearts\": " + pet.hearts + ",\n" +
            "  \"correctAnswers\": " + pet.correctAnswers + ",\n" +
            "  \"stage\": \"" + escapeJson(pet.stage != null ? pet.stage : "egg") + "\"\n" +
            "}";

        try {
            Files.write(saveFile, json.getBytes());
            System.out.println("Pet saved successfully! File: " + saveFile.getFileName());
        } catch (IOException e) {
            System.out.println("Error saving pet: " + e.getMessage());
        }
    }

    public Starter.pet loadPet(String filename) {
        OSDetection();
        Path saveDir = FileSYS();
        Path saveFile = saveDir.resolve(filename);

        if (!Files.exists(saveFile)) {
            System.out.println("Save file not found: " + filename);
            return null;
        }

        try {
            String content = new String(Files.readAllBytes(saveFile));
            Starter.pet pet = new Starter.pet();
            pet.petName = extractJsonString(content, "petName");
            pet.petId = extractJsonInt(content, "petId");
            pet.hunger = extractJsonInt(content, "hunger");
            pet.happiness = extractJsonInt(content, "happiness");
            pet.lastSeen = LocalDateTime.parse(extractJsonString(content, "lastSeen"));
            pet.ispetexisting = extractJsonBoolean(content, "ispetexisting");
            pet.ownerName = extractJsonString(content, "ownerName");
            pet.petType = extractJsonString(content, "petType");
            pet.hearts = extractJsonInt(content, "hearts");
            pet.correctAnswers = extractJsonInt(content, "correctAnswers");
            pet.stage = extractJsonString(content, "stage");
            System.out.println("Pet loaded: " + pet.petName + " (ID: " + pet.petId + ")");
            return pet;
        } catch (Exception e) {
            System.out.println("Error loading pet: " + e.getMessage());
            return null;
        }
    }

    public List<String> listSaveFiles() {
        OSDetection();
        Path saveDir = FileSYS();
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
}
