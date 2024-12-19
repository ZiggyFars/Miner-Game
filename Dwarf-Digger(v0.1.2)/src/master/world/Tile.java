package master.world;

import java.awt.image.BufferedImage;

public class Tile {

    public boolean isSolid; // True if the tile is solid, false otherwise
    public BufferedImage texture; // Texture of the tile

    // Constructor
    public Tile(boolean isSolid, BufferedImage texture) {
        this.isSolid = isSolid;
        this.texture = texture;
    }
}
