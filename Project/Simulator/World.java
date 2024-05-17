package Simulator;

import java.util.ArrayList;
import java.util.List;

import Simulator.Organisms.Organism;
import Simulator.Organisms.Animals.*;
import Simulator.Organisms.Plants.*;
import Simulator.Tile;
import Simulator.Tile.Direction;
import Utils.RNG;

public class World {
    private int width;
    private int height;
    private long time = 0;
    private List<Organism> organisms; // will be sorted by initiative and age
    private List<Tile> tiles;
    private List<String> logs = new ArrayList<>();
    private Human human = null;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.organisms = new ArrayList<>();
        this.tiles = new ArrayList<>();
        this.logs = new ArrayList<>();
        createBoard(width, height);
    }

    public World() {
        this(20, 20);
    }

    private void createBoard(int width, int height) {
        // Initialize tiles based on width and height
        for (int i = 0; i < width * height; i++) {
            tiles.add(new Tile(i));
        }
        // Set links between tiles
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles.get(y * width + x);
                if (y > 0) {
                    tile.setLink(Direction.UP, tiles.get((y - 1) * width + x));
                }
                if (x < width - 1) {
                    tile.setLink(Direction.RIGHT, tiles.get(y * width + x + 1));
                }
                if (y < height - 1) {
                    tile.setLink(Direction.DOWN, tiles.get((y + 1) * width + x));
                }
                if (x > 0) {
                    tile.setLink(Direction.LEFT, tiles.get(y * width + x - 1));
                }
            }
        }
    }

    public void setWorld(int width, int height, long time) {
        this.clearTiles();
        this.width = width;
        this.height = height;
        this.time = time;
        createBoard(width, height);
    }

    public long checkTime() {
        return time;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public Tile getTile(int x, int y) {
        return tiles.get(y * width + x);
    }

    public void setTile(int x, int y, Tile tile) {
        tiles.set(y * width + x, tile);
    }

    public Organism getOrganism(int index) {
        return organisms.get(index);
    }

    public void addOrganism(Organism organism, Tile tile) {
        organisms.add(organism);
        organism.setTile(tile);
        tile.placeOrganism(organism);
    }

    public void simulate() {
        time++;
        clearLogs();

        // Sort organisms based on initiative and age
        organisms.sort((a, b) -> {
            if (a.getInitiative() == b.getInitiative()) {
                // Fixed to handle comparison of long values
                return Long.compare(b.getAge(), a.getAge()); // Note the order is reversed for age
            }
            return Integer.compare(b.getInitiative(), a.getInitiative()); // Higher initiative first
        });

        // Iterate through organisms for actions
        for (Organism organism : new ArrayList<>(organisms)) { // To avoid ConcurrentModificationException
            if (organism.isSkipped() || organism.isDead())
                continue;
            if (!GlobalSettings.AI_ACTION && !(organism instanceof Human))
                continue;

            organism.action();

            if (organism.getTile().getOrganismCount() > 1) {
                Organism other = organism.getTile().getOrganism();
                if (organism.equals(other) || other.isDead())
                    continue;
                organism.collision(other);
            }
        }

        // Handle post-round actions and remove dead organisms
        organisms.removeIf(organism -> {
            if (organism.isDead()) {
                organism.getTile().removeOrganism(organism);
                if (organism instanceof Human)
                    human = null;
                return true;
            }
            organism.age();
            organism.unskipTurn();
            return false;
        });
    }

    public int getOrganismCount() {
        return organisms.size();
    }

    public void spreadOrganisms(Organism organism, int count) {
        if (count > 0) {
            final int max = width * height;
            while (true) {
                if (getOrganismCount() == max)
                    break;
                Tile tile = tiles.get(RNG.getInstance().roll(0, tiles.size() - 1));
                if (tile.isFree()) {
                    addOrganism(organism.construct(), tile);
                    if (--count == 0)
                        break;
                }
            }
        }
    }

    public void linkOrganismsWithTiles() {
        for (Organism organism : organisms) {
            getTile(organism.getTile().index).placeOrganism(organism);
        }
    }

    public List<Organism> getOrganisms() {
        return new ArrayList<>(organisms);
    }

    public void setOrganisms(List<Organism> organisms) {
        this.clearOrganisms();
        this.organisms = new ArrayList<>(organisms);
        linkOrganismsWithTiles();
    }

    public void clearOrganisms() {
        // Assuming there's a method to properly remove or clear an organism's data
        for (Organism organism : organisms) {
            organism = null;
        }
        organisms.clear();
        human = null;
    }

    public void clearTiles() {
        for (int i = 0; i < tiles.size(); i++) {
            tiles.set(i, null); // Explicitly setting to null, if necessary
        }
        tiles.clear();
    }

    public void cleanTiles() {
        for (Tile tile : tiles) {
            tile.clear();
        }
    }

    public void resetWorld() {
        clearOrganisms();
        cleanTiles();
        clearLogs();
        time = 0;
    }

    public void populateWorld() {
        resetWorld();

        Human human = new Human(this);
        spreadOrganisms(human, 1);
        setHuman(human); // Assuming setHuman sets the 'human' field in World

        spreadOrganisms(new SosnowskyHogweed(this), 3);
        spreadOrganisms(new Grass(this), 3);
        spreadOrganisms(new Guarana(this), 3);
        spreadOrganisms(new Milkweed(this), 3);
        spreadOrganisms(new WolfBerries(this), 3);

        spreadOrganisms(new Wolf(this), 3);
        spreadOrganisms(new Sheep(this), 3);
        // spreadOrganisms(new CyberSheep(this), 3);
        spreadOrganisms(new Fox(this), 3);
        spreadOrganisms(new Turtle(this), 3);
        spreadOrganisms(new Antelope(this), 3);
    }

    public void addLog(String log) {
        logs.add(log);
    }

    public List<String> getLogs() {
        return logs;
    }

    public void clearLogs() {
        logs.clear();
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public void setHuman(Organism organism) {
        if (organism instanceof Human) {
            this.human = (Human) organism;
        }
    }

    public Human getHuman() {
        return human;
    }

    public boolean hasHuman() {
        return human != null;
    }

    public Human findHuman() {
        for (Organism organism : organisms) {
            if (organism instanceof Human) {
                return (Human) organism;
            }
        }
        return null;
    }

}