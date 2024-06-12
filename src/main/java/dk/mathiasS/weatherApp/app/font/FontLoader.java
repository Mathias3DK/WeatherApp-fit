package dk.mathiasS.weatherApp.app.font;
import dk.mathiasS.weatherApp.app.WeatherApp;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

    public static Font loadFont(String path) {
        try {
            InputStream is = new FileInputStream(path);
            if (is == null) {
                System.err.println("Font file not found: " + path);
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // Load the Klavika font
        Font klavika = loadFont("src/main/resources/assets/font.otf");

        if (klavika != null) {
            // Create a new font with a specific size
            Font klavikaSized = klavika.deriveFont(12f);

            // Example of applying the font to a JLabel
            javax.swing.JLabel label = new javax.swing.JLabel("Hello, Klavika!");
            label.setFont(klavikaSized);

            // Create a JFrame to display the label
            javax.swing.JFrame frame = new javax.swing.JFrame("Custom Font Example");
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.add(label);
            frame.pack();
            frame.setVisible(true);
        } else {
            System.out.println("Failed to load Klavika font.");
        }
    }
}
