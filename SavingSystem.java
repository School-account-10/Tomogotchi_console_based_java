public class SavingSystem {
    Starter.pet iniVariables = new Starter.pet();
    String COS; // variable for the current os 

    public String OSDetection(){
        COS = System.getProperty("os.name");
        COS = COS.replaceAll("\\s+", "").toLowerCase();
        //System.out.println("current os: "+ COS); for debug 
        return COS;
    }


}
