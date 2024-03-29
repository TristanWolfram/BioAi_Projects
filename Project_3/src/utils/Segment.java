package utils;

import java.util.ArrayList;

public class Segment {
    private ArrayList<Pixel> segment = new ArrayList<>();

    public Segment() {
    }

    public ArrayList<Pixel> getSegment() {
        return segment;
    }

    public void setSegment(ArrayList<Pixel> segment) {
        this.segment = segment;
    }

    public void addPixel(Pixel pixel) {
        segment.add(pixel);
    }

    public boolean contains(Pixel pixel) {
        return segment.contains(pixel);
    }
}
