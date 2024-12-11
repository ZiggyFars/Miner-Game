package tile;

import java.awt.*;
import main.GamePanel;

public class TileManager {

    GamePanel gp;
    Tile[] tile;
    PerlinNoise perlin;
    boolean[][] heightMap; // Array to store terrain data (1 = filled, 0 = empty)

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tile = new Tile[2]; // Initialize tile types
        perlin = new PerlinNoise();
        heightMap = new boolean[gp.maxScreenCol][gp.maxScreenRow];
        generateTerrain();
    }

    private void generateTerrain() {
        double scale = 0.05; // Scale of noise
        int octaves = 4; // Octaves means amount of detail, more is more detail
        double persistence = 0.5; // Amplitude decrease factor
        double lacunarity = 2.0; // Frequency increase factor

        // Set the threshold for filled tiles
        double threshold = 0.5; // Adjust this value for more or less cave density

        for (int x = 0; x < heightMap.length; x++) {
            for (int y = 0; y < heightMap[0].length; y++) {
                // Generate noise value using the new noise method
                double noiseValue = perlin.noise(x * scale, y * scale, octaves, persistence, lacunarity);

                // Normalize noise value to the range of 0 to 1 and compare with the threshold
                heightMap[x][y] = (noiseValue > threshold) ? true : false; // true = filled, false = empty
            }
        }
    }

    // Method to check if a tile is solid
    public boolean isTileSolid(int x, int y) {
        if (x < 0 || x >= gp.maxScreenCol || y < 0 || y >= gp.maxScreenRow) {
            return false; // Out of bounds
        }
        return heightMap[x][y];
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.white);
        for (int x = 0; x < heightMap.length; x++) {
            for (int y = 0; y < heightMap[0].length; y++) {
                if (heightMap[x][y]) {
                    // Draw filled tile
                    g2.fillRect(x * gp.tileSize, y * gp.tileSize, gp.tileSize, gp.tileSize);
                }
            }
        }
    }
}