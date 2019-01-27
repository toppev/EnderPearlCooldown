package com.gmail.thetoppe5.enderpearlcooldown;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    public static final HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();

    private final EnderPearlCooldown plugin;

    public PlayerListener(EnderPearlCooldown plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for (ItemStack it : e.getPlayer().getInventory().getContents()) {
            if (it != null && it.getType() == Material.ENDER_PEARL) {
                ItemMeta iMeta = it.getItemMeta();
                iMeta.setDisplayName(null);
                it.setItemMeta(iMeta);
            }
        }
        if (plugin.isXpBar()) {
            e.getPlayer().setLevel(0);
            e.getPlayer().setExp(0);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onLaunch(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (cooldown.containsKey(p.getUniqueId())) {
                    double l = cooldown.get(p.getUniqueId());
                    double i = plugin.getCooldown() * 1000;
                    double c = System.currentTimeMillis();
                    if (l + i > c) {
                        e.setCancelled(true);
                        double x = (l + i - c) / 1000;
                        p.sendMessage(ChatColor
                                .translateAlternateColorCodes('&', plugin.getConfig().getString("cooldown-message"))
                                .replace("<time>", plugin.getFormat().format(x)));
                        return;
                    }
                }
                cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                new PearlTask(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent e) {
        updateItem(e.getItem().getItemStack(), e.getPlayer());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        if (item != null && item.getType() == Material.ENDER_PEARL) {
            updateItem(item, p);
        }
    }

    private void updateItem(ItemStack item, Player p) {
        if (item.getType() == Material.ENDER_PEARL) {
            if (cooldown.containsKey(p.getUniqueId())) {
                double l = cooldown.get(p.getUniqueId());
                double i = plugin.getCooldown() * 1000;
                double c = System.currentTimeMillis();
                if (l + i > c) {
                    double x = (l + i - c) / 1000;
                    item.setItemMeta(getPearlItemMeta(item, plugin.getFormat().format(x)));
                    return;
                }
            }
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(null);
            item.setItemMeta(im);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item != null && item.getType() == Material.ENDER_PEARL) {
            ItemMeta iMeta = item.getItemMeta();
            iMeta.setDisplayName(null);
            item.setItemMeta(iMeta);
        }
    }

    private ItemMeta getPearlItemMeta(ItemStack item, String l) {
        String s = ChatColor.translateAlternateColorCodes('&', plugin.getPearlName());
        ItemMeta iMeta = item.getItemMeta();
        iMeta.setDisplayName(s.replace("<time>", l));
        return iMeta;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (plugin.isUpdateAvailable() && e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(
                    ChatColor.GRAY + "There is a new update available for " + plugin.getDescription().getName());
            e.getPlayer().sendMessage(ChatColor.GRAY + "Link: https://www.spigotmc.org/resources/43307/updates");
        }
    }

    private class PearlTask extends BukkitRunnable {

        private Player p;
        private int counter;

        public PearlTask(Player p) {
            this.p = p;
            counter = plugin.getInterval();
            if (plugin.isAsyncUpdate()) {
                runTaskTimerAsynchronously(plugin, 0, 1);
            } else {
                runTaskTimer(plugin, 0, 1);
            }
        }

        @Override
        public void run() {
            if (p != null) {
                long left = left(p);
                if (plugin.isXpBar()) {
                    p.setLevel((int) (left / 1000));
                    p.setExp((float) (left / (plugin.getCooldown() * 1000)));
                }
                counter--;
                if (counter == 0) {
                    counter = plugin.getInterval();
                    if (p.getItemInHand().getType() == Material.ENDER_PEARL) {
                        if (left > 0) {
                            double x = left / 1000;
                            String format = plugin.getFormat().format(x);
                            for (ItemStack it : p.getInventory().getContents()) {
                                if (it != null && it.getType() == Material.ENDER_PEARL) {
                                    it.setItemMeta(getPearlItemMeta(it, format));
                                }
                            }
                        } else {
                            cooldown.remove(p.getUniqueId());
                            for (ItemStack it : p.getInventory().getContents()) {
                                if (it != null && it.getType() == Material.ENDER_PEARL) {
                                    ItemMeta iMeta = it.getItemMeta();
                                    iMeta.setDisplayName(null);
                                    it.setItemMeta(iMeta);
                                }
                            }
                            cancel();
                        }
                    }
                }
            } else
                cancel();
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            this.p = null;
        }

        private long left(Player p) {
            if (cooldown.containsKey(p.getUniqueId())) {
                double i = plugin.getCooldown() * 1000;
                double l = cooldown.get(p.getUniqueId());
                long c = System.currentTimeMillis();
                if (l + i > c) {
                    return (long) (l + i - c);
                }
            }
            return 0;
        }
    }
}
