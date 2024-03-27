package utils;

import java.util.ArrayList;

public class SolutionRepresentation {
    private ArrayList<Pixel> solution = new ArrayList<>();

    public SolutionRepresentation(ArrayList<Pixel> solution) {
        this.solution = solution;
    }

    public ArrayList<Pixel> getSolution() {
        return solution;
    }

    public void setSolution(ArrayList<Pixel> solution) {
        this.solution = solution;
    }
}
