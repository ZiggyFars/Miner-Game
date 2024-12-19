package master.sound;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static Map<String, Sound> soundMap = new HashMap<>();

    // Load a sound and store it in the map
    public static void loadSound(String name, String filePath) {
        soundMap.put(name, new Sound(filePath));
    }

    // Play a specific sound
    public static void playSound(String name) {
        Sound sound = soundMap.get(name);
        if (sound != null) {
            sound.play();
        } else {
            System.out.println("Sound not found: " + name);
        }
    }

    // Stop a specific sound
    public static void stopSound(String name) {
        Sound sound = soundMap.get(name);
        if (sound != null) {
            sound.stop();
        }
    }

    // Set the volume of a specific sound
    public static void setVolume(String name, float volume) {
        Sound sound = soundMap.get(name);
        if (sound != null) {
            sound.setVolume(volume);
        } else {
            System.out.println("Sound not found: " + name);
        }
    }

    // Set the volume for all sounds
    public static void setAllVolumes(float volume) {
        for (Sound sound : soundMap.values()) {
            sound.setVolume(volume);
        }
    }
}