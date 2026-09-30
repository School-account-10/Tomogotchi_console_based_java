import java.util.Scanner;
import java.util.List;

public class Menu {

    public static void show() {

        Scanner choice_menu = new Scanner(System.in);
        Starter.pet myPet = new Starter.pet();
        NewGame NG = new NewGame();
        NewGame.BasicPetInfo BPI = NG.new BasicPetInfo();
        SavingSystem SVSYS = new SavingSystem();

        boolean isrunning = myPet.running;

        while (isrunning) {
            System.out.println("\n===== TAMAGOTCHI =====");
            System.out.println("1. New Pet");
            System.out.println("2. Load Pet");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = choice_menu.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Starting new pet...");
                    Starter.pet newPet = BPI.newplayerdisplay();
                    if (newPet != null) {
                        gameLoop(choice_menu, SVSYS, newPet);
                    }
                    break;
                case "2":
                    System.out.println("Loading pet...");
                    Starter.pet loadedPet = loadPetMenu(choice_menu, SVSYS);
                    if (loadedPet != null) {
                        gameLoop(choice_menu, SVSYS, loadedPet);
                    }
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    isrunning = false;
                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static void gameLoop(Scanner scanner, SavingSystem SVSYS, Starter.pet pet) {
        System.out.println("\n===== YOUR TAMAGOTCHI =====");
        boolean playing = true;

        while (playing) {
            System.out.println("\n--- " + pet.petName + " ---");
            System.out.println("Happiness: " + pet.happiness);
            System.out.println("Hunger: " + pet.hunger);
            System.out.println("1. Feed");
            System.out.println("2. Play");
            System.out.println("3. Save and Quit");
            System.out.print("Choose an action: ");

            String action = scanner.nextLine();

            switch (action) {
                case "1":
                    pet.hunger = Math.min(100, pet.hunger + 20);
                    pet.happiness = Math.min(100, pet.happiness + 5);
                    System.out.println(pet.petName + " munches happily! Hunger: " + pet.hunger);
                    break;
                case "2":
                    pet.happiness = Math.min(100, pet.happiness + 15);
                    pet.hunger = Math.max(0, pet.hunger - 10);
                    System.out.println(pet.petName + " plays excitedly! Happiness: " + pet.happiness);
                    break;
                case "3":
                    SVSYS.savePet(pet);
                    System.out.println("Game saved. See you next time!");
                    playing = false;
                    break;
                default:
                    System.out.println("Invalid action.");
            }
        }
    }

    private static Starter.pet loadPetMenu(Scanner scanner, SavingSystem SVSYS) {
        List<String> saves = SVSYS.listSaveFiles();

        if (saves.isEmpty()) {
            System.out.println("No save files found. Create a new pet first.");
            return null;
        }

        System.out.println("\nAvailable saves:");
        for (int i = 0; i < saves.size(); i++) {
            System.out.println((i + 1) + ". " + saves.get(i));
        }
        System.out.print("Choose a save (number): ");

        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < saves.size()) {
                Starter.pet loadedPet = SVSYS.loadPet(saves.get(idx));
                if (loadedPet != null) {
                    System.out.println("Pet loaded successfully!");
                    return loadedPet;
                }
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        return null;
    }
}
