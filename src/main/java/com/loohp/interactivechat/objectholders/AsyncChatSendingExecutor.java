package com.loohp.interactivechat.objectholders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.InteractiveChat;

public class AsyncChatSendingExecutor implements AutoCloseable {
	
	private Supplier<Long> executionWaitTime;
	private long killThreadAfter;
	
	private ReentrantLock executeLock;
	private Map<UUID, Queue<MessageOrderInfo>> messagesOrder;
	private Queue<OutboundPacket> sendingQueue;
	private ThreadPoolExecutor executor;
	private Map<Future<?>, ExecutingTaskData> executingTasks;
	private Map<UUID, Map<UUID, OutboundPacket>> waitingPackets;
	private Map<UUID, Long> lastSuccessfulCheck;
	
	private List<Integer> taskIds;
	private AtomicBoolean isValid;
	
	public AsyncChatSendingExecutor(Supplier<Long> executionWaitTime, long killThreadAfter) {
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractiveChat Async ChatMessage Processing Thread #%d").build();
		this.executor = new ThreadPoolExecutor(8, 32, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), factory);
		this.executeLock = new ReentrantLock(true);
		this.executionWaitTime = executionWaitTime;
		this.killThreadAfter = killThreadAfter;
		this.executingTasks = new ConcurrentHashMap<>();
		this.sendingQueue = new ConcurrentLinkedQueue<>();
		this.messagesOrder = new ConcurrentHashMap<>();
		this.taskIds = new ArrayList<>();
		this.isValid = new AtomicBoolean(true);
		this.waitingPackets = new ConcurrentHashMap<>();
		this.lastSuccessfulCheck = new ConcurrentHashMap<>();
		
