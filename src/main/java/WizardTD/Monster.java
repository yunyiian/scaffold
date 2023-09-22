package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.List;
import java.util.ArrayList;
import WizardTD.subtiles.*;


public class Monster {

    private PImage image;
    private int x, y;  // Position of the monster
    private Board board;
    private String direction = "";  // Can be "up", "down", "left", "right"
    List<String> pathToWizard;
    int pathIndex = 0;
    private PathFinder pathFinder = new PathFinder();

    public Monster(Board board, PApplet app) {
        this.board = board;
        this.image = app.loadImage("src/main/resources/WizardTD/gremlin.png");
        List<int[]> spawnPoints = getSpawnPoints();
        int[] chosenSpawn = spawnPoints.get((int) (Math.random() * spawnPoints.size()));
        this.x = chosenSpawn[0];
        this.y = chosenSpawn[1];
    }

    private List<int[]> getSpawnPoints() {
        List<int[]> spawnPoints = new ArrayList<>();
        Tile[][] tiles = board.getTiles();
    
        // Check the top row
        for (int j = 0; j < tiles[0].length; j++) {
            if (tiles[0][j] instanceof PathTile) {
                spawnPoints.add(new int[]{j, -1});  // Spawn above the board
            }
        }
    
        // Check the bottom row
        for (int j = 0; j < tiles[tiles.length - 1].length; j++) {
            if (tiles[tiles.length - 1][j] instanceof PathTile) {
                spawnPoints.add(new int[]{j, tiles.length});  // Spawn below the board
            }
        }
    
        // Check the leftmost column
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i][0] instanceof PathTile) {
                spawnPoints.add(new int[]{-1, i});  // Spawn to the left of the board
            }
        }
    
        // Check the rightmost column
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i][tiles[i].length - 1] instanceof PathTile) {
                spawnPoints.add(new int[]{tiles[i].length, i});  // Spawn to the right of the board
            }
        }
        return spawnPoints;
    }
    
    public void move() {
        Tile[][] tiles = board.getTiles();
    
        if (x < 0 || x >= tiles[0].length || y < 0 || y >= tiles.length) {
            moveOutside(tiles);
        } else {
            moveInside(tiles);
        }
    }
    
    private void moveOutside(Tile[][] tiles) {
        if (y == -1) direction = "down";
        else if (y == tiles.length) direction = "up";
        else if (x == -1) direction = "right";
        else if (x == tiles[0].length) direction = "left";
        
        moveInDirection();
    }
    
    private void moveInDirection() {
        switch (direction) {
            case "up":
                y--;
                break;
            case "down":
                y++;
                break;
            case "left":
                x--;
                break;
            case "right":
                x++;
                break;
        }
    }
    
     /**
     * moveInside method
     *          
     * This method implements the A* pathfinding algorithm,
     *
     * For details on the A* algorithm, refer to the credits in the PathFinder class.
     */
    private void moveInside(Tile[][] tiles) {
        PathFinder.Node start = pathFinder.new Node(x, y);
        
        // Find the goal (the 'W' tile)
        int goalX = -1, goalY = -1;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] instanceof WizardHouseTile) {
                    goalX = j;
                    goalY = i;
                    break;
                }
            }
            if (goalX != -1) {
                break;
            }
        }
        
        PathFinder.Node goal = pathFinder.new Node(goalX, goalY);
        
        List<PathFinder.Node> path = pathFinder.findPath(start, goal, tiles);
        
        if (path.size() > 1) {
            PathFinder.Node nextStep = path.get(1);
            this.x = nextStep.x;
            this.y = nextStep.y;
        }
    }       
    

    public void render(PApplet app) {
        if (x != -1 && y != -1) { // Only render if monster hasn't disappeared
            int drawX = x * App.CELLSIZE + App.CELLSIZE / 2 - image.width / 2;
            int drawY = y * App.CELLSIZE + App.CELLSIZE / 2 - image.height / 2 + App.TOPBAR;
            app.image(image, drawX, drawY);
        }
    }
}