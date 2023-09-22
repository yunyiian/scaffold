package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.MouseEvent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int SIDEBAR = 120;
    public static final int TOPBAR = 40;
    public static final int BOARD_WIDTH = 20;

    public static int WIDTH = CELLSIZE*BOARD_WIDTH+SIDEBAR;
    public static int HEIGHT = BOARD_WIDTH*CELLSIZE+TOPBAR;

    public static final int FPS = 60;

    public String configPath;

    public Random random = new Random();
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.
     // The board instance.
    private Board board;
    private TopBar topBar;
    private Sidebar sidebar;
    int initialMana;
    int initialManaCap;
    int manaGainedPerSecond;
    Monster monster;
    private List<Wave> waves = new ArrayList<>();  // List to manage waves
    private int currentWaveIndex = 0;  // Track the current wave
    private float waveTimer = 0;


    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player, enemies and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        JSONObject config = loadJSONObject(configPath);
        initialMana = config.getInt("initial_mana");
        initialManaCap = config.getInt("initial_mana_cap");
        manaGainedPerSecond = config.getInt("initial_mana_gained_per_second");
        topBar = new TopBar(WIDTH, TOPBAR, initialMana, initialManaCap);
        sidebar = new Sidebar(SIDEBAR, HEIGHT);

        // Load images during setup
		// Eg:
        // loadImage("src/main/resources/WizardTD/tower0.png");
        // loadImage("src/main/resources/WizardTD/tower1.png");
        // loadImage("src/main/resources/WizardTD/tower2.png");

        board = new Board(); // initialize the board
        board.loadLayout(config.getString("layout"), this);  
        monster = new Monster(board, this);

        JSONArray wavesConfig = config.getJSONArray("waves");
        for (int i = 0; i < wavesConfig.size(); i++) {
            JSONObject waveConfig = wavesConfig.getJSONObject(i);
            int duration = waveConfig.getInt("duration");
            float preWavePause = waveConfig.getFloat("pre_wave_pause");
            JSONArray monstersConfig = waveConfig.getJSONArray("monsters");
            
            for (int j = 0; j < monstersConfig.size(); j++) {
                JSONObject monsterConfig = monstersConfig.getJSONObject(j);
                Wave wave = new Wave(duration, preWavePause, monsterConfig, board, this);
                waves.add(wave);
            }
        }        
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(){
        
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){

    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {

    }
    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(255); // Clear the background.
    
        // Render the board
        board.render(this);
    
        // Handle waves
        if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(currentWaveIndex);
            
            if (waveTimer < currentWave.getPreWavePause()) {
                // We're in the pause phase of the wave, don't update monsters
            } else {
                currentWave.update();
                currentWave.render(this);
            }
            
            waveTimer += 1.0 / FPS;  // Increment timer by frame duration
    
            // Check if the entire wave duration (pause + active) is over
            if (waveTimer > (currentWave.getPreWavePause() + currentWave.getDuration())) {
                waveTimer = 0;  // Reset timer
                currentWaveIndex++;  // Move to the next wave
            }
        }
    
        // Render the sidebar
        sidebar.render(this);
    
        // Render the top bar
        topBar.render(this);
    
        topBar.updateMana(manaGainedPerSecond / (float)FPS); 
    
        board.renderWizardHouse(this);
    }


    public static void main(String[] args) {
        PApplet.main("WizardTD.App");
    }

    /**
     * Source: https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
     * @param pimg The image to be rotated
     * @param angle between 0 and 360 degrees
     * @return the new rotated image
     */
    public PImage rotateImageByDegrees(PImage pimg, double angle) {
        BufferedImage img = (BufferedImage) pimg.getNative();
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        PImage result = this.createImage(newWidth, newHeight, ARGB);
        //BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage rotated = (BufferedImage) result.getNative();
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                result.set(i, j, rotated.getRGB(i, j));
            }
        }

        return result;
    }
}
