import java.util.Scanner;
import java.util.random.RandomGenerator;
import java.time.LocalDateTime;
import java.time.Duration;

public class NewGame {
    Scanner GameSC = new Scanner(System.in);
    Starter.pet iniVariables = new Starter.pet();
    boolean running = iniVariables.running = true;
    RandomGenerator RDM = RandomGenerator.getDefault();
    boolean justrancheck;

    public class BasicPetInfo {

        // add a check for new player if theres no saves in the system using the
        // r1 (raid 1 type backup system)
        public void newplayerdisplay() {
            System.out.println("just ran?: " + justrancheck); // create func for this
            System.out.println("HI!!!!!!!!!!!! Welcome to our Tomogatchi Game");
            System.out.println("Yay!! My new friend is finally here! Let`s have fun together.");

            do { // cleaning part just incase
                iniVariables.petName = "0";
                iniVariables.hunger = 0;
                iniVariables.petId = 0;
                iniVariables.happiness = 0;
                justrancheck = false;

            } while (justrancheck == true);

            pet_name();
            display_all_Finalinfo(); // for debuging only

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
            iniVariables.hunger = RDM.nextInt(70, 101);
            iniVariables.petId = RDM.nextInt(1, 99999);
            iniVariables.happiness = RDM.nextInt(70, 101);
            iniVariables.lastSeen = LocalDateTime.now();

            iniVariables.ispetexisting = true;

            justrancheck = true;
            return iniVariables;

        }

        public void display_all_Finalinfo() {

            System.out.println("Name: " + iniVariables.petName);
            System.out.println("happyness: " + iniVariables.happiness);
            System.out.println("hunger: " + iniVariables.hunger);
            System.out.println("ID: " + iniVariables.petId);
            System.out.println("TimeStamp_LOS: " + iniVariables.lastSeen);

            System.out.println("just ran?: " + justrancheck);

        }

        // System.out.println("HII welcome back to the chracter creation"); // if player
        // has an r1 file already you can just say this
        // System.out.println("");

    }

}
