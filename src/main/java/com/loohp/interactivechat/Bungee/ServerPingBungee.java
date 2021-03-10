package com.loohp.interactivechat.Bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.text.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.loohp.interactivechat.Registry.Registry;
import com.loohp.interactivechat.Utils.DataStreamIO;
import com.loohp.interactivechat.Utils.MCVersion;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.chat.ComponentSerializer;

public class ServerPingBungee {
	
	public static final String INTERACTIVECHAT_PROTOCOL_IDENTIFIER = "InterativeChatBungeePing";
	public static final String UNKNOWN_VERSION = "unknown";
	public static final int UNKNOWN_PROTOCOL = -1;
	
	public static CompletableFuture<ServerPingBungee> getPing(ServerInfo server) {
		CompletableFuture<ServerPingBungee> future = new CompletableFuture<>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (Socket socket = new Socket()) {
					socket.connect(server.getSocketAddress(), 1500);
					
					if (socket.isConnected()) {
						DataInputStream input = new DataInputStream(socket.getInputStream());
						DataOutputStream output = new DataOutputStream(socket.getOutputStream());
						
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(buffer);
						out.writeByte(0x00);
						DataStreamIO.writeVarInt(out, -1);
						DataStreamIO.writeString(out, Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER, StandardCharsets.UTF_8);
						out.writeShort(25566);
						DataStreamIO.writeVarInt(out, 1);
						byte[] handshake = buffer.toByteArray();
						DataStreamIO.writeVarInt(output, handshake.length);
						output.write(handshake);
						
						buffer = new ByteArrayOutputStream();
						out = new DataOutputStream(buffer);
						out.writeByte(0x00);
						byte[] request = buffer.toByteArray();
						DataStreamIO.writeVarInt(output, request.length);
						output.write(request);
						
						DataStreamIO.readVarInt(input);
					    int packetId = DataStreamIO.readVarInt(input);
	
					    if (packetId == -1) {
					        throw new IOException("Premature end of stream.");
					    }
	
					    if (packetId != 0x00) { //we want a status response
					        throw new IOException("Invalid packetID " + Integer.toHexString(packetId));
					    }
					    int length = DataStreamIO.readVarInt(input); //length of json string
	
					    if (length == -1) {
					        throw new IOException("Premature end of stream.");
					    }
	
					    if (length == 0) {
					        throw new IOException("Invalid string length.");
					    }
	
					    byte[] in = new byte[length];
					    input.readFully(in);
					    String jsonStr = new String(in);
					    
					    boolean present;
					    String version;
					    MCVersion minecraftVersion;
					    String exactMinecraftVersion;
					    int protocol;
					    try {
						    JSONObject json = (JSONObject) new JSONParser().parse(jsonStr);
						    Object descriptionObj = json.get("description");
						    JSONObject data;
						    if (descriptionObj instanceof JSONObject) {
						    	JSONObject description = (JSONObject) json.get("description");
							    String descriptionAsStr = ComponentSerializer.parse(description.toJSONString())[0].toPlainText();
							    data = (JSONObject) new JSONParser().parse(descriptionAsStr);
						    } else {
						    	data = (JSONObject) new JSONParser().parse(StringEscapeUtils.unescapeJava(descriptionObj.toString()));
						    }
						    present = (boolean) data.get("present");
						    version = (String) data.get("version");
						    minecraftVersion = MCVersion.fromNumber((int) (long) data.get("minecraftVersion"));
						    exactMinecraftVersion = (String) data.get("exactMinecraftVersion");
						    protocol = data.containsKey("protocol") ? (int) (long) data.get("protocol") : 0;
					    } catch (Exception e) {
					    	present = false;
					    	version = UNKNOWN_VERSION;
					    	minecraftVersion = MCVersion.UNSUPPORTED;
					    	exactMinecraftVersion = UNKNOWN_VERSION;
					    	protocol = UNKNOWN_PROTOCOL;
					    }
					    
					    long start = System.currentTimeMillis();
					    
					    buffer = new ByteArrayOutputStream();
						out = new DataOutputStream(buffer);
						out.writeByte(0x01);
						out.writeLong(start);
						byte[] ping = buffer.toByteArray();
						DataStreamIO.writeVarInt(output, ping.length);
						output.write(ping);
						
						DataStreamIO.readVarInt(input);
					    packetId = DataStreamIO.readVarInt(input);
					    
					    int pong = (int) (System.currentTimeMillis() - start);
	
					    if (packetId == -1) {
					        throw new IOException("Premature end of stream.");
					    }
	
					    if (packetId != 0x01) { //we want a pong
					        throw new IOException("Invalid packetID " + Integer.toHexString(packetId));
					    }				    		
	
						future.complete(new ServerPingBungee(pong, present));
						
						BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
						if (data == null) {
							InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), true, present, version, minecraftVersion, exactMinecraftVersion, pong, protocol));
						} else {
							data.setPing(pong);
							if (!data.isOnline()) {
								data.setOnline(true);
							}
							if (data.hasInteractiveChat() != present) {
								data.setInteractiveChat(present);
							}
							if (!data.getVersion().equals(version)) {
								data.setVersion(version);
							}
							if (!data.getMinecraftVersion().equals(minecraftVersion)) {
								data.setMinecraftVersion(minecraftVersion);
							}
							if (!data.getExactMinecraftVersion().equals(exactMinecraftVersion)) {
								data.setExactMinecraftVersion(exactMinecraftVersion);
							}
							if (data.getProtocolVersion() != protocol) {
								data.setProtocolVersion(protocol);
							}
						}
					} else {
						future.complete(new ServerPingBungee(-1, false));
						
						BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
						if (data == null) {
							InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), false, false, UNKNOWN_VERSION, MCVersion.UNSUPPORTED, UNKNOWN_VERSION, -1, UNKNOWN_PROTOCOL));
						} else {
							data.setPing(-1);
							if (data.isOnline()) {
								data.setOnline(false);
							}
							if (data.hasInteractiveChat()) {
								data.setInteractiveChat(false);
							}
							if (!data.getVersion().equals(UNKNOWN_VERSION)) {
								data.setVersion(UNKNOWN_VERSION);
							}
							if (data.getProtocolVersion() != UNKNOWN_PROTOCOL) {
								data.setProtocolVersion(UNKNOWN_PROTOCOL);
							}
						}
					}
				} catch (IOException e) {
					future.complete(new ServerPingBungee(-1, false));
					
					BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
					if (data == null) {
						InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), false, false, UNKNOWN_VERSION, MCVersion.UNSUPPORTED, UNKNOWN_VERSION, -1, UNKNOWN_PROTOCOL));
					} else {
						data.setPing(-1);
						if (data.isOnline()) {
							data.setOnline(false);
						}
						if (data.hasInteractiveChat()) {
							data.setInteractiveChat(false);
						}
						if (!data.getVersion().equals(UNKNOWN_VERSION)) {
							data.setVersion(UNKNOWN_VERSION);
						}
						if (data.getProtocolVersion() != UNKNOWN_PROTOCOL) {
							data.setProtocolVersion(UNKNOWN_PROTOCOL);
						}
					}
				}
			}	
		}).start();
		return future;
	}
	
	//=====================
	
	private int ping;
	private boolean hasInteractiveChat;
	
	private ServerPingBungee(int ping, boolean hasInteractiveChat) {
		this.ping = ping;
		this.hasInteractiveChat = hasInteractiveChat;
	}
	
	public int getPing() {
		return ping;
	}
	
	public boolean hasInteractiveChat() {
		return hasInteractiveChat;
	}

}
