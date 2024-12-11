package entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import main.KeyHandler;
import main.MouseHandler;
import tile.TileManager;
import main.GamePanel;

public class Player {
    // -+ Player attributes +-

    // Coordinates
    public int x; // top-left corner x-coordinate
    public int y; // top-left corner y-coordinate

    // Dimensions
    int width = 8; // width of the player
    int height = 16; // height of the player

    // Sub-Pixels
    int subPixels = 256; // Total amount of sub-pixels per pixel
    int subPixelX; // Current sub-pixel x coordinate

    // Current Velocity
    private double xv = 0; // Current velocity in the X direction
    private double yv = 0; // Current velocity in the Y direction

    // Physics Parameters (x)
    private final double accelerationW = 38.4; // Speed increase per update (walking)
    private final double accelerationS = 52.0; // Speed increase per update (sprinting)
    private final double deceleration = 25.6; // Speed decrease per update
    public final double maxSpeedW = 768; // Maximum speed (walking)
    public final double maxSpeedS = 1000; // Maximum speed (sprinting)

    // Physics Parameters (y)
    private final double gravity = 0.1; // Acceleration due to gravity
    private final double terminalVelocity = 3; // Speed at which acceleration due to grav stops
    private final double initialJumpVelocity = 12;
    private boolean isAirborne = true;

    // Tile Size
    final int tileSize = GamePanel.tileSize;

    // Collision
    private boolean isCollidingY = false;
    private boolean isCollidingRight = false;
    private boolean isCollidingLeft = false;

    // -+ Methods +-

    // Constructor
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(TileManager tileM, KeyHandler keyH, MouseHandler mouseH) {
        handleVerticalCollision(tileM);
        handleHorizontalCollision(tileM);
        handleMovement(keyH);
        handleJumping(keyH);
        handleMouse(mouseH);
    }

    private void handleMovement(KeyHandler keyH) {

        System.out.println(xv);

        // Apply movement for the left direction
        if (!isCollidingLeft) {
            if (keyH.leftPressed) {
                if (keyH.sprintPressed) {
                    xv -= accelerationS; // Move left (sprinting)
                    // Clamp speed to max speed in the negative direction (left)
                    if (xv < -(maxSpeedS + 0.1)) {
                        xv = -(maxSpeedS + 0.1);
                    }
                }
                else {
                    xv -= accelerationW; // Move left (walking)
                    // Clamp speed to max speed in the negative direction (left)
                    if (xv < -(maxSpeedW + 0.1)) {
                        xv = -(maxSpeedW + 0.1);
                    }
                }
            } else {
                applyDeceleration(); // Apply deceleration when no keys are pressed
            }
        } else {
            xv = Math.max(xv, 0); // Stop any negative velocity if colliding left
            // Snap the player's x position to the corresponding tile edge
            x = (int) (Math.ceil((float) x / tileSize) * tileSize);
            subPixelX = 0; // Reset subPixel counter
        }

        // Apply movement for the right direction
        if (!isCollidingRight) {
            if (keyH.rightPressed) {
                if (keyH.sprintPressed) {
                    xv += accelerationS; // Move right (sprinting)
                    // Clamp speed to max speed in the positive direction (right)
                    if (xv > (maxSpeedS)) {
                        xv = (maxSpeedS);
                    }
                }
                else {
                    xv += accelerationW; // Move right (sprinting)
                    // Clamp speed to max speed in the positive direction (right)
                    if (xv > (maxSpeedW)) {
                        xv = (maxSpeedW);
                    }
                }
            } else {
                applyDeceleration(); // Apply deceleration when no keys are pressed
            }
        } else {
            xv = Math.min(xv, 0); // Stop any positive velocity if colliding right
            // Snap the player's x position to the corresponding tile edge
            x = (int) (Math.floor((float) x / tileSize) * tileSize);
            subPixelX = 0; // Reset subPixel counter
        }

        // Update the player's position using sub-pixels
        subPixelX += xv;
        if (subPixelX >= subPixels) { // Moving right
            x += 1; // Move right by 1 pixel
            subPixelX = 0; // Reset sub-pixel counter
        }
        else if (subPixelX <= -subPixels) { // Moving left
            x -= 1; // Move left by 1 pixel
            subPixelX = 0; // Reset sub-pixel counter
        }
    }

    // Helper function to apply deceleration based on the current velocity direction
    private void applyDeceleration() {
        if (xv > 0) {
            xv -= deceleration; // Slow down to the left
            if (xv < 0) xv = 0; // Stop if we decelerate past zero
        } else if (xv < 0) {
            xv += deceleration; // Slow down to the right
            if (xv > 0) xv = 0; // Stop if we decelerate past zero
        }
    }

