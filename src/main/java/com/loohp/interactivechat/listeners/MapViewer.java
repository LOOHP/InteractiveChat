package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapViewer implements Listener {

    public static final Map<Player, ItemStack> MAP_VIEWERS = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    public static void showMap(Player player, ItemStack item) {
        if (!FilledMapUtils.isFilledMap(item)) {
            throw new IllegalArgumentException("ItemStack is not a filled map");
        }

        try {
            int mapId = FilledMapUtils.getMapId(item);
            MapView mapView = FilledMapUtils.getMapView(item);

            PacketContainer packet1;
            if (InteractiveChat.version.isOld()) {
                packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
                packet1.getIntegers().write(0, 0);
                packet1.getIntegers().write(1, player.getInventory().getHeldItemSlot() + 36);
                packet1.getItemModifier().write(0, item);
            } else {
                packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                packet1.getIntegers().write(0, player.getEntityId());
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
                    List<Pair<ItemSlot, ItemStack>> list = new ArrayList<>();
                    list.add(new Pair<ItemSlot, ItemStack>(ItemSlot.MAINHAND, item));
                    packet1.getSlotStackPairLists().write(0, list);
                } else {
                    packet1.getItemSlots().write(0, ItemSlot.MAINHAND);
                    packet1.getItemModifier().write(0, item);
                }
            }

            PacketContainer packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.MAP);
            int mapIconFieldPos = 2;
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
                packet2.getIntegers().write(0, mapId);
                packet2.getBytes().write(0, (byte) 0);
                packet2.getBooleans().write(0, false);
            } else {
                packet2.getIntegers().write(0, mapId);
                packet2.getBytes().write(0, (byte) 0);
                if (!InteractiveChat.version.isOld()) {
                    packet2.getBooleans().write(0, false);
                    mapIconFieldPos++;
                }
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_14)) {
                    packet2.getBooleans().write(1, false);
                    mapIconFieldPos++;
                }
                packet2.getIntegers().write(1, 0);
                packet2.getIntegers().write(2, 0);
                packet2.getIntegers().write(3, 128);
                packet2.getIntegers().write(4, 128);
            }

            MAP_VIEWERS.put(player, item);

            InteractiveChat.protocolManager.sendServerPacket(player, packet1);

            int mapIconFieldPos0 = mapIconFieldPos;
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack itemStack = MAP_VIEWERS.get(player);
                    if (itemStack != null && itemStack.equals(item)) {
                        if (!player.getInventory().containsAtLeast(itemStack, 1)) {
                            try {
                                byte[] colors = FilledMapUtils.getColors(mapView, player);
                                List<MapCursor> mapCursors = FilledMapUtils.getCursors(mapView, player);
                                Iterator<MapCursor> itr = mapCursors.iterator();
                                while (itr.hasNext()) {
                                    MapCursor mapCursor = itr.next();
                                    int type = mapCursor.getRawType();
                                    if (type == 0 || type == 6 || type == 7) {
                                        itr.remove();
                                    }
                                }
                                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
                                    packet2.getModifier().write(3, FilledMapUtils.toNMSMapIconList(mapCursors));
                                    packet2.getModifier().write(4, FilledMapUtils.getNMSWorldMapBClassConstructor().newInstance(0, 0, 128, 128, colors));
                                } else {
                                    List<?> nmsMapIconsList = FilledMapUtils.toNMSMapIconList(mapCursors);
                                    Object nmsMapIconsArray = Array.newInstance(FilledMapUtils.getNMSMapIconClass(), nmsMapIconsList.size());
                                    for (int i = 0; i < nmsMapIconsList.size(); i++) {
                                        Object nmsMapIconObject = nmsMapIconsList.get(i);
                                        Array.set(nmsMapIconsArray, i, nmsMapIconObject);
                                    }

                                    packet2.getByteArrays().write(0, colors);
                                    packet2.getModifier().write(mapIconFieldPos0, nmsMapIconsArray);
                                }
                                InteractiveChat.protocolManager.sendServerPacket(player, packet2);
                            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | FieldAccessException | InstantiationException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(InteractiveChat.plugin, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        int slot = event.getSlot();

        if (removed) {
            if (player.getInventory().equals(event.getClickedInventory()) && slot >= 9) {
                ItemStack item = player.getInventory().getItem(slot);
                Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.getInventory().setItem(slot, item), 1);
            } else {
                event.setCursor(null);
            }
        }

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (event.getNewSlot() == event.getPreviousSlot()) {
            return;
        }

        Player player = event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MAP_VIEWERS.remove(event.getPlayer());
    }

}
