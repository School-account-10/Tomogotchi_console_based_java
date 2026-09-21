import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SavingSystem {
    Starter.pet iniVariables = new Starter.pet();
    Path AppdataDirectory;
    String COS; // variable for the current os
    String name = "Tom";

    public String OSDetection() {
        COS = System.getProperty("os.name");
        COS = COS.replaceAll("\\s+", "").toLowerCase();
        // System.out.println("current os: "+ COS); for debug
        FileSYS();
        return COS;
    }

    public Path FileSYS() { // main Container of saves // not main save folder

        if (COS.contains("linux")) {
            AppdataDirectory = Paths.get(System.getProperty("user.home"), "Desktop", name);
            try {
                Files.createDirectories(AppdataDirectory);
                return AppdataDirectory;
            } catch (Exception e) {

            }

        } else if (COS.contains("mac")) {
            AppdataDirectory = Paths.get(System.getProperty("user.home"), "Desktop", name);
            try {
                Files.createDirectories(AppdataDirectory);
                return AppdataDirectory;
            } catch (Exception e) {

            }

        } else if (COS.contains("win")) {
            AppdataDirectory = Paths.get(System.getProperty("user.home"), "Desktop", name);
            try {
                Files.createDirectories(AppdataDirectory);
                return AppdataDirectory;
            } catch (Exception e) {

            }

        }
        return AppdataDirectory;
    }
}
