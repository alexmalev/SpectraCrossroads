package game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Intersection {
    private Tuple position;
    private Set<Vehicle> waitingNorth = new HashSet<>();
    private Set<Vehicle> waitingSouth = new HashSet<>();
    private Set<Vehicle> waitingWest = new HashSet<>();
    private Set<Vehicle> waitingEast = new HashSet<>();
    private RoadQueue northEntrance = new RoadQueue(Color.GREEN);
    private RoadQueue southEntrance = new RoadQueue(Color.GREEN);
    private RoadQueue eastEntrance = new RoadQueue(Color.RED);
    private RoadQueue westEntrance = new RoadQueue(Color.RED);
    private BufferedImage greenLightNorth = ImageIO.read(new File("img/greenLightNorth.png"));
    private BufferedImage redLightNorth = ImageIO.read(new File("img/redLightNorth.png"));
    private BufferedImage offLightNorth = ImageIO.read(new File("img/offLightNorth.png"));
    private BufferedImage greenLightEast = ImageIO.read(new File("img/greenLightEast.png"));
    private BufferedImage redLightEast = ImageIO.read(new File("img/redLightEast.png"));
    private BufferedImage offLightEast = ImageIO.read(new File("img/offLightEast.png"));
    private BufferedImage greenLightSouth = ImageIO.read(new File("img/greenLightSouth.png"));
    private BufferedImage redLightSouth = ImageIO.read(new File("img/redLightSouth.png"));
    private BufferedImage offLightSouth = ImageIO.read(new File("img/offLightSouth.png"));
    private BufferedImage greenLightWest = ImageIO.read(new File("img/greenLightWest.png"));
    private BufferedImage redLightWest = ImageIO.read(new File("img/redLightWest.png"));
    private BufferedImage offLightWest = ImageIO.read(new File("img/offLightWest.png"));

    public Intersection(Tuple position) throws IOException {
        this.position = position;
    }

    public Tuple getPosition() {
        return position;
    }

    public RoadQueue getEntrance(Direction direction) {
        switch (direction){
            case EAST:
                return westEntrance;
            case SOUTH:
                return northEntrance;
            case WEST:
                return eastEntrance;
            case NORTH:
                return southEntrance;
        }
        return null;
    }
    public Set<Vehicle> getWaitingList(Direction direction){
        switch (direction){
            case EAST:
                return waitingWest;
            case SOUTH:
                return waitingNorth;
            case WEST:
                return waitingEast;
            case NORTH:
                return waitingSouth;
        }
        return null;
    }

    public BufferedImage getTrafficLightImage(Direction direction){
        switch (direction){
            case NORTH:
                if (northEntrance.getLight().equals(Color.GREEN))
                    return greenLightNorth;
                if (northEntrance.getLight().equals(Color.RED))
                    return redLightNorth;
                return offLightNorth;
            case WEST:
                if (westEntrance.getLight().equals(Color.GREEN))
                    return greenLightWest;
                if (westEntrance.getLight().equals(Color.RED))
                    return redLightWest;
                return offLightWest;
            case SOUTH:
                if (southEntrance.getLight().equals(Color.GREEN))
                    return greenLightSouth;
                if (southEntrance.getLight().equals(Color.RED))
                    return redLightSouth;
                return offLightSouth;
            case EAST:
                if (eastEntrance.getLight().equals(Color.GREEN))
                    return greenLightEast;
                if (eastEntrance.getLight().equals(Color.RED))
                    return redLightEast;
                return offLightEast;
        }
        return null;
    }

}
