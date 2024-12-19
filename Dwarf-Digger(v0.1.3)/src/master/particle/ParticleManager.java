package master.particle;

import master.main.Camera;
import master.world.TileManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private List<Particle> particles;
    private List<BufferedImage> dirtParticleSprites;
    private TileManager tileManager;

    public ParticleManager(TileManager tileManager) {
        this.tileManager = tileManager;
        particles = new ArrayList<>();
        dirtParticleSprites = new ArrayList<>();

        // Load and split particle sprite sheet
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites/dirt_particle.png"));
            int spriteSize = 8; // Size of each individual sprite (8x8)

            // Extract individual sprites from the sprite sheet
            for (int i = 0; i < spriteSheet.getHeight() / spriteSize; i++) {
                BufferedImage sprite = spriteSheet.getSubimage(0, i * spriteSize, spriteSize, spriteSize);
                dirtParticleSprites.add(sprite);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load particle sprite sheet!");
        }
    }

    // Spawn particles
    public void spawnTileParticles(int worldX, int worldY, Camera camera) {
        for (int i = 0; i < (Math.ceil(Math.random() * 2) + 2); i++) { // Spawn multiple particles (random amount, at least 2)
            double xv = Math.random() * 2 - 1; // Random horizontal velocity
            double yv = Math.random() * -2 - 2; // Random upward velocity
            int lifetime = 240 + (int) (Math.random() * 30); // Random lifetime (at least 240 frames)
            int tileOffset = (int) ((TileManager.TILE_SIZE / 2) + (Math.random() * (TileManager.TILE_SIZE / 4)) +
                                    (Math.random() * -(TileManager.TILE_SIZE / 4))); // Tile offset to position particles near the center of tiles
            double rotation = (Math.random() * 360 + Math.random() * -360); // Random rotation
            double rotationSpeed = Math.random() * 2 + Math.random() * -2; // Random rotation speed
            BufferedImage dirtParticleSprite = dirtParticleSprites.get((int) (Math.random() * dirtParticleSprites.size())); // Random sprite
            particles.add(new Particle(worldX + tileOffset, worldY + tileOffset, xv, yv, lifetime, rotation, rotationSpeed, dirtParticleSprite));
        }
    }

    public void update(Camera camera) {
        // Update and remove dead particles
        particles.removeIf(Particle::isDead);
        for (Particle particle : particles) {
            particle.update(camera);
        }
    }

    public void draw(Graphics2D g2) {
        for (Particle particle : particles) {
            particle.draw(g2);
        }
    }
}