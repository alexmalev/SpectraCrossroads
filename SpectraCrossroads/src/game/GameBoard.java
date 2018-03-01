package game;


import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * holds the state of the game: number of cars, cars crossing, and traffic lights
 */
public class GameBoard {
    private static Random rand = new Random();

    private Map<Tuple, Tile> boardMap = new HashMap<>();
    private int horizontalTiles = 20;
    private int verticalTiles = 15;
    private Intersection intersection;
    private RoadQueue southExit;
    private RoadQueue northExit;
    private RoadQueue eastExit;
    private RoadQueue westExit;
    int eastTurn = 0;
    int westTurn = 0;
    int horizontalMin = 30;
    int horizontalMax = 31;
    int nextEast = getRandomInt(horizontalMin, horizontalMax);
    int nextWest = getRandomInt(horizontalMin, horizontalMax);

    int northTurn = 0;
    int southTurn = 0;
    int verticalMin = 30;
    int verticalMax = 31;
    int nextNorth = getRandomInt(verticalMin, verticalMax);
    int nextSouth = getRandomInt(verticalMin, verticalMax);


    GameBoard() throws IOException {
        generateBoard();
    }
    
    /**
     * initializes an empty board and its UI;
     * @throws IOException
     */
    private void generateBoard() throws IOException {
        Tuple intersectionPosition = insertIntersection();
        this.intersection = new Intersection(intersectionPosition);
        this.southExit = new RoadQueue(Color.GREEN);
        this.northExit = new RoadQueue(Color.GREEN);
        this.eastExit = new RoadQueue(Color.GREEN);
        this.westExit = new RoadQueue(Color.GREEN);
        insertRoads(intersectionPosition);
        insertGrass();
    }

    /**
     * inserts an intersection Tile in the board
     * @return a Tuple of the intersections coords
     * @throws IOException
     */
    private Tuple insertIntersection() throws IOException {
        int intersectionX = horizontalTiles / 2;
        int intersectionY = verticalTiles / 2;
        Tuple position = new Tuple(intersectionX, intersectionY);
        boardMap.put(position, new Tile(TileType.INTERSECTION));
        return position;
    }
    
    /**
     * inserts road tiles on the board relative to the intersection coords
     * @param intersectionPosition
     * @throws IOException
     */
    private void insertRoads(Tuple intersectionPosition) throws IOException {
        for (int i = 0; i < horizontalTiles; i++) {
            Tuple tilePosition = new Tuple(i, intersectionPosition.getY());
            if (boardMap.containsKey(tilePosition))
                continue;
            boardMap.put(tilePosition, new Tile(TileType.HORIZONTAL));
        }
        for (int j = 0; j < verticalTiles; j++) {
            Tuple tilePosition = new Tuple(intersectionPosition.getX(), j);
            if (boardMap.containsKey(tilePosition))
                continue;
            boardMap.put(tilePosition, new Tile(TileType.VERTICAL));
        }
    }
    /**
     * inserts grass tiles in all the vacant positions
     * @throws IOException
     */
    private void insertGrass() throws IOException {
        for (int i = 0; i < horizontalTiles; i++) {
            for (int j = 0; j < verticalTiles; j++) {
                Tuple tilePosition = new Tuple(i, j);
                if (!boardMap.containsKey(tilePosition))
                    boardMap.put(tilePosition, new Tile(TileType.GRASS));
            }
        }
    }
    /**
     * draws the board with all the elements and the count of waiting cars.
     * @param g
     */
    void draw(Graphics g) {
        for (Tuple tuple : boardMap.keySet()) {
            boardMap.get(tuple).draw(tuple, g);
        }
        g.drawImage(intersection.getTrafficLightImage(Direction.NORTH), (intersection.getPosition().getX() - 1) * 40, (intersection.getPosition().getY() - 1) * 40, null);
        g.drawImage(intersection.getTrafficLightImage(Direction.EAST), (intersection.getPosition().getX() + 1) * 40, (intersection.getPosition().getY() - 1) * 40, null);
        g.drawImage(intersection.getTrafficLightImage(Direction.SOUTH), (intersection.getPosition().getX() + 1) * 40, (intersection.getPosition().getY() + 1) * 40, null);
        g.drawImage(intersection.getTrafficLightImage(Direction.WEST), (intersection.getPosition().getX() - 1) * 40, (intersection.getPosition().getY() + 1) * 40, null);
        for (Vehicle vehicle : intersection.getEntrance(Direction.NORTH).getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : intersection.getEntrance(Direction.EAST).getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : intersection.getEntrance(Direction.SOUTH).getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : intersection.getEntrance(Direction.WEST).getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : southExit.getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : northExit.getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : westExit.getQueue()) {
            vehicle.draw(g);
        }
        for (Vehicle vehicle : eastExit.getQueue()) {
            vehicle.draw(g);
        }
        
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.setColor(java.awt.Color.white);
        g.drawString(String.valueOf(intersection.getWaitingList(Direction.NORTH).size()), 455, 400);
        g.drawString(String.valueOf(intersection.getWaitingList(Direction.SOUTH).size()), 375, 220);
        g.drawString(String.valueOf(intersection.getWaitingList(Direction.EAST).size()), 320, 347);
        g.drawString(String.valueOf(intersection.getWaitingList(Direction.WEST).size()), 500, 265);
    }




