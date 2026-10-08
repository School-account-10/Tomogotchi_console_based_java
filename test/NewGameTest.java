//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Scanner;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class NewGameTest {
//
//    @TempDir
//    Path tempDir;
//
//    private String originalUserDir;
//    private PrintStream originalOut;
//    private ByteArrayOutputStream output;
//    private SavingSystem savingSystem;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        originalUserDir = System.getProperty("user.dir");
//        originalOut = System.out;
//        System.setProperty("user.dir", tempDir.toString());
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        Path configs = tempDir.resolve("configs");
//        Files.createDirectories(configs);
//        Files.write(configs.resolve("quiz.json"),
//            "{\"validPets\": [\"cat\", \"dog\", \"fox\"]}".getBytes());
//        Files.write(configs.resolve("namesdb.json"),
//            "{\"blockedNames\": [\"badword\", \"rude\"]}".getBytes());
//        Files.write(configs.resolve("personalities.json"),
//            ("{\n  \"cat\": {\n    \"greeting\": \"Meow!\",\n    \"traits\": [\"lazy\", \"curious\"]\n  }\n}").getBytes());
//        Files.write(configs.resolve("stages.json"),
//            ("{\n  \"stages\": [\n" +
//             "    {\"id\": 1, \"name\": \"egg\", \"minCorrectAnswers\": 0, \"xpToNext\": 10, \"description\": \"An egg\"}\n" +
//             "  ]\n}").getBytes());
//
//        savingSystem = new SavingSystem();
//    }
//
//    @AfterEach
//    void tearDown() {
//        System.setOut(originalOut);
//        System.setProperty("user.dir", originalUserDir);
//    }
//
//    private Starter.Pet run(String input) {
//        return NewGame.createNewPet(new Scanner(input), savingSystem);
//    }
//
//    private long savedFileCount() throws IOException {
//        Path saves = tempDir.resolve("saves");
//        if (!Files.exists(saves)) {
//            return 0;
//        }
//        try (Stream<Path> files = Files.list(saves)) {
//            return files.count();
//        }
//    }
//
//    @Test
//    void createsPetWithChosenValues() {
//        Starter.Pet pet = run("Sam\n1\nFluffy\nyes\n");
//
//        assertNotNull(pet);
//        assertEquals("Sam", pet.ownerName);
//        assertEquals("cat", pet.petType);
//        assertEquals("Fluffy", pet.petName);
//    }
//
//    @Test
//    void newPetStartsWithDefaultProgress() {
//        Starter.Pet pet = run("Sam\n1\nFluffy\nyes\n");
//
//        assertEquals(5, pet.hearts);
//        assertEquals(0, pet.correctAnswers);
//        assertEquals(0, pet.xp);
//        assertEquals("egg", pet.stage);
//        assertTrue(pet.isPetExisting);
//        assertNotNull(pet.lastSeen);
//    }
//
//    @Test
//    void newPetStatsAreInExpectedRanges() {
//        for (int i = 0; i < 50; i++) {
//            Starter.Pet pet = run("Sam\n2\nFluffy\ny\n");
//            assertTrue(pet.hunger >= 70 && pet.hunger <= 100);
//            assertTrue(pet.happiness >= 70 && pet.happiness <= 100);
//            assertTrue(pet.petId >= 1 && pet.petId <= 99999);
//        }
//    }
//
//    @Test
//    void acceptingRulesSavesPetToDisk() throws IOException {
//        run("Sam\n1\nFluffy\nyes\n");
//
//        assertTrue(Files.exists(tempDir.resolve("saves").resolve("Sam_Fluffy.json")));
//    }
//
//    @Test
//    void decliningRulesReturnsNullAndSavesNothing() throws IOException {
//        Starter.Pet pet = run("Sam\n1\nFluffy\nno\n");
//
//        assertNull(pet);
//        assertEquals(0, savedFileCount());
//    }
//
//    @Test
//    void shortAnswerYIsAccepted() {
//        assertNotNull(run("Sam\n1\nFluffy\ny\n"));
//    }
//
//    @Test
//    void rulesAnswerIsCaseInsensitive() {
//        assertNotNull(run("Sam\n1\nFluffy\n  YES  \n"));
//    }
//
//    @Test
//    void invalidPetTypeIsRetriedUntilValid() {
//        Starter.Pet pet = run("Sam\n0\n9\nabc\n3\nFluffy\nyes\n");
//
//        assertNotNull(pet);
//        assertEquals("fox", pet.petType);
//    }
//
//    @Test
//    void blockedPetNameIsRetried() {
//        Starter.Pet pet = run("Sam\n1\nbadword\nFluffy\nyes\n");
//
//        assertNotNull(pet);
//        assertEquals("Fluffy", pet.petName);
//    }
//
//    @Test
//    void blockedNameMatchingIgnoresCase() {
//        Starter.Pet pet = run("Sam\n1\nSuperRUDEpet\nFluffy\nyes\n");
//
//        assertEquals("Fluffy", pet.petName);
//    }
//
//    @Test
//    void tooShortPetNameIsRetried() {
//        Starter.Pet pet = run("Sam\n1\nA\nFluffy\nyes\n");
//
//        assertEquals("Fluffy", pet.petName);
//    }
//
//    @Test
//    void tooLongPetNameIsRetried() {
//        Starter.Pet pet = run("Sam\n1\nABCDEFGHIJKLMNOP\nFluffy\nyes\n");
//
//        assertEquals("Fluffy", pet.petName);
//    }
//
//    @Test
//    void boundaryLengthNamesAreAccepted() {
//        Starter.Pet shortest = run("Sam\n1\nAb\nyes\n");
//        Starter.Pet longest = run("Sam\n1\nABCDEFGHIJKLMNO\nyes\n");
//
//        assertEquals("Ab", shortest.petName);
//        assertEquals("ABCDEFGHIJKLMNO", longest.petName);
//    }
//
//    @Test
//    void greetingFromPersonalityIsPrinted() {
//        run("Sam\n1\nFluffy\nyes\n");
//
//        assertTrue(output.toString().contains("Meow!"));
//    }
//
//    @Test
//    void statusIncludesPersonalityTraits() {
//        run("Sam\n1\nFluffy\nyes\n");
//
//        assertTrue(output.toString().contains("Personality: lazy, curious"));
//    }
//
//    @Test
//    void showPetStatusPrintsPetDetails() {
//        Starter.Pet pet = new Starter.Pet();
//        pet.petName = "Fluffy";
//        pet.ownerName = "Sam";
//        pet.petType = "cat";
//        pet.happiness = 88;
//        pet.hunger = 77;
//        pet.hearts = 5;
//        pet.correctAnswers = 3;
//        pet.stage = "baby";
//
//        NewGame.showPetStatus(pet, null);
//
//        String text = output.toString();
//        assertTrue(text.contains("Name: Fluffy"));
//        assertTrue(text.contains("Owner: Sam"));
//        assertTrue(text.contains("Type: cat"));
//        assertTrue(text.contains("Happiness: 88/100"));
//        assertTrue(text.contains("Hunger: 77/100"));
//        assertTrue(text.contains("Stage: baby"));
//    }
//}
