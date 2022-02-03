package com.loohp.interactivechat.objectholders;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.ICPlayerJoinEvent;
import com.loohp.interactivechat.api.events.ICPlayerQuitEvent;
import com.loohp.interactivechat.api.events.OfflineICPlayerCreationEvent;
import com.loohp.interactivechat.api.events.OfflineICPlayerUpdateEvent;
import com.loohp.interactivechat.utils.ItemNBTUtils;
import net.craftersland.data.bridge.PD;
import net.craftersland.data.bridge.objects.DatabaseEnderchestData;
import net.craftersland.data.bridge.objects.DatabaseExperienceData;
import net.craftersland.data.bridge.objects.DatabaseInventoryData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ICPlayerFactory {

    private static final Object LOCK = new Object();
    private static final Set<UUID> REMOTE_UUID = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<UUID, ICPlayer> ICPLAYERS = new ConcurrentHashMap<>();
    private static final Map<UUID, ICPlayer> LOGGING_IN = new ConcurrentHashMap<>();
    private static final Map<UUID, WeakReference<OfflineICPlayer>> REFERENCED_OFFLINE_PLAYERS = new ConcurrentHashMap<>();

    static {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onJoin(PlayerLoginEvent event) {
                synchronized (LOCK) {
                    Player player = event.getPlayer();
                    if (!ICPLAYERS.containsKey(player.getUniqueId())) {
                        ICPlayer icplayer = new ICPlayer(player);
                        ICPLAYERS.put(icplayer.getUniqueId(), icplayer);
                        LOGGING_IN.put(icplayer.getUniqueId(), icplayer);
                    }
                }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoinConfirm(PlayerLoginEvent event) {
                if (!event.getResult().equals(Result.ALLOWED)) {
                    onLeave(new PlayerQuitEvent(event.getPlayer(), null));
                } else {
                    UUID uuid = event.getPlayer().getUniqueId();
                    LOGGING_IN.remove(uuid);
                    Bukkit.getPluginManager().callEvent(new ICPlayerJoinEvent(getICPlayer(uuid), false));
                }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onLeave(PlayerQuitEvent event) {
                synchronized (LOCK) {
                    UUID uuid = event.getPlayer().getUniqueId();
                    if (!REMOTE_UUID.contains(uuid)) {
                        ICPlayer icplayer = ICPLAYERS.remove(uuid);
                        if (icplayer != null && LOGGING_IN.remove(uuid) == null) {
                            Bukkit.getPluginManager().callEvent(new ICPlayerQuitEvent(icplayer, false));
                        }
                    }
                }
            }
        }, InteractiveChat.plugin);

        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> REFERENCED_OFFLINE_PLAYERS.values().removeIf(each -> each.get() == null), 12000, 12000);
    }

    public static RemotePlayerCreateResult createOrUpdateRemoteICPlayer(String server, String name, UUID uuid, boolean rightHanded, int selectedSlot, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
        synchronized (LOCK) {
            ICPlayer icplayer = getICPlayer(uuid);
            boolean newlyCreated;
            if (icplayer == null) {
                icplayer = new ICPlayer(server, name, uuid, rightHanded, selectedSlot, experienceLevel, equipment, inventory, enderchest);
                ICPLAYERS.put(uuid, icplayer);
                newlyCreated = true;
                Bukkit.getPluginManager().callEvent(new ICPlayerJoinEvent(icplayer, true));
            } else {
                icplayer.setRemoteServer(server);
                icplayer.setRemoteName(name);
                icplayer.setRemoteRightHanded(rightHanded);
                icplayer.setRemoteSelectedSlot(selectedSlot);
                icplayer.setRemoteExperienceLevel(experienceLevel);
                icplayer.setRemoteEquipment(equipment);
                icplayer.setRemoteInventory(inventory);
                icplayer.setRemoteEnderChest(enderchest);
                newlyCreated = false;
            }
            REMOTE_UUID.add(uuid);
            return new RemotePlayerCreateResult(icplayer, newlyCreated);
        }
    }

    public static RemotePlayerRemoveResult removeRemoteICPlayer(UUID uuid) {
        synchronized (LOCK) {
            if (!REMOTE_UUID.contains(uuid)) {
                return null;
            }
            ICPlayer icplayer = getICPlayer(uuid);
            if (icplayer == null) {
                return null;
            }
            REMOTE_UUID.remove(uuid);
            boolean keptDueToLocallyOnline = true;
            if (!icplayer.isLocal()) {
                ICPLAYERS.remove(uuid);
                Bukkit.getPluginManager().callEvent(new ICPlayerQuitEvent(icplayer, true));
                keptDueToLocallyOnline = false;
            }
            return new RemotePlayerRemoveResult(icplayer, keptDueToLocallyOnline);
        }
    }

    public static Set<UUID> getRemoteUUIDs() {
        return new HashSet<>(REMOTE_UUID);
    }

    public static Collection<ICPlayer> getOnlineICPlayers() {
        return new ArrayList<>(ICPLAYERS.values());
    }

    public static Set<UUID> getOnlineUUIDs() {
        return new LinkedHashSet<>(ICPLAYERS.keySet());
    }

    public static ICPlayer getICPlayer(Player player) {
        ICPlayer icplayer = ICPLAYERS.get(player.getUniqueId());
        if (icplayer != null) {
            return icplayer;
        }
        return new ICPlayer(player);
    }

    public static ICPlayer getICPlayer(UUID uuid) {
        ICPlayer icplayer = ICPLAYERS.get(uuid);
        if (icplayer != null) {
            return icplayer;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return new ICPlayer(player);
        }
        return null;
    }

    public static OfflineICPlayer getOfflineICPlayer(UUID uuid) {
        ICPlayer icplayer = getICPlayer(uuid);
        if (icplayer != null) {
            return icplayer;
        }
        File dat = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/playerdata", uuid.toString() + ".dat");
        if (!dat.exists()) {
            OfflineICPlayer offlineICPlayer = getReferenced(uuid);
            if (offlineICPlayer == null) {
                offlineICPlayer = new OfflineICPlayer(uuid);
                OfflineICPlayerCreationEvent event = new OfflineICPlayerCreationEvent(offlineICPlayer);
                Bukkit.getPluginManager().callEvent(event);
                REFERENCED_OFFLINE_PLAYERS.put(uuid, new WeakReference<>(offlineICPlayer));
            } else {
                OfflineICPlayerUpdateEvent event = new OfflineICPlayerUpdateEvent(offlineICPlayer);
                Bukkit.getPluginManager().callEvent(event);
            }
            return offlineICPlayer;
        }
        boolean mysqlPDBInventorySync = false;
        boolean mysqlPDBArmorSync = false;
        boolean mysqlPDBEnderChestSync = false;
        boolean mysqlPDBExpSync = false;
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        Player dummyPlayer = DummyPlayer.newInstance(playerName, uuid);
        if (InteractiveChat.mysqlPDBHook) {
            mysqlPDBInventorySync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareInventory");
            mysqlPDBArmorSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareArmor");
            if (!PD.instance.getInventoryStorageHandler().hasAccount(playerName)) {
                mysqlPDBInventorySync = false;
                mysqlPDBArmorSync = false;
            }
            mysqlPDBEnderChestSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareEnderchest");
            if (!PD.instance.getEnderchestStorageHandler().hasAccount(playerName)) {
                mysqlPDBEnderChestSync = false;
            }
            mysqlPDBExpSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareExperience");
            if (!PD.instance.getExperienceStorageHandler().hasAccount(playerName)) {
                mysqlPDBExpSync = false;
            }
        }
        try {
            NamedTag nbtData = NBTUtil.read(dat);
            CompoundTag rootTag = (CompoundTag) nbtData.getTag();
            int selectedSlot = rootTag.getInt("SelectedItemSlot");
            boolean rightHanded = !rootTag.containsKey("LeftHanded") || !rootTag.getBoolean("LeftHanded");
            int xpLevel = rootTag.getInt("XpLevel");
            ICPlayerEquipment equipment = new ICPlayerEquipment();
            Inventory inventory = Bukkit.createInventory(null, 45);
            Inventory enderchest = Bukkit.createInventory(null, 27);
            for (CompoundTag entry : rootTag.getListTag("Inventory").asTypedList(CompoundTag.class)) {
                int slot = entry.getByte("Slot");
                entry.remove("Slot");
                ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
                if (slot == 100) {
                    equipment.setBoots(item);
                    slot = 36;
                } else if (slot == 101) {
                    equipment.setLeggings(item);
                    slot = 37;
                } else if (slot == 102) {
                    equipment.setChestplate(item);
                    slot = 38;
                } else if (slot == 103) {
                    equipment.setHelmet(item);
                    slot = 39;
                } else if (slot == -106) {
                    slot = 40;
                }
                inventory.setItem(slot, item);
            }
            for (CompoundTag entry : rootTag.getListTag("EnderItems").asTypedList(CompoundTag.class)) {
                int slot = entry.getByte("Slot");
                entry.remove("Slot");
                ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
                enderchest.setItem(slot, item);
            }
            if (mysqlPDBInventorySync || mysqlPDBArmorSync) {
                DatabaseInventoryData invData = PD.instance.getInventoryStorageHandler().getData(dummyPlayer);
                if (mysqlPDBInventorySync) {
                    ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(invData.getRawInventory());
                    for (int i = 0; i < items.length && i < inventory.getSize(); i++) {
                        inventory.setItem(i, items[i]);
                    }
                    selectedSlot = invData.getHotBarSlot();
                }
                if (mysqlPDBArmorSync) {
                    ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(invData.getRawArmor());
                    for (int i = 0; i < items.length && i < 4; i++) {
                        inventory.setItem(i + 36, items[i]);
                    }
                }
            }
            if (mysqlPDBEnderChestSync) {
                DatabaseEnderchestData enderData = PD.instance.getEnderchestStorageHandler().getData(dummyPlayer);
                ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(enderData.getRawEnderchest());
                for (int i = 0; i < items.length && i < enderchest.getSize(); i++) {
                    enderchest.setItem(i, items[i]);
                }
            }
            if (mysqlPDBExpSync) {
                DatabaseExperienceData expData = PD.instance.getExperienceStorageHandler().getData(dummyPlayer);
                if (expData.getLevel() != null) {
                    xpLevel = expData.getLevel();
                }
            }
            OfflineICPlayer offlineICPlayer = getReferenced(uuid);
            if (offlineICPlayer == null) {
                offlineICPlayer = new OfflineICPlayer(uuid, playerName, selectedSlot, rightHanded, xpLevel, equipment, inventory, enderchest);
                OfflineICPlayerCreationEvent event = new OfflineICPlayerCreationEvent(offlineICPlayer);
                Bukkit.getPluginManager().callEvent(event);
                REFERENCED_OFFLINE_PLAYERS.put(uuid, new WeakReference<>(offlineICPlayer));
            } else {
                offlineICPlayer.setName(playerName);
                offlineICPlayer.setSelectedSlot(selectedSlot);
                offlineICPlayer.setRightHanded(rightHanded);
                offlineICPlayer.setExperienceLevel(xpLevel);
                offlineICPlayer.setEquipment(equipment);
                offlineICPlayer.setInventory(inventory);
                offlineICPlayer.setEnderchest(enderchest);
                OfflineICPlayerUpdateEvent event = new OfflineICPlayerUpdateEvent(offlineICPlayer);
                Bukkit.getPluginManager().callEvent(event);
            }
            return offlineICPlayer;
        } catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static OfflineICPlayer getReferenced(UUID uuid) {
        WeakReference<OfflineICPlayer> ref = REFERENCED_OFFLINE_PLAYERS.get(uuid);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    public static class RemotePlayerCreateResult {

        private ICPlayer player;
        private boolean isNewlyCreated;

        public RemotePlayerCreateResult(ICPlayer player, boolean isNewlyCreated) {
            this.player = player;
            this.isNewlyCreated = isNewlyCreated;
        }

        public ICPlayer getPlayer() {
            return player;
        }

        public boolean isNewlyCreated() {
            return isNewlyCreated;
        }

    }

    public static class RemotePlayerRemoveResult {

        private ICPlayer player;
        private boolean keptDueToLocallyOnline;

        public RemotePlayerRemoveResult(ICPlayer player, boolean keptDueToLocallyOnline) {
            this.player = player;
            this.keptDueToLocallyOnline = keptDueToLocallyOnline;
        }

        public ICPlayer getPlayer() {
            return player;
        }

        public boolean isKeptDueToLocallyOnline() {
            return keptDueToLocallyOnline;
        }

    }

}
