package master.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean aPressed, dPressed; // Left & Right Movement
    public boolean jumpPressed; // Jumping
    public boolean sprintPressed; // Sprinting
    public boolean sPressed; // Down (ducking)
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    // Map
    public boolean mapPressed = false;
    public boolean mapToggle = false;

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        // Left & Right Movement
        if(code == KeyEvent.VK_A) {
            aPressed = true;
        }
        if(code == KeyEvent.VK_D) {
            dPressed = true;
        }

        // Jumping
        if(code == KeyEvent.VK_SPACE) {
            jumpPressed = true;
        }

        // Sprinting
        if(code == KeyEvent.VK_CONTROL) {
            sprintPressed = true;
        }

        // Ducking
        if(code == KeyEvent.VK_S) {
            sPressed = true;
        }

        // Digging
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
        }

        // Map
        if (code == KeyEvent.VK_M) {
            mapToggle = !mapToggle; // Toggle map state
        } else if (code == KeyEvent.VK_ESCAPE) {
            mapToggle = false; // Close map on Escape
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        // Left & Right Movement
        if(code == KeyEvent.VK_A) {
            aPressed = false;
        }
        if(code == KeyEvent.VK_D) {
            dPressed = false;
        }

        // Jumping
        if(code == KeyEvent.VK_SPACE) {
            jumpPressed = false;
        }

        // Sprinting
        if(code == KeyEvent.VK_CONTROL) {
            sprintPressed = false;
        }

        // Ducking
        if(code == KeyEvent.VK_S) {
            sPressed = false;
        }

        // Digging
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
        }
    }
}
