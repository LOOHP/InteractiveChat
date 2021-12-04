package com.loohp.interactivechat.objectholders;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class DummyPlayer implements Player {

	private String name;
	private UUID uuid;

	public DummyPlayer(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public PlayerInventory getInventory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory getEnderChest() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MainHand getMainHand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setWindowProperty(Property prop, int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView getOpenInventory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView openInventory(Inventory inventory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView openWorkbench(Location location, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView openEnchanting(Location location, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void openInventory(InventoryView inventory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView openMerchant(Villager trader, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryView openMerchant(Merchant merchant, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeInventory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack getItemInHand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setItemInHand(ItemStack item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack getItemOnCursor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setItemOnCursor(ItemStack item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasCooldown(Material material) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCooldown(Material material) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCooldown(Material material, int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSleepTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean sleep(Location location, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void wakeup(boolean setSpawnLocation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getBedLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameMode getGameMode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGameMode(GameMode mode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBlocking() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isHandRaised() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack getItemInUse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getExpToLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getAttackCooldown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean discoverRecipe(NamespacedKey recipe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int discoverRecipes(Collection<NamespacedKey> recipes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean undiscoverRecipe(NamespacedKey recipe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<NamespacedKey> getDiscoveredRecipes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getShoulderEntityLeft() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShoulderEntityLeft(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getShoulderEntityRight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShoulderEntityRight(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean dropItem(boolean dropAll) {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getExhaustion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExhaustion(float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getSaturation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSaturation(float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFoodLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFoodLevel(int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSaturatedRegenRate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSaturatedRegenRate(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUnsaturatedRegenRate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUnsaturatedRegenRate(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStarvationRate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStarvationRate(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getEyeHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getEyeHeight(boolean ignorePose) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getEyeLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Block getTargetBlockExact(int maxDistance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RayTraceResult rayTraceBlocks(double maxDistance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemainingAir() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRemainingAir(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaximumAir() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaximumAir(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getArrowCooldown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setArrowCooldown(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getArrowsInBody() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setArrowsInBody(int count) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaximumNoDamageTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaximumNoDamageTicks(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getLastDamage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastDamage(double damage) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNoDamageTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Player getKiller() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPotionEffect(PotionEffect effect) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPotionEffect(PotionEffect effect, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> effects) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PotionEffect getPotionEffect(PotionEffectType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePotionEffect(PotionEffectType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasLineOfSight(Entity other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRemoveWhenFarAway(boolean remove) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityEquipment getEquipment() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCanPickupItems(boolean pickup) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getCanPickupItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeashed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setLeashHolder(Entity holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGliding() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGliding(boolean gliding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSwimming() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSwimming(boolean swimming) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRiptiding() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSleeping() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isClimbing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAI(boolean ai) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasAI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void attack(Entity target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void swingMainHand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void swingOffHand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCollidable(boolean collidable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCollidable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<UUID> getCollidableExemptions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getMemory(MemoryKey<T> memoryKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void setMemory(MemoryKey<T> memoryKey, T memoryValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityCategory getCategory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInvisible(boolean invisible) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInvisible() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AttributeInstance getAttribute(Attribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void damage(double amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void damage(double amount, Entity source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getHealth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHealth(double health) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getAbsorptionAmount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAbsorptionAmount(double amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMaxHealth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxHealth(double health) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetMaxHealth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation(Location loc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVelocity(Vector velocity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector getVelocity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getWidth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BoundingBox getBoundingBox() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInWater() {
		throw new UnsupportedOperationException();
	}

	@Override
	public World getWorld() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRotation(float yaw, float pitch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean teleport(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean teleport(Location location, TeleportCause cause) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean teleport(Entity destination) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean teleport(Entity destination, TeleportCause cause) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Entity> getNearbyEntities(double x, double y, double z) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getEntityId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFireTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxFireTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFireTicks(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVisualFire(boolean fire) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVisualFire() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFreezeTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxFreezeTicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreezeTicks(int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFrozen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDead() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isValid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Server getServer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPersistent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPersistent(boolean persistent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getPassenger() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setPassenger(Entity passenger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Entity> getPassengers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPassenger(Entity passenger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removePassenger(Entity passenger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean eject() {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getFallDistance() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFallDistance(float distance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTicksLived() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTicksLived(int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playEffect(EntityEffect type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInsideVehicle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean leaveVehicle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getVehicle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCustomNameVisible(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCustomNameVisible() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGlowing(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGlowing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInvulnerable(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInvulnerable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSilent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSilent(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasGravity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGravity(boolean gravity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPortalCooldown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPortalCooldown(int cooldown) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getScoreboardTags() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addScoreboardTag(String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeScoreboardTag(String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BlockFace getFacing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pose getPose() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MetadataValue> getMetadata(String metadataKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(String... messages) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(UUID sender, String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(UUID sender, String... messages) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPermissionSet(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPermissionSet(Permission perm) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPermission(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPermission(Permission perm) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void recalculatePermissions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOp(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCustomName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCustomName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PersistentDataContainer getPersistentDataContainer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConversing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void acceptConversationInput(String input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean beginConversation(Conversation conversation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abandonConversation(Conversation conversation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendRawMessage(UUID sender, String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOnline() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBanned() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWhitelisted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWhitelisted(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Player getPlayer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getFirstPlayed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastPlayed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPlayedBefore() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStatistic(Statistic statistic) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> serialize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDisplayName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPlayerListName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerListName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPlayerListHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPlayerListFooter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerListHeader(String header) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerListFooter(String footer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerListHeaderFooter(String header, String footer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCompassTarget(Location loc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getCompassTarget() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InetSocketAddress getAddress() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendRawMessage(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void kickPlayer(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void chat(String msg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean performCommand(String command) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOnGround() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSneaking() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSneaking(boolean sneak) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSprinting() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSprinting(boolean sprinting) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSleepingIgnored(boolean isSleeping) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSleepingIgnored() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getBedSpawnLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBedSpawnLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBedSpawnLocation(Location location, boolean force) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playNote(Location loc, byte instrument, byte note) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playNote(Location loc, Instrument instrument, Note note) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playSound(Location location, Sound sound, float volume, float pitch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playSound(Location location, String sound, float volume, float pitch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopSound(Sound sound) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopSound(String sound) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopSound(Sound sound, SoundCategory category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopSound(String sound, SoundCategory category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playEffect(Location loc, Effect effect, int data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean breakBlock(Block block) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendBlockChange(Location loc, Material material, byte data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendBlockChange(Location loc, BlockData block) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendBlockDamage(Location loc, float progress) {
		throw new UnsupportedOperationException();
	}

	public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMap(MapView map) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateInventory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerTime(long time, boolean relative) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getPlayerTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getPlayerTimeOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPlayerTimeRelative() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetPlayerTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerWeather(WeatherType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public WeatherType getPlayerWeather() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetPlayerWeather() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void giveExp(int amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void giveExpLevels(int amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getExp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExp(float exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLevel(int level) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTotalExperience() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTotalExperience(int exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendExperienceChange(float progress) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendExperienceChange(float progress, int level) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getAllowFlight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAllowFlight(boolean flight) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void hidePlayer(Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void hidePlayer(Plugin plugin, Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void showPlayer(Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void showPlayer(Plugin plugin, Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canSee(Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFlying() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFlying(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFlySpeed(float value) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWalkSpeed(float value) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getFlySpeed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getWalkSpeed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTexturePack(String url) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setResourcePack(String url) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setResourcePack(String url, byte[] hash) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Scoreboard getScoreboard() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isHealthScaled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHealthScaled(boolean scale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHealthScale(double scale) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getHealthScale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getSpectatorTarget() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSpectatorTarget(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendTitle(String title, String subtitle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetTitle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, double extra) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, double extra) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, double extra, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, double extra, T data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AdvancementProgress getAdvancementProgress(Advancement advancement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getClientViewDistance() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCommands() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void openBook(ItemStack book) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Spigot spigot() {
		throw new UnsupportedOperationException();
	}

	public void stopAllSounds() {
		throw new UnsupportedOperationException();
	}

	public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack item) {
		throw new UnsupportedOperationException();
	}

	public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	public void showDemoScreen() {
		throw new UnsupportedOperationException();
	}

	public boolean isAllowingServerListings() {
		return false;
	}

	@Override
	public boolean canSee(Entity arg0) {
		return false;
	}

	@Override
	public void hideEntity(Plugin arg0, Entity arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void showEntity(Plugin arg0, Entity arg1) {
		throw new UnsupportedOperationException();
	}

}
