package utils;

public class TimeWindow {
    int start;
    int end;

    public TimeWindow(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getMiddleTime() {
        return (start + end) / 2;
    }

    public String toString() {
        return "(" + start + ", " + end + ")";
    }
}
