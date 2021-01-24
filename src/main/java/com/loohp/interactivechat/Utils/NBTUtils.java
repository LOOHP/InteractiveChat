package com.loohp.interactivechat.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Sets/Gets NBT tags from ItemStacks 
 * Supports 1.8-1.16
 * 
 * Github: https://github.com/BananaPuncher714/NBTEditor
 * Spigot: https://www.spigotmc.org/threads/269621/
 * 
 * @version 7.16
 * @author BananaPuncher714
 */
@SuppressWarnings({"deprecation", "unchecked"})
public final class NBTUtils {
	private static final Map< String, Class<?> > classCache;
	private static final Map< String, Method > methodCache;
	private static final Map< Class< ? >, Constructor< ? > > constructorCache;
	private static final Map< Class< ? >, Class< ? > > NBTClasses;
	private static final Map< Class< ? >, Field > NBTTagFieldCache;
	private static Field NBTListData;
	private static Field NBTCompoundMap;
	private static final String VERSION;
	private static final MinecraftVersion LOCAL_VERSION;

	static {
		VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		LOCAL_VERSION = MinecraftVersion.get( VERSION );

		classCache = new HashMap< String, Class<?> >();
		try {
			classCache.put( "NBTBase", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTBase" ) );
			classCache.put( "NBTTagCompound", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagCompound" ) );
			classCache.put( "NBTTagList", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagList" ) );
			classCache.put( "MojangsonParser", Class.forName( "net.minecraft.server." + VERSION + "." + "MojangsonParser" ) );

			classCache.put( "ItemStack", Class.forName( "net.minecraft.server." + VERSION + "." + "ItemStack" ) );
			classCache.put( "CraftItemStack", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".inventory." + "CraftItemStack" ) );
			classCache.put( "CraftMetaSkull", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".inventory." + "CraftMetaSkull" ) );

			classCache.put( "Entity", Class.forName( "net.minecraft.server." + VERSION + "." + "Entity" ) );
			classCache.put( "CraftEntity", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".entity." + "CraftEntity" ) );
			classCache.put( "EntityLiving", Class.forName( "net.minecraft.server." + VERSION + "." + "EntityLiving" ) );

			classCache.put( "CraftWorld", Class.forName( "org.bukkit.craftbukkit." + VERSION + "." + "CraftWorld" ) );
			classCache.put( "CraftBlockState", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".block." + "CraftBlockState" ) );
			classCache.put( "BlockPosition", Class.forName( "net.minecraft.server." + VERSION + "." + "BlockPosition" ) );
			classCache.put( "TileEntity", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntity" ) );
			classCache.put( "World", Class.forName( "net.minecraft.server." + VERSION + "." + "World" ) );
			classCache.put( "IBlockData", Class.forName( "net.minecraft.server." + VERSION + "." + "IBlockData" ) );
			
			classCache.put( "TileEntitySkull", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntitySkull" ) );

			classCache.put( "GameProfile", Class.forName( "com.mojang.authlib.GameProfile" ) );
			classCache.put( "Property", Class.forName( "com.mojang.authlib.properties.Property" ) );
			classCache.put( "PropertyMap", Class.forName( "com.mojang.authlib.properties.PropertyMap" ) );
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
		}

		NBTClasses = new HashMap< Class< ? >, Class< ? > >();
		try {
			NBTClasses.put( Byte.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
			NBTClasses.put( Boolean.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
			NBTClasses.put( String.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagString" ) );
			NBTClasses.put( Double.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagDouble" ) );
			NBTClasses.put( Integer.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagInt" ) );
			NBTClasses.put( Long.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagLong" ) );
			NBTClasses.put( Short.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagShort" ) );
			NBTClasses.put( Float.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagFloat" ) );
			NBTClasses.put( Class.forName( "[B" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByteArray" ) );
			NBTClasses.put( Class.forName( "[I" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagIntArray" ) );
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
		}

		methodCache = new HashMap< String, Method >();
		try {
			methodCache.put( "get", getNMSClass( "NBTTagCompound" ).getMethod( "get", String.class ) );
			methodCache.put( "set", getNMSClass( "NBTTagCompound" ).getMethod( "set", String.class, getNMSClass( "NBTBase" ) ) );
			methodCache.put( "hasKey", getNMSClass( "NBTTagCompound" ).getMethod( "hasKey", String.class ) );
			methodCache.put( "setIndex", getNMSClass( "NBTTagList" ).getMethod( "a", int.class, getNMSClass( "NBTBase" ) ) );
			if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
				methodCache.put( "getTypeId", getNMSClass( "NBTBase" ).getMethod( "getTypeId" ) );
				methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", int.class, getNMSClass( "NBTBase" ) ) );
			} else {
				methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", getNMSClass( "NBTBase" ) ) );
			}
			methodCache.put( "size", getNMSClass( "NBTTagList" ).getMethod( "size" ) );

			if ( LOCAL_VERSION == MinecraftVersion.v1_8 ) {
				methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "a", int.class )  );
			} else {
				methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "remove", int.class )  );
			}
			methodCache.put( "remove", getNMSClass( "NBTTagCompound" ).getMethod( "remove", String.class ) );

			if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_13 ) ) {
				methodCache.put( "getKeys", getNMSClass( "NBTTagCompound" ).getMethod( "getKeys" ) );
			} else {
				methodCache.put( "getKeys", getNMSClass( "NBTTagCompound" ).getMethod( "c" ) );
			}

			methodCache.put( "hasTag", getNMSClass( "ItemStack" ).getMethod( "hasTag" ) );
			methodCache.put( "getTag", getNMSClass( "ItemStack" ).getMethod( "getTag" ) );
			methodCache.put( "setTag", getNMSClass( "ItemStack" ).getMethod( "setTag", getNMSClass( "NBTTagCompound" ) ) );
			methodCache.put( "asNMSCopy", getNMSClass( "CraftItemStack" ).getMethod( "asNMSCopy", ItemStack.class ) );
			methodCache.put( "asBukkitCopy", getNMSClass( "CraftItemStack" ).getMethod( "asBukkitCopy", getNMSClass( "ItemStack" ) ) );

			methodCache.put( "getEntityHandle", getNMSClass( "CraftEntity" ).getMethod( "getHandle" ) );
			if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
				methodCache.put( "getEntityTag", getNMSClass( "Entity" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );
				methodCache.put( "setEntityTag", getNMSClass( "Entity" ).getMethod( "load", getNMSClass( "NBTTagCompound" ) ) );
			} else {
				methodCache.put( "getEntityTag", getNMSClass( "Entity" ).getMethod( "c", getNMSClass( "NBTTagCompound" ) ) );
				methodCache.put( "setEntityTag", getNMSClass( "Entity" ).getMethod( "f", getNMSClass( "NBTTagCompound" ) ) );
			}

			methodCache.put( "save", getNMSClass( "ItemStack" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );

			if ( LOCAL_VERSION.lessThanOrEqualTo( MinecraftVersion.v1_10 ) ) {
				methodCache.put( "createStack", getNMSClass( "ItemStack" ).getMethod( "createStack", getNMSClass( "NBTTagCompound" ) ) );
			} else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_13 ) ) {
				methodCache.put( "createStack", getNMSClass( "ItemStack" ).getMethod( "a", getNMSClass( "NBTTagCompound" ) ) );
			}

			if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
				methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "load", getNMSClass( "IBlockData" ), getNMSClass( "NBTTagCompound" ) ) );
				methodCache.put( "getType", getNMSClass( "World" ).getMethod( "getType", getNMSClass( "BlockPosition" ) ) );
			} else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_12 ) ) {
				methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "load", getNMSClass( "NBTTagCompound" ) ) );
			} else {
				methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "a", getNMSClass( "NBTTagCompound" ) ) );
			}
			methodCache.put( "getTileEntity", getNMSClass( "World" ).getMethod( "getTileEntity", getNMSClass( "BlockPosition" ) ) );
			methodCache.put( "getWorldHandle", getNMSClass( "CraftWorld" ).getMethod( "getHandle" ) );

			methodCache.put( "setGameProfile", getNMSClass( "TileEntitySkull" ).getMethod( "setGameProfile", getNMSClass( "GameProfile" ) ) );
			methodCache.put( "getProperties", getNMSClass( "GameProfile" ).getMethod( "getProperties" ) );
			methodCache.put( "getName", getNMSClass( "Property" ).getMethod( "getName" ) );
			methodCache.put( "getValue", getNMSClass( "Property" ).getMethod( "getValue" ) );
			methodCache.put( "values", getNMSClass( "PropertyMap" ).getMethod( "values" ) );
			methodCache.put( "put", getNMSClass( "PropertyMap" ).getMethod( "put", Object.class, Object.class ) );

			methodCache.put( "loadNBTTagCompound", getNMSClass( "MojangsonParser" ).getMethod( "parse", String.class ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		try {
			methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );
		} catch( NoSuchMethodException exception ) {
			try {
				methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "b", getNMSClass( "NBTTagCompound" ) ) );
			} catch ( Exception exception2 ) {
				exception2.printStackTrace();
			}
		} catch( Exception exception ) {
			exception.printStackTrace();
		}

		try {
			methodCache.put( "setProfile", getNMSClass( "CraftMetaSkull" ).getDeclaredMethod( "setProfile", getNMSClass( "GameProfile" ) ) );
			methodCache.get( "setProfile" ).setAccessible( true );
		} catch( NoSuchMethodException exception ) {
			// The method doesn't exist, so it's before 1.15.2
		}

		constructorCache = new HashMap< Class< ? >, Constructor< ? > >();
		try {
			constructorCache.put( getNBTTag( Byte.class ), getNBTTag( Byte.class ).getDeclaredConstructor( byte.class ) );
			constructorCache.put( getNBTTag( Boolean.class ), getNBTTag( Boolean.class ).getDeclaredConstructor( byte.class ) );
			constructorCache.put( getNBTTag( String.class ), getNBTTag( String.class ).getDeclaredConstructor( String.class ) );
			constructorCache.put( getNBTTag( Double.class ), getNBTTag( Double.class ).getDeclaredConstructor( double.class ) );
			constructorCache.put( getNBTTag( Integer.class ), getNBTTag( Integer.class ).getDeclaredConstructor( int.class ) );
			constructorCache.put( getNBTTag( Long.class ), getNBTTag( Long.class ).getDeclaredConstructor( long.class ) );
			constructorCache.put( getNBTTag( Float.class ), getNBTTag( Float.class ).getDeclaredConstructor( float.class ) );
			constructorCache.put( getNBTTag( Short.class ), getNBTTag( Short.class ).getDeclaredConstructor( short.class ) );
			constructorCache.put( getNBTTag( Class.forName( "[B" ) ), getNBTTag( Class.forName( "[B" ) ).getDeclaredConstructor( Class.forName( "[B" ) ) );
			constructorCache.put( getNBTTag( Class.forName( "[I" ) ), getNBTTag( Class.forName( "[I" ) ).getDeclaredConstructor( Class.forName( "[I" ) ) );

			// This is for 1.15 since Mojang decided to make the constructors private
			for ( Constructor< ? > cons : constructorCache.values() ) {
				cons.setAccessible( true );
			}

			constructorCache.put( getNMSClass( "BlockPosition" ), getNMSClass( "BlockPosition" ).getConstructor( int.class, int.class, int.class ) );

			constructorCache.put( getNMSClass( "GameProfile" ), getNMSClass( "GameProfile" ).getConstructor( UUID.class, String.class ) );
			constructorCache.put( getNMSClass( "Property" ), getNMSClass( "Property" ).getConstructor( String.class, String.class ) );

			if ( LOCAL_VERSION == MinecraftVersion.v1_11 || LOCAL_VERSION == MinecraftVersion.v1_12 ) {
				constructorCache.put( getNMSClass( "ItemStack" ), getNMSClass( "ItemStack" ).getConstructor( getNMSClass( "NBTTagCompound" ) ) );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}

		NBTTagFieldCache = new HashMap< Class< ? >, Field >();
		try {
			for ( Class< ? > clazz : NBTClasses.values() ) {
				Field data = clazz.getDeclaredField( "data" );
				data.setAccessible( true );
				NBTTagFieldCache.put( clazz, data );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}

		try {
			NBTListData = getNMSClass( "NBTTagList" ).getDeclaredField( "list" );
			NBTListData.setAccessible( true );
			NBTCompoundMap = getNMSClass( "NBTTagCompound" ).getDeclaredField( "map" );
			NBTCompoundMap.setAccessible( true );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static Class< ? > getNBTTag( Class< ? > primitiveType ) {
		if ( NBTClasses.containsKey( primitiveType ) )
			return NBTClasses.get( primitiveType );
		return primitiveType;
	}

	private static Object getNBTVar( Object object ) {
		if ( object == null ) {
			return null;
		}
		Class< ? > clazz = object.getClass();
		try {
			if ( NBTTagFieldCache.containsKey( clazz ) ) {
				return NBTTagFieldCache.get( clazz ).get( object );
			}
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		return null;
	}

	private static Method getMethod( String name ) {
		return methodCache.containsKey( name ) ? methodCache.get( name ) : null;
	}

	private static Constructor< ? > getConstructor( Class< ? > clazz ) {
		return constructorCache.containsKey( clazz ) ? constructorCache.get( clazz ) : null;
	}

	private static Class<?> getNMSClass(String name) {
		if ( classCache.containsKey( name ) ) {
			return classCache.get( name );
		}

		try {
			return Class.forName("net.minecraft.server." + VERSION + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getMatch( String string, String regex ) {
		Pattern pattern = Pattern.compile( regex );
		Matcher matcher = pattern.matcher( string );
		if ( matcher.find() ) {
			return matcher.group( 1 );
		} else {
			return null;
		}
	}

	private static Object createItemStack( Object compound ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		if ( LOCAL_VERSION == MinecraftVersion.v1_11 || LOCAL_VERSION == MinecraftVersion.v1_12 ) {
			return getConstructor( getNMSClass( "ItemStack" ) ).newInstance( compound );
		}
		return getMethod( "createStack" ).invoke( null, compound );
	}

	/**
	 * Gets the Bukkit version
	 * 
	 * @return
	 * The Bukkit version in standard package format
	 */
	public static String getVersion() {
		return VERSION;
	}
	
	public static MinecraftVersion getMinecraftVersion() {
		return LOCAL_VERSION;
	}

	/**
	 * Creates a skull with the given url as the skin
	 * 
	 * @param skinURL
	 * The URL of the skin, must be from mojang
	 * @return
	 * An item stack with count of 1
	 */
	public static ItemStack getHead( String skinURL ) {
		Material material = Material.getMaterial( "SKULL_ITEM" );
		if ( material == null ) {
			// Most likely 1.13 materials
			material = Material.getMaterial( "PLAYER_HEAD" );
		}
		ItemStack head = new ItemStack( material, 1, ( short ) 3 );
		if ( skinURL == null || skinURL.isEmpty() ) {
			return head;
		}
		ItemMeta headMeta = head.getItemMeta();
		Object profile = null;
		try {
			profile = getConstructor( getNMSClass( "GameProfile" ) ).newInstance( UUID.randomUUID(), null );
			Object propertyMap = getMethod( "getProperties" ).invoke( profile );
			Object textureProperty = getConstructor( getNMSClass( "Property" ) ).newInstance( "textures", new String( Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", skinURL ).getBytes() ) ) );
			getMethod( "put" ).invoke( propertyMap, "textures", textureProperty );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e1 ) {
			e1.printStackTrace();
		}

		if ( methodCache.containsKey( "setProfile" ) ) {
			try {
				getMethod( "setProfile" ).invoke( headMeta, profile );
			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
			}
		} else {
			Field profileField = null;
			try {
				profileField = headMeta.getClass().getDeclaredField("profile");
			} catch ( NoSuchFieldException | SecurityException e ) {
				e.printStackTrace();
			}
			profileField.setAccessible(true);
			try {
				profileField.set(headMeta, profile);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		head.setItemMeta(headMeta);
		return head;
	}

	/**
	 * Fetches the texture of a skull
	 * 
	 * @param head
	 * The item stack itself
	 * @return
	 * The URL of the texture
	 */
	public static String getTexture( ItemStack head ) {
		ItemMeta meta = head.getItemMeta();
		Field profileField = null;
		try {
			profileField = meta.getClass().getDeclaredField("profile");
		} catch ( NoSuchFieldException | SecurityException e ) {
			e.printStackTrace();
			throw new IllegalArgumentException( "Item is not a player skull!" );
		}
		profileField.setAccessible(true);
		try {
			Object profile = profileField.get( meta );
			if ( profile == null ) {
				return null;
			}

			Collection< Object > properties = ( Collection< Object > ) getMethod( "values" ).invoke( getMethod( "getProperties" ).invoke( profile ) );
			for ( Object prop : properties ) {
				if ( "textures".equals( getMethod( "getName" ).invoke( prop ) ) ) {
					String texture = new String( Base64.getDecoder().decode( ( String ) getMethod( "getValue" ).invoke( prop ) ) );
					return getMatch( texture, "\\{\"url\":\"(.*?)\"\\}" );
				}
			}
			return null;
		} catch ( IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets an NBT tag in a given item with the specified keys
	 * 
	 * @param item
	 * The itemstack to get the keys from
	 * @param keys
	 * The keys to fetch; an integer after a key value indicates that it should get the nth place of
	 * the previous compound because it is a list;
	 * @return
	 * The item represented by the keys, and an integer if it is showing how long a list is.
	 */
	private static Object getItemTag( ItemStack item, Object... keys ) {
		try {
			return getTag( getCompound( item ), keys );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
			return null;
		}
	}

	// Gets the NBTTagCompound
	private static Object getCompound( ItemStack item ) {
		if ( item == null ) {
			return null;
		}
		try {
			Object stack = null;
			stack = getMethod( "asNMSCopy" ).invoke( null, item );

			Object tag = null;

			if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
				tag = getMethod( "getTag" ).invoke( stack );
			} else {
				tag = getNMSClass( "NBTTagCompound" ).newInstance();
			}

			return tag;
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets an NBTCompound from the item provided. Use {@link #getNBTCompound(Object, Object...)} instead.
	 * 
	 * @param item
	 * Itemstack
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An NBTCompound
	 */
	private static NBTCompound getItemNBTTag( ItemStack item, Object... keys ) {
		if ( item == null ) {
			return null;
		}
		try {
			Object stack = null;
			stack = getMethod( "asNMSCopy" ).invoke( null, item );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			tag = getMethod( "save" ).invoke( stack, tag );

			return getNBTTag( tag, keys );
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets an NBT tag in an item with the provided keys and value
	 * Should use the {@link #set(Object, Object, Object...)} method instead
	 * 
	 * @param item
	 * The itemstack to set
	 * @param value
	 * The value to set
	 * @param keys
	 * The keys to set, String for NBTCompound, int or null for an NBTTagList
	 * @return
	 * A new ItemStack with the updated NBT tags
	 */
	private static ItemStack setItemTag( ItemStack item, Object value, Object... keys ) {
		if ( item == null ) {
			return null;
		}
		try {
			Object stack = getMethod( "asNMSCopy" ).invoke( null, item );

			Object tag = null;

			if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
				tag = getMethod( "getTag" ).invoke( stack );
			} else {
				tag = getNMSClass( "NBTTagCompound" ).newInstance();
			}

			if ( keys.length == 0 && value instanceof NBTCompound ) {
				tag = ( ( NBTCompound ) value ).tag;
			} else {
				setTag( tag, value, keys );
			}

			getMethod( "setTag" ).invoke( stack, tag );
			return ( ItemStack ) getMethod( "asBukkitCopy" ).invoke( null, stack );
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Constructs an ItemStack from a given NBTCompound
	 * 
	 * @param compound
	 * An NBTCompound following an ItemStack structure
	 * @return
	 * A new ItemStack
	 */
	public static ItemStack getItemFromTag( NBTCompound compound ) {
		if ( compound == null ) {
			return null;
		}
		try {
			Object tag = compound.tag;
			Object count = getTag( tag, "Count" );
			Object id = getTag( tag, "id" );
			if ( count == null || id == null ) {
				return null;
			}
			if ( count instanceof Byte && id instanceof String ) {
				return ( ItemStack ) getMethod( "asBukkitCopy" ).invoke( null, createItemStack( tag ) );
			}
			return null;
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets an NBT tag in a given entity with the specified keys
	 * 
	 * @param entity
	 * The entity to get the keys from
	 * @param keys
	 * The keys to fetch; an integer after a key value indicates that it should get the nth place of
	 * the previous compound because it is a list;
	 * @return
	 * The item represented by the keys, and an integer if it is showing how long a list is.
	 */
	private static Object getEntityTag( Entity entity, Object... keys ) {
		try {
			return getTag( getCompound( entity ), keys );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
			return null;
		}
	}

	// Gets the NBTTagCompound
	private static Object getCompound( Entity entity ) {
		if ( entity == null ) {
			return entity;
		}
		try {
			Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

			return tag;
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets an NBTCompound from the entity provided. Use {@link #getNBTCompound(Object, Object...)} instead.
	 * 
	 * @param entity
	 * The Bukkit entity provided
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An NBTCompound
	 */
	private static NBTCompound getEntityNBTTag( Entity entity, Object...keys ) {
		if ( entity == null ) {
			return null;
		}
		try {
			Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

			return getNBTTag( tag, keys );
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets an NBT tag in an entity with the provided keys and value
	 * Should use the {@link #set(Object, Object, Object...)} method instead
	 * 
	 * @param entity
	 * The entity to set
	 * @param value
	 * The value to set
	 * @param keys
	 * The keys to set, String for NBTCompound, int or null for an NBTTagList
	 */
	private static void setEntityTag( Entity entity, Object value, Object... keys ) {
		if ( entity == null ) {
			return;
		}
		try {
			Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance() ;

			getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

			if ( keys.length == 0 && value instanceof NBTCompound ) {
				tag = ( ( NBTCompound ) value ).tag;
			} else {
				setTag( tag, value, keys );
			}

			getMethod( "setEntityTag" ).invoke( NMSEntity, tag );
		} catch ( Exception exception ) {
			exception.printStackTrace();
			return;
		}
	}

	/**
	 * Gets an NBT tag in a given block with the specified keys. Use {@link #getNBTCompound(Object, Object...)} instead.
	 * 
	 * @param block
	 * The block to get the keys from
	 * @param keys
	 * The keys to fetch; an integer after a key value indicates that it should get the nth place of
	 * the previous compound because it is a list;
	 * @return
	 * The item represented by the keys, and an integer if it is showing how long a list is.
	 */
	private static Object getBlockTag( Block block, Object... keys ) {
		try {
			return getTag( getCompound( block ), keys );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
			return null;
		}
	}

	// Gets the NBTTagCompound
	private static Object getCompound( Block block ) {
		try {
			if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
				return null;
			}
			Location location = block.getLocation();

			Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

			Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

			Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			getMethod( "getTileTag" ).invoke( tileEntity, tag );

			return tag;
		} catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets an NBTCompound from the block provided
	 * 
	 * @param block
	 * The block provided
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An NBTCompound
	 */
	private static NBTCompound getBlockNBTTag( Block block, Object... keys ) {
		try {
			if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
				return null;
			}
			Location location = block.getLocation();

			Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

			Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

			Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			getMethod( "getTileTag" ).invoke( tileEntity, tag );

			return getNBTTag( tag, keys );
		} catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets an NBT tag in an block with the provided keys and value
	 * Should use the {@link #set(Object, Object, Object...)} method instead
	 * 
	 * @param block
	 * The block to set
	 * @param value
	 * The value to set
	 * @param keys
	 * The keys to set, String for NBTCompound, int or null for an NBTTagList
	 */
	private static void setBlockTag( Block block, Object value, Object... keys ) {
		try {
			if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
				return;
			}
			Location location = block.getLocation();

			Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

			Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

			Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

			Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

			getMethod( "getTileTag" ).invoke( tileEntity, tag );

			if ( keys.length == 0 && value instanceof NBTCompound ) {
				tag = ( ( NBTCompound ) value ).tag;
			} else {
				setTag( tag, value, keys );
			}

			if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_16 ) ) {
				getMethod( "setTileTag" ).invoke( tileEntity, getMethod( "getType" ).invoke( nmsWorld, blockPosition ), tag );
			} else {
				getMethod( "setTileTag" ).invoke( tileEntity, tag );
			}
		} catch( Exception exception ) {
			exception.printStackTrace();
			return;
		}
	}

	/**
	 * Sets the texture of a skull block
	 * 
	 * @param block
	 * The block, must be a skull
	 * @param texture
	 * The URL of the skin
	 */
	public static void setSkullTexture( Block block, String texture ) {
		try {
			Object profile = getConstructor( getNMSClass( "GameProfile" ) ).newInstance( UUID.randomUUID(), null );
			Object propertyMap = getMethod( "getProperties" ).invoke( profile );
			Object textureProperty = getConstructor( getNMSClass( "Property" ) ).newInstance( "textures", new String( Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", texture ).getBytes() ) ) );
			getMethod( "put" ).invoke( propertyMap, "textures", textureProperty );
			
			Location location = block.getLocation();

			Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

			Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

			Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

			getMethod( "setGameProfile" ).invoke( tileEntity, profile );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private static Object getValue( Object object, Object... keys ) {
		if ( object instanceof ItemStack ) {
			return getItemTag( ( ItemStack ) object, keys );
		} else if ( object instanceof Entity ) {
			return getEntityTag( ( Entity ) object, keys );
		} else if ( object instanceof Block ) {
			return getBlockTag( ( Block ) object, keys );
		} else if ( object instanceof NBTCompound ) {
			try {
				return getTag( ( ( NBTCompound ) object ).tag, keys );
			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
				return null;
			}
		} else {
			throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
		}
	}

	/**
	 * Gets an NBTCompound from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An NBTCompound, or null if none is stored at the provided location
	 */
	public static NBTCompound getNBTCompound( Object object, Object... keys ) {
		if ( object instanceof ItemStack ) {
			return getItemNBTTag( ( ItemStack ) object, keys );
		} else if ( object instanceof Entity ) {
			return getEntityNBTTag( ( Entity ) object, keys );
		} else if ( object instanceof Block ) {
			return getBlockNBTTag( ( Block ) object, keys );
		} else if ( object instanceof NBTCompound ) {
			try {
				return getNBTTag( ( ( NBTCompound ) object ).tag, keys );
			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
				return null;
			}
		} else if ( getNMSClass( "NBTTagCompound" ).isInstance( object ) ) {
			try {
				return getNBTTag( object, keys );
			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
				return null;
			}
		} else {
			throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
		}
	}

	/**
	 * Gets a string from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A string, or null if none is stored at the provided location
	 */
	public static String getString( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof String ? ( String ) result : null;
	}

	/**
	 * Gets an int from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An integer, or 0 if none is stored at the provided location
	 */
	public static int getInt( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Integer ? ( int ) result : 0;
	}

	/**
	 * Gets a double from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A double, or 0 if none is stored at the provided location
	 */
	public static double getDouble( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Double ? ( double ) result : 0;
	}

	/**
	 * Gets a long from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A long, or 0 if none is stored at the provided location
	 */
	public static long getLong( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Long ? ( long ) result : 0;
	}

	/**
	 * Gets a float from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A float, or 0 if none is stored at the provided location
	 */
	public static float getFloat( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Float ? ( float ) result : 0;
	}

	/**
	 * Gets a short from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A short, or 0 if none is stored at the provided location
	 */
	public static short getShort( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Short ? ( short ) result : 0;
	}

	/**
	 * Gets a byte from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A byte, or 0 if none is stored at the provided location
	 */
	public static byte getByte( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof Byte ? ( byte ) result : 0;
	}

	/**
	 * Gets a boolean from an object
	 *
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A boolean or false if none is stored at the provided location
	 */
	public static boolean getBoolean( Object object, Object... keys ) {
		return getByte( object, keys ) == 1;
	}

	/**
	 * Gets a byte array from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A byte array, or null if none is stored at the provided location
	 */
	public static byte[] getByteArray( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof byte[] ? ( byte[] ) result : null;
	}

	/**
	 * Gets an int array from an object
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * An int array, or null if none is stored at the provided location
	 */
	public static int[] getIntArray( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result instanceof int[] ? ( int[] ) result : null;
	}

	/**
	 * Checks if the object contains the given key
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * Whether or not the particular tag exists, may not be a primitive
	 */
	public static boolean contains( Object object, Object... keys ) {
		Object result = getValue( object, keys );
		return result != null;
	}

	/**
	 * Get the keys at the specific location, if it is a compound.
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * A set of keys
	 */
	public static Collection< String > getKeys( Object object, Object... keys ) {
		Object compound;
		if ( object instanceof ItemStack ) {
			compound = getCompound( ( ItemStack ) object );
		} else if ( object instanceof Entity ) {
			compound = getCompound( ( Entity ) object );
		} else if ( object instanceof Block ) {
			compound = getCompound( ( Block ) object );
		} else if ( object instanceof NBTCompound ) {
			compound = ( ( NBTCompound ) object ).tag;
		} else {
			throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
		}

		try {
			NBTCompound nbtCompound = getNBTTag( compound, keys );

			Object tag = nbtCompound.tag;
			if ( getNMSClass( "NBTTagCompound" ).isInstance( tag ) ) {
				return ( Collection< String > ) getMethod( "getKeys" ).invoke( tag );
			} else {
				return null;
			}

		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets the size of the list or NBTCompound at the given location.
	 * 
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param keys
	 * Keys in descending order
	 * @return
	 * The size of the list or compound at the given location.
	 */
	public static int getSize( Object object, Object... keys ) {
		Object compound;
		if ( object instanceof ItemStack ) {
			compound = getCompound( ( ItemStack ) object );
		} else if ( object instanceof Entity ) {
			compound = getCompound( ( Entity ) object );
		} else if ( object instanceof Block ) {
			compound = getCompound( ( Block ) object );
		} else if ( object instanceof NBTCompound ) {
			compound = ( ( NBTCompound ) object ).tag;
		} else {
			throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
		}

		try {
			NBTCompound nbtCompound = getNBTTag( compound, keys );
			if ( getNMSClass( "NBTTagCompound" ).isInstance( nbtCompound.tag ) ) {
				return getKeys( nbtCompound ).size();
			} else if ( getNMSClass( "NBTTagList" ).isInstance( nbtCompound.tag ) ) {
				return ( int ) getMethod( "size" ).invoke( nbtCompound.tag );
			}
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
			return 0;
		}

		throw new IllegalArgumentException( "Value is not a compound or list!" );
	}

	/**
	 * Sets the value in the object with the given keys
	 * 
	 * @param <T>
	 * ItemStack, Entity, Block, or NBTCompound.
	 * @param object
	 * Must be an ItemStack, Entity, Block, or NBTCompound
	 * @param value
	 * The value to set, can be an NBTCompound
	 * @param keys
	 * The keys in descending order
	 * @return
	 * The new item stack if the object provided is an item, else original object
	 */
	public static < T > T set( T object, Object value, Object... keys ) {
		if ( object instanceof ItemStack ) {
			return ( T ) setItemTag( ( ItemStack ) object, value, keys );
		} else if ( object instanceof Entity ) {
			setEntityTag( ( Entity ) object, value, keys );
		} else if ( object instanceof Block ) {
			setBlockTag( ( Block ) object, value, keys );
		} else if ( object instanceof NBTCompound ) {
			try {
				setTag( ( ( NBTCompound ) object ).tag, value, keys );
			} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
		}
		return object;
	}

	/**
	 * Load an NBTCompound from a String.
	 * 
	 * @param json
	 * A String in json format.
	 * @return
	 * An NBTCompound from the String provided. May or may not be a valid ItemStack.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static NBTCompound getNBTCompound( String json ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return NBTCompound.fromJson( json );
	}

	/**
	 * Get an empty NBTCompound.
	 * 
	 * @return
	 * A new NBTCompound that contains a NBTTagCompound object.
	 */
	public static NBTCompound getEmptyNBTCompound() {
		try {
			return new NBTCompound( getNMSClass( "NBTTagCompound" ).newInstance() );
		} catch ( InstantiationException | IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}

	private static void setTag( Object tag, Object value, Object... keys ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object notCompound;
		// Get the real value of what we want to set here
		if ( value != null ) {
			if ( value instanceof NBTCompound ) {
				notCompound = ( ( NBTCompound ) value ).tag;
			} else if ( getNMSClass( "NBTTagList" ).isInstance( value ) || getNMSClass( "NBTTagCompound" ).isInstance( value ) ) {
				notCompound = value;
			} else {
				if ( value instanceof Boolean ) {
					value = ( byte ) ( ( Boolean ) value == true ? 1 : 0 );
				}
				notCompound = getConstructor( getNBTTag( value.getClass() ) ).newInstance( value );
			}
		} else {
			notCompound = null;
		}

		Object compound = tag;
		for ( int index = 0; index < keys.length - 1; index++ ) {
			Object key = keys[ index ];
			Object oldCompound = compound;
			if ( key instanceof Integer ) {
				compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
			} else if ( key != null ) {
				compound = getMethod( "get" ).invoke( compound, ( String ) key );
			}
			if ( compound == null || key == null ) {
				if ( keys[ index + 1 ] == null || keys[ index + 1 ] instanceof Integer ) {
					compound = getNMSClass( "NBTTagList" ).newInstance();
				} else {
					compound = getNMSClass( "NBTTagCompound" ).newInstance();
				}
				if ( oldCompound.getClass().getSimpleName().equals( "NBTTagList" ) ) {
					if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
						getMethod( "add" ).invoke( oldCompound, getMethod( "size" ).invoke( oldCompound ), compound );
					} else {
						getMethod( "add" ).invoke( oldCompound, compound );
					}
				} else {
					getMethod( "set" ).invoke( oldCompound, ( String ) key, compound );
				}
			}
		}
		if ( keys.length > 0 ) {
			Object lastKey = keys[ keys.length - 1 ];
			if ( lastKey == null ) {
				if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
					getMethod( "add" ).invoke( compound, getMethod( "size" ).invoke( compound ), notCompound );
				} else {
					getMethod( "add" ).invoke( compound, notCompound );
				}
			} else if ( lastKey instanceof Integer ) {
				if ( notCompound == null ) {
					getMethod( "listRemove" ).invoke( compound, ( int ) lastKey );
				} else {
					getMethod( "setIndex" ).invoke( compound, ( int ) lastKey, notCompound );
				}
			} else {
				if ( notCompound == null ) {
					getMethod( "remove" ).invoke( compound, ( String ) lastKey );
				} else {
					getMethod( "set" ).invoke( compound, ( String ) lastKey, notCompound );
				}
			}
		} else {
			if ( notCompound != null ) {
			}
		}
	}

	private static NBTCompound getNBTTag( Object tag, Object...keys ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object compound = tag;

		for ( Object key : keys ) {
			if ( compound == null ) {
				return null;
			} else if ( getNMSClass( "NBTTagCompound" ).isInstance( compound ) ) {
				compound = getMethod( "get" ).invoke( compound, ( String ) key );
			} else if ( getNMSClass( "NBTTagList" ).isInstance( compound ) ) {
				compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
			}
		}
		return new NBTCompound( compound );
	}

	private static Object getTag( Object tag, Object... keys ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ( keys.length == 0 ) {
			return getTags( tag );
		}

		Object notCompound = tag;

		for ( Object key : keys ) {
			if ( notCompound == null ) {
				return null;
			} else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
				notCompound = getMethod( "get" ).invoke( notCompound, ( String ) key );
			} else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
				notCompound = ( ( List< ? > ) NBTListData.get( notCompound ) ).get( ( int ) key );
			} else {
				return getNBTVar( notCompound );
			}
		}
		if ( notCompound == null ) {
			return null;
		} else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
			return getTags( notCompound );
		} else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
			return getTags( notCompound );
		} else {
			return getNBTVar( notCompound );
		}
	}

	private static Object getTags( Object tag ) {
		Map< Object, Object > tags = new HashMap< Object, Object >();
		try {
			if ( getNMSClass( "NBTTagCompound" ).isInstance( tag ) ) {
				Map< String, Object > tagCompound = ( Map< String, Object > ) NBTCompoundMap.get( tag );
				for ( String key : tagCompound.keySet() ) {
					Object value = tagCompound.get( key );
					if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
						continue;
					}
					tags.put( key, getTag( value ) );
				}
			} else if ( getNMSClass( "NBTTagList" ).isInstance( tag ) ) {
				List< Object > tagList = ( List< Object > ) NBTListData.get( tag );
				for ( int index = 0; index < tagList.size(); index++ ) {
					Object value = tagList.get( index );
					if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
						continue;
					}
					tags.put( index, getTag( value ) );
				}
			} else {
				return getNBTVar( tag );
			}
			return tags;
		} catch ( Exception e ) {
			e.printStackTrace();
			return tags;
		}
	}

	/**
	 * A class for holding NBTTagCompounds
	 */
	public static final class NBTCompound {
		protected final Object tag;

		protected NBTCompound( @Nonnull Object tag ) {
			this.tag = tag;
		}

		public void set( Object value, Object... keys ) {
			try {
				setTag( tag, value, keys );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}

		/**
		 * The exact same as the toString method
		 * 
		 * @return
		 * Convert the compound to a string.
		 */
		public String toJson() {
			return tag.toString();
		}

		public static NBTCompound fromJson( String json ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return new NBTCompound( getMethod( "loadNBTTagCompound" ).invoke( null, json ) );
		}

		@Override
		public String toString() {
			return tag.toString();
		}

		@Override
		public int hashCode() {
			return tag.hashCode();
		}

		@Override
		public boolean equals( Object obj ) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NBTCompound other = (NBTCompound) obj;
			if (tag == null) {
				if (other.tag != null)
					return false;
			} else if (!tag.equals(other.tag))
				return false;
			return true;
		}
	}

	/**
	 * Minecraft variables as enums
	 * 
	 * @author BananaPuncher714
	 */
	public enum MinecraftVersion {
		v1_8( "1_8", 0 ),
		v1_9( "1_9", 1 ),
		v1_10( "1_10", 2 ),
		v1_11( "1_11", 3 ),
		v1_12( "1_12", 4 ),
		v1_13( "1_13", 5 ),
		v1_14( "1_14", 6 ),
		v1_15( "1_15", 7 ),
		v1_16( "1_16", 8 ),
		v1_17( "1_17", 9 ),
		v1_18( "1_18", 10 ),
		v1_19( "1_19", 11 );

		private int order;
		private String key;

		MinecraftVersion( String key, int v ) {
			this.key = key;
			order = v;
		}

		// Would be really cool if we could overload operators here
		public boolean greaterThanOrEqualTo( MinecraftVersion other ) {
			return order >= other.order;
		}

		public boolean lessThanOrEqualTo( MinecraftVersion other ) {
			return order <= other.order;
		}

		public static MinecraftVersion get( String v ) {
			for ( MinecraftVersion k : MinecraftVersion.values() ) {
				if ( v.contains( k.key ) ) {
					return k;
				}
			}
			return null;
		}
	}
}