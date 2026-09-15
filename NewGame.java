import java.util.Scanner;
import java.util.random.RandomGenerator;

public class NewGame {
    Scanner GameSC = new Scanner(System.in);
    Starter.pet iniVariables = new Starter.pet();
    boolean running = iniVariables.running = true;
    RandomGenerator RDM = RandomGenerator.getDefault();

    public class BasicPetInfo {
       
        // add a check for new player if theres no saves in the system using the
        // r1 (raid 1 type backup system)
        public void newplayerdisplay() {
            System.out.println("HI!!!!!!!!!!!! Welcome to our Tomogatchi Game");
            System.out.println("Yay!! My new friend is finally here! Let`s have fun together.");

            //check if a name matches in the json file 




            
            pet_name();
            display_all_Finalinfo();
        }

        

        public String pet_name() {
            System.out.println("so ill guide you with with the Pet creation");
            System.out.println("          ");
            System.out.print("So what would you like to name your pet as: ");
            iniVariables.petName = GameSC.nextLine();
            ini_all_variables();
            return iniVariables.petName;
        }

        public Starter.pet ini_all_variables() {
            iniVariables.hunger = RDM.nextInt(70, 110);
            iniVariables.petId = RDM.nextInt(1, 99999);
            iniVariables.happiness = RDM.nextInt(70, 110);
            return iniVariables;
            
        }

        public void display_all_Finalinfo() {

            System.out.println("Name: " + iniVariables.petName);
            System.out.println("happyness: " + iniVariables.happiness);
            System.out.println("hunger: " + iniVariables.hunger);
            System.out.println("ID: " + iniVariables.petId);

        }
         

        // System.out.println("HII welcome back to the chracter creation"); // if player
        // has an r1 file already you can just say this
        // System.out.println("");

    }

}
