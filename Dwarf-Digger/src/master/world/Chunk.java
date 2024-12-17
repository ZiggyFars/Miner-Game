package master.world;

import java.awt.*;
import java.awt.image.BufferedImage;
import master.main.Camera;
import java.util.Map;

public class Chunk {

    private static final int CHUNK_WIDTH = 64;
    private static final int CHUNK_HEIGHT = 38;

    private int[][] tiles; // Tile data: 0 = empty, 1 = grass, 2 = dirt
    private BufferedImage[] grassTextures;
    private BufferedImage[] dirtTextures;
    private int chunkX, chunkY;

    public Chunk(int chunkX, int chunkY, BufferedImage[] grassTextures, BufferedImage[] dirtTextures, PerlinNoise perlin) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.grassTextures = grassTextures;
        this.dirtTextures = dirtTextures;
        this.tiles = new int[CHUNK_WIDTH][CHUNK_HEIGHT];

        generateTiles(perlin);
    }

    private void generateTiles(PerlinNoise perlin) {
        double scale = 0.15;
        int octaves = 6;
        double persistence = 0.5;
        double lacunarity = 2.0;
        double dirtThreshold = 0.5; // Threshold for dirt
        double grassThreshold = 0.49;  // Grass randomness factor
        double surfaceScale = 0.03;    // Scale for surface terrain
        double surfaceHeightMultiplier = 315.0; // Height multiplier for surface terrain
        double thresholdBlendHeight = -50.0; // Y range where the dirt threshold is adjusted

        // First pass: Generate caves and surface terrain independently
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                int worldX = (chunkX * CHUNK_WIDTH) + x;
                int worldY = (chunkY * CHUNK_HEIGHT) + y;

                double caveNoise = perlin.noise(worldX * scale, worldY * scale, octaves, persistence, lacunarity);
                double surfaceNoise = perlin.noise(worldX * surfaceScale, 0, octaves, persistence, lacunarity);
                double surfaceHeight = surfaceNoise * surfaceHeightMultiplier;

                if (worldY < 0) {
                    // Surface terrain generation (above world Y = 0)
                    if (-worldY < surfaceHeight) {
                        tiles[x][y] = 1; // Grass
                    } else {
                        tiles[x][y] = 0; // Empty
                    }
                } else {
                    // Cave terrain generation (below world Y = 0)
                    if (caveNoise > dirtThreshold) {
                        tiles[x][y] = 2; // Dirt
                    } else {
                        tiles[x][y] = 0; // Empty
                    }
                }
            }
        }

        // Second pass: Carve caves into the surface terrain with a dynamic threshold
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                int worldX = (chunkX * CHUNK_WIDTH) + x;
                int worldY = (chunkY * CHUNK_HEIGHT) + y;

                if (worldY < 0 && tiles[x][y] != 0) {
                    double caveNoise = perlin.noise(worldX * scale, worldY * scale, octaves, persistence, lacunarity);

                    // Adjust the dirtThreshold based on the Y-coordinate
                    double adjustedDirtThreshold = dirtThreshold;
                    if (-worldY < thresholdBlendHeight) {
                        double blendFactor = Math.max(0, 1 - (-worldY / thresholdBlendHeight));
                        adjustedDirtThreshold += blendFactor * 0.2; // Increase the threshold as we approach the surface
                    }

                    // Carve tiles based on the adjusted threshold
                    if (caveNoise <= adjustedDirtThreshold) {
                        tiles[x][y] = 0; // Carve out the tile
                    }
                }
            }
        }

        // Third pass: Generate grass on borders with air for both terrains
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                if (tiles[x][y] == 2) { // Only check dirt tiles
                    int worldX = (chunkX * CHUNK_WIDTH) + x;
                    int worldY = (chunkY * CHUNK_HEIGHT) + y;

                    // Add randomness for grass placement
                    double grassNoise = perlin.noise(worldX * scale * 1.5, worldY * scale * 1.5, octaves, persistence, lacunarity);

                    if (isAdjacentToAir(x, y) && grassNoise > grassThreshold) {
                        tiles[x][y] = 1; // Grass
                    }
                }
            }
        }
    }

    // Helper method to check if a tile is adjacent to air
    private boolean isAdjacentToAir(int x, int y) {
        // Check orthogonal neighbors
        if (x > 0 && tiles[x - 1][y] == 0) return true; // Left
        if (x < CHUNK_WIDTH - 1 && tiles[x + 1][y] == 0) return true; // Right
        if (y > 0 && tiles[x][y - 1] == 0) return true; // Above
        if (y < CHUNK_HEIGHT - 1 && tiles[x][y + 1] == 0) return true; // Below

        // Check diagonals
        if (x > 0 && y > 0 && tiles[x - 1][y - 1] == 0) return true; // Top-left
        if (x < CHUNK_WIDTH - 1 && y > 0 && tiles[x + 1][y - 1] == 0) return true; // Top-right
        if (x > 0 && y < CHUNK_HEIGHT - 1 && tiles[x - 1][y + 1] == 0) return true; // Bottom-left
        if (x < CHUNK_WIDTH - 1 && y < CHUNK_HEIGHT - 1 && tiles[x + 1][y + 1] == 0) return true; // Bottom-right

        return false;
    }

    public void draw(Graphics2D g2, Camera camera, int tileSize, int chunkX, int chunkY, Map<String, Chunk> chunks) {
        int offsetX = chunkX * CHUNK_WIDTH * tileSize - camera.getX();
        int offsetY = chunkY * CHUNK_HEIGHT * tileSize - camera.getY();

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                if (tiles[x][y] != 0) {
                    int screenX = offsetX + x * tileSize;
                    int screenY = offsetY + y * tileSize;

                    int textureIndex = getTileTextureIndex(x, y, chunkX, chunkY, chunks);
                    BufferedImage texture = (tiles[x][y] == 1) ? grassTextures[textureIndex] : dirtTextures[textureIndex];

                    g2.drawImage(texture, screenX, screenY, tileSize, tileSize, null);
                }
            }
        }
    }

    private int getTileTextureIndex(int localX, int localY, int chunkX, int chunkY, Map<String, Chunk> chunks) {
        int tileType = tiles[localX][localY];

        // Check orthogonal surroundings
        boolean above = getTileState(localX, localY - 1, chunkX, chunkY, chunks) != 0;
        boolean below = getTileState(localX, localY + 1, chunkX, chunkY, chunks) != 0;
        boolean left = getTileState(localX - 1, localY, chunkX, chunkY, chunks) != 0;
        boolean right = getTileState(localX + 1, localY, chunkX, chunkY, chunks) != 0;

        // Check diagonal surroundings
        boolean topLeft = getTileState(localX - 1, localY - 1, chunkX, chunkY, chunks) != 0;
        boolean topRight = getTileState(localX + 1, localY - 1, chunkX, chunkY, chunks) != 0;
        boolean bottomLeft = getTileState(localX - 1, localY + 1, chunkX, chunkY, chunks) != 0;
        boolean bottomRight = getTileState(localX + 1, localY + 1, chunkX, chunkY, chunks) != 0;

        // Use tileType-specific adjacency rules
        switch (tileType) {
            case 1: // Grass
                return determineGrassTexture(above, below, left, right, topLeft, topRight, bottomLeft, bottomRight);
            case 2: // Dirt
                return determineDirtTexture(above, below, left, right, topLeft, topRight, bottomLeft, bottomRight);
            default:
                return -1; // Empty or unknown type
        }
    }

    // Helper methods for texture determination
    private int determineGrassTexture(boolean above, boolean below, boolean left, boolean right,
                                      boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
        // Logic for determining grass texture index
        if (!above && !left && right && below && !bottomRight) return 24; // Top-left corner 2
        if (!above && !right && left && below && !bottomLeft) return 25; // Top-right corner 2
        if (!below && !left && right && above && !topRight) return 27; // Bottom-left corner 2
        if (!below && !right && left && above && !topLeft) return 28; // Bottom-right corner 2

        if (!above && below && left && right && !bottomRight && !bottomLeft) return 30; // Top edge 2
        if (!above && below && left && right && !bottomRight && bottomLeft) return 31; // Top edge 3
        if (!above && below && left && right && bottomRight && !bottomLeft) return 32; // Top edge 4

        if (!below && above && left && right && !topRight && !topLeft) return 33; // Bottom edge 2
        if (!below && above && left && right && !topRight && topLeft) return 34; // Bottom edge 3
        if (!below && above && left && right && topRight && !topLeft) return 35; // Bottom edge 4

        if (above && below && !left && right && !topRight && !bottomRight) return 36; // Left edge 2
        if (above && below && !left && right && topRight && !bottomRight) return 37; // Left edge 3
        if (above && below && !left && right && !topRight && bottomRight) return 38; // Left edge 4

        if (above && below && left && !right && !topLeft && !bottomLeft) return 39; // Right edge 2
        if (above && below && left && !right && topLeft && !bottomLeft) return 40; // Right edge 3
        if (above && below && left && !right && !topLeft && bottomLeft) return 41; // Right edge 4

        if (above && left && right && below && !topLeft && !topRight) return 18; // Top-left&right center
        if (above && left && right && below && !bottomLeft && !bottomRight) return 19; // Bottom-left&right center
        if (above && left && right && below && !topRight && !bottomRight) return 21; // Top&bottom right center
        if (above && left && right && below && !topLeft && !bottomLeft) return 22; // Top&bottom left center
        if (above && left && right && below && !topRight && !bottomLeft) return 20; // Top-right&bottom-left center
        if (above && left && right && below && !topLeft && !bottomRight) return 23; // Top-left&bottom-right center

        if (above && below && left && right && topLeft && !topRight && !bottomLeft && !bottomRight) return 42; // 3 corner center 1
        if (above && below && left && right && !topLeft && topRight && !bottomLeft && !bottomRight) return 43; // 3 corner center 2
        if (above && below && left && right && !topLeft && !topRight && !bottomLeft && bottomRight) return 44; // 3 corner center 3
        if (above && below && left && right && !topLeft && !topRight && bottomLeft && !bottomRight) return 45; // 3 corner center 4

        if (above && below && left && right && !topLeft && !topRight && !bottomLeft && !bottomRight) return 46; // 4 corner center

        if (above && left && !topLeft && right && below) return 9;  // Top-left center
        if (above && right && !topRight && left && below) return 11; // Top-right center
        if (below && left && !bottomLeft && above && right) return 15; // Bottom-left center
        if (below && right && !bottomRight && above && left) return 17; // Bottom-right center

        if (!above && !below && !left && !right) return 13; // None adjacent
        if (!above && below && !left && !right) return 10; // Up with 1 adjacent
        if (!left && right && !above && !below) return 12; // Left with 1 adjacent
        if (!right && left && !above && !below) return 14; // Right with 1 adjacent
        if (!below && above && !left && !right) return 16; // Down with 1 adjacent

        if (!below && !above && left && right) return 26; // Horizontal 2 adjacent
        if (below && above && !left && !right) return 29; // Vertical 2 adjacent

        if (!above && !left) return 0; // Top-left corner
        if (!above && !right) return 2; // Top-right corner
        if (!above) return 1; // Top edge
        if (!below && !left) return 6; // Bottom-left corner
        if (!below && !right) return 8; // Bottom-right corner
        if (!below) return 7; // Bottom edge
        if (!left) return 3; // Left edge
        if (!right) return 5; // Right edge

        return 4; // Center (default)
    }

    private int determineDirtTexture(boolean above, boolean below, boolean left, boolean right,
                                     boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
        // Logic for determining dirt texture index
        if (above && left && right && below && !topLeft && !topRight) return 18; // Top-left&right center
        if (above && left && right && below && !bottomLeft && !bottomRight) return 19; // Bottom-left&right center
        if (above && left && right && below && !topRight && !bottomRight) return 21; // Top&bottom right center
        if (above && left && right && below && !topLeft && !bottomLeft) return 22; // Top&bottom left center
        if (above && left && right && below && !topRight && !bottomLeft) return 20; // Top-right&bottom-left center
        if (above && left && right && below && !topLeft && !bottomRight) return 23; // Top-left&bottom-right center

        if (above && left && !topLeft && right && below) return 9;  // Top-left center
        if (above && right && !topRight && left && below) return 11; // Top-right center
        if (below && left && !bottomLeft && above && right) return 15; // Bottom-left center
        if (below && right && !bottomRight && above && left) return 17; // Bottom-right center

        if (!above && !below && !left && !right) return 13; // None adjacent
        if (!above && below && !left && !right) return 10; // Up with 1 adjacent
        if (!left && right && !above && !below) return 12; // Left with 1 adjacent
        if (!right && left && !above && !below) return 14; // Right with 1 adjacent
        if (!below && above && !left && !right) return 16; // Down with 1 adjacent

        if (!below && !above && left && right) return 26; // Horizontal 2 adjacent
        if (below && above && !left && !right) return 29; // Vertical 2 adjacent

        if (!above && !left) return 0; // Top-left corner
        if (!above && !right) return 2; // Top-right corner
        if (!above) return 1; // Top edge
        if (!below && !left) return 6; // Bottom-left corner
        if (!below && !right) return 8; // Bottom-right corner
        if (!below) return 7; // Bottom edge
        if (!left) return 3; // Left edge
        if (!right) return 5; // Right edge

        return 4; // Center (default)
    }

    private int getTileState(int localX, int localY, int chunkX, int chunkY, Map<String, Chunk> chunks) {
        if (localX >= 0 && localX < CHUNK_WIDTH && localY >= 0 && localY < CHUNK_HEIGHT) {
            // The tile is within the current chunk
            return tiles[localX][localY];
        } else {
            // The tile is outside the current chunk; check neighboring chunks
            if (localX < 0) {
                chunkX -= 1;
                localX = CHUNK_WIDTH - 1;
            } else if (localX >= CHUNK_WIDTH) {
                chunkX += 1;
                localX = 0;
            }

            if (localY < 0) {
                chunkY -= 1;
                localY = CHUNK_HEIGHT - 1;
            } else if (localY >= CHUNK_HEIGHT) {
                chunkY += 1;
                localY = 0;
            }

            // Retrieve the neighboring chunk
            String neighborKey = chunkX + "," + chunkY;
            Chunk neighborChunk = chunks.get(neighborKey);
            if (neighborChunk != null) {
                return neighborChunk.getTileType(localX, localY);
            } else {
                // If the neighboring chunk isn't loaded, assume it's empty
                return 0; // Empty
            }
        }
    }

    public int getTileType(int x, int y) {
        if (x < 0 || x >= CHUNK_WIDTH || y < 0 || y >= CHUNK_HEIGHT) {
            return 0; // Empty
        }
        return tiles[x][y];
    }

    public boolean isTileSolid(int x, int y) {
        if (x < 0 || x >= CHUNK_WIDTH || y < 0 || y >= CHUNK_HEIGHT) {
            return false;
        }
        return tiles[x][y] != 0;
    }

    public void setTile(int x, int y, int value) {
        if (x >= 0 && x < CHUNK_WIDTH && y >= 0 && y < CHUNK_HEIGHT) {
            tiles[x][y] = value; // Set the tile to the specified value
        }
    }

    public int getChunkX() {
        return chunkX;
    }
    public int getChunkY() {
        return chunkY;
    }
}