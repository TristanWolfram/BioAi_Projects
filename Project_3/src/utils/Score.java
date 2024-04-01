package utils;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class Score {

    public static double[] calcScore(ArrayList<Segment> solution){
        double [] score = new double[3];
        for (Segment segment : solution) {
            score[0] += edgeScore(segment);
            score[1] += connectivityScore(segment);
            score[2] += deviationScore(segment);
        }
        return score;
    }

    private static double edgeScore(Segment segment){
        double edgeScore = 0;
        //todo implement this scoring formula from assignment description
        for (Pixel pixel : segment.getSegment()) {
            ArrayList<Pixel> neighbours = pixel.getNeighbors();
            for (Pixel neighbour : neighbours) {
                if (neighbour != null && !segment.contains(neighbour)) {
                    edgeScore += pixel.getDistanceTo(neighbour.getColor());
                }
            }
        }
        return edgeScore;
    }

    private static double connectivityScore(Segment segment){
        double connectivityScore = 0;
        //todo implement this scoring formula from assignment description
        for (Pixel pixel : segment.getSegment()) {
            ArrayList<Pixel> neighbours = pixel.getNeighbors();
            for (Pixel neighbour : neighbours) {
                if(neighbour != null && !segment.contains(neighbour)){
                    connectivityScore += 0.125;
                }
            }
        }
        return connectivityScore;
    }

    private static double deviationScore(Segment segment){
        double deviationScore = 0;

        int amountOfPixels = segment.getSegment().size();
        int totalR = 0;
        int totalB = 0;
        int totalG = 0;

        for (Pixel pixel : segment.getSegment()) {
            totalR += pixel.getColor().getRed();
            totalG += pixel.getColor().getGreen();
            totalB += pixel.getColor().getBlue();
        }

        RGBRepresentation centroid = new RGBRepresentation(totalR / amountOfPixels, totalG / amountOfPixels, totalB / amountOfPixels);

        for (Pixel pixel : segment.getSegment()){
            deviationScore += pixel.getDistanceTo(centroid);
        }
        return deviationScore;
    }
}
