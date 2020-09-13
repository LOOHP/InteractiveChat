package com.loohp.interactivechat.Bungee;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.Bungee.Metrics.Charts;
import com.loohp.interactivechat.Bungee.Metrics.Metrics;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.CustomArrayUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class InteractiveChatBungee extends Plugin implements Listener {
	
	public static Plugin plugin;
	public static Metrics metrics;
	private static Random random = new Random();
	public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
	
	@Override
    public void onEnable() {
		plugin = this;
		
		getProxy().registerChannel("interchat:main");		   
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info(ChatColor.GREEN + "[InteractiveChat] Registered Plugin Messaging Channels!");
        
        metrics = new Metrics(plugin, 8839);
        Charts.setup(metrics);
        
        run();
	    
		getLogger().info(ChatColor.GREEN + "[InteractiveChat] InteractiveChatBungee has been enabled!");
    }
	
	@Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "[InteractiveChat] InteractiveChatBungee has been disabled!");
    }
	
	private void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					sendPlayerListData();
					sendDelay();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, 10000);
	}
	
    @EventHandler
    public void onReceive(PluginMessageEvent event) {
        if (!event.getTag().equals("interchat:main")) {
            return;
        }

        SocketAddress senderServer = event.getSender().getSocketAddress();

        for (ServerInfo server : getProxy().getServers().values()) {
            if (!server.getSocketAddress().equals(senderServer) && server.getPlayers().size() > 0) {
                server.sendData("interchat:main", event.getData());
            }
        }
    }
    
    @EventHandler
    public void onJoin(PostLoginEvent event) {
    	try {
			sendPlayerListData();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
    	new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					sendPlayerListData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	}, 1000);
    }
    
    private void sendPlayerListData() throws IOException {
    	ByteArrayDataOutput output = ByteStreams.newDataOutput();
    	Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
    	output.writeInt(players.size());
    	for (ProxiedPlayer player : players) {
    		DataTypeIO.writeUUID(output, player.getUniqueId());
    		DataTypeIO.writeString(output, player.getDisplayName(), StandardCharsets.UTF_8);
    	}
    	
    	int packetNumber = random.nextInt();
    	int packetId = 0x00;
    	byte[] data = output.toByteArray();
		
		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);
		
		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);
			
	        out.writeShort(packetId);
	        out.writeBoolean(i == (dataArray.length - 1));
	        
	        out.write(chunk);
	        
	        for (ServerInfo server : getProxy().getServers().values()) {
	            server.sendData("interchat:main", out.toByteArray());
	        }
		}
    }
    
    private void sendDelay() throws IOException {
    	ByteArrayDataOutput output = ByteStreams.newDataOutput();
    	
    	List<CompletableFuture<Integer>> futures = new LinkedList<>();
    	
    	for (ServerInfo server : getProxy().getServers().values()) {
    		futures.add(getPing(server));
        }
    	int highestPing = futures.stream().mapToInt(each -> {
			try {
				return each.get();
			} catch (InterruptedException | ExecutionException e) {
				return 0;
			}
		}).max().orElse(0);
    	
    	output.writeInt(highestPing * 2 + 200);
    	
    	int packetNumber = random.nextInt();
    	int packetId = 0x01;
    	byte[] data = output.toByteArray();
		
		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);
		
		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);
			
	        out.writeShort(packetId);
	        out.writeBoolean(i == (dataArray.length - 1));
	        
	        out.write(chunk);
	        
	        for (ServerInfo server : getProxy().getServers().values()) {
	            server.sendData("interchat:main", out.toByteArray());
	        }
		}
    }
    
    private CompletableFuture<Integer> getPing(ServerInfo server) {
    	CompletableFuture<Integer> future = new CompletableFuture<>();
    	long start = System.currentTimeMillis();
    	Callback<ServerPing> callback = new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (error == null) {
					future.complete((int) (System.currentTimeMillis() - start));
				} else {
					future.complete(0);
				}
			}
    	};
    	server.ping(callback);
    	return future;
    }
}