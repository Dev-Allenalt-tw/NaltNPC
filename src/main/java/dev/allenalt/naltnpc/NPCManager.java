package dev.allenalt.naltnpc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class NPCManager {

    private final NaltNPC plugin;
    private final Map<String, NPCData> npcs;
    private final Map<UUID, String> selectedNPCs;

    public NPCManager(NaltNPC plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();
        this.selectedNPCs = new HashMap<>();
        
        // Start look at players task
        startLookAtPlayersTask();
    }

    public void createNPC(String id, EntityType type, String name, Location location) {
        // PLAYER type NPCs cannot be spawned - show error
        if (type == EntityType.PLAYER) {
            plugin.getLogger().warning("Cannot create NPC with PLAYER type. PLAYER entities cannot be spawned.");
            return;
        }
        
        Entity entity = location.getWorld().spawnEntity(location, type);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setPersistent(true);
        
        // Prevent NPC from moving
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setAI(false);
            living.setCollidable(false);
        }
        
        NPCData data = new NPCData(id, type, name, entity, location);
        data.setLookAtPlayers(false); // Default to false
        npcs.put(id, data);
    }

    public void removeNPC(String id) {
        NPCData data = npcs.get(id);
        if (data != null) {
            if (data.getEntity() != null) {
                data.getEntity().remove();
            }
            if (data.getHologram() != null) {
                data.getHologram().remove();
            }
            npcs.remove(id);
        }
    }

    public void moveNPC(String id, Location location) {
        NPCData data = npcs.get(id);
        if (data != null) {
            data.setLocation(location);
            if (data.getEntity() != null) {
                data.getEntity().teleport(location);
            }
            if (data.getHologram() != null) {
                Location hologramLoc = location.clone().add(0, 2.5, 0);
                data.getHologram().teleport(hologramLoc);
            }
        }
    }

    public void addHologramLine(String id, String text) {
        NPCData data = npcs.get(id);
        if (data != null) {
            if (data.getHologram() == null) {
                createHologram(id, text);
            } else {
                String currentText = data.getHologram().getCustomName();
                data.getHologram().setCustomName(currentText + "\n" + text);
            }
        }
    }

    public void setHologram(String id, String text) {
        setHologram(id, text, false);
    }

    public void setHologram(String id, String text, boolean textShadow) {
        removeHologram(id);
        createHologram(id, text, textShadow);
    }

    private void createHologram(String id, String text) {
        createHologram(id, text, false);
    }

    private void createHologram(String id, String text, boolean textShadow) {
        NPCData data = npcs.get(id);
        if (data != null) {
            Location hologramLoc = data.getLocation().clone().add(0, 2.5, 0);
            ArmorStand hologram = (ArmorStand) hologramLoc.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
            hologram.setCustomName(text);
            hologram.setCustomNameVisible(true);
            hologram.setGravity(false);
            hologram.setInvulnerable(true);
            hologram.setVisible(false);
            hologram.setMarker(true);
            
            // Set text shadow (this is cosmetic, actual implementation may vary by client)
            if (!textShadow) {
                // Note: Text shadow is client-side, this is a placeholder
                // In practice, you might need to use display entities in 1.19.4+
            }
            
            data.setHologram(hologram);
        }
    }

    public void removeHologram(String id) {
        NPCData data = npcs.get(id);
        if (data != null && data.getHologram() != null) {
            data.getHologram().remove();
            data.setHologram(null);
        }
    }

    public void setLookAtPlayers(String id, boolean lookAtPlayers) {
        NPCData data = npcs.get(id);
        if (data != null) {
            data.setLookAtPlayers(lookAtPlayers);
        }
    }

    private void startLookAtPlayersTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPCData data : npcs.values()) {
                    if (data.isLookAtPlayers() && data.getEntity() != null) {
                        Location npcLoc = data.getEntity().getLocation();
                        Player nearest = null;
                        double nearestDistance = 10.0; // Look at players within 10 blocks

                        for (Player player : npcLoc.getWorld().getPlayers()) {
                            double distance = player.getLocation().distance(npcLoc);
                            if (distance < nearestDistance) {
                                nearest = player;
                                nearestDistance = distance;
                            }
                        }

                        if (nearest != null) {
                            Location playerLoc = nearest.getEyeLocation();
                            Location lookLoc = npcLoc.clone();
                            lookLoc.setDirection(playerLoc.toVector().subtract(npcLoc.toVector()));
                            data.getEntity().teleport(lookLoc);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Run every 5 ticks (0.25 seconds)
    }

    public boolean npcExists(String id) {
        return npcs.containsKey(id);
    }

    public Location getNPCLocation(String id) {
        NPCData data = npcs.get(id);
        return data != null ? data.getLocation() : null;
    }

    public Map<String, NPCData> getAllNPCs() {
        return new HashMap<>(npcs);
    }

    public void setSelectedNPC(Player player, String id) {
        selectedNPCs.put(player.getUniqueId(), id);
    }

    public String getSelectedNPC(Player player) {
        return selectedNPCs.get(player.getUniqueId());
    }

    public void addAction(String id, String executorType, String command) {
        NPCData data = npcs.get(id);
        if (data != null) {
            data.addAction(executorType, command);
        }
    }

    public void clearActions(String id) {
        NPCData data = npcs.get(id);
        if (data != null) {
            data.clearActions();
        }
    }

    public int getActionCount(String id) {
        NPCData data = npcs.get(id);
        return data != null ? data.getActionCount() : 0;
    }

    public boolean setSkin(String id, String skinName) {
        NPCData data = npcs.get(id);
        if (data == null || data.getEntity() == null) {
            return false;
        }

        Entity entity = data.getEntity();
        
        // Store skin name in NPCData for future reference
        data.setSkinName(skinName);
        
        // Basic skin support - this is limited without NMS
        // For full player skin support, you'd need to use NMS or a library like Citizens
        
        // For now, we can set custom name to indicate the skin
        String currentName = entity.getCustomName();
        if (currentName != null) {
            entity.setCustomName(currentName + " §7[" + skinName + "]");
        }
        
        plugin.getLogger().info("Skin '" + skinName + "' applied to NPC '" + id + "' (limited support)");
        
        return true;
    }
}
