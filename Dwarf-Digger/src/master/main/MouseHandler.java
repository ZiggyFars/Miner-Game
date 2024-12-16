package master.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {

    public boolean leftClick;
    public int mouseX, mouseY;
    private Camera camera; // Reference to the Camera

    // Constructor to initialize the MouseHandler with the Camera
    public MouseHandler(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Click is press AND release
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Capture mouse position on left-click
            mouseX = e.getX() + camera.getX();
            mouseY = e.getY() + camera.getY();
            leftClick = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClick = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Handle mouse entering the window
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Handle mouse exiting the window
    }
}