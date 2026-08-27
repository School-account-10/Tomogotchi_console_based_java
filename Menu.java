import java.util.Scanner;
public class Menu{
    public static void show(){

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("===== TAMAGOTCHI =====");
            System.out.println("1. New Pet");
            System.out.println("2. Load Pet");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Starting new pet...");
                    break;
                case "2":
                    System.out.println("Loading pet...");
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }
}
    