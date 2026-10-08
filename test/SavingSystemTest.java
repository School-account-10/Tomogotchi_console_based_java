//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class SavingSystemTest {
//
//    private static final String STAGES_JSON =
//        "{\n" +
//        "  \"stages\": [\n" +
//        "    {\"id\": 1, \"name\": \"egg\", \"minCorrectAnswers\": 0, \"xpToNext\": 10, \"description\": \"An egg\"},\n" +
//        "    {\"id\": 2, \"name\": \"baby\", \"minCorrectAnswers\": 3, \"xpToNext\": 20, \"description\": \"A baby\"},\n" +
//        "    {\"id\": 3, \"name\": \"adult\", \"minCorrectAnswers\": 10, \"xpToNext\": 0, \"description\": \"An adult\"}\n" +
//        "  ],\n" +
//        "  \"xpRewards\": {\"correct\": 5, \"feed\": 2}\n" +
//        "}";
//
//    private static final String PERSONALITIES_JSON =
//        "{\n" +
//        "  \"cat\": {\n" +
//        "    \"greeting\": \"Meow!\",\n" +
//        "    \"traits\": [\"lazy\", \"curious\"]\n" +
//        "  }\n" +
//        "}";
//
//    @TempDir
//    Path tempDir;
//
//    private String originalUserDir;
//    private SavingSystem savingSystem;
//
//    @BeforeEach
//    void setUp() {
//        originalUserDir = System.getProperty("user.dir");
//        System.setProperty("user.dir", tempDir.toString());
//        savingSystem = new SavingSystem();
//    }
//
//    @AfterEach
//    void tearDown() {
//        System.setProperty("user.dir", originalUserDir);
//    }
//
//    private void writeConfig(String name, String content) throws IOException {
//        Path configs = tempDir.resolve("configs");
//        Files.createDirectories(configs);
//        Files.write(configs.resolve(name), content.getBytes());
//    }
//
//    @Test
//    void extractJsonStringArrayReturnsItems() {
//        String json = "{\"validPets\": [\"cat\", \"dog\", \"fox\"]}";
//        List<String> result = savingSystem.extractJsonStringArray(json, "validPets");
//        assertEquals(List.of("cat", "dog", "fox"), result);
//    }
//
//    @Test
//    void extractJsonStringArrayReturnsEmptyWhenKeyMissing() {
//        List<String> result = savingSystem.extractJsonStringArray("{}", "validPets");
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void unescapeJsonHandlesCommonEscapes() {
//        assertEquals("a\nb\t\"c\"\\", savingSystem.unescapeJson("a\\nb\\t\\\"c\\\"\\\\"));
//    }
//
//    @Test
//    void unescapeJsonHandlesUnicode() {
//        assertEquals("A", savingSystem.unescapeJson("\\u0041"));
//    }
//
//    @Test
//    void loadConfigReturnsNullWhenFileMissing() {
//        assertNull(savingSystem.loadConfig("missing.json"));
//    }
//
//    @Test
//    void loadConfigReturnsFileContent() throws IOException {
//        writeConfig("quiz.json", "hello");
//        assertEquals("hello", savingSystem.loadConfig("quiz.json"));
//    }
//
//    @Test
//    void extractPetPersonalityReadsGreetingAndTraits() throws IOException {
//        writeConfig("personalities.json", PERSONALITIES_JSON);
//        Map<String, Object> personality = savingSystem.extractPetPersonality("cat");
//        assertEquals("Meow!", personality.get("greeting"));
//        assertEquals(List.of("lazy", "curious"), personality.get("traits"));
//    }
//
//    @Test
//    void extractPetPersonalityReturnsEmptyForUnknownPet() throws IOException {
//        writeConfig("personalities.json", PERSONALITIES_JSON);
//        assertTrue(savingSystem.extractPetPersonality("dragon").isEmpty());
//    }
//
//    @Test
//    void extractPetPersonalityReturnsEmptyWithoutConfig() {
//        assertTrue(savingSystem.extractPetPersonality("cat").isEmpty());
//    }
//
//    @Test
//    void initialStageDefaultsToEggWithoutConfig() {
//        assertEquals("egg", savingSystem.getInitialStage());
//    }
//
//    @Test
//    void initialStageComesFromConfig() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertEquals("egg", savingSystem.getInitialStage());
//    }
//
//    @Test
//    void stageForCorrectAnswersFollowsThresholds() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertEquals("egg", savingSystem.getStageForCorrectAnswers(0));
//        assertEquals("egg", savingSystem.getStageForCorrectAnswers(2));
//        assertEquals("baby", savingSystem.getStageForCorrectAnswers(3));
//        assertEquals("adult", savingSystem.getStageForCorrectAnswers(10));
//        assertEquals("adult", savingSystem.getStageForCorrectAnswers(50));
//    }
//
//    @Test
//    void nextStageAdvancesAndStopsAtFinal() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertEquals("baby", savingSystem.getNextStage("egg"));
//        assertEquals("adult", savingSystem.getNextStage("baby"));
//        assertEquals("adult", savingSystem.getNextStage("adult"));
//    }
//
//    @Test
//    void isFinalStageOnlyTrueForLastStage() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertFalse(savingSystem.isFinalStage("egg"));
//        assertTrue(savingSystem.isFinalStage("adult"));
//    }
//
//    @Test
//    void xpToNextReadsFromStage() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertEquals(10, savingSystem.getXpToNext("egg"));
//        assertEquals(20, savingSystem.getXpToNext("baby"));
//        assertEquals(0, savingSystem.getXpToNext("unknown"));
//    }
//
//    @Test
//    void xpRewardsAreParsed() throws IOException {
//        writeConfig("stages.json", STAGES_JSON);
//        assertEquals(5, savingSystem.getXpReward("correct"));
//        assertEquals(2, savingSystem.getXpReward("feed"));
//        assertEquals(0, savingSystem.getXpReward("nothing"));
//    }
//
//    @Test
//    void calculateXpRewardIsAtLeastOne() {
//        assertTrue(savingSystem.calculateXpReward(1) >= 1);
//        assertTrue(savingSystem.calculateXpReward(100000) >= 1);
//    }
//
//    @Test
//    void savePetThenLoadPetRoundTrips() {
//        Starter.Pet pet = new Starter.Pet();
//        pet.petName = "Fluffy";
//        pet.petId = 4242;
//        pet.isPetExisting = true;
//        pet.hunger = 80;
//        pet.happiness = 90;
//        pet.lastSeen = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
//        pet.ownerName = "Sam";
//        pet.petType = "cat";
//        pet.hearts = 4;
//        pet.correctAnswers = 7;
//        pet.xp = 12;
//        pet.stage = "baby";
//
//        savingSystem.savePet(pet);
//
//        assertTrue(Files.exists(tempDir.resolve("saves").resolve("Sam_Fluffy.json")));
//
//        Starter.Pet loaded = savingSystem.loadPet("Sam_Fluffy.json");
//        assertNotNull(loaded);
//        assertEquals("Fluffy", loaded.petName);
//        assertEquals(4242, loaded.petId);
//        assertEquals(80, loaded.hunger);
//        assertEquals(90, loaded.happiness);
//        assertEquals("Sam", loaded.ownerName);
//        assertEquals("cat", loaded.petType);
//        assertEquals(4, loaded.hearts);
//        assertEquals(7, loaded.correctAnswers);
//        assertEquals(12, loaded.xp);
//        assertEquals("baby", loaded.stage);
//    }
//
//    @Test
//    void loadPetReturnsNullWhenFileMissing() {
//        assertNull(savingSystem.loadPet("nobody.json"));
//    }
//}
