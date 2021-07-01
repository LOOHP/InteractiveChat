package com.loohp.interactivechat.proxy.objectholders;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

public class MessageForwardingHandler implements AutoCloseable {
	
	private Map<UUID, Queue<ForwardMessageInfo>> messageOrder;
	private Map<UUID, ForwardMessageInfo> messageData;
	private ExecutorService executor;
	private Queue<OutboundMessage> sendingQueue;
	private BiConsumer<ForwardMessageInfo, String> forwardForProcessing;
	private BiConsumer<ForwardMessageInfo, String> sendToPlayer;
	private Predicate<UUID> isPlayerOnline;
	private Predicate<UUID> hasInteractiveChatOnConnectedServer;
	private Supplier<Long> executionWaitTime;
	
	private AtomicBoolean isValid;
	
	public MessageForwardingHandler(ExecutorService executor, BiConsumer<ForwardMessageInfo, String> forwardForProcessing, BiConsumer<ForwardMessageInfo, String> sendToPlayer, Predicate<UUID> isPlayerOnline, Predicate<UUID> hasInteractiveChatOnConnectedServer, Supplier<Long> executionWaitTime) {
		this.isValid = new AtomicBoolean(true);
		this.messageOrder = new ConcurrentHashMap<>();
		this.messageData = new ConcurrentHashMap<>();
		this.executor = executor;
		this.sendingQueue = new ConcurrentLinkedQueue<>();
		this.forwardForProcessing = forwardForProcessing;
		this.sendToPlayer = sendToPlayer;
		this.isPlayerOnline = isPlayerOnline;
		this.hasInteractiveChatOnConnectedServer = hasInteractiveChatOnConnectedServer;
		this.executionWaitTime = executionWaitTime;
		
		packetSender();
		monitor();
	}
	
	public synchronized void processMessage(UUID player, String message, byte position) {
		UUID messageId = UUID.randomUUID();
		if (hasInteractiveChatOnConnectedServer.test(player)) {
			messageOrder.putIfAbsent(player, new ConcurrentLinkedQueue<>());
			Queue<ForwardMessageInfo> queue = messageOrder.get(player);
			ForwardMessageInfo forwardMessageInfo = new ForwardMessageInfo(messageId, player, position, System.currentTimeMillis());
			messageData.put(messageId, forwardMessageInfo);
			queue.add(forwardMessageInfo);
			forwardForProcessing.accept(forwardMessageInfo, message);
		} else {
			ForwardMessageInfo forwardMessageInfo = new ForwardMessageInfo(messageId, player, position, System.currentTimeMillis());
			sendToPlayer.accept(forwardMessageInfo, message);
		}
	}
	
	public void recievedProcessedMessage(UUID messageId, String message) {
		executor.execute(() -> {
			ForwardMessageInfo info = messageData.remove(messageId);
			if (info != null) {
				Queue<ForwardMessageInfo> queue = messageOrder.get(info.getPlayer());
				if (queue != null) {
					while (true) {
						ForwardMessageInfo head = queue.peek();
						if (queue.contains(info)) {
							try {
								Awaitility.await().pollDelay(0, TimeUnit.NANOSECONDS).pollInterval(10000, TimeUnit.NANOSECONDS).atMost(executionWaitTime.get(), TimeUnit.MILLISECONDS).until(() -> queue.peek() == null || queue.peek().equals(info));
								break;
							} catch (ConditionTimeoutException e) {
								queue.remove(head);
							}
						} else {
							break;
						}
					}
					sendingQueue.add(new OutboundMessage(info, message));
					queue.remove(info);
				}
			}
		});
	}
	
	public void clearPlayer(UUID player) {
		Queue<ForwardMessageInfo> messages = messageOrder.remove(player);
		if (messages != null) {
			for (ForwardMessageInfo info : messages) {
				messageData.remove(info.getId());
			}
		}
	}
	
	private void packetSender() {
		new Timer().schedule(new TimerTask() {	
			@Override
			public void run() {
				while (!sendingQueue.isEmpty()) {
					OutboundMessage out = sendingQueue.poll();
					if (isPlayerOnline.test(out.getInfo().getPlayer())) {
						sendToPlayer.accept(out.getInfo(), out.getMessage());
					}
				}
			}
		}, 0, 50);
	}
	
	private void monitor() {
		new Timer().schedule(new TimerTask() {	
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				for (Queue<ForwardMessageInfo> queue : messageOrder.values()) {
					queue.removeIf(each -> each.getTime() + executionWaitTime.get() < time);
				}
				Iterator<ForwardMessageInfo> itr0 = messageData.values().iterator();
				while (itr0.hasNext()) {
					ForwardMessageInfo id = itr0.next();
					if (id.getTime() + executionWaitTime.get() < time) {
						itr0.remove();
					}
				}
				
				for (UUID player : messageOrder.keySet()) {
					if (!isPlayerOnline.test(player)) {
						clearPlayer(player);
					}
				}
			}
		}, 0, 1000);
	}
	
	@Override
	public synchronized void close() throws Exception {
		isValid.set(false);
		executor.shutdown();
		executor.awaitTermination(6000, TimeUnit.MILLISECONDS);
	}
	
	public boolean isValid() {
		return isValid.get();
	}
	
	public static class ForwardMessageInfo {
		
		private UUID id;
		private UUID player;
		private byte position;
		private long time;
		
		public ForwardMessageInfo(UUID id, UUID player, byte position, long time) {
			this.id = id;
			this.player = player;
			this.position = position;
			this.time = time;
		}

		public UUID getId() {
			return id;
		}

		public UUID getPlayer() {
			return player;
		}

		public byte getPosition() {
			return position;
		}

		public long getTime() {
			return time;
		}
		
	}
	
	private static class OutboundMessage {
		
		private ForwardMessageInfo info;
		private String message;
		
		public OutboundMessage(ForwardMessageInfo info, String message) {
			this.info = info;
			this.message = message;
		}

		public ForwardMessageInfo getInfo() {
			return info;
		}

		public String getMessage() {
			return message;
		}
		
	}

}