		taskIds.add(packetSender());
		packetOrderSender();
		monitor();
	}

	public void execute(Runnable runnable, Player player, UUID id) {
		executeLock.lock();
		messagesOrder.putIfAbsent(player.getUniqueId(), new ConcurrentLinkedQueue<>());
		Queue<MessageOrderInfo> queue = messagesOrder.get(player.getUniqueId());
		Optional<MessageOrderInfo> optInfo = queue.stream().filter(each -> each.getId().equals(id)).findFirst();
		if (optInfo.isPresent()) {
			optInfo.get().setTime(System.currentTimeMillis());
		} else {
			queue.add(new MessageOrderInfo(id, System.currentTimeMillis()));
		}
		Future<?> future = executor.submit(runnable);
		executingTasks.put(future, new ExecutingTaskData(System.currentTimeMillis(), player.getUniqueId(), id));
		executeLock.unlock();
	}
	
	public void send(PacketContainer packet, Player player, UUID id) {
		OutboundPacket outboundPacket = new OutboundPacket(player, packet);
		Queue<MessageOrderInfo> queue = messagesOrder.get(player.getUniqueId());
		if (queue == null) {
			sendingQueue.add(outboundPacket);
		} else {
			if (queue.stream().anyMatch(each -> each.getId().equals(id))) {
				waitingPackets.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
				Map<UUID, OutboundPacket> waitingMap = waitingPackets.get(player.getUniqueId());
				waitingMap.put(id, outboundPacket);
			} else {
				sendingQueue.add(outboundPacket);
			}
		}
	}
	
	public void discard(UUID player, UUID id) {
		Queue<MessageOrderInfo> queue = messagesOrder.get(player);
		if (queue != null) {
			queue.removeIf(each -> each.getId().equals(id));
		}
	}
	
	@Override
	public synchronized void close() throws Exception {
		isValid.set(false);
		for (int id : taskIds) {
			if (id >= 0) {
				Bukkit.getScheduler().cancelTask(id);
			}
		}
		executor.shutdown();
	}
	
	public boolean isValid() {
		return isValid.get();
	}
	
	private void packetOrderSender() {
		new Thread(() -> {
			while (true) {
				for (Entry<UUID, Map<UUID, OutboundPacket>> entry : waitingPackets.entrySet()) {
					long time = System.currentTimeMillis();
					UUID playerUUID = entry.getKey();
					Queue<MessageOrderInfo> orderingQueue = messagesOrder.get(playerUUID);
					Map<UUID, OutboundPacket> playerWaitingPackets = entry.getValue();
					MessageOrderInfo messageOrderInfo;
					if (orderingQueue != null) {
						if ((messageOrderInfo = orderingQueue.peek()) == null) {
							Iterator<Entry<UUID, OutboundPacket>> itr = playerWaitingPackets.entrySet().iterator();
							while (itr.hasNext()) {
								sendingQueue.add(itr.next().getValue());
								itr.remove();
							}
						} else {
							UUID id = messageOrderInfo.getId();
							OutboundPacket outboundPacket = playerWaitingPackets.get(id);
							if (outboundPacket != null) {
								sendingQueue.add(outboundPacket);
								playerWaitingPackets.remove(id);
								orderingQueue.remove(messageOrderInfo);
								lastSuccessfulCheck.put(playerUUID, time);
							} else {
								if (playerWaitingPackets.isEmpty()) {
									lastSuccessfulCheck.put(playerUUID, time);
								} else {
									Long lastSuccessful = lastSuccessfulCheck.get(playerUUID);
									if (lastSuccessful == null) {
										lastSuccessfulCheck.put(playerUUID, time);
									} else if ((lastSuccessful + executionWaitTime.get()) < time) {
										orderingQueue.poll();
										lastSuccessfulCheck.put(playerUUID, time);
									}
								}						
							}
						}
					}
				}
				
				if (!isValid()) {
					break;
				}
				try {
					TimeUnit.NANOSECONDS.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}, "InteractiveChat Async ChatPacket Ordered Sending Thread").start();
	}
	
	private int packetSender() {
		return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			while (!sendingQueue.isEmpty()) {
				OutboundPacket out = sendingQueue.poll();
				try {
					if (out.getReciever().isOnline()) {
						InteractiveChat.protocolManager.sendServerPacket(out.getReciever(), out.getPacket(), false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1).getTaskId();
	}
	
	private void monitor() {
		new Thread(() -> {
			while (true) {
				long time = System.currentTimeMillis();
				Iterator<Entry<Future<?>, ExecutingTaskData>> itr = executingTasks.entrySet().iterator();
				while (itr.hasNext()) {
					Entry<Future<?>, ExecutingTaskData> entry = itr.next();
					Future<?> future = entry.getKey();
					ExecutingTaskData data = entry.getValue();
					if (future.isDone()) {					
						itr.remove();
					} else if (data.getStartTime() + killThreadAfter < time) {
						future.cancel(true);
						itr.remove();
						Queue<MessageOrderInfo> queue = messagesOrder.get(data.getPlayer());
						if (queue != null) {
							queue.removeIf(each -> each.getId().equals(data.getId()));
						}
					}
				}
				
				Iterator<Entry<UUID, Queue<MessageOrderInfo>>> itr1 = messagesOrder.entrySet().iterator();
				while (itr1.hasNext()) {
					Entry<UUID, Queue<MessageOrderInfo>> entry = itr1.next();
					if (Bukkit.getPlayer(entry.getKey()) == null) {
						itr1.remove();
					} else {
						entry.getValue().removeIf(each -> (each.getTime() + executionWaitTime.get()) < time);
					}
				}
				
				Iterator<Entry<UUID, Map<UUID, OutboundPacket>>> itr2 = waitingPackets.entrySet().iterator();
				while (itr2.hasNext()) {
					Entry<UUID, Map<UUID, OutboundPacket>> entry = itr2.next();
					if (Bukkit.getPlayer(entry.getKey()) == null) {
						itr2.remove();
					}
				}
				
				Iterator<Entry<UUID, Long>> itr3 = lastSuccessfulCheck.entrySet().iterator();
				while (itr3.hasNext()) {
					Entry<UUID, Long> entry = itr3.next();
					if (Bukkit.getPlayer(entry.getKey()) == null) {
						itr3.remove();
					}
				}
				if (!isValid()) {
					break;
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}, "InteractiveChat Async Monitor Thread").start();
	}
	
	public static class ExecutingTaskData {
		
		private long startTime;
		private UUID player;
		private UUID id;
		
		public ExecutingTaskData(long startTime, UUID player, UUID id) {
			this.startTime = startTime;
			this.player = player;
			this.id = id;
		}
		
		public long getStartTime() {
			return startTime;
		}
		
		public UUID getPlayer() {
			return player;
		}
		
		public UUID getId() {
			return id;
		}
	}
	
	private static class MessageOrderInfo {
		
		private UUID id;
		private long time;
		
		public MessageOrderInfo(UUID id, long time) {
			this.id = id;
			this.time = time;
		}
		
		public UUID getId() {
			return id;
		}
		
		public long getTime() {
			return time;
		}
		
		public void setTime(long time) {
			this.time = time;
		}
		
		public String toString() {
			return id.toString();
		}
		
	}

}
