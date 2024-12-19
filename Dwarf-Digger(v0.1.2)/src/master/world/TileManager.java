package master.world;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import master.main.GamePanel;
import master.main.Camera;

public class TileManager {

    public static final int CHUNK_WIDTH = 64; // Tiles per chunk (X-axis)
    public static final int CHUNK_HEIGHT = 38; // Tiles per chunk (Y-axis)
    private static final int LOAD_DISTANCE = 1; // Distance (in chunks) to load around the player
    private static final int TILE_SIZEpx = 16; // Size of each tile in pixels (in sprite sheet)
    public static final int TILE_SIZE = 32; // Size of each tile in pixels (in game)
    private static final int GRASS_TILE_COUNT = 48; // Total grass tile textures
    private static final int DIRT_TILE_COUNT = 30; // Total dirt tile textures
    private static final int DIRT_WALL_COUNT = 16; // Total dirt wall textures

    private GamePanel gp;
    private BufferedImage[] grassTileTextures; // Grass tile textures
    private BufferedImage[] dirtTileTextures; // Dirt tile textures
    private BufferedImage[] dirtWallTextures;
    private Map<String, Chunk> chunks; // Currently loaded chunks
    private Map<String, Chunk> cachedChunks; // Previously loaded chunks
    private PerlinNoise perlin;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.grassTileTextures = new BufferedImage[GRASS_TILE_COUNT];
        this.dirtTileTextures = new BufferedImage[DIRT_TILE_COUNT];
        this.dirtWallTextures = new BufferedImage[DIRT_WALL_COUNT];
        this.chunks = new HashMap<>();
        this.cachedChunks = new HashMap<>();
        this.perlin = new PerlinNoise();

