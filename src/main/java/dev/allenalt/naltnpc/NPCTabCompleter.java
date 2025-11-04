package dev.allenalt.naltnpc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NPCTabCompleter implements TabCompleter {

    private final NPCManager npcManager;

    public NPCTabCompleter(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            completions.addAll(Arrays.asList(
                "create", "skin", "hologram", "look", "list", "teleport", "tp",
                "movehere", "move", "remove", "delete", "action"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "create":
                    // Suggest ID (player can type their own)
                    completions.add("<id>");
                    break;
                case "skin":
                case "hologram":
                case "look":
                case "teleport":
                case "tp":
                case "movehere":
                case "move":
                case "remove":
                case "delete":
                    // Suggest existing NPC IDs
                    completions.addAll(npcManager.getAllNPCs().keySet());
                    break;
                case "action":
                    // Suggest action types
                    completions.addAll(Arrays.asList("add", "set", "remove"));
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "create":
                    // Suggest all entity types
                    completions.addAll(getValidEntityTypes());
                    break;
                case "skin":
                    // Suggest skin name placeholder
                    completions.add("<skinName>");
                    break;
                case "hologram":
                    // Suggest hologram actions
                    completions.addAll(Arrays.asList("add", "set", "remove"));
                    break;
                case "look":
                    // Suggest true/false
                    completions.addAll(Arrays.asList("true", "false"));
                    break;
                case "move":
                    // Suggest coordinate placeholder
                    completions.add("<x>");
                    break;
                case "action":
                    // Suggest existing NPC IDs for action command
                    completions.addAll(npcManager.getAllNPCs().keySet());
                    break;
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "create":
                    // Suggest name placeholder
                    completions.add("<name>");
                    break;
                case "hologram":
                    String hologramAction = args[2].toLowerCase();
                    if (hologramAction.equals("set")) {
                        completions.add("text_shadow");
                        completions.add("<text>");
                    } else {
                        completions.add("<text>");
                    }
                    break;
                case "move":
                    completions.add("<y>");
                    break;
                case "action":
                    // Suggest executor types
                    completions.addAll(Arrays.asList("CONSOLE", "PLAYER", "SERVER"));
                    break;
            }
        } else if (args.length == 5) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("hologram")) {
                String hologramAction = args[2].toLowerCase();
                if (hologramAction.equals("set") && args[3].equalsIgnoreCase("text_shadow")) {
                    completions.addAll(Arrays.asList("true", "false"));
                }
            } else if (subCommand.equals("move")) {
                completions.add("<z>");
            } else if (subCommand.equals("action")) {
                completions.add("<command>");
            }
        }

        // Filter completions based on what the user has typed
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }

    private List<String> getValidEntityTypes() {
        List<String> types = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            // Filter out some unsuitable types
            if (type.isSpawnable() && type.isAlive() && 
                type != EntityType.PLAYER &&  // Cannot spawn PLAYER type
                type != EntityType.ENDER_DRAGON &&
                type != EntityType.WITHER) {
                types.add(type.name());
            }
        }
        return types;
    }
}
