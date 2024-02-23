import org.json.simple.JSONObject;
// import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;

public class HomeCareOptimization {
    public static void main(String[] args) {
        System.out.println("This is the new home care optimization system!");

        GeneticAlgorithm ga = new GeneticAlgorithm(100, 100, 0.01, 0.9);

        System.out.println("Generations: " + ga.generations);

        try {
            String currentPath = new java.io.File(".").getCanonicalPath();
            System.out.println("Current path: " + currentPath);
            Object o = new JSONParser().parse(new FileReader(currentPath + "/Project_2/training/train_0.json"));
            JSONObject j = (JSONObject) o;
            String name = (String) j.get("instance_name");
            long n = (long) j.get("nbr_nurses");
            System.out.println("Instance Name: " + name + " | Number of Nurses: " + (n * 2));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("Error reading or parsing config.json");
        }
    }
}