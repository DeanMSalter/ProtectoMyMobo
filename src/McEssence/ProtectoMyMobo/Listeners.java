package McEssence.ProtectoMyMobo;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class Listeners implements Listener {
    private final Config config;
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ProtectoMyMobo");
    private final NamespacedKey mobOwnerKey = new NamespacedKey(plugin, "mobOwner");
    private final NamespacedKey protectionDateTimeKey = new NamespacedKey(plugin, "protectionDateTime");

    public Listeners(Config configTemp){
        config = configTemp;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onEntityBreed(EntityBreedEvent event){
        Player player = (Player) event.getBreeder();
        if (player.hasPermission("ProtectoMyMobo.protect")) {
            PersistentDataContainer dataContainer = event.getEntity().getPersistentDataContainer();
            dataContainer.set(mobOwnerKey, PersistentDataType.BYTE_ARRAY, Util.getBytesFromUUID(player.getUniqueId()));
            dataContainer.set(protectionDateTimeKey, PersistentDataType.LONG, Instant.now().getEpochSecond());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!IsAllowed(event.getEntity(), player)) {
                event.setCancelled(true);
                player.sendMessage(config.getCanNotDamage());
            }
        }
    }

    private boolean IsAllowed(Entity entity, Player player) {
        try {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            if (!dataContainer.has(mobOwnerKey, PersistentDataType.BYTE_ARRAY)) return true;
            if (!dataContainer.has(protectionDateTimeKey, PersistentDataType.LONG)) return true;

            if (Instant.now().getEpochSecond() - dataContainer.get(protectionDateTimeKey, PersistentDataType.LONG) < config.getProtectionDelay()) return true;

            byte[] playerBytes = Util.getBytesFromUUID(player.getUniqueId());
            byte[] mobOwnerBytes = dataContainer.get(mobOwnerKey, PersistentDataType.BYTE_ARRAY);
            UUID mobOwnerUUID = Util.getUUIDFromBytes(mobOwnerBytes);

            if (!Arrays.equals(playerBytes, mobOwnerBytes)) {
                if (!config.getTrustedPlayers(mobOwnerUUID).contains(String.valueOf(player.getUniqueId()))) {
                    if (!player.hasPermission("ProtectoMyMobo.bypass")) {
                        return false;
                    }
                }
            }
            return true;
        }catch(Exception e){
            Bukkit.getLogger().severe("An error occured when checking if a player can interact with mob.");
            e.printStackTrace();
        }
        return true;
    }
}