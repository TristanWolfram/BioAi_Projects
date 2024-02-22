package utils;
public class SolutionRepresentation {
    Route[] solution;

    public SolutionRepresentation(Route[] solution) {
        this.solution = solution;
    }

    public Route[] getSolution() {
        return solution;
    }

    public float getFitness() {
        float fitness = 0;
        for (Route route : solution) {
            fitness += route.getTravelTime();
        }
        return fitness;
    }
}