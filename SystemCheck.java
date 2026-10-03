import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.List;

public class SystemCheck {

    private static final String[] REQUIRED_CONFIGS = {"personalities.json", "quiz.json", "namesdb.json"};
    private static final String[] REQUIRED_DIRS = {"configs", "saves"};

    public static boolean run() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("         SYSTEM CHECK");
        System.out.println("  ==========================================");
        System.out.println();

        boolean allGood = true;

        // Check directories
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

        // Check config files
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

        System.out.println();

        if (!allGood) {
            System.out.println("  WARNING: Some required files are missing!");
            System.out.println("  The game may not work correctly.");
        }

        // Ask for permission
        Scanner scanner = new Scanner(System.in);
        System.out.print("  Do you agree to run? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (answer.equals("yes") || answer.equals("y")) {
            System.out.println("  System check passed. Starting game...");
            System.out.println();
            return true;
        } else {
            System.out.println("  Game not started. Goodbye!");
            return false;
        }
    }
}
