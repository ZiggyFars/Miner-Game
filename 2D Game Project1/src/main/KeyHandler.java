package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean leftPressed, rightPressed; // Left & Right Movement
    public boolean jumpPressed; // Jumping
    public boolean sprintPressed; // Sprinting

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        // Left & Right Movement
        if(code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if(code == KeyEvent.VK_D) {
            rightPressed = true;
        }

        // Jumping
        if(code == KeyEvent.VK_SPACE) {
            jumpPressed = true;
        }

        // Sprinting
        if(code == KeyEvent.VK_CONTROL) {
            sprintPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        // Left & Right Movement
        if(code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if(code == KeyEvent.VK_D) {
            rightPressed = false;
        }

        // Jumping
        if(code == KeyEvent.VK_SPACE) {
            jumpPressed = false;
        }

        // Sprinting
        if(code == KeyEvent.VK_CONTROL) {
            sprintPressed = false;
        }
    }
}
