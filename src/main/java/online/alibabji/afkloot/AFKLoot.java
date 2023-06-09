package online.alibabji.afkloot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class AFKLoot extends JavaPlugin implements Listener {
    private Map<UUID, ArmorStand> armorStandMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Register the event listener
        System.out.println("AFKLoot Plugin Activated");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
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
        System.out.println("AFK Looting Plugin deactivated");
    }
}
