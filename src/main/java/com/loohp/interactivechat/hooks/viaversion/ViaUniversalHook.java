package com.loohp.interactivechat.hooks.viaversion;

import java.util.UUID;

import com.loohp.interactivechat.InteractiveChat;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;

public class ViaUniversalHook {
	
	public static void reducePacketPerSecondSent(UUID uuid, long reduction) {
		if (InteractiveChat.viaVersionHook) {
			UserConnection connection = Via.getManager().getConnection(uuid);
			if (connection != null) {
				long sentPackets = connection.getSentPackets() - reduction;
				long intervalPackets = connection.getIntervalPackets() - reduction;
				connection.setSentPackets(Math.max(sentPackets, 0));
				connection.setIntervalPackets(Math.max(intervalPackets, 0));
			}
		}
	}
	
	public static void reducePacketPerSecondReceived(UUID uuid, long reduction) {
		if (InteractiveChat.viaVersionHook) {
			UserConnection connection = Via.getManager().getConnection(uuid);
			if (connection != null) {
				long receivedPackets = connection.getReceivedPackets() - reduction;
				long intervalPackets = connection.getIntervalPackets() - reduction;
				connection.setSentPackets(Math.max(receivedPackets, 0));
				connection.setIntervalPackets(Math.max(intervalPackets, 0));
			}
		}
	}

}
