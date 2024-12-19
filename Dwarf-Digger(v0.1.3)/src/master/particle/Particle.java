package master.particle;

import master.main.Camera;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Particle {
    // Motion & Position
    private double x, y; // Position
    private int screenX, screenY; // Screen Position
    private double xv, yv; // Velocity
    private int lifetime; // How long the particle lasts
    private double rotation; // Rotation angle in degrees
    private double rotationSpeed; // Speed of rotation
    // Sprite
    private BufferedImage sprite; // The sprite for the particle
    private double scale = 2.0 + (Math.random() / 2) - (Math.random() / 2); // Scale factor for the particle

    public Particle(double x, double y, double xv, double yv, int lifetime, double rotation, double rotationSpeed, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.xv = xv;
        this.yv = yv;
        this.lifetime = lifetime;
        this.sprite = sprite;
        this.rotation = rotation;
        this.rotationSpeed = rotationSpeed;
    }

    public boolean isDead() {
        // Particle disappears if lifetime/screen dimensions are exceeded
        return lifetime <= 0 || screenY > 1080 || screenY < -1080 || screenX > 1920 || screenX < -1920;
    }

    public void update(Camera camera) {
        x += xv;
        y += yv;
        screenX = (int) (x - camera.getX());
        screenY = (int) (y - camera.getY());
        rotation += rotationSpeed;
        yv += 0.1; // Gravity effect
        lifetime--;
    }

    public void draw(Graphics2D g2) {
        // Save the original transform
        AffineTransform originalTransform = g2.getTransform();

        // Translate to the particle's position (centered on the screenX, screenY)
        g2.translate(screenX, screenY);

        // Apply rotation around the center of the particle
        g2.rotate(Math.toRadians(rotation), (sprite.getWidth() * scale) / 2.0, (sprite.getHeight() * scale) / 2.0);

        // Scale the sprite
        g2.scale(scale, scale);

        // Draw the sprite with its top-left corner at (0, 0) after transformations
        g2.drawImage(sprite, 0, 0, null);

        // Restore the original transform
        g2.setTransform(originalTransform);
    }
}