    int getRandomInt(int min, int max) {
        return rand.nextInt((max + 1) - min) + min;
    }

    /**
     * updates the game board.
     * shoots new cars to the board in the required time intervals
     * controls all the vehicles on the board.
     * @throws IOException
     */
    void updateGameBoard() throws IOException {
        eastTurn++;
        westTurn++;
        northTurn++;
        southTurn++;
        if (eastTurn == nextEast) {
            intersection.getEntrance(Direction.EAST).getQueue().add(new Vehicle(new Tuple(800, 280), Direction.WEST));
            eastTurn = 0;
            nextEast = getRandomInt(horizontalMin, horizontalMax);
        }
        if (westTurn == nextWest) {
            intersection.getEntrance(Direction.WEST).getQueue().add(new Vehicle(new Tuple(-40, 300), Direction.EAST));
            westTurn = 0;
            nextWest = getRandomInt(horizontalMin, horizontalMax);
        }
        if (northTurn == nextNorth) {
            intersection.getEntrance(Direction.NORTH).getQueue().add(new Vehicle(new Tuple(400, -40), Direction.SOUTH));
            northTurn = 0;
            nextNorth = getRandomInt(verticalMin, verticalMax);
        }
        if (southTurn == nextSouth) {
            intersection.getEntrance(Direction.SOUTH).getQueue().add(new Vehicle(new Tuple(420, 600), Direction.NORTH));
            southTurn = 0;
            nextSouth = getRandomInt(verticalMin, verticalMax);
        }

        controlVehicles();
    }

    /**
     * controls the vehicles on the board:
     * moves cars that can move, removes cars that have left the screen,
     * and stops cars that cannot move.
     */
    private void controlVehicles() {
        LinkedList<Vehicle> southExit = this.southExit.getQueue();
        LinkedList<Vehicle> northExit = this.northExit.getQueue();
        LinkedList<Vehicle> westExit = this.westExit.getQueue();
        LinkedList<Vehicle> eastExit = this.eastExit.getQueue();

        controlExit(southExit);
        controlExit(northExit);
        controlExit(westExit);
        controlExit(eastExit);

        LinkedList<Vehicle> northEntrance = intersection.getEntrance(Direction.NORTH).getQueue();
        LinkedList<Vehicle> southEntrance = intersection.getEntrance(Direction.SOUTH).getQueue();
        LinkedList<Vehicle> eastEntrance = intersection.getEntrance(Direction.EAST).getQueue();
        LinkedList<Vehicle> westEntrance = intersection.getEntrance(Direction.WEST).getQueue();

        controlVehiclesInQueue(northEntrance);
        controlVehiclesInQueue(southEntrance);
        controlVehiclesInQueue(eastEntrance);
        controlVehiclesInQueue(westEntrance);

    }

