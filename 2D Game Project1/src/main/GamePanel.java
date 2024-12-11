package main;

import entity.Player;
import tile.TileManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    // Screen Settings
    static final int OriginalTileSize = 8;
    static final int scale = 1;
    public static final int tileSize = OriginalTileSize * scale;
    public final int screenWidth = 1920;
    public final int screenHeight = 1080;
    public final int maxScreenCol = screenWidth / tileSize;
    public final int maxScreenRow = screenHeight / tileSize;

    TileManager tileM = new TileManager(this);
    Player player; // Player instance
    KeyHandler keyH = new KeyHandler();
    MouseHandler mouseH = new MouseHandler();

    // FPS
    int FPS = 120;

    Thread gameThread;
    public double deltaTime = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(mouseH);
        this.setFocusable(true);

        // Initialize player
        player = new Player(100, 100);
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
        player.update(tileM, keyH, mouseH); // Update player logic
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        tileM.draw(g2); // Draw tiles

        player.draw(g2, keyH); // Draw the player

        g2.dispose();
    }
}