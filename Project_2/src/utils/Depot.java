package utils;
public class Depot {
    int return_time;

    int x_coord;
    int y_coord;

    public Depot(int return_time, int x_coord, int y_coord) {
        this.return_time = return_time;
        this.x_coord = x_coord;
        this.y_coord = y_coord;
    }

    public String toString() {
        return "Depot: Return Time: " + return_time + " | X Coord: " + x_coord + " | Y Coord: " + y_coord;
    }
}
