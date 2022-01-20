package com.loohp.interactivechat.utils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;

public class NativeAdventureConverter {
	
	private static final String NATIVE_PACKAGE = new String(new char[] {'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i', '.', 'a', 'd', 'v', 'e', 'n', 't', 'u', 'r', 'e'});
	
	private static boolean hasNativeAdventureImplementation = false;
	
	private static Class<?> nativeComponentClass;
	private static Class<?> nativeDecoderClass;
	private static Class<?> nativeEncoderClass;
	private static Class<?> nativeGsonComponentSerializerClass;
	private static Object nativeGsonComponentSerializerObject;
	private static Method nativeGsonComponentSerializeMethod;
	private static Method nativeGsonComponentDeserializeMethod;
	private static Class<?> nativeShowItemClass;
	private static Class<?> nativeKeyClass;
	private static Method nativeKeyConstructionMethod;
	private static Method nativeKeyAsStringMethod;
	private static Class<?> nativeBinaryTagHolderClass;
	private static Method nativeBinaryTagHolderContructionMethod;
	private static Method nativeBinaryTagHolderStringMethod;
	private static Method nativeShowItemConstructionMethod;
	private static Method nativeShowItemGetItemMethod;
	private static Method nativeShowItemGetCountMethod;
	private static Method nativeShowItemGetNbtMethod;
	private static Class<?> nativeShowEntityClass;
	private static Method nativeShowEntityClassConstructionMethod;
	private static Method nativeShowEntityGetTypeMethod;
	private static Method nativeShowEntityGetIdMethod;
	private static Method nativeShowEntityGetNameMethod;
	private static Class<?> nativeAudienceClass;
	private static Method nativeAudienceSendMessageMethod;
	
