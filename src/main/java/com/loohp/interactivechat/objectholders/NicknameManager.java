package com.loohp.interactivechat.objectholders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NicknameManager implements AutoCloseable {

    private Map<UUID, Set<String>> nicknames;
    private Function<UUID, Collection<String>> nicknameFunction;
    private Supplier<Set<UUID>> uuidSupplier;
    private long updatePeriod;
    private BiConsumer<UUID, Set<String>>[] changeListeners;

    private TimerTask timerTask;
    private AtomicBoolean isValid;
    private ReentrantLock lock;

    @SafeVarargs
    public NicknameManager(Function<UUID, Collection<String>> nicknameFunction, Supplier<Set<UUID>> uuidSupplier, long updatePeriod, BiConsumer<UUID, Set<String>>... changeListeners) {
        this.nicknames = new ConcurrentHashMap<>();
        this.nicknameFunction = nicknameFunction;
        this.uuidSupplier = uuidSupplier;
        this.updatePeriod = updatePeriod;
        this.changeListeners = changeListeners;

        this.isValid = new AtomicBoolean(true);
        this.lock = new ReentrantLock();
        run();
    }

    private void run() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Set<UUID> uuids = uuidSupplier.get();
                nicknames.entrySet().removeIf(entry -> !uuids.contains(entry.getKey()));
                for (UUID uuid : uuids) {
                    Collection<String> newNicknames = new HashSet<>(nicknameFunction.apply(uuid));
                    Set<String> oldNicknames = nicknames.get(uuid);
                    if (oldNicknames == null) {
                        Set<String> addedNicknames = new HashSet<>(newNicknames);
                        nicknames.put(uuid, addedNicknames);
                        fireListeners(uuid, addedNicknames);
                    } else {
                        lock.lock();
                        oldNicknames.clear();
                        oldNicknames.addAll(newNicknames);
                        lock.unlock();
                        fireListeners(uuid, oldNicknames);
                    }
                }
            }
        };
        new Timer().schedule(timerTask, updatePeriod, updatePeriod);
    }

    private void fireListeners(UUID uuid, Set<String> names) {
        for (BiConsumer<UUID, Set<String>> listener : changeListeners) {
            listener.accept(uuid, names);
        }
    }

    public Set<String> getNicknames(UUID uuid) {
        Set<String> names = nicknames.get(uuid);
        if (names == null) {
            return Collections.emptySet();
        }
        lock.lock();
        names = new HashSet<>(names);
        lock.unlock();
        return names;
    }

    @Override
    public synchronized void close() {
        if (isValid.getAndSet(false)) {
            timerTask.cancel();
        }
    }

}
