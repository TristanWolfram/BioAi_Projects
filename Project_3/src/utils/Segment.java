package utils;

import java.util.ArrayList;
import java.util.HashSet;

public class Segment {
    private HashSet<Pixel> segment = new HashSet<>();

    public Segment() {
    }

    public HashSet<Pixel> getSegment() {
        return segment;
    }

    public void setSegment(HashSet<Pixel> segment) {
        this.segment = segment;
    }

    public void addPixel(Pixel pixel) {
        segment.add(pixel);
    }

    public boolean contains(Pixel pixel) {
        return segment.contains(pixel);
    }
}
