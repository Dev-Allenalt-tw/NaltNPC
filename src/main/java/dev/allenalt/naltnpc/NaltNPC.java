package dev.allenalt.naltnpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NaltNPC extends JavaPlugin {

    private NPCManager npcManager;
    private File npcsFile;
    private FileConfiguration npcsConfig;

    @Override
    public void onEnable() {
        getLogger().info("NaltNPC v1.0.0 by Dev_Allenalt_tw has been enabled!");
        
        // Initialize NPC manager
        npcManager = new NPCManager(this);
        
        // Setup data files
        setupDataFiles();
        
        // Load NPCs from config
        loadNPCs();
        
        // Register tab completer
        getCommand("npc").setTabCompleter(new NPCTabCompleter(npcManager));
        
        getLogger().info("NaltNPC plugin loaded successfully!");
    }

    @Override
    public void onDisable() {
        // Save all NPCs before shutdown
        saveNPCs();
        getLogger().info("NaltNPC has been disabled!");
    }

    private void setupDataFiles() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        npcsFile = new File(getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            try {
                npcsFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create npcs.yml file!");
                e.printStackTrace();
            }
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
    }

    private void loadNPCs() {
        // Load NPCs from config file
        if (npcsConfig.contains("npcs")) {
            for (String id : npcsConfig.getConfigurationSection("npcs").getKeys(false)) {
                String path = "npcs." + id;
                // NPC data will be loaded here when server is ready
            }
        }
    }

    private void saveNPCs() {
        try {
            npcsConfig.save(npcsFile);
        } catch (IOException e) {
            getLogger().severe("Could not save npcs.yml file!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("npc")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "skin":
                return handleSkin(player, args);
            case "hologram":
                return handleHologram(player, args);
            case "look":
                return handleLook(player, args);
            case "list":
                return handleList(player);
            case "teleport":
            case "tp":
                return handleTeleport(player, args);
            case "movehere":
                return handleMoveHere(player, args);
            case "move":
                return handleMove(player, args);
            case "remove":
            case "delete":
                return handleRemove(player, args);
            case "action":
                return handleAction(player, args);
            default:
                sendHelp(player);
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /npc create <id> <type> <name>");
            return true;
        }

        String id = args[1];
        String type = args[2].toUpperCase();
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            nameBuilder.append(args[i]).append(" ");
        }
        String name = ChatColor.translateAlternateColorCodes('&', nameBuilder.toString().trim());

        // Validate entity type
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid entity type: " + type);
            return true;
        }

        if (npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "An NPC with ID '" + id + "' already exists!");
            return true;
        }

        Location loc = player.getLocation();
        npcManager.createNPC(id, entityType, name, loc);
        
        // Save to config
        String path = "npcs." + id;
        npcsConfig.set(path + ".type", type);
        npcsConfig.set(path + ".name", name);
        npcsConfig.set(path + ".world", loc.getWorld().getName());
        npcsConfig.set(path + ".x", loc.getX());
        npcsConfig.set(path + ".y", loc.getY());
        npcsConfig.set(path + ".z", loc.getZ());
        npcsConfig.set(path + ".yaw", loc.getYaw());
        npcsConfig.set(path + ".pitch", loc.getPitch());
        saveNPCs();

        player.sendMessage(ChatColor.GREEN + "NPC '" + id + "' created successfully!");
        return true;
    }

    private boolean handleSkin(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /npc skin <id> <skinName>");
            return true;
        }

        String id = args[1];
        String skinName = args[2];

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        boolean success = npcManager.setSkin(id, skinName);
        
        if (success) {
            // Save to config
            npcsConfig.set("npcs." + id + ".skin", skinName);
            saveNPCs();
            player.sendMessage(ChatColor.GREEN + "Skin set to '" + skinName + "' for NPC '" + id + "'!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set skin. This NPC type may not support skins.");
            player.sendMessage(ChatColor.YELLOW + "Note: Only certain mob types support skin changes.");
        }

        return true;
    }

    private boolean handleHologram(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /npc hologram <id> <add/set/remove> <text>");
            return true;
        }

        String id = args[1];
        String action = args[2].toLowerCase();

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            textBuilder.append(args[i]).append(" ");
        }
        String text = ChatColor.translateAlternateColorCodes('&', textBuilder.toString().trim());

        switch (action) {
            case "add":
                npcManager.addHologramLine(id, text);
                player.sendMessage(ChatColor.GREEN + "Hologram line added!");
                break;
            case "set":
                boolean textShadow = false;
                // Check if text_shadow parameter exists
                if (args.length > 4 && args[3].equalsIgnoreCase("text_shadow")) {
                    if (args.length > 5) {
                        textShadow = Boolean.parseBoolean(args[4]);
                        // Rebuild text without text_shadow parameters
                        textBuilder = new StringBuilder();
                        for (int i = 5; i < args.length; i++) {
                            textBuilder.append(args[i]).append(" ");
                        }
                        text = ChatColor.translateAlternateColorCodes('&', textBuilder.toString().trim());
                    }
                }
                npcManager.setHologram(id, text, textShadow);
                player.sendMessage(ChatColor.GREEN + "Hologram set with text_shadow: " + textShadow);
                break;
            case "remove":
                npcManager.removeHologram(id);
                player.sendMessage(ChatColor.GREEN + "Hologram removed!");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid action! Use add, set, or remove.");
                return true;
        }

        return true;
    }

    private boolean handleLook(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /npc look <id> <true/false>");
            return true;
        }

        String id = args[1];
        boolean lookAtPlayers = Boolean.parseBoolean(args[2]);

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        npcManager.setLookAtPlayers(id, lookAtPlayers);
        npcsConfig.set("npcs." + id + ".lookAtPlayers", lookAtPlayers);
        saveNPCs();

        player.sendMessage(ChatColor.GREEN + "NPC '" + id + "' look at players: " + lookAtPlayers);
        return true;
    }

    private boolean handleList(Player player) {
        Map<String, NPCData> npcs = npcManager.getAllNPCs();
        
        if (npcs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No NPCs created yet!");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== NPC List ===");
        for (Map.Entry<String, NPCData> entry : npcs.entrySet()) {
            NPCData data = entry.getValue();
            player.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + entry.getKey() + 
                             ChatColor.YELLOW + " | Type: " + ChatColor.WHITE + data.getType().name() +
                             ChatColor.YELLOW + " | Name: " + data.getName());
        }
        return true;
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /npc teleport <id>");
            return true;
        }

        String id = args[1];
        Location loc = npcManager.getNPCLocation(id);

        if (loc == null) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Teleported to NPC '" + id + "'!");
        return true;
    }

    private boolean handleMoveHere(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /npc movehere <id>");
            return true;
        }

        String id = args[1];

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        Location loc = player.getLocation();
        npcManager.moveNPC(id, loc);

        // Update config
        String path = "npcs." + id;
        npcsConfig.set(path + ".world", loc.getWorld().getName());
        npcsConfig.set(path + ".x", loc.getX());
        npcsConfig.set(path + ".y", loc.getY());
        npcsConfig.set(path + ".z", loc.getZ());
        npcsConfig.set(path + ".yaw", loc.getYaw());
        npcsConfig.set(path + ".pitch", loc.getPitch());
        saveNPCs();

        player.sendMessage(ChatColor.GREEN + "NPC '" + id + "' moved to your location!");
        return true;
    }

    private boolean handleMove(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Usage: /npc move <id> <x> <y> <z>");
            return true;
        }

        String id = args[1];

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        try {
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);

            Location currentLoc = npcManager.getNPCLocation(id);
            Location newLoc = new Location(currentLoc.getWorld(), x, y, z, currentLoc.getYaw(), currentLoc.getPitch());
            npcManager.moveNPC(id, newLoc);

            // Update config
            String path = "npcs." + id;
            npcsConfig.set(path + ".x", x);
            npcsConfig.set(path + ".y", y);
            npcsConfig.set(path + ".z", z);
            saveNPCs();

            player.sendMessage(ChatColor.GREEN + "NPC '" + id + "' moved to coordinates!");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates!");
        }

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /npc remove <id>");
            return true;
        }

        String id = args[1];

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        npcManager.removeNPC(id);
        npcsConfig.set("npcs." + id, null);
        saveNPCs();

        player.sendMessage(ChatColor.GREEN + "NPC '" + id + "' removed!");
        return true;
    }

    private boolean handleAction(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Usage: /npc action <add/set/remove> <id> <CONSOLE/PLAYER/SERVER> <command>");
            return true;
        }

        String action = args[1].toLowerCase();
        String id = args[2];
        String executorType = args[3].toUpperCase();

        if (!npcManager.npcExists(id)) {
            player.sendMessage(ChatColor.RED + "NPC with ID '" + id + "' does not exist!");
            return true;
        }

        if (!executorType.equals("CONSOLE") && !executorType.equals("PLAYER") && !executorType.equals("SERVER")) {
            player.sendMessage(ChatColor.RED + "Invalid executor type! Use CONSOLE, PLAYER, or SERVER.");
            return true;
        }

        StringBuilder commandBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            commandBuilder.append(args[i]).append(" ");
        }
        String command = commandBuilder.toString().trim();

        switch (action) {
            case "add":
                npcManager.addAction(id, executorType, command);
                player.sendMessage(ChatColor.GREEN + "Action added to NPC '" + id + "'!");
                
                // Save to config
                int actionIndex = npcManager.getActionCount(id) - 1;
                String path = "npcs." + id + ".actions." + actionIndex;
                npcsConfig.set(path + ".type", executorType);
                npcsConfig.set(path + ".command", command);
                saveNPCs();
                break;
            case "set":
                npcManager.clearActions(id);
                npcManager.addAction(id, executorType, command);
                player.sendMessage(ChatColor.GREEN + "Action set for NPC '" + id + "'!");
                
                // Save to config
                npcsConfig.set("npcs." + id + ".actions", null);
                npcsConfig.set("npcs." + id + ".actions.0.type", executorType);
                npcsConfig.set("npcs." + id + ".actions.0.command", command);
                saveNPCs();
                break;
            case "remove":
                npcManager.clearActions(id);
                player.sendMessage(ChatColor.GREEN + "All actions removed from NPC '" + id + "'!");
                
                // Save to config
                npcsConfig.set("npcs." + id + ".actions", null);
                saveNPCs();
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid action! Use add, set, or remove.");
                return true;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== NaltNPC Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/npc create <id> <type> <name>" + ChatColor.WHITE + " - Create a new NPC");
        player.sendMessage(ChatColor.YELLOW + "/npc hologram <add/set/remove> <text>" + ChatColor.WHITE + " - Manage hologram");
        player.sendMessage(ChatColor.YELLOW + "/npc look <id> <true/false>" + ChatColor.WHITE + " - Toggle NPC looking at players");
        player.sendMessage(ChatColor.YELLOW + "/npc list" + ChatColor.WHITE + " - List all NPCs");
        player.sendMessage(ChatColor.YELLOW + "/npc teleport <id>" + ChatColor.WHITE + " - Teleport to NPC");
        player.sendMessage(ChatColor.YELLOW + "/npc movehere <id>" + ChatColor.WHITE + " - Move NPC to your location");
        player.sendMessage(ChatColor.YELLOW + "/npc move <id> <x> <y> <z>" + ChatColor.WHITE + " - Move NPC to coordinates");
        player.sendMessage(ChatColor.YELLOW + "/npc remove <id>" + ChatColor.WHITE + " - Remove an NPC");
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }
}