    /**
     * controls vehicles that have passed an intersection and are towards an exit.
     * moves them until they have left the screen then removes them.
     * @param exit
     */
    private void controlExit(LinkedList<Vehicle> exit) {
        for (ListIterator<Vehicle> iterator = exit.listIterator(); iterator.hasNext(); ) {
            Vehicle currentVehicle = iterator.next();
            currentVehicle.drive(true);
            if (leftTheScreen(currentVehicle)) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Determines if a given car has left the screen.
     * @param currentVehicle
     * @return
     */
    private boolean leftTheScreen(Vehicle currentVehicle) {
        switch (currentVehicle.getDirection()) {
            case SOUTH:
                return currentVehicle.getPosition().getY() > 600;
            case NORTH:
                return currentVehicle.getPosition().getY() < -40;
            case EAST:
                return currentVehicle.getPosition().getX() > 800;
            case WEST:
                return currentVehicle.getPosition().getX() < -40;
        }
        return false;
    }
    
    /**
     * controls the cars in a given queue.
     * moves them if they can move.
     * adds them to the waiting list if they cannot.
     * passes them to the exit queue 
     * and removes them from the waiting list if they entered the intersection
     * @param queue
     */
    private void controlVehiclesInQueue(LinkedList<Vehicle> queue) {
        for (ListIterator<Vehicle> iterator = queue.listIterator(); iterator.hasNext(); ) {
            Vehicle currentVehicle = iterator.next();
            if (currentVehicle == queue.getFirst()) {
                if (isFirstNotYetInIntersection(currentVehicle)) {
                    currentVehicle.drive(true);
                    if (isFirstVehicleBeforeIntersection(currentVehicle)) {
                        intersection.getWaitingList(currentVehicle.getDirection()).add(currentVehicle);
                    }

                } else {
                    if (isGreenLight(currentVehicle.getDirection())) {
                        currentVehicle.drive(true);
                        passVehicleToNextQueue(currentVehicle);
                        iterator.remove();
                    } else {
                        currentVehicle.drive(false);
                    }
                }

            } else {
                iterator.previous();
                Vehicle vehicleInFront = iterator.previous();
                if (hasSpaceToMove(currentVehicle, vehicleInFront)) {
                    currentVehicle.drive(true);
                } else {
                    currentVehicle.drive(false);
                    intersection.getWaitingList(currentVehicle.getDirection()).add(currentVehicle);
                }
                iterator.next();
                iterator.next();
            }
        }
    }

    private boolean isGreenLight(Direction direction) {
        return intersection.getEntrance(direction).isCanPass();
    }

    /**
     * determines if a given vehicle can move
     * returns true if it is not behind a standing car or if it is not at a red light.
     * @param currentVehicle
     * @param vehicleInFront
     * @return
     */
    private boolean hasSpaceToMove(Vehicle currentVehicle, Vehicle vehicleInFront) {
        switch (currentVehicle.getDirection()) {
            case SOUTH:
                return currentVehicle.getPosition().getY() <= vehicleInFront.getPosition().getY() - 40;
            case NORTH:
                return currentVehicle.getPosition().getY() >= vehicleInFront.getPosition().getY() + 40;
            case EAST:
                return currentVehicle.getPosition().getX() <= vehicleInFront.getPosition().getX() - 40;
            case WEST:
                return currentVehicle.getPosition().getX() >= vehicleInFront.getPosition().getX() + 40;
        }
        return false;
    }

    /**
     * pass a give vehicle from the intersection queue to the exit queue
     * @param vehicle
     */
    private void passVehicleToNextQueue(Vehicle vehicle) {
        switch (vehicle.getDirection()) {
            case SOUTH:
                southExit.getQueue().add(vehicle);
                break;
            case NORTH:
                northExit.getQueue().add(vehicle);
                break;
            case EAST:
                eastExit.getQueue().add(vehicle);
                break;
            case WEST:
                westExit.getQueue().add(vehicle);
                break;
        }
        intersection.getWaitingList(vehicle.getDirection()).remove(vehicle);
    }

    /**
     * determines if a given vehicle is at the entrance to an intersection
     * @param vehicle
     * @return
     */
    private boolean isFirstVehicleBeforeIntersection(Vehicle vehicle) {
        switch (vehicle.getDirection()) {
            case SOUTH:
                return vehicle.getPosition().getY() == intersection.getPosition().getY() * 40 - 40;
            case NORTH:
                return vehicle.getPosition().getY() == intersection.getPosition().getY() * 40 + 40;
            case EAST:
                return vehicle.getPosition().getX() == intersection.getPosition().getX() * 40 - 40;
            case WEST:
                return vehicle.getPosition().getX() == intersection.getPosition().getX() * 40 + 40;
        }
        return false;

    }

    /**
     * determines if the intersection is occupied by a horizontal vehicle
     * @return
     */
    public boolean isHorizontalPassing() {

        return isVehicleInIntersection(eastExit.getQueue()) || isVehicleInIntersection(westExit.getQueue());

    }
    
    /**
     * determines if the intersection is occupied by a vertical vehicle
     * @return
     */
    public boolean isVerticalPassing() {

        return isVehicleInIntersection(northExit.getQueue()) || isVehicleInIntersection(southExit.getQueue());

    }

    /**
     * determines if a vehicle from an exit queue is still in the intersection
     * @param queue
     * @return
     */
    private boolean isVehicleInIntersection(LinkedList<Vehicle> queue) {
        if (queue.size() == 0)
            return false;
        Vehicle vehicle = queue.getLast();
        switch (vehicle.getDirection()) {
            case SOUTH:
                return vehicle.getPosition().getY() < intersection.getPosition().getY() * 40 + 35;
            case NORTH:
                return vehicle.getPosition().getY() > intersection.getPosition().getY() * 40 - 45;
            case EAST:
                return vehicle.getPosition().getX() < intersection.getPosition().getX() * 40 + 35;
            case WEST:
                return vehicle.getPosition().getX() > intersection.getPosition().getX() * 40 - 45;
        }
        return false;
    }

    /**
     * determines if a given vehicle is not yet arrived to intersection
     * @param vehicle
     * @return
     */
    private boolean isFirstNotYetInIntersection(Vehicle vehicle) {
        switch (vehicle.getDirection()) {
            case SOUTH:
                return vehicle.getPosition().getY() < intersection.getPosition().getY() * 40 - 40;
            case NORTH:
                return vehicle.getPosition().getY() > intersection.getPosition().getY() * 40 + 40;
            case EAST:
                return vehicle.getPosition().getX() < intersection.getPosition().getX() * 40 - 40;
            case WEST:
                return vehicle.getPosition().getX() > intersection.getPosition().getX() * 40 + 40;

        }
        return true;

    }

    public Intersection getIntersection() {
        return intersection;
    }

}
