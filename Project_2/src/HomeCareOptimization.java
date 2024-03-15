import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
// in order to run the code you'll need to add the json-simple-1.1.1.jar to the classpath
// -> https://code.google.com/archive/p/json-simple/downloads

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import utils.*;

public class HomeCareOptimization {
    public static void main(String[] args) {
        System.out.println("\nThis is the new home care optimization system!");

        String training_data = "train_9";

        String name = null;
        int nbr_nurses = 0;
        int capacity_nurse = 0;
        Depot depot = null;
        ArrayList<Patient> patients = new ArrayList<Patient>();
        double[][] travelTimes = null;

        // reading in json file
        try {
            String currentPath = new java.io.File(".").getCanonicalPath();
            JSONObject project_json = (JSONObject) new JSONParser()
                    .parse(new FileReader(currentPath + "/Project_2/training/" + training_data + ".json"));

            name = (String) project_json.get("instance_name");

            nbr_nurses = (int) (long) project_json.get("nbr_nurses");

            capacity_nurse = (int) (long) project_json.get("capacity_nurse");

            JSONObject depots = (JSONObject) project_json.get("depot");
            depot = new Depot(
                    (int) (long) depots.get("return_time"),
                    (int) (long) depots.get("x_coord"),
                    (int) (long) depots.get("y_coord"));

            JSONObject patients_json = (JSONObject) project_json.get("patients");

            // read in patients
            for (Object patient : patients_json.keySet()) {
                int key = Integer.parseInt((String) patient);
                Integer keyInt = Integer.valueOf(key);
                JSONObject patientObj = (JSONObject) patients_json.get("" + key);

                patients.add(new Patient(
                        keyInt,
                        (int) (long) patientObj.get("x_coord"),
                        (int) (long) patientObj.get("y_coord"),
                        (int) (long) patientObj.get("demand"),
                        (int) (long) patientObj.get("start_time"),
                        (int) (long) patientObj.get("end_time"),
                        (int) (long) patientObj.get("care_time")));
            }

            // for each patient calculate the distance to each other patient
            for (Patient patient : patients) {
                ArrayList<PatientDistancePair> currentDistances = new ArrayList<PatientDistancePair>();
                for (Patient otherPatient : patients) {
                    if (patient != otherPatient) {
                        currentDistances.add(new PatientDistancePair(otherPatient, patient.distanceTo(otherPatient)));
                    }
                }

                currentDistances.sort((pair1, pair2) -> pair1.compareTo(pair2));
                patient.setDistances(currentDistances);
            }

            JSONArray travel_times = (JSONArray) project_json.get("travel_times");

            travelTimes = new double[travel_times.size()][travel_times.size()];

            for (int i = 0; i < travel_times.size(); i++) {
                JSONArray row = (JSONArray) travel_times.get(i);
                for (int j = 0; j < row.size(); j++) {
                    travelTimes[i][j] = Double.parseDouble(row.get(j).toString());
                }
            }

            System.out.println("Instance Name: " + name + " | Number of Nurses: " + nbr_nurses
                    + " | Capacity of Nurse: " + capacity_nurse);
            System.out.println(depot);
            System.out.println("Patient count: " + patients.size());
            System.out.println(
                    "Travel times: " + travelTimes.length + "x" + travelTimes[0].length + " (Patients + Depot)\n");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("Error reading or parsing config.json");
            throw new RuntimeException();
        }

        int generations = 2000;
        int populationSize = 500;
        double mutationRate = 0.025;
        double crossoverRate = 1;

        // train_0 -> 10 cluster -> hitting benchmark
        // train_1 -> 8 cluster
        // train_2 -> 20 cluster
        // train_3 -> 11 cluster -> hitting benchmark
        // train_4 -> 20 cluster -> 20%
        // train_5 -> 5 cluster -> 30%
        // train_6 -> 5 cluster -> 30%
        // train_7 -> ??
        // train_8 -> 8 cluster -> 30% (almost)
        // train_9 -> 11 cluster -> 20% (after 50000 generations tho)
        int startingClusters = 10;

        int returnTime = depot.getReturnTime();

        GeneticAlgorithm ga = new GeneticAlgorithm(generations, populationSize,
                mutationRate, crossoverRate, startingClusters, nbr_nurses,
                capacity_nurse,
                patients, travelTimes, returnTime);

        System.out.println("Starting algorithm ... \nGenerations: ->" +
                ga.generations + "\nPopulation Size: ->"
                + ga.populationSize + "\nMutation Rate: ->" + ga.mutationRate + "\nCrossoverRate: ->"
                + ga.crossoverRate);

        SolutionRepresentation best = ga.run();

        System.out.println(best);

        System.out.println("Fitness: -> " + best.getFitness());
        System.out.println("Solution is feasible: -> " + best.isFeasible(travelTimes,
                returnTime));

        // write the solution to a file
        String export = best.exportToStringFormat();

        try {
            // Write the content to the file at filePath.
            // This will create the file if it doesn't exist, or overwrite it if it does.
            Files.write(Paths.get("Project_2/output/" + training_data + "_output.txt"), export.getBytes());
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}