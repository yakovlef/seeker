package io.yakovlef;

public class Game {
    private World world;
    private Player player;

    public Game() {
        world = new World(1000, 1000);
        player = new Player(0, 0);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
