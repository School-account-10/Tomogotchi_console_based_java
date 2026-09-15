import java.util.Scanner;



public class Menu {
    

  

    public static void show() {

        Scanner choice_menu = new Scanner(System.in);
        Starter.pet myPet = new Starter.pet();
        NewGame NG = new NewGame();
        NewGame.BasicPetInfo BPI = NG.new BasicPetInfo();
        boolean isrunning = myPet.running;

        while (isrunning) {
            System.out.println("===== TAMAGOTCHI =====");
            System.out.println("1. New Pet");
            System.out.println("2. Load Pet");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = choice_menu.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Starting new pet...");
                    BPI.newplayerdisplay();
                    break;
                case "2":
                    
                    System.out.println("Loading pet...");
                    break;
                case "3":

                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }
}
