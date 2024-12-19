package master.world;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Map;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import master.main.GamePanel;
import master.entity.Player;
import master.sound.SoundManager;

public class MapScreen implements MouseWheelListener {

    private final double ZOOM_STEP = 0.1;  // Amount to zoom per scroll
    private final double MIN_ZOOM = 0.01;   // Minimum zoom-out level
    private final double MAX_ZOOM = 3.0;   // Maximum zoom-in level
    private double zoomLevel = 1.0;        // Default zoom level

    private boolean mapOpen = false;
    private TileManager tileManager;
    private Player player;
    private GamePanel gp;
    private BufferedImage playerIcon;

    public MapScreen(TileManager tileManager, Player player, GamePanel gp) {
        this.tileManager = tileManager;
        this.player = player;
        this.gp = gp;

        // Load the player icon sprite
        try {
            playerIcon = ImageIO.read(getClass().getResourceAsStream("/sprites/player_icon.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading player icon sprite!");
        }

        // Attach mouse wheel listener
        gp.addMouseWheelListener(this);
    }

    public void toggleMap() {
        mapOpen = !mapOpen;

        // Play sound based on map state
        if (mapOpen) {
            SoundManager.playSound("map_open");
        } else {
            SoundManager.playSound("map_close");
        }
    }

    public boolean isMapOpen() {
        return mapOpen;
    }

    public void closeMap() {
        mapOpen = false;
    }

    public void draw(Graphics2D g2) {
        int screenWidth = gp.screenWidth;
        int screenHeight = gp.screenHeight;

        // Adjust tile size based on zoom level
        int tilesAcrossScreen = (int) (TileManager.CHUNK_WIDTH * 2 / zoomLevel);
        int tilesDownScreen = (int) (TileManager.CHUNK_HEIGHT * 2 / zoomLevel);
        int scaledTileSizeX = screenWidth / tilesAcrossScreen;
        int scaledTileSizeY = screenHeight / tilesDownScreen;
        int scaledTileSize = Math.min(scaledTileSizeX, scaledTileSizeY);

        // Center of the map on screen
        int mapCenterX = screenWidth / 2;
        int mapCenterY = screenHeight / 2;

        // Player's exact position in world coordinates
        double playerWorldX = player.getX();
        double playerWorldY = player.getY();

        // Draw all loaded and cached chunks
        for (Map.Entry<String, Chunk> entry : tileManager.getCachedChunks().entrySet()) {
            Chunk chunk = entry.getValue();
            int chunkX = chunk.getChunkX() * TileManager.CHUNK_WIDTH;
            int chunkY = chunk.getChunkY() * TileManager.CHUNK_HEIGHT;

            for (int x = 0; x < TileManager.CHUNK_WIDTH; x++) {
                for (int y = 0; y < TileManager.CHUNK_HEIGHT; y++) {
                    int worldX = chunkX + x;
                    int worldY = chunkY + y;

                    // Calculate map position with zoom applied
                    double mapX = mapCenterX + (worldX - playerWorldX / TileManager.TILE_SIZE) * scaledTileSize;
                    double mapY = mapCenterY + (worldY - playerWorldY / TileManager.TILE_SIZE) * scaledTileSize;

                    // Determine the color based on the tile type
                    Color tileColor;
                    int tileType = chunk.getForegroundTileType(x, y);
                    switch (tileType) {
                        case 1: tileColor = new Color(20, 82, 20); break; // Grass (green)
                        case 2: tileColor = new Color(66, 31, 10); break; // Dirt (brown)
                        default: tileColor = Color.BLACK; break; // Air (black)
                    }

                    // Draw the tile
                    g2.setColor(tileColor);
                    g2.fillRect((int) mapX, (int) mapY, scaledTileSize, scaledTileSize);
                }
            }
        }

        // Draw the player icon
        int iconSize = scaledTileSize + 4; // Scale the icon

        int playerIconX = mapCenterX;
        int playerIconY = mapCenterY;

        // Check if the player is facing left to flip the icon
        if (player.isFacingLeft()) {
            // Create an AffineTransform to flip the image horizontally
            AffineTransform transform = new AffineTransform();
            transform.translate(playerIconX + iconSize, playerIconY); // Move to the correct position
            transform.scale(-((double) iconSize / 12), ((double) iconSize / 12)); // Flip horizontally
            g2.drawImage(playerIcon, transform, null);
        } else {
            // Draw normally if not facing left
            g2.drawImage(playerIcon, playerIconX, playerIconY, iconSize, iconSize, null);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();

        if (rotation > 0) { // Scrolled down -> Zoom out
            zoomLevel = Math.max(MIN_ZOOM, zoomLevel - ZOOM_STEP);
        } else if (rotation < 0) { // Scrolled up -> Zoom in
            zoomLevel = Math.min(MAX_ZOOM, zoomLevel + ZOOM_STEP);
        }
    }
}