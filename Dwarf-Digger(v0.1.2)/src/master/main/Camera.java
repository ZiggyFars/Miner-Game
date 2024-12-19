package master.main;

public class Camera {
    private int x; // Camera's X position
    private int y; // Camera's Y position
    private int screenWidth; // Width of the screen
    private int screenHeight; // Height of the screen

    // Constructor
    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x = 0;
        this.y = 0;
    }

    // Update the camera's position based on the player's position
    public void centerOnPlayer(int playerX, int playerY) {
        x = Math.round(playerX - (screenWidth / 2.0f)); // Center horizontally
        y = Math.round(playerY - (screenHeight / 2.0f)); // Center vertically
        System.out.println("Updated Camera X: " + x + ", Camera Y: " + y);
    }

    // Getters for the camera's position
    public int getX() {
        System.out.println("Retrieved Camera X: " + x);
        return x;
    }

    public int getY() {
        System.out.println("Retrieved Camera Y: " + y);
        return y;
    }
}