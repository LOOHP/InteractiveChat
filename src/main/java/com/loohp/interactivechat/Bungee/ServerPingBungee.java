package com.loohp.interactivechat.Bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loohp.interactivechat.Utils.DataStreamIO;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.chat.ComponentSerializer;

public class ServerPingBungee {
	
	public static final String INTERACTIVECHAT_PROTOCOL_IDENTIFIER = "InterativeChatBungeePing";
	
	public static CompletableFuture<ServerPingBungee> getPing(ServerInfo server) {
		CompletableFuture<ServerPingBungee> future = new CompletableFuture<>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (Socket socket = new Socket()) {
					socket.connect(server.getSocketAddress(), 1500);
					DataInputStream input = new DataInputStream(socket.getInputStream());
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(buffer);
					out.writeByte(0x00);
					DataStreamIO.writeVarInt(out, -1);
					DataStreamIO.writeString(out, "InterativeChatBungeePing", StandardCharsets.UTF_8);
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
				    try {
					    JSONObject json = (JSONObject) new JSONParser().parse(jsonStr);
					    JSONObject description = (JSONObject) json.get("description");
					    String descriptionAsStr = ComponentSerializer.parse(description.toJSONString())[0].toPlainText();
					    JSONObject data = (JSONObject) new JSONParser().parse(descriptionAsStr);
					    present = (boolean) data.get("present");
				    } catch (ParseException e) {
				    	present = false;
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

				    if (packetId == -1) {
				        throw new IOException("Premature end of stream.");
				    }

				    if (packetId != 0x01) { //we want a pong
				        throw new IOException("Invalid packetID " + Integer.toHexString(packetId));
				    }				    			 

					future.complete(new ServerPingBungee((int) (System.currentTimeMillis() - start), present));
				} catch (IOException e) {
					future.complete(new ServerPingBungee(-1, false));
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
