package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * an object of graphics for a tile in the game board
 *
 */
public class Tile {
    private BufferedImage tileImage;
    Tile(TileType type) throws IOException {
        switch (type) {
            case GRASS:
                this.tileImage = ImageIO.read(new File("img/grass.png"));
                break;
            case VERTICAL:
                this.tileImage = ImageIO.read(new File("img/vertical_road.png"));
                break;
            case HORIZONTAL:
                this.tileImage = ImageIO.read(new File("img/horizontal_road.png"));
                break;
            case INTERSECTION:
                this.tileImage = ImageIO.read(new File("img/intersection.png"));
                break;
        }
    }



    public void draw(Tuple position, Graphics g) {
        int tileSize = 40;
        g.drawImage(tileImage, position.getX() * tileSize, position.getY() * tileSize, null);
    }
}
