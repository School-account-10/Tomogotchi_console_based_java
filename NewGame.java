import java.util.Scanner;

public class NewGame {
    Scanner GameSC = new Scanner(System.in);
    Starter.pet iniVariables = new Starter.pet();
    boolean running = iniVariables.running = true;

    public class BasicPetInfo {
        // add a check for new player if theres no saves in the system using the
        // r1 (raid 1 type backup system)
        public void newplayerdisplay() {
            System.out.println("HI!!!!!!!!!!!! Welcome to our Tomogatchi Game");
            System.out.println("Yay!! My new friend is finally here! Let`s have fun together.");
            pet_name();

        }

        public String pet_name() {
            System.out.println("so ill guide you with with the Pet creation");
            System.out.println("");
            System.out.print("So what would you like to name your pet as: ");
            iniVariables.petName = GameSC.nextLine();
            return iniVariables.petName;
        }

        public void display_all_Finalinfo() {

            System.out.print(iniVariables.petName);

        }

        // System.out.println("HII welcome back to the chracter creation"); // if player
        // has an r1 file already you can just say this
        // System.out.println("");

    }

}
