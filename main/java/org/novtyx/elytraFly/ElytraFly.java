package org.novtyx.elytraFly;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;


public final class ElytraFly extends JavaPlugin implements Listener{

    private String notifyMessage;
    private boolean checkOp;
    private boolean checkCreative;
    private int itemDurability;
    private int periodDurability;
    public void onEnable() {
        getLogger().info("Плагин успешно запущен!");
        getServer().getPluginManager().registerEvents(this, (Plugin)this);
        saveDefaultConfig();
        reloadConfigValues();
        new BukkitRunnable(){
            @Override
            public void run(){
                checkAllPlayers();
            }
        }.runTaskTimer(this, 0L, 20 * periodDurability);
    }

    private void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()){
            ItemStack elytra = player.getInventory().getChestplate();
            if (elytra != null && elytra.getType() == Material.ELYTRA){
                if (player.isOp() && !checkOp) return;
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR && !checkCreative)
                    return;
                if (player.isFlying()){
                    ItemMeta itemMeta = elytra.getItemMeta();
                    if (itemMeta instanceof Damageable){
                        Damageable damageable = (Damageable) itemMeta;
                        int currentDurability = damageable.getDamage();
                        int newDamage = currentDurability + itemDurability;
                        short maxDurability = elytra.getType().getMaxDurability(); // Получаем макс. прочность

                        if (newDamage >= maxDurability) { // Проверяем, не сломались ли элитры
                            player.getInventory().setChestplate(null); // Ломаем элитры
                        } else {
                            damageable.setDamage(newDamage); // Устанавливаем новый урон
                            elytra.setItemMeta(itemMeta); // Устанавливаем ItemMeta обратно в элитры
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !checkOp) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        if (player.getAllowFlight())
            if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() != Material.ELYTRA) {
                player.setAllowFlight(false);
                player.spigot().sendMessage(TextComponent.fromLegacyText(
                        formatHexColors(notifyMessage)
                ));
            }
    }

    private void reloadConfigValues() {
        reloadConfig();
        FileConfiguration config = getConfig();
        notifyMessage = config.getString("notify-message",
                "&fТы не можешь летать без элитр!");
        checkOp = config.getBoolean("checkOp", false);
        checkCreative = config.getBoolean("checkCreative", false);
        itemDurability = config.getInt("itemDurability", 1);
        periodDurability = config.getInt("periodDurability", 3);
    }

    private String formatHexColors(String notifyMessage) {
        return ChatColor.translateAlternateColorCodes('&', notifyMessage);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
