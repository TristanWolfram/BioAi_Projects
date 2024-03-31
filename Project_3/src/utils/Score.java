package utils;

import java.util.ArrayList;

public abstract class Score {

    public static double[] calcScore(Object solution){
        double [] score = new double[3];
        score[0] = edgeScore(solution);
        score[1] = edgeScore(connectivityScore(solution));
        score[2] = edgeScore(deviationScore(solution));
        return score;
    }

    private static double edgeScore(Object solution){
        double edgeScore = 0;
        //todo implement this scoring formula from assignment description
        return edgeScore;
    }

    private static double connectivityScore(Object solution){
        double connectivityScore = 0;
        //todo implement this scoring formula from assignment description
        return connectivityScore;
    }

    private static double deviationScore(Object solution){
        double deviationScore = 0;
        //todo implement this scoring formula from assignment description
        return deviationScore;
    }
}
