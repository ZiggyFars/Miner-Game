package master.entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import master.main.KeyHandler;
import master.main.MouseHandler;
import master.particle.ParticleManager;
import master.world.TileManager;
import master.main.GamePanel;
import master.main.Camera;
import master.sound.SoundManager;

public class Player {
    // -+ Player attributes +-

    // Coordinates
    public int x; // top-left corner x-coordinate
    public int y; // top-left corner y-coordinate
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    // Dimensions
    private int scale = 2; // Scale factor applied to player
    int width = 16 * scale; // width of the player
    int height = 16 * scale; // height of the player

    // Sprite
    private BufferedImage spriteSheet; // Holds the sprite sheet
    private BufferedImage currentFrame; // Holds the current frame
    // Animation state
    private int animationFrame = 0; // Current animation frame (0 or 1)
    private boolean facingLeft = false; // Current direction
    private boolean turningLeft = false; // Whether the player is turning to the left
    private boolean turningRight = false; // Whether the player is turning to the right
    // Animation timer
    private int animationTimer = 0; // Timer for switching frames
    private int animationSpeed = 7; // Base animation speed (lower = faster)
    private float turningSpriteV = 400; // Speed above which turning sprite is shown when turning

    // Direction
    public boolean isFacingLeft() {
        return facingLeft;
    }

    // Sub-Pixels
    int subPixels = 256; // Total amount of sub-pixels per pixel
    int subPixelX; // Current sub-pixel x coordinate
    int subPixelY;

    // Current Velocity
    private double xv = 0; // Current velocity in the X direction
    private double yv = 0; // Current velocity in the Y direction

    // Physics Parameters (x)
    private double currentDecel; // Keeps track of the current deceleration
    private double currentMaxSpeed;
    private double currentAccel;
    private final double deceleration = 24.7; // Speed decrease per update (not in the air)
    private final double decelerationAir = 15.0; // Speed decrease per update (in air)
    private final double accelerationD = 10.0; // Speed increase per update (ducking)
    private final double accelerationW = 18.4; // Speed increase per update (walking)
    private final double accelerationS = 20.48; // Speed increase per update (sprinting)
    public final double maxSpeedD = 250; // Maximum speed (ducking)
    public final double maxSpeedW = 425; // Maximum speed (walking)
    public final double maxSpeedS = 768; // Maximum speed (sprinting)

    // Physics Parameters (y)
    private final double jumpReleaseGravity = 50.25; // Base acceleration due to gravity (when not holding jump)
    private final double jumpHoldGravity = 35.5; // Base gravity when holding jump
    private final double gravityDecayFactor = 0.0005; // Controls how quickly gravity decreases
    private final double initialJumpVelocity = 940;
    private boolean isAirborne = true;
    private boolean canJump = true; // Flag to prevent player from jumping again immediately after touching ground,
                                    // (for cases where player is holding jump)
    private double xvInfluence = 0.15; // Percentage that xv influences initial jump velocity
    private boolean isDucking = false;

    // Tile Size
    final int tileSize = GamePanel.tileSize;

    // Collision
    private boolean isCollidingGround = false;
    private boolean isCollidingCeiling = false;
    private boolean isCollidingRight = false;
    private boolean isCollidingLeft = false;

    // -+ Methods +-