        loadTileTextures();
    }

    private void loadTileTextures() {

        // - Load Tile SpriteSheets -

        try {
            // Load grass tile sprite sheet
            BufferedImage grassSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites/grass.png"));
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < (GRASS_TILE_COUNT / 3); y++) {
                    int index = (3 * y) + x;
                    grassTileTextures[index] = grassSpriteSheet.getSubimage(x * TILE_SIZEpx, y * TILE_SIZEpx, TILE_SIZEpx, TILE_SIZEpx);
                }
            }

            // Load dirt tile sprite sheet
            BufferedImage dirtSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites/dirt.png"));
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < (DIRT_TILE_COUNT / 3); y++) {
                    int index = (3 * y) + x;
                    dirtTileTextures[index] = dirtSpriteSheet.getSubimage(x * TILE_SIZEpx, y * TILE_SIZEpx, TILE_SIZEpx, TILE_SIZEpx);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to load tile sprite sheets!");
        }

        // - Load Wall Sprite Sheets -

        try {
            // Load dirt wall tile sprite sheet
            BufferedImage dirtWallSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites/dirt_wall.png"));
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < (DIRT_WALL_COUNT / 3); y++) {
                    int index = (3 * y) + x;
                    dirtWallTextures[index] = dirtWallSpriteSheet.getSubimage(x * TILE_SIZEpx, y * TILE_SIZEpx, TILE_SIZEpx, TILE_SIZEpx);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to load wall sprite sheets!");
        }
    }

    public void update(Camera camera) {
        int playerChunkX = Math.floorDiv(camera.getX(), CHUNK_WIDTH * TILE_SIZE);
        int playerChunkY = Math.floorDiv(camera.getY(), CHUNK_HEIGHT * TILE_SIZE);

        Map<String, Chunk> newChunks = new HashMap<>();

        // Load chunks within the load distance
        // Preload chunks in a separate loop before assigning to `chunks`
        for (int chunkX = playerChunkX - LOAD_DISTANCE; chunkX <= playerChunkX + LOAD_DISTANCE; chunkX++) {
            for (int chunkY = playerChunkY - LOAD_DISTANCE; chunkY <= playerChunkY + LOAD_DISTANCE; chunkY++) {
                String chunkKey = getChunkKey(chunkX, chunkY);
                if (!chunks.containsKey(chunkKey) && !cachedChunks.containsKey(chunkKey)) {
                    Chunk newChunk = new Chunk(chunkX, chunkY, grassTileTextures, dirtTileTextures, dirtWallTextures, perlin);
                    cachedChunks.put(chunkKey, newChunk); // Preload new chunk into cache
                }
            }
        }

        // Now load chunks from cache or existing data
        for (int chunkX = playerChunkX - LOAD_DISTANCE; chunkX <= playerChunkX + LOAD_DISTANCE; chunkX++) {
            for (int chunkY = playerChunkY - LOAD_DISTANCE; chunkY <= playerChunkY + LOAD_DISTANCE; chunkY++) {
                String chunkKey = getChunkKey(chunkX, chunkY);
                if (chunks.containsKey(chunkKey)) {
                    newChunks.put(chunkKey, chunks.get(chunkKey));
                } else if (cachedChunks.containsKey(chunkKey)) {
                    newChunks.put(chunkKey, cachedChunks.get(chunkKey));
                }
            }
        }

        // Replace current loaded chunks
        chunks = newChunks;
    }

    public void draw(Graphics2D g2, Camera camera) {
        // Draw the sky
        drawSky(g2, camera);

        // Draw chunks
        for (Map.Entry<String, Chunk> entry : chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            chunk.draw(g2, camera, TILE_SIZE, chunks);
        }
    }

    public void drawSky(Graphics2D g2, Camera camera) {
        int colorChangeStartY = -3500;   // Y at Terrain starts blending
        int colorChangeEndY = -4150;    // Y at Fully sky-colored
        int screenWidth = gp.screenWidth;
        int screenHeight = gp.screenHeight;

        // Get the camera's current Y position
        int currentY = camera.getY();

        // Define the terrain and sky colors
        Color terrainColor = new Color(0, 0, 0);    // Black for terrain
        Color skyColor = new Color(84, 142, 218);  // Light blue for sky

        // Ensure colorChangeStartY > colorChangeEndY (since Y is inverted)
        if (colorChangeStartY < colorChangeEndY) {
            int temp = colorChangeStartY;
            colorChangeStartY = colorChangeEndY;
            colorChangeEndY = temp;
        }

        // Normalize Y position for blending
        double normalizedY = Math.max(0, Math.min(1, (double) (colorChangeStartY - currentY) / (colorChangeStartY - colorChangeEndY)));

        // Interpolate between terrainColor and skyColor
        int red = (int) (terrainColor.getRed() * (1 - normalizedY) + skyColor.getRed() * normalizedY);
        int green = (int) (terrainColor.getGreen() * (1 - normalizedY) + skyColor.getGreen() * normalizedY);
        int blue = (int) (terrainColor.getBlue() * (1 - normalizedY) + skyColor.getBlue() * normalizedY);

        // Compute the interpolated color
        Color backgroundColor = new Color(red, green, blue);

        // Set the background color
        g2.setColor(backgroundColor);

        // Fill the entire background
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    public boolean isTileSolid(int worldX, int worldY) {
        // Correctly calculate the chunk coordinates using Math.floor
        int chunkX = (int) Math.floor((double) worldX / CHUNK_WIDTH);
        int chunkY = (int) Math.floor((double) worldY / CHUNK_HEIGHT);

        // Retrieve the chunk
        String chunkKey = getChunkKey(chunkX, chunkY);
        Chunk chunk = chunks.get(chunkKey);
        if (chunk == null) {
            return false; // No chunk means no solid tiles
        }

        // Calculate the local coordinates within the chunk
        int localX = Math.floorMod(worldX, CHUNK_WIDTH);
        int localY = Math.floorMod(worldY, CHUNK_HEIGHT);

        // Return the tile's solidity state
        return chunk.isTileSolid(localX, localY);
    }

    public void removeTile(int worldX, int worldY) {
        int chunkX = (int) Math.floor((double) worldX / CHUNK_WIDTH);
        int chunkY = (int) Math.floor((double) worldY / CHUNK_HEIGHT);

        // Get the chunk
        String chunkKey = getChunkKey(chunkX, chunkY);
        Chunk chunk = chunks.get(chunkKey);

        if (chunk != null) {
            // Calculate local coordinates within the chunk
            int localX = Math.floorMod(worldX, CHUNK_WIDTH);
            int localY = Math.floorMod(worldY, CHUNK_HEIGHT);

            // Set the tile to empty
            chunk.setTile(localX, localY, 0); // 0 = empty tile
        }
    }

    private String getChunkKey(int chunkX, int chunkY) {
        return chunkX + "," + chunkY;
    }

    public Map<String, Chunk> getLoadedChunks() {
        return chunks; // Return loaded/cached chunks
    }

    public Map<String, Chunk> getCachedChunks() {
        return cachedChunks;
    }
}