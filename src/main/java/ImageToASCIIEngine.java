
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The ImageToASCII class provides functionality to convert an image to ASCII
 * art. It supports loading an image from a specified path, validating the image
 * and its resolutions, converting the image to ASCII art, and displaying the
 * ASCII art in a window or writing it to a file.
 *
 * Usage example: ImageToASCII converter = new
 * ImageToASCII("path/to/image.jpg");
 * converter.convertToASCIIInFile("output.txt");
 * converter.convertToASCIIInWindow();
 *
 * Note: The image dimensions should not exceed 1920x1080.
 *
 * @author Caden Finley
 */
public class ImageToASCIIEngine {

    private BufferedImage image = null;
    private int width = 0;
    private int height = 0;
    private String imagePath = "";
    private File outputPath = null;

    /**
     * Constructs an ImageToASCII object with the specified image path.
     *
     * @param pathToImage the path to the image file
     */
    public ImageToASCIIEngine(String pathToImage) {
        image = null;
        this.imagePath = pathToImage;
        try {
            loadImage();
        } catch (IOException e) {
            System.out.println("Error loading image!");
            System.out.println("Error: " + e);
        }
    }

    /**
     * Loads the image from the specified path.
     *
     * @throws IOException if an error occurs during reading
     */
    private void loadImage() throws IOException {
        File inputImage = new File(imagePath);
        this.image = ImageIO.read(inputImage);
        this.height = image.getHeight();
        this.width = image.getWidth();
    }

    /**
     * Validates the resolutions of the loaded image.
     *
     * @return true if the resolutions are valid, false otherwise
     */
    private boolean validateResolutions() {
        if (image.getHeight() == 0 || image.getWidth() == 0) {
            System.out.println("Image dimensions are 0");
            return false;
        }
        if (image.getHeight() > 1080 || image.getWidth() > 1920) {
            System.out.println("Image dimensions are too large");
            return false;
        }
        return true;
    }

    /**
     * Validates if the image is loaded.
     *
     * @return true if the image is loaded, false otherwise
     */
    private boolean validateImage() {
        if (image == null) {
            System.out.println("Image not loaded");
            return false;
        }
        return true;
    }

    /**
     * Validates if the output path is set.
     *
     * @return true if the output path is set, false otherwise
     */
    private boolean validateOutputPath() {
        if (outputPath == null) {
            System.out.println("Output path not set");
            return false;
        }
        return true;
    }

    /**
     * Converts a pixel to its corresponding ASCII character.
     *
     * @param pixel the pixel value
     * @return the corresponding ASCII character
     */
    private char pixelToASCII(int pixel) {
        final char[] asciiChars = {'@', '#', 'S', '%', '?', '*', '+', ';', ':', ',', '.'};
        Color color = new Color(pixel, true);
        int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        int index = (gray * (asciiChars.length - 1)) / 255;
        return asciiChars[index];
    }

    /**
     * Unused method
     *
     * Converts a pixel to its corresponding colored ASCII character.
     *
     * @param pixel the pixel value
     * @return the corresponding colored ASCII character
     */
    private String pixelToColoredASCII(int pixel) {
        final char[] asciiChars = {'@', '#', 'S', '%', '?', '*', '+', ';', ':', ',', '.'};
        Color color = new Color(pixel, true);
        int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        int index = (gray * (asciiChars.length - 1)) / 255;
        char asciiChar = asciiChars[index];

        // ANSI color code
        String ansiColor = String.format("\u001B[38;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
        return ansiColor + asciiChar + "\u001B[0m"; // Reset color after character
    }

    /**
     * Converts the image to ASCII art and writes it to a file.
     *
     * @param nameOfOutputFile the name of the output file
     */
    public void convertToASCIIInFile(String nameOfOutputFile) {
        outputPath = new File(nameOfOutputFile);
        if (!validateImage() || !validateOutputPath() || !validateResolutions()) {
            return;
        }
        try (FileWriter writer = new FileWriter(this.outputPath)) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    char ascii = pixelToASCII(pixel);
                    writer.write(ascii);
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to file");
            System.out.println("Error: " + e);
        }
    }

    /**
     * Converts the image to ASCII art and displays it in a window.
     */
    public void convertToASCIIInWindow() {
        if (!validateImage() || !validateResolutions()) {
            return;
        }
        StringBuilder asciiArt = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                char ascii = pixelToASCII(pixel);
                asciiArt.append(ascii);
            }
            asciiArt.append("\n");
        }

        // Display ASCII art in a JTextArea with a specific font
        JTextArea textArea = new JTextArea(asciiArt.toString());
        textArea.setFont(new Font("COURIER", Font.PLAIN, 2));
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        JFrame frame = new JFrame("ASCII Art");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    /**
     * Gets the output file path.
     *
     * @return the output file path
     */
    public File getOutputPath() {
        return outputPath;
    }

    /**
     * Gets the width of the image.
     *
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the image.
     *
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the loaded image.
     *
     * @return the loaded image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets the path of the image.
     *
     * @return the path of the image
     */
    public String getImagePath() {
        return imagePath;
    }
}
