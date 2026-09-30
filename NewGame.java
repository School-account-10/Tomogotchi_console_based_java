import java.util.Scanner;
import java.util.random.RandomGenerator;
import java.time.LocalDateTime;
import java.time.Duration;

public class NewGame {
    Scanner GameSC = new Scanner(System.in);
    SavingSystem SVSYS = new SavingSystem();
    Starter.pet iniVariables = new Starter.pet();
    boolean running = iniVariables.running = true;
    RandomGenerator RDM = RandomGenerator.getDefault();
    boolean justrancheck;

    public class BasicPetInfo {

        public Starter.pet newplayerdisplay() {
            System.out.println("just ran?: " + justrancheck);
            System.out.println();
            System.out.println("  Hey there! Welcome to Tomogotchi.");
            System.out.println("  I know, I know - it's been a while since anyone's heard from me.");
            System.out.println("  But I've got a little friend who needs a home, and I think you're it.");

            do {
                iniVariables.petName = "0";
                iniVariables.hunger = 0;
                iniVariables.petId = 0;
                iniVariables.happiness = 0;
                iniVariables.lastSeen = null;
                iniVariables.ownerName = "";
                justrancheck = false;
            } while (justrancheck == true);

            owner_name();
            pet_name();
            display_all_Finalinfo();
            return iniVariables;
        }

        public String owner_name() {
            System.out.println();
            System.out.print("So, what's your name? ");
            iniVariables.ownerName = GameSC.nextLine();
            System.out.println("Nice to meet you, " + iniVariables.ownerName + ".");
            return iniVariables.ownerName;
        }

        public String pet_name() {
            System.out.println();
            System.out.println("Now, what should we call your new buddy?");
            System.out.print("Pet name: ");
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
            System.out.println();
            System.out.println("Alright, here's what we've got:");
            System.out.println("  Owner: " + iniVariables.ownerName);
            System.out.println("  Pet: " + iniVariables.petName);
            System.out.println("  Happiness: " + iniVariables.happiness);
            System.out.println("  Hunger: " + iniVariables.hunger);
            System.out.println("  ID: " + iniVariables.petId);
            System.out.println("  First seen: " + iniVariables.lastSeen);

            System.out.println("just ran?: " + justrancheck);

            SVSYS.savePet(iniVariables);
        }
    }

    public static void main(String[] args) {
        NewGame game = new NewGame();
        game.new BasicPetInfo().newplayerdisplay();
    }
}