	static {
		try {
			Class.forName(NATIVE_PACKAGE + ".text.Component");
			
			try {
				nativeComponentClass = Class.forName(NATIVE_PACKAGE + ".text.Component");
				nativeDecoderClass = Class.forName(NATIVE_PACKAGE + ".util.Codec$Decoder");
				nativeEncoderClass = Class.forName(NATIVE_PACKAGE + ".util.Codec$Encoder");
				nativeGsonComponentSerializerClass = Class.forName(NATIVE_PACKAGE + ".text.serializer.gson.GsonComponentSerializer");
				Method nativeGsonComponentSerializerGsonMethod = nativeGsonComponentSerializerClass.getMethod("gson");
				nativeGsonComponentSerializerGsonMethod.setAccessible(true);
				nativeGsonComponentSerializerObject = nativeGsonComponentSerializerGsonMethod.invoke(null);
				nativeGsonComponentSerializeMethod = nativeGsonComponentSerializerObject.getClass().getMethod("serialize", nativeComponentClass);
				nativeGsonComponentSerializeMethod.setAccessible(true);
				nativeGsonComponentDeserializeMethod = nativeGsonComponentSerializerObject.getClass().getMethod("deserialize", String.class);
				nativeGsonComponentDeserializeMethod.setAccessible(true);
				nativeShowItemClass = Class.forName(NATIVE_PACKAGE + ".text.event.HoverEvent$ShowItem");
				nativeKeyClass = Class.forName(NATIVE_PACKAGE + ".key.Key");
				nativeKeyConstructionMethod = nativeKeyClass.getMethod("key", String.class);
				nativeKeyConstructionMethod.setAccessible(true);
				nativeKeyAsStringMethod = nativeKeyClass.getMethod("asString");
				nativeKeyAsStringMethod.setAccessible(true);
				nativeBinaryTagHolderClass = Class.forName(NATIVE_PACKAGE + ".nbt.api.BinaryTagHolder");
				nativeBinaryTagHolderContructionMethod = nativeBinaryTagHolderClass.getMethod("of", String.class);
				nativeBinaryTagHolderContructionMethod.setAccessible(true);
				nativeBinaryTagHolderStringMethod = nativeBinaryTagHolderClass.getMethod("string");
				nativeBinaryTagHolderStringMethod.setAccessible(true);
				nativeShowItemConstructionMethod = nativeShowItemClass.getMethod("of", nativeKeyClass, int.class, nativeBinaryTagHolderClass);
				nativeShowItemConstructionMethod.setAccessible(true);
				nativeShowItemGetItemMethod = nativeShowItemClass.getMethod("item");
				nativeShowItemGetItemMethod.setAccessible(true);
				nativeShowItemGetCountMethod = nativeShowItemClass.getMethod("count");
				nativeShowItemGetCountMethod.setAccessible(true);
				nativeShowItemGetNbtMethod = nativeShowItemClass.getMethod("nbt");
				nativeShowItemGetNbtMethod.setAccessible(true);
				nativeShowEntityClass = Class.forName(NATIVE_PACKAGE + ".text.event.HoverEvent$ShowEntity");
				nativeShowEntityClassConstructionMethod = nativeShowEntityClass.getMethod("of", nativeKeyClass, UUID.class, nativeComponentClass);
				nativeShowEntityClassConstructionMethod.setAccessible(true);
				nativeShowEntityGetTypeMethod = nativeShowEntityClass.getMethod("type");
				nativeShowEntityGetTypeMethod.setAccessible(true);
				nativeShowEntityGetIdMethod = nativeShowEntityClass.getMethod("id");
				nativeShowEntityGetIdMethod.setAccessible(true);
				nativeShowEntityGetNameMethod = nativeShowEntityClass.getMethod("name");
				nativeShowEntityGetNameMethod.setAccessible(true);
				nativeAudienceClass = Class.forName(NATIVE_PACKAGE + ".audience.Audience");
				nativeAudienceSendMessageMethod = nativeAudienceClass.getMethod("sendMessage", nativeComponentClass);
				nativeAudienceSendMessageMethod.setAccessible(true);
				
				hasNativeAdventureImplementation = true;
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {}
	}
	
	public static boolean hasNativeAdventure() {
		return hasNativeAdventureImplementation;
	}
	
	private static void printError(Throwable e) {
		if (hasNativeAdventureImplementation) {
			new RuntimeException("There is no native adventure implementation on this platform", e).printStackTrace();
		} else {
			e.printStackTrace();
		}
	}
	
	public static Object componentToNative(Component component, boolean legacyRGB) {
		try {
			return nativeGsonComponentDeserializeMethod.invoke(nativeGsonComponentSerializerObject, legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static Component componentFromNative(Object component) {
		try {
			return InteractiveChatComponentSerializer.gson().deserialize(nativeGsonComponentSerializeMethod.invoke(nativeGsonComponentSerializerObject, component).toString());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static String jsonStringFromNative(Object component) {
		try {
			return nativeGsonComponentSerializeMethod.invoke(nativeGsonComponentSerializerObject, component).toString();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static Object showItemToNative(ShowItem showItem) {
		String key = showItem.item().asString();
		int amount = showItem.count();
		String nbt = showItem.nbt() == null ? null : showItem.nbt().string();
		try {
			return nativeShowItemConstructionMethod.invoke(null, nativeKeyConstructionMethod.invoke(null, key), amount, nbt == null ? null : nativeBinaryTagHolderContructionMethod.invoke(null, nbt));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static ShowItem showItemFromNative(Object showItem) {
		try {
			String key = nativeKeyAsStringMethod.invoke(nativeShowItemGetItemMethod.invoke(showItem)).toString();
			int amount = (int) nativeShowItemGetCountMethod.invoke(showItem);
			Object nbtObject = nativeShowItemGetNbtMethod.invoke(showItem);
			String nbt = nbtObject == null ? null : nativeBinaryTagHolderStringMethod.invoke(nbtObject).toString();
			return ShowItem.of(Key.key(key), amount, nbt == null ? null : BinaryTagHolder.of(nbt));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static Object showEntityToNative(ShowEntity showEntity, boolean legacyRGB) {
		try {
			String key = showEntity.type().asString();
			UUID uuid = showEntity.id();
			Component component = showEntity.name() == null ? null : showEntity.name();
			return nativeShowEntityClassConstructionMethod.invoke(null, nativeKeyConstructionMethod.invoke(null, key), uuid, componentToNative(component, legacyRGB));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static ShowEntity showEntityFromNative(Object showEntity) {
		try {
			String key = nativeKeyAsStringMethod.invoke(nativeShowEntityGetTypeMethod.invoke(showEntity)).toString();
			UUID uuid = (UUID) nativeShowEntityGetIdMethod.invoke(showEntity);
			Object componentObject = nativeShowEntityGetNameMethod.invoke(showEntity);
			Component component = componentObject == null ? null : componentFromNative(componentObject); 
			return ShowEntity.of(Key.key(key), uuid, component);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
		return null;
	}
	
	public static boolean isInstanceOfNativeAudience(Object object) {
		return nativeAudienceClass.isInstance(object);
	}
	
	public static void sendNativeAudienceMessage(Object object, Component component, boolean legacyRGB) {
		Object nativeAudience = nativeAudienceClass.cast(object);
		try {
			Object nativeComponent = componentToNative(component, legacyRGB);
			nativeAudienceSendMessageMethod.invoke(nativeAudience, nativeComponent);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printError(e);
		}
	}
	
	public static class NativeLegacyHoverEventSerializerWrapper implements LegacyHoverEventSerializer {
		
		private Object nativeImplementation;
		
		private Method nativeDeserializeShowItemMethod;
		private Method nativeDeserializeShowEntityMethod;
		private Method nativeSerializeShowItemMethod;
		private Method nativeSerializeShowEntityMethod;
		
		public NativeLegacyHoverEventSerializerWrapper(Object nativeImplementation) {
			this.nativeImplementation = nativeImplementation;
			
			for (Method method : nativeImplementation.getClass().getDeclaredMethods()) {
				if (method.getName().equals("deserializeShowItem")) {
					method.setAccessible(true);
					nativeDeserializeShowItemMethod = method;
				} else if (method.getName().equals("deserializeShowEntity")) {
					method.setAccessible(true);
					nativeDeserializeShowEntityMethod = method;
				} else if (method.getName().equals("serializeShowItem")) {
					method.setAccessible(true);
					nativeSerializeShowItemMethod = method;
				} else if (method.getName().equals("serializeShowEntity")) {
					method.setAccessible(true);
					nativeSerializeShowEntityMethod = method;
				}
			}
		}

		@Override
		public @NotNull ShowItem deserializeShowItem(@NotNull Component input) throws IOException {
			try {
				Object nativeInput = componentToNative(input, false);
				Object nativeShowItem = nativeDeserializeShowItemMethod.invoke(nativeImplementation, nativeInput);
				return showItemFromNative(nativeShowItem);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				printError(e);
			}
			return null;
		}

		@Override
		public @NotNull ShowEntity deserializeShowEntity(@NotNull Component input, Decoder<Component, String, ? extends RuntimeException> componentDecoder) throws IOException {
			try {
				Object nativeInput = componentToNative(input, true);
				Object nativeShowEntity = nativeDeserializeShowEntityMethod.invoke(nativeImplementation, nativeInput, Proxy.newProxyInstance(nativeDecoderClass.getClassLoader(), new Class[] {nativeDecoderClass}, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String methodName = method.getName();
		                Class<?>[] parameterTypes = method.getParameterTypes();
		                if (methodName.equals("decode") && parameterTypes.length == 1) {
		                	Component compoent = componentDecoder.decode(args[0].toString());
							return componentToNative(compoent, true);
		                }
						return null;
					}
				}));
				return showEntityFromNative(nativeShowEntity);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				printError(e);
			}
			return null;
		}

		@Override
		public @NotNull Component serializeShowItem(@NotNull ShowItem input) throws IOException {
			try {
				Object nativeInput = showItemToNative(input);
				Object nativeComponent = nativeSerializeShowItemMethod.invoke(nativeImplementation, nativeInput);
				return componentFromNative(nativeComponent);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				printError(e);
			}
			return null;
		}

		@Override
		public @NotNull Component serializeShowEntity(@NotNull ShowEntity input, Encoder<Component, String, ? extends RuntimeException> componentEncoder) throws IOException {
			try {
				Object nativeInput = showEntityToNative(input, true);
				Object nativeComponent = nativeSerializeShowEntityMethod.invoke(nativeImplementation, nativeInput, Proxy.newProxyInstance(nativeEncoderClass.getClassLoader(), new Class[] {nativeEncoderClass}, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String methodName = method.getName();
		                Class<?>[] parameterTypes = method.getParameterTypes();
		                if (methodName.equals("decode") && parameterTypes.length == 1) {
		                	Component component = componentFromNative(args[0]);
		                	return componentEncoder.encode(component);
		                }
						return null;
					}
				}));
				return componentFromNative(nativeComponent);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				printError(e);
			}
			return null;
		}
	}

}
