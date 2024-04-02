package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SolutionRepresentation {
    private ArrayList<Pixel> solution = new ArrayList<>();
    private final int imgWidth;
    private ArrayList<Segment> segments = new ArrayList<>();
    private HashMap<Pixel, Segment> pixelSegmentMap = new HashMap<>();
    private double edgeValueScore;
    private double connectivityScore;
    private double deviationScore;

    public SolutionRepresentation(ArrayList<Pixel> solution, int imgWidth) {
        this.solution = solution;
        this.imgWidth = imgWidth;
        this.edgeValueScore = -1;
        this.connectivityScore = -1;
        this.deviationScore = -1;
    }

    public int getImageWidth() {
        return this.imgWidth;
    }

    public ArrayList<Pixel> getSolution() {
        return solution;
    }

    public void setSolution(ArrayList<Pixel> solution) {
        this.solution = solution;
    }

    public ArrayList<Segment> getSegments() {
        if (this.segments.size() == 0) {
            this.segments = this.generateSegments();
        }
        return this.segments;
    }

    public double[] getScore() {
        double[] score = new double[3];
        if (this.edgeValueScore != -1) {
            score[0] = this.edgeValueScore;
            score[1] = this.connectivityScore;
            score[2] = this.deviationScore;
        } else {
            score = Score.calcScore(getSegments(), this);
        }
        return score;
    }

    public ArrayList<Segment> generateSegments() {
        clearPixelAssignments();
        clearHashMap();

        HashSet<Segment> segments = new HashSet<>();

        ArrayList<Pixel> pixelQueue = new ArrayList<>(solution);
        int iterations = 0;
        while (!pixelQueue.isEmpty()) {

            Pixel currentPixel = pixelQueue.get(0);
            Pixel neighbourEnd = null;
            HashSet<Pixel> visited = new HashSet<>();
            visited.add(currentPixel);

            boolean createNewSegment = true;
            boolean endTraverse = false;
            while (endTraverse == false) {
                int nextKey = currentPixel.getConnectedNeighbor();
                if (nextKey == -1) {
                    endTraverse = true;
                } else {
                    Pixel next = solution.get(nextKey);
                    if (next.assigned == true) {
                        endTraverse = true;
                        createNewSegment = false;
                        neighbourEnd = next;
                    } else if (visited.contains(next)) {
                        endTraverse = true;
                    } else {
                        visited.add(next);
                        currentPixel = next;
                    }
                }
            }

            if (createNewSegment) {
                Segment s = new Segment();
                s.setSegment(visited);
                markPixelAsAssigned(visited);
                assignPixelsToSegment(visited, s);
                segments.add(s);
            } else {
                Segment s = findSegment(neighbourEnd);
                assignPixelsToSegment(visited, s);
                s.getSegment().addAll(visited);
            }

            pixelQueue.removeAll(visited);
            // System.out.println("Iterations: " + iterations++);
        }

        // for (Pixel p : solution) {
        // if (!p.assigned) {
        // Segment s = new Segment();
        // depthFirstSearch(p, s);
        // segments.add(s);
        // }
        // }

        System.out.println("Segments: " + segments.size());

        return new ArrayList<>(segments);
    }

    private void depthFirstSearch(Pixel p, Segment s) {
        p.assigned = true;
        s.addPixel(p);
        pixelSegmentMap.put(p, s);

        int nextKey = p.getConnectedNeighbor();
        Pixel next = solution.get(nextKey);
        if (next != null && !next.assigned) {
            depthFirstSearch(next, s);
        }
    }

    private Segment findSegment(Pixel p) {
        return pixelSegmentMap.get(p);
    }

    private void assignPixelsToSegment(HashSet<Pixel> pixels, Segment s) {
        for (Pixel p : pixels) {
            pixelSegmentMap.put(p, s);
        }
    }

    private void clearPixelAssignments() {
        for (Pixel p : solution) {
            p.assigned = false;
        }
    }

    private void markPixelAsAssigned(HashSet<Pixel> pixels) {
        for (Pixel p : pixels) {
            p.assigned = true;
        }
    }

    private void clearHashMap() {
        pixelSegmentMap.clear();
    }

    public String toString() {
        String str = "";
        str += solution.size() + "\n";
        for (int i = 0; i < solution.size(); i++) {
            str += solution.get(i).toString();
            if ((i + 1) % imgWidth == 0) {
                str += "\n";
            }
        }
        return str;
    }
}

// ArrayList<Pixel> pixelQueue = new ArrayList<>(solution);
// int iterations = 0;
// while (!pixelQueue.isEmpty()) {

// Pixel currentPixel = pixelQueue.get(0);

// ArrayList<Pixel> visited = new ArrayList<>();
// visited.add(currentPixel);

// boolean createNewSegment = true;
// boolean endTraverse = false;
// while (endTraverse == false) {
// Pixel next = currentPixel.getConnectedNeighbor();
// if (next == null) {
// endTraverse = true;
// } else {
// if (next.assigned == true) {
// endTraverse = true;
// createNewSegment = false;
// } else if (visited.contains(next)) {
// endTraverse = true;
// } else {
// visited.add(next);
// currentPixel = next;
// }
// }
// }

// if (createNewSegment) {
// Segment s = new Segment();
// s.setSegment(visited);
// markPixelAsAssigned(visited);
// assignPixelsToSegment(visited, s);
// segments.add(s);
// } else {
// Pixel pixelInSegment = visited.get(visited.size() -
// 1).getConnectedNeighbor();
// Segment s = findSegment(pixelInSegment);
// assignPixelsToSegment(visited, s);
// s.getSegment().addAll(visited);
// }

// pixelQueue.removeAll(visited);
// System.out.println("Iterations: " + iterations++);
// }