package master.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Sound {
    private Clip clip;
    private FloatControl volumeControl; // Control for sound volume
    private byte[] audioData; // Cached audio data for creating new clips

    public Sound(String soundFilePath) {
        try {
            // Load the audio file
            URL url = getClass().getResource(soundFilePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            // Cache the audio data for overlapping playback
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);
            audioData = byteArrayOutputStream.toByteArray();

            // Create the initial clip
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData)));

            // Get the volume control
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("Error loading sound file: " + soundFilePath);
        }
    }

    // Play the sound with overlapping support
    public void play() {
        try {
            // Create a new temporary clip for overlapping playback
            Clip tempClip = AudioSystem.getClip();
            tempClip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData)));

            // Set the same volume as the original clip
            FloatControl tempVolumeControl = (FloatControl) tempClip.getControl(FloatControl.Type.MASTER_GAIN);
            tempVolumeControl.setValue(volumeControl.getValue());

            // Play the sound
            tempClip.start();

            // Automatically release resources when playback ends
            tempClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    tempClip.close();
                }
            });
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.out.println("Error playing sound.");
        }
    }

    // Stop the current clip
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // Set the volume (-80.0f is mute, 0.0f is maximum)
    public void setVolume(float volume) {
        if (volumeControl != null) {
            volumeControl.setValue(volume);
        }
    }

    // Check if the sound is playing
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}