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
        x = playerX - (screenWidth / 2); // Center the camera horizontally
        y = playerY - (screenHeight / 2); // Center the camera vertically
    }

    // Getters for the camera's position
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}