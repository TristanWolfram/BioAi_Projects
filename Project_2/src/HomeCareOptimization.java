public class HomeCareOptimization {
    public static void main(String[] args) {
        System.out.println("This is the new home care optimization system!");

        GeneticAlgorithm ga = new GeneticAlgorithm(100, 100, 0.01, 0.9);

        System.out.println("Generations: " + ga.generations);
    }
}