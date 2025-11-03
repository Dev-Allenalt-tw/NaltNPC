package dev.allenalt.naltnpc;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class NPCData {
    
    private final String id;
    private final EntityType type;
    private String name;
    private Entity entity;
    private Location location;
    private ArmorStand hologram;
    private boolean lookAtPlayers;
    private List<NPCAction> actions;

    public NPCData(String id, EntityType type, String name, Entity entity, Location location) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.entity = entity;
        this.location = location;
        this.hologram = null;
        this.lookAtPlayers = false;
        this.actions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public EntityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (entity != null) {
            entity.setCustomName(name);
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArmorStand getHologram() {
        return hologram;
    }

    public void setHologram(ArmorStand hologram) {
        this.hologram = hologram;
    }

    public boolean isLookAtPlayers() {
        return lookAtPlayers;
    }

    public void setLookAtPlayers(boolean lookAtPlayers) {
        this.lookAtPlayers = lookAtPlayers;
    }

    public void addAction(String executorType, String command) {
        actions.add(new NPCAction(executorType, command));
    }

    public void clearActions() {
        actions.clear();
    }

    public List<NPCAction> getActions() {
        return new ArrayList<>(actions);
    }

    public int getActionCount() {
        return actions.size();
    }

    public static class NPCAction {
        private final String executorType;
        private final String command;

        public NPCAction(String executorType, String command) {
            this.executorType = executorType;
            this.command = command;
        }

        public String getExecutorType() {
            return executorType;
        }

        public String getCommand() {
            return command;
        }
    }
}
