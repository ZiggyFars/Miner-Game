package master.main;

import master.entity.Player;
import master.world.TileManager;
import master.sound.SoundManager;
import master.world.MapScreen;
import master.particle.ParticleManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    // Screen Settings
    static final int OriginalTileSize = 16;
    static final int scale = 2;
    public static final int tileSize = OriginalTileSize * scale;
    public final int screenWidth = 1920;
    public final int screenHeight = 1080;

    TileManager tileM = new TileManager(this);
    Player player; // Player instance
    Camera camera = new Camera(screenWidth, screenHeight); // Initialize camera
    KeyHandler keyH = new KeyHandler();
    MouseHandler mouseH = new MouseHandler(camera);
    private MapScreen mapScreen;
    private ParticleManager particleManager;

    private boolean mapOpen = false; // Keeps track of if the map is open

    // FPS
    int FPS = 120;

    Thread gameThread;
    public double deltaTime = 0;

    public GamePanel() {
        player = new Player(-64, 0); // Initialize player
        mapScreen = new MapScreen(tileM, player, this); // Initialize the map screen
        particleManager = new ParticleManager(tileM); // Initialize particle manager

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(mouseH);
        this.setFocusable(true);

        // Load sound files
        SoundManager.loadSound("jump", "/sounds/jump.wav");
        SoundManager.loadSound("teleport", "/sounds/teleport.wav");
        SoundManager.loadSound("dig", "/sounds/dig.wav");
        SoundManager.loadSound("map_open", "/sounds/menu_open.wav");
        SoundManager.loadSound("map_close", "/sounds/menu_close.wav");
        // Adjust volume
        SoundManager.setAllVolumes(-10.0f); // Reduce volume globally
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double lastTime = System.nanoTime();
        double drawInterval = 1000000000.0 / FPS; // Time per frame in nanoseconds

        while (gameThread != null) {
            double currentTime = System.nanoTime();
            deltaTime += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            while (deltaTime >= 1) {
                update(); // Update game logic
                deltaTime--; // Decrease dt by one frame
            }

            repaint();

            try {
                double nextDrawTime = System.nanoTime() + drawInterval;
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000.0;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void update() {
        if (keyH.mapToggle) {
            mapScreen.toggleMap();
            keyH.mapToggle = false; // Reset toggle
        }
        if (!mapScreen.isMapOpen()) {
            // Normal game logic
            player.update(tileM, keyH, mouseH, particleManager, camera); // Update player logic
            camera.centerOnPlayer(player.x, player.y); // Center the camera on the player's position
            tileM.update(camera); // Update world
            particleManager.update(camera); // Update particles
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (mapScreen.isMapOpen()) {
            mapScreen.draw(g2); // Draw the map
        } else {
            // Render game normally
            tileM.draw(g2, camera); // Draw the world
            player.draw(g2, keyH, camera); // Draw the player
            particleManager.draw(g2); // Draw particles
        }

        g2.dispose();
    }
}