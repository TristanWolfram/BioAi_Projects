package utils;

import java.util.ArrayList;
import java.util.HashMap;

public class SolutionRepresentation {
    private ArrayList<Pixel> solution = new ArrayList<>();
    private final int imgWidth;
    private ArrayList<Segment> segments = new ArrayList<>();
    private HashMap<Pixel, Segment> pixelSegmentMap = new HashMap<>();

    public SolutionRepresentation(ArrayList<Pixel> solution, int imgWidth) {
        this.solution = solution;
        this.imgWidth = imgWidth;
    }

    public ArrayList<Pixel> getSolution() {
        return solution;
    }

    public void setSolution(ArrayList<Pixel> solution) {
        this.solution = solution;
    }

    public ArrayList<Segment> getSegments() {
        return segments;
    }

    public ArrayList<Segment> generateSegments() {
        clearPixelAssignments();
        clearHashMap();

        ArrayList<Segment> segments = new ArrayList<>();

        for (Pixel p : solution) {
            if (!p.assigned) {
                Segment s = new Segment();
                depthFirstSearch(p, s);
                segments.add(s);
            }
        }

        System.out.println("Segments: " + segments.size());

        return segments;
    }

    private void depthFirstSearch(Pixel p, Segment s) {
        p.assigned = true;
        s.addPixel(p);
        pixelSegmentMap.put(p, s);

        Pixel next = p.getConnectedNeighbor();
        if (next != null && !next.assigned) {
            depthFirstSearch(next, s);
        }
    }

    private Segment findSegment(Pixel p) {
        return pixelSegmentMap.get(p);
    }

    private void assignPixelsToSegment(ArrayList<Pixel> pixels, Segment s) {
        for (Pixel p : pixels) {
            pixelSegmentMap.put(p, s);
        }
    }

    private void clearPixelAssignments() {
        for (Pixel p : solution) {
            p.assigned = false;
        }
    }

    private void markPixelAsAssigned(ArrayList<Pixel> pixels) {
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