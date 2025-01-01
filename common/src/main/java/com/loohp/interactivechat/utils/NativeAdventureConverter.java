/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.InteractiveChat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

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
    private static Method nativeBinaryTagHolderConstructionMethod;
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
                nativeBinaryTagHolderConstructionMethod = nativeBinaryTagHolderClass.getMethod("of", String.class);
                nativeBinaryTagHolderConstructionMethod.setAccessible(true);
                nativeBinaryTagHolderStringMethod = nativeBinaryTagHolderClass.getMethod("string");
                nativeBinaryTagHolderStringMethod.setAccessible(true);
                try {
                    nativeShowItemConstructionMethod = nativeShowItemClass.getMethod("showItem", nativeKeyClass, int.class, nativeBinaryTagHolderClass);
                } catch (Throwable e) {
                    nativeShowItemConstructionMethod = nativeShowItemClass.getMethod("of", nativeKeyClass, int.class, nativeBinaryTagHolderClass);
                }
                nativeShowItemConstructionMethod.setAccessible(true);
                nativeShowItemGetItemMethod = nativeShowItemClass.getMethod("item");
                nativeShowItemGetItemMethod.setAccessible(true);
                nativeShowItemGetCountMethod = nativeShowItemClass.getMethod("count");
                nativeShowItemGetCountMethod.setAccessible(true);
                nativeShowItemGetNbtMethod = nativeShowItemClass.getMethod("nbt");
                nativeShowItemGetNbtMethod.setAccessible(true);
                nativeShowEntityClass = Class.forName(NATIVE_PACKAGE + ".text.event.HoverEvent$ShowEntity");
                try {
                    nativeShowEntityClassConstructionMethod = nativeShowEntityClass.getMethod("showEntity", nativeKeyClass, UUID.class, nativeComponentClass);
                } catch (Throwable e) {
                    nativeShowEntityClassConstructionMethod = nativeShowEntityClass.getMethod("of", nativeKeyClass, UUID.class, nativeComponentClass);
                }
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
        } catch (Throwable e) {
        }
    }

    public static boolean hasNativeAdventure() {
        return hasNativeAdventureImplementation;
    }

    public static boolean canHandle(Component component) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_3)) {
            return true;
        }
        try {
            NativeAdventureConverter.componentToNative(component, InteractiveChat.version.isLegacyRGB());
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    private static RuntimeException error(Throwable e) {
        if (!hasNativeAdventureImplementation) {
            return new RuntimeException("There is no native adventure implementation on this platform", e);
        } else {
            return new RuntimeException("There is an error when converting to native adventure components", e);
        }
    }

    public static Object componentToNative(Component component, boolean legacyRGB) {
        try {
            return nativeGsonComponentDeserializeMethod.invoke(nativeGsonComponentSerializerObject, legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static Component componentFromNative(Object component) {
        try {
            return InteractiveChatComponentSerializer.gson().deserialize(nativeGsonComponentSerializeMethod.invoke(nativeGsonComponentSerializerObject, component).toString());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static String jsonStringFromNative(Object component) {
        try {
            return nativeGsonComponentSerializeMethod.invoke(nativeGsonComponentSerializerObject, component).toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static Object jsonStringToNative(String json) {
        try {
            return nativeGsonComponentDeserializeMethod.invoke(nativeGsonComponentSerializerObject, json);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static Object showItemToNative(ShowItem showItem) {
        String key = showItem.item().asString();
        int amount = showItem.count();
        String nbt = showItem.nbt() == null ? null : showItem.nbt().string();
        try {
            return nativeShowItemConstructionMethod.invoke(null, nativeKeyConstructionMethod.invoke(null, key), amount, nbt == null ? null : nativeBinaryTagHolderConstructionMethod.invoke(null, nbt));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static ShowItem legacyShowItemFromNative(Object showItem) {
        try {
            String key = nativeKeyAsStringMethod.invoke(nativeShowItemGetItemMethod.invoke(showItem)).toString();
            int amount = (int) nativeShowItemGetCountMethod.invoke(showItem);
            Object nbtObject = nativeShowItemGetNbtMethod.invoke(showItem);
            String nbt = nbtObject == null ? null : nativeBinaryTagHolderStringMethod.invoke(nbtObject).toString();
            return ShowItem.showItem(Key.key(key), amount, nbt == null ? null : BinaryTagHolder.binaryTagHolder(nbt));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static Object legacyShowEntityToNative(ShowEntity showEntity, boolean legacyRGB) {
        try {
            String key = showEntity.type().asString();
            UUID uuid = showEntity.id();
            Component component = showEntity.name() == null ? null : showEntity.name();
            return nativeShowEntityClassConstructionMethod.invoke(null, nativeKeyConstructionMethod.invoke(null, key), uuid, componentToNative(component, legacyRGB));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
    }

    public static ShowEntity showEntityFromNative(Object showEntity) {
        try {
            String key = nativeKeyAsStringMethod.invoke(nativeShowEntityGetTypeMethod.invoke(showEntity)).toString();
            UUID uuid = (UUID) nativeShowEntityGetIdMethod.invoke(showEntity);
            Object componentObject = nativeShowEntityGetNameMethod.invoke(showEntity);
            Component component = componentObject == null ? null : componentFromNative(componentObject);
            return ShowEntity.showEntity(Key.key(key), uuid, component);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw error(e);
        }
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
            throw error(e);
        }
    }

    public static class NativeLegacyHoverEventSerializerWrapper implements LegacyHoverEventSerializer {

        private final Object nativeImplementation;

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
                return legacyShowItemFromNative(nativeShowItem);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                error(e);
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
                error(e);
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
                error(e);
            }
            return null;
        }

        @Override
        public @NotNull Component serializeShowEntity(@NotNull ShowEntity input, Encoder<Component, String, ? extends RuntimeException> componentEncoder) throws IOException {
            try {
                Object nativeInput = legacyShowEntityToNative(input, true);
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
                error(e);
            }
            return null;
        }

    }

}
