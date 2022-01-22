package com.loohp.interactivechat.objectholders;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

public abstract class DummyPlayer implements Player {

    private final String name;
    private final UUID uuid;

    public DummyPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public static DummyPlayer newInstance(String name, UUID uuid) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(DummyPlayer.class);
        factory.setFilter(
                new MethodFilter() {
                    @Override
                    public boolean isHandled(Method method) {
                        return Modifier.isAbstract(method.getModifiers());
                    }
                }
        );
        MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                throw new UnsupportedOperationException();
            }
        };
        try {
            return (DummyPlayer) factory.create(new Class[] {String.class, UUID.class}, new Object[] {name, uuid}, handler);
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

}
