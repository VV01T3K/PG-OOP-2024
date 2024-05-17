package Simulator.Organisms.Animals;

import Simulator.Tile;
import Simulator.Tile.Direction;
import Simulator.Organisms.Organism;
import Simulator.World;
import Simulator.GlobalSettings;

public abstract class Animal extends Organism {
    protected Tile oldTile = null;

    public void move(Tile newTile) {
        if (newTile == null)
            return;
        oldTile = this.tile;
        this.tile = newTile;
        this.tile.placeOrganism(this);
        oldTile.removeOrganism(this);
    }

    protected void move(Direction direction) {
        move(this.tile.getNeighbour(direction));
    }

    public Animal(int power, int initiative, World world, Type type) {
        super(type, power, initiative, world);
    }

    @Override
    public void action() {
        move(this.tile.getRandomNeighbour());
    }

    @Override
    public void collision(Organism other) {
        if (other.collisionReaction(this))
            return;
        if (this.getClass() == other.getClass()) {
            this.undoMove();
            if (!GlobalSettings.AI_REPRODUCE)
                return;
            other.skipTurn();
            if (getBreedCooldown() > 0)
                return;
            Tile newTile = other.getTile().getRandomFreeNeighbour();
            if (newTile == null)
                return;
            Animal newAnimal = construct();
            newAnimal.skipTurn();
            this.setBreedCooldown(5);
            other.setBreedCooldown(5);
            newAnimal.setBreedCooldown(10);
            world.addOrganism(newAnimal, newTile);

            world.addLog(this.getSymbol() + " and " + other.getSymbol() +
                    " bred a new " + newAnimal.getSymbol() + "!");

        } else if (getPower() > other.getPower()) {
            other.die();
            world.addLog(this.getSymbol() + " killed " + other.getSymbol() + "!");
        } else {
            this.die();
            world.addLog(this.getSymbol() + " was killed by " +
                    other.getSymbol() + "!");
        }
    }

    @Override
    public boolean collisionReaction(Organism other) {
        return false;
    }

    public void setBreedCooldown(int cooldown) {
        this.reproductionCooldown = cooldown;
    }

    public int getBreedCooldown() {
        return this.reproductionCooldown;
    }

    public void undoMove() {
        if (oldTile == null)
            return;
        oldTile.placeOrganism(this);
        tile.removeOrganism(this);
        tile = oldTile;
        oldTile = null;
    }

    public abstract Animal construct();
}