    // Constructor
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        loadSpriteSheet();
        currentFrame = getFrame(0, 0);
    }

    private void loadSpriteSheet() {
        try {
            // Load the sprite sheet from the resources folder
            spriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites/mario.png"));
            // Extract the first frame (top-left 16x16 area of the sprite sheet)
            currentFrame = spriteSheet.getSubimage(0, 0, 16, 16);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to load the sprite sheet!");
        }
    }

    private BufferedImage getFrame(int col, int row) {
        return spriteSheet.getSubimage(col * 16, row * 16, 16, 16);
    }

    public void update(TileManager tileM, KeyHandler keyH, MouseHandler mouseH, ParticleManager particleM, Camera camera) {
        handleVerticalCollision(tileM);
        handleHorizontalCollision(tileM);
        handleMovement(keyH);
        handleJumping(keyH);
        handleDucking(keyH);
        handleMouse(mouseH);
        handleDigging(tileM, keyH, particleM, camera);
        updateAnimation();
    }

    private void handleMovement(KeyHandler keyH) {

        // Apply movement for the left direction
        if (!isCollidingLeft) {
            if (!isDucking || (isAirborne || !isCollidingGround)) { // If not ducking or in the air
                if (keyH.aPressed) {
                    // Sprinting logic
                    if (keyH.sprintPressed && !isDucking) {
                        xv -= accelerationS; // Move left (sprinting)
                        // Clamp speed to max speed in the negative direction (left)
                        if (xv < -(maxSpeedS)) {
                            xv = -maxSpeedS;
                        }
                    } else {
                        // Walking logic
                        if (!isDucking) {
                            currentMaxSpeed = maxSpeedW; // Set max speed correctly if not ducking
                            currentAccel = accelerationW; // Set acceleration correct if not ducking
                        }
                        if (xv < -currentMaxSpeed) {
                            // If speed exceeds current max, decelerate gradually
                            xv += currentDecel; // Decelerate smoothly (increase velocity toward zero)
                            if (xv > -currentMaxSpeed) {
                                xv = -currentMaxSpeed; // Clamp to current max speed
                            }
                        } else {
                            xv -= currentAccel; // Accelerate at current max speed
                            if (xv < -currentMaxSpeed) {
                                xv = -currentMaxSpeed; // Clamp to current max speed
                            }
                        }
                    }

                    // Set direction for 'turning' sprite
                    if (xv <= turningSpriteV || (isAirborne || !isCollidingGround)) {
                        facingLeft = true; // Set direction to left
                        // Set turning state
                        turningLeft = false;
                        turningRight = false;
                    } else {
                        // Set turning state
                        turningLeft = true;
                        turningRight = false;
                    }
                }
            } else { // If ducking
                if (xv < 0) {
                    xv += (currentDecel * 2); // Decelerate (slide) to 0
                }
            }
        } else { // If colliding left
            if (xv < 0) { // If moving left
                xv = Math.max(xv, 0); // Stop any negative velocity
                subPixelX = 0; // Reset subPixel counter
            }
        }

        // Apply movement for the right direction
        if (!isCollidingRight) {
            if (!isDucking || (isAirborne || !isCollidingGround)) { // If not ducking or in the air
                if (keyH.dPressed) {
                    // Sprinting logic
                    if (keyH.sprintPressed && !isDucking) {
                        xv += accelerationS; // Move right (sprinting)
                        // Clamp speed to max speed in the positive direction (right)
                        if (xv > (maxSpeedS)) {
                            xv = (maxSpeedS);
                        }
                    } else {
                        // Walking logic
                        if (!isDucking) {
                            currentMaxSpeed = maxSpeedW; // Set max speed correctly if not ducking
                            currentAccel = accelerationW; // Set acceleration correct if not ducking
                        }
                        if (xv > currentMaxSpeed) {
                            // If speed exceeds current max, decelerate gradually
                            xv -= currentDecel; // Decelerate smoothly (increase velocity toward zero)
                            if (xv < currentMaxSpeed) {
                                xv = currentMaxSpeed; // Clamp to current max speed
                            }
                        } else {
                            xv += currentAccel; // Accelerate at current max speed
                            if (xv > currentMaxSpeed) {
                                xv = currentMaxSpeed; // Clamp to current max speed
                            }
                        }
                    }

                    // Set direction for 'turning' sprite
                    if (xv >= -turningSpriteV || (isAirborne || !isCollidingGround)) {
                        facingLeft = false; // Set direction to right
                        // Set turning state
                        turningLeft = false;
                        turningRight = false;
                    } else {
                        // Set turning state
                        turningRight = true;
                        turningLeft = false;
                    }
                }
            } else { // If ducking
                if (xv > 0) {
                    xv -= (currentDecel); // Decelerate (slide) to 0
                }
            }
        } else { // If colliding right
            if (xv > 0) { // If moving right
                xv = Math.min(xv, 0); // Stop any positive velocity if colliding right
                subPixelX = 0; // Reset subPixel counter
            }
        }

        applyDeceleration(keyH); // Apply deceleration


        // Update the player's position using sub-pixels
        subPixelX += xv;
        if (subPixelX >= subPixels) { // if exceeding sub-pixel count in the positive (right) direction
            x += (subPixelX / subPixels); // move proper amount of pixels (right)
            // reset counter to within bounds
            subPixelX = (int) (subPixelX - (subPixels * (Math.floor(subPixelX / subPixels))));
        }
        else if (subPixelX <= -subPixels) { // if exceeding sub-pixel count in the negative (left) direction
            x -= -(subPixelX / subPixels); // move proper amount of pixels (left)
            // reset counter to within bounds
            subPixelX = (int) (subPixelX - (subPixels * (Math.floor(subPixelX / subPixels))));
        }
    }

    // Helper function to apply deceleration based on the current velocity direction
    private void applyDeceleration(KeyHandler keyH) {
        if (xv < 0 && !keyH.aPressed) { // If moving left
            xv += currentDecel; // Slow down to the right
            if (xv > 0) xv = 0; // Stop if we decelerate past zero
        }
        if (xv > 0 && !keyH.dPressed) { // If moving right
            xv -= currentDecel; // Slow down to the left
            if (xv < 0) xv = 0; // Stop if we decelerate past zero
        }
    }

    private void handleDucking(KeyHandler keyH) {
        isDucking = keyH.sPressed;
        // Set acceleration and max speed if ducking
        if (isDucking) {
            currentAccel = accelerationD;
            currentMaxSpeed = maxSpeedD;
        }
    }

    private void handleGravity(KeyHandler keyH) {
        if (!isCollidingGround) { // If player is not on the ground
            double gravity;

            if (keyH.jumpPressed) {
                // Apply inverse exponential gravity when holding jump
                gravity = jumpHoldGravity * Math.exp(-gravityDecayFactor * Math.abs(yv));
            } else {
                // Apply inverse exponential gravity when not holding jump
                gravity = jumpReleaseGravity * Math.exp(-gravityDecayFactor * Math.abs(yv));
            }

            yv -= gravity; // Apply gravity to the vertical velocity
        } else {
            isAirborne = false; // Reset airborne state when grounded
        }

        // Update the player's position using sub-pixels
        subPixelY -= yv;
        if (subPixelY >= subPixels) {
            y += (subPixelY / subPixels);
            subPixelY = (int) (subPixelY - (subPixels * (Math.floor(subPixelY / subPixels))));
        } else if (subPixelY <= -subPixels) {
            y -= -(subPixelY / subPixels);
            subPixelY = (int) (subPixelY - (subPixels * (Math.floor(subPixelY / subPixels))));
        }
    }

    private void handleJumping(KeyHandler keyH) {
        if (isAirborne || !isCollidingGround) {
            handleGravity(keyH);
            if (isCollidingCeiling) {
                if (yv > 0) { // If still jumping or moving upwards
                    yv = 0; // Stop upward velocity
                }
            }
            currentDecel = decelerationAir; // Set deceleration to air deceleration
        } else { // If grounded
            currentDecel = deceleration; // Set deceleration to normal
            if (keyH.jumpPressed && canJump) { // Allow jump only if canJump is true
                yv = initialJumpVelocity + Math.abs(xv * xvInfluence); // Apply jump force
                isAirborne = true; // Mark as airborne
                canJump = false; // Disable jumping until the key is released
                // SoundManager.playSound("jump"); // Play jump sound
            } else { // If grounded and not initiating a jump
                if (!keyH.jumpPressed) { // Key must be released to allow jumping again
                    canJump = true; // Re-enable jumping after key release
                }

                // Snap the player's Y position to the nearest tile boundary
                y = Math.floorDiv(y, tileSize) * tileSize;
                subPixelY = 0; // Reset sub-pixel counter
                yv = 0; // Stop vertical velocity
            }
        }
    }

    private void handleVerticalCollision(TileManager tileM) {
        // Define a threshold for detecting proximity to the next tile
        final int proximityThresholdX = 8; // horizontal
        final int proximityThresholdYup = 4; // vertical (ceiling)

        // Calculate the tile coordinates for the player's top and bottom edges
        int playerTileYTop = Math.floorDiv(y - proximityThresholdYup, tileSize); // Top edge
        int playerTileYBottom = Math.floorDiv(y + height, tileSize); // Bottom edge

        // Calculate the tile coordinates for the player's left and right edges
        int playerTileXLeft = Math.floorDiv(x + proximityThresholdX, tileSize);
        int playerTileXRight = Math.floorDiv(x + width - proximityThresholdX, tileSize);

        // Reset collision flags
        isCollidingGround = false;
        isCollidingCeiling = false;

        // Check for ceiling collision (top edge of the player)
        if (tileM.isTileSolid(playerTileXLeft, playerTileYTop) || // Left edge
                tileM.isTileSolid(playerTileXRight, playerTileYTop)) { // Right edge
            isCollidingCeiling = true;
        }

        // Check for ground collision (bottom edge of the player)
        if (tileM.isTileSolid(playerTileXLeft, playerTileYBottom) || // Left edge
                tileM.isTileSolid(playerTileXRight, playerTileYBottom)) { // Right edge
            isCollidingGround = true;
        }
    }

    private void handleHorizontalCollision(TileManager tileM) {
        // Define a threshold for collision proximity
        final int proximityThreshold = 4;

        // Correctly calculate the left and right edge tiles
        int playerTileX1 = (x + proximityThreshold) / tileSize;
        if ((x + proximityThreshold) % tileSize < 0) {
            playerTileX1--; // Adjust for negative coordinates
        }

        int playerTileX2 = (x + width - proximityThreshold) / tileSize;
        if ((x + width - proximityThreshold) % tileSize < 0) {
            playerTileX2--; // Adjust for negative coordinates
        }

        // Calculate the tile coordinate for the player's vertical midpoint
        int playerTileYMid = Math.floorDiv(y + (height / 2), tileSize);

        // Reset collision flags before checking
        isCollidingLeft = false;
        isCollidingRight = false;

        // Check the tile directly to the left of the player
        if (tileM.isTileSolid(playerTileX1, playerTileYMid)) {
            isCollidingLeft = true;
        }

        // Check the tile directly to the right of the player
        if (tileM.isTileSolid(playerTileX2, playerTileYMid)) {
            isCollidingRight = true;
        }
    }

    private void updateAnimation() {
        if (isAirborne || !isCollidingGround) { // Jumping animation
            int row = facingLeft ? 1 : 0; // Row 0 = right, Row 1 = left
            currentFrame = getFrame(2, row); // Column 3 for jumping
        } else if (xv != 0) { // Moving animation
            if (turningLeft || turningRight) { // Turning animation
                int row = turningLeft ? 0 : 1; // Opposite direction for turning
                currentFrame = getFrame(3, row); // Column 4 for turning
            } else { // Walking animation
                animationTimer++;
                int adjustedSpeed = (int) (animationSpeed / Math.abs(xv * 0.001) - (animationSpeed / 2)); // Speed-based animation
                if (animationTimer >= adjustedSpeed) {
                    animationFrame = (animationFrame + 1) % 2; // Alternate between frames 0 and 1
                    animationTimer = 0; // Reset timer
                }
                int row = facingLeft ? 1 : 0;
                currentFrame = getFrame(animationFrame, row);
            }
        } else { // Idle animation
            int row = facingLeft ? 1 : 0;
            currentFrame = getFrame(0, row); // Stationary frame
        }
        if (isDucking) { // Ducking overrides most other animations, so it is separate
            int row = facingLeft ? 1 : 0;
            currentFrame = getFrame(4, row); // Column 5 for ducking
        }
    }

    private boolean previousLeftClick = false;

    private void handleMouse(MouseHandler mouseH) {
        // Check if the left mouse button was just pressed (transition from false to true)
        if (mouseH.leftClick && !previousLeftClick) {
            // Teleport the player to mouse X and Y position
            x = mouseH.mouseX;
            y = mouseH.mouseY;

            // Play teleport sound
            SoundManager.playSound("teleport");
        }

        // Update the previous state of left click
        previousLeftClick = mouseH.leftClick;
    }

    private void handleDigging(TileManager tileManager, KeyHandler keyH, ParticleManager particleManager, Camera camera) {
        // Calculate the tile the player is attempting to dig
        int digX = Math.floorDiv(x + width / 2, TileManager.TILE_SIZE);
        int digY = Math.floorDiv(y + height / 2, TileManager.TILE_SIZE);

        // Determine the dig direction
        if (keyH.upPressed) {
            digY -= 1; // Tile above
        } else if (keyH.downPressed) {
            digY += 1; // Tile below
        } else if (keyH.leftPressed) {
            digX -= 1; // Tile to the left
        } else if (keyH.rightPressed) {
            digX += 1; // Tile to the right
        } else {
            return; // No input for digging
        }

        // Check if the tile exists and is solid
        if (tileManager.isTileSolid(digX, digY)) {
            // Remove the tile
            tileManager.removeTile(digX, digY);

            // Spawn particles at the dug tile's world coordinates
            int worldX = digX * TileManager.TILE_SIZE;
            int worldY = digY * TileManager.TILE_SIZE;
            particleManager.spawnTileParticles(worldX, worldY, camera);

            // Play digging sound
            SoundManager.playSound("dig");
        }
    }

    public void draw(Graphics2D g2, KeyHandler keyH, Camera camera) {
        int screenX = x - camera.getX(); // Adjust X based on the camera
        int screenY = y - camera.getY(); // Adjust Y based on the camera
        // Draw the current frame of the sprite
        if (currentFrame != null) {
            g2.drawImage(currentFrame, screenX, screenY, width, height, null);
        } else {
            // Fallback: Draw a red rectangle if the sprite fails to load
            g2.setColor(Color.red);
            g2.fillRect(screenX, screenY, width, height);
        }
    }
}