package online.alibabji.afkloot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKLoot extends JavaPlugin implements Listener, CommandExecutor {
    private Map<UUID, ArmorStand> armorStandMap = new HashMap<>();
    private boolean pluginEnabled = true;

    @Override
    public void onEnable() {
        // Register the event listener
        getLogger().info("\u001B[32mAFKLoot Plugin Activated\u001B[0m");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("afklooton").setExecutor(this);
        getCommand("afklootoff").setExecutor(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!pluginEnabled) return;

        Player player = event.getPlayer();
        Location location = player.getLocation();

        // Create an armor stand at the player's location
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        // Customize the armor stand's appearance and behavior
        armorStand.setVisible(true);  // Make it visible
        armorStand.setGravity(false);  // Disable gravity
        armorStand.setMarker(true);    // Make it a marker (no collision)

        // Set the armor stand's pose to lying down
        armorStand.setHeadPose(armorStand.getHeadPose().setX(Math.PI / 2));

        // Set the armor stand's equipment
        armorStand.getEquipment().setHelmet(getPlayerSkull(player));
        armorStand.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        armorStand.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        armorStand.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        // Set the custom name to identify the player
        armorStand.setCustomName(player.getName());
        armorStand.setCustomNameVisible(true);

        armorStandMap.put(player.getUniqueId(), armorStand);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ArmorStand armorStand = armorStandMap.remove(player.getUniqueId());
        if (armorStand != null) {
            armorStand.remove();
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            Player player = event.getPlayer();

            if (armorStandMap.containsValue(armorStand)) {
                Player targetPlayer = Bukkit.getPlayerExact(armorStand.getCustomName());
                if (targetPlayer != null) {
                    player.openInventory(targetPlayer.getInventory());
                }
            }
        }
    }


    private ItemStack getPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("\u001B[31mAFK Looting Plugin deactivated\u001B[0m");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("afklooton")) {
            pluginEnabled = true;
            sender.sendMessage("AFKLoot plugin enabled.");
            return true;
        } else if (command.getName().equalsIgnoreCase("afklootoff")) {
            pluginEnabled = false;
            // Remove all active armor stands
            for (ArmorStand armorStand : armorStandMap.values()) {
                armorStand.remove();
            }
            armorStandMap.clear();
            sender.sendMessage("AFKLoot plugin disabled.");
            return true;
        }
        return false;
    }
}