    private void handleGravity() {

        if (!isCollidingY) { // If player is not on ground
            yv -= gravity; // Apply gravity to rate of change of vertical velocity
        } else { // If player is on ground
            isAirborne = false;
        }

        y -= (int) yv; // Apply gravity to position

        // Clamp player Y velocity
        if (yv > terminalVelocity) {
            yv = terminalVelocity;
        }
    }

    private void handleJumping(KeyHandler keyH) {
        if (isAirborne || !isCollidingY) {
            handleGravity();
        } else { // If grounded
            if (keyH.jumpPressed) {
                yv = initialJumpVelocity; // Initial 'jump' force
                isAirborne = true;
            }
            else { // If the jump key isn't pressed (in any case other than jumping & on ground)
                // Snap the player's y position to the nearest tile boundary, rounding down (up in coordinate space)
                y = (int) (Math.floor((float) y / tileSize) * tileSize);
                yv = 0; // 0 y velocity
            }
        }
    }

    private void handleVerticalCollision(TileManager tileM) {
        // Calculate the tile coordinates for the player's left and right edges
        int playerTileX1 = x / tileSize;
        int playerTileX2 = (x + width) / tileSize;

        // Calculate the tile coordinates for the player's top and bottom edges
        int playerTileY1 = y / tileSize;
        int playerTileY2 = (y + height) / tileSize;

        // Initialize the collision flag as false before checking for any collisions
        isCollidingY = false;

        // Loop through all tiles in the necessary range
        for (int tileX = playerTileX1; tileX <= playerTileX2; tileX++) {
            for (int tileY = playerTileY1; tileY <= playerTileY2; tileY++) {
                // Check if the tile at (tileX, tileY) is solid (collideable)
                if (tileM.isTileSolid(tileX, tileY)) {
                    // If we find a solid tile, mark as colliding and break out of the loop early
                    isCollidingY = true;
                    break;
                }
            }
            // If a collision is found, break out of the outer loop early
            if (isCollidingY) {
                break;
            }
        }
    }

    private void handleHorizontalCollision(TileManager tileM) {
        // Calculate the tile coordinates for the player's left and right edges
        int playerTileX1 = x / tileSize;
        int playerTileX2 = (x + width) / tileSize;

        // Calculate the tile coordinates for the player's top and bottom edges,
        // avoid interfering with vertical collisions by using a small offset
        int playerTileY1 = (y / tileSize) + 1;
        int playerTileY2 = ((y + height) / tileSize) - 1;

        // Reset collision flags before checking
        isCollidingLeft = false;
        isCollidingRight = false;

        // Loop through tiles on the left side of the player
        for (int tileX = playerTileX1; tileX <= playerTileX1; tileX++) {
            for (int tileY = playerTileY1; tileY <= playerTileY2; tileY++) {
                // Check if the tile is solid
                if (tileM.isTileSolid(tileX, tileY)) {
                   isCollidingLeft = true;
                   break; // Stop searching once we detect a collision
                }
            }
            // Exit early if collision has already been detected
            if (isCollidingLeft) {
                break;
            }
        }

        // Loop through tiles on the right side of the player
        for (int tileX = playerTileX2; tileX <= playerTileX2; tileX++) {
            for (int tileY = playerTileY1; tileY <= playerTileY2; tileY++) {
                // Check if the tile is solid
                if (tileM.isTileSolid(tileX, tileY)) {
                    isCollidingRight = true;
                    break; // Stop searching once we detect a collision
                }
            }
            // Exit early if collision has already been detected
            if (isCollidingRight) {
                break;
            }
        }
    }

    public void draw(Graphics2D g2, KeyHandler keyH) {

        // Save the current transformation
        AffineTransform oldTransform = g2.getTransform();

        // Translate to player's position then draw
        g2.translate(x + (width/2), y + (height/2)); // Translate to center of the player

        // Draw the player (centered around the origin after translation)
        g2.setColor(Color.red);
        g2.fillRect(-(width/2), -(height/2), width, height); // Adjust to draw centered

        // Restore the original transformation
        g2.setTransform(oldTransform);
    }

    private void handleMouse(MouseHandler mouseH) {
        // Handle left-click to teleport
        if (mouseH.leftClick) {
            // Teleport the player to mouse X and Y position
            // (Debug) System.out.println("Mouse clicked at: (" + mouseH.mouseX + ", " + mouseH.mouseY + ")");
            x = mouseH.mouseX;
            y = mouseH.mouseY;
        }
    }
}