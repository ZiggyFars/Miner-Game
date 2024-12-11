package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {

    public boolean leftClick;
    public int mouseX, mouseY;

    @Override
    public void mouseClicked(MouseEvent e) {
        // Click is press AND release
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Capture mouse position on left-click
            mouseX = e.getX();
            mouseY = e.getY();

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