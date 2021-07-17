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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;

public class AsyncChatSendingExecutor implements AutoCloseable {
	
	public static final long RE_WAIT_TIME = 200;
	
	private Supplier<Long> executionWaitTime;
	private long killThreadAfter;
	
	private Map<UUID, Queue<MessageOrderInfo>> messagesOrder;
	private Queue<OutboundPacket> sendingQueue;
	private ExecutorService executor;
	private Map<Future<?>, ExecutingTaskData> executingTasks;
	
	private AtomicBoolean isValid;
	private List<Integer> taskIds;
	
	public AsyncChatSendingExecutor(ExecutorService executor, Supplier<Long> executionWaitTime, long killThreadAfter) {
		this.executor = executor;
		this.executionWaitTime = executionWaitTime;
		this.killThreadAfter = killThreadAfter;
		this.executingTasks = new ConcurrentHashMap<>();
		this.sendingQueue = new ConcurrentLinkedQueue<>();
		this.messagesOrder = new ConcurrentHashMap<>();
		this.isValid = new AtomicBoolean(true);
		this.taskIds = new ArrayList<>(2);
		
		taskIds.add(packetSender());
		taskIds.add(monitor());
	}

	public synchronized void execute(Runnable runnable, Player player, UUID id) {
		messagesOrder.putIfAbsent(player.getUniqueId(), new ConcurrentLinkedQueue<>());
		Queue<MessageOrderInfo> queue = messagesOrder.get(player.getUniqueId());
		queue.add(new MessageOrderInfo(id, System.currentTimeMillis()));
		Future<?> future = executor.submit(runnable);
		executingTasks.put(future, new ExecutingTaskData(System.currentTimeMillis(), player.getUniqueId(), id));
	}
	
	public void send(PacketContainer packet, Player player, UUID id) {
		OutboundPacket outboundPacket = new OutboundPacket(player, packet);
		Queue<MessageOrderInfo> queue = messagesOrder.get(player.getUniqueId());
		if (queue == null) {
			sendingQueue.add(outboundPacket);
		} else {
			Optional<MessageOrderInfo> optInfo = Optional.empty();
			long timeout = executionWaitTime.get();
			while (true) {
				optInfo = queue.stream().filter(each -> each.getId().equals(id)).findFirst();
				MessageOrderInfo head = queue.peek();
				if (optInfo.isPresent()) {
					try {
						Awaitility.await().pollDelay(0, TimeUnit.NANOSECONDS).pollInterval(10000, TimeUnit.NANOSECONDS).atMost(timeout, TimeUnit.MILLISECONDS).until(() -> queue.peek() == null || queue.peek().getId().equals(id));
						break;
					} catch (ConditionTimeoutException e) {
						queue.remove(head);
						timeout = RE_WAIT_TIME;
					}
				} else {
					break;
				}
			}
			sendingQueue.add(outboundPacket);
			optInfo.ifPresent(each -> queue.remove(each));
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
	
	private int monitor() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
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
				entry.getValue().removeIf(each -> each.getTime() + executionWaitTime.get() < time);
				if (Bukkit.getPlayer(entry.getKey()) == null && entry.getValue().isEmpty()) {
					itr1.remove();
				}
			}
		}, 0, 20).getTaskId();
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
		
	}

}
