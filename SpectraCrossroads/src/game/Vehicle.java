package game;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Vehicle {
    private Tuple position;
    private Direction direction;
    private BufferedImage carImage;
    private int delayCounter = -1;

    public Vehicle(Tuple position, Direction direction) throws IOException {
        this.position = position;
        this.direction = direction;
        this.carImage = createImage(direction);
                
    }


    private BufferedImage createImage(Direction direction) throws IOException {
        String fileName = "";
        switch (direction){
            case SOUTH:
                fileName = "img/car_south.png";
                break;
            case NORTH:
                fileName = "img/car_north.png";
                break;
            case EAST:
                fileName = "img/car_east.png";
                break;
            case WEST:
                fileName = "img/car_west.png";
                break;
        }

        BufferedImage carImage = ImageIO.read(new File(fileName));
        return carImage;
    }


    public void drive(boolean state) {
        delayCounter = state ? delayCounter -1: 5;
        if (delayCounter < 0)
            performMove(position);
    }


    public Direction getDirection() {
        return direction;
    }

    private void performMove(Tuple position) {
        switch (direction) {
            case EAST:
                position.moveX(2);
                break;
            case WEST:
                position.moveX(-2);
                break;
            case NORTH:
                position.moveY(-2);
                break;
            case SOUTH:
                position.moveY(2);
                break;
        }

    }

    public Tuple getPosition() {
        return new Tuple(position.getX(), position.getY());
    }


    public void draw(Graphics g) {
        g.drawImage(carImage, position.getX(), position.getY(), null);
    }
}