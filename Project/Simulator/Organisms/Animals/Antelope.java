package Simulator.Organisms.Animals;

import Simulator.Organisms.Animals.Animal;
import Simulator.Organisms.Organism;
import Utils.RNG;
import Simulator.World;
import Simulator.Tile;
import java.util.List;

public class Antelope extends Animal {

    public Antelope(World world) {
        super(4, 4, world, Type.ANTELOPE);
    }

    @Override
    public Animal construct() {
        return new Antelope(world);
    }

    @Override
    public void action() {
        super.action();
        if (tile.getOrganismCount() > 1)
            if (tile.getOrganism().isAlive())
                return;
        List<Tile> neighbours = tile.getNeighbours();
        neighbours.remove(oldTile);
        if (neighbours.isEmpty())
            return;
        Tile newTile = neighbours.get(RNG.getInstance().roll(0, neighbours.size() - 1));
        move(newTile);
    }

    @Override
    public void collision(Organism other) {
        if (other instanceof Antelope)
            super.collision(other);
        else if (RNG.getInstance().roll(0, 100) < 50) {
            Tile newTile = tile.getRandomFreeNeighbour();
            if (newTile == null) {
                super.collision(other);
                return;
            }
            move(newTile);
            world.addLog(getSymbol() + " escaped from " + other.getSymbol() + "!");
        } else
            super.collision(other);
    }

    @Override
    public boolean collisionReaction(Organism other) {
        if (other instanceof Antelope)
            return false;
        if (RNG.getInstance().roll(0, 100) < 50) {
            Tile newTile = tile.getRandomFreeNeighbour();
            if (newTile == null)
                return false;
            move(newTile);
            return true;
        }
        return false;
    }
}