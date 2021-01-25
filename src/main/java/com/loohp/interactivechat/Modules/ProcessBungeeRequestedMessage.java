package com.loohp.interactivechat.Modules;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsResult;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ProcessBungeeRequestedMessage {
	
	public static String processAndRespond(Player reciever, String component) {
		BaseComponent basecomponent = ChatComponentUtils.join(ComponentSerializer.parse(ChatColorUtils.filterIllegalColorCodes(component)));
		BaseComponent originalComponent = ChatComponentUtils.clone(basecomponent);
        
        try {
        	if (basecomponent.toLegacyText().equals("")) {
        		return component;
        	}
        } catch (Exception e) {
        	return component;
        }
        
        if ((InteractiveChat.version.isOld()) && JsonUtils.containsKey(ComponentSerializer.toString(basecomponent), "translate")) {		       
        	return component;
        }
        
        String rawMessageKey = basecomponent.toPlainText();
        if (!InteractiveChat.keyTime.containsKey(rawMessageKey)) {
        	InteractiveChat.keyTime.put(rawMessageKey, System.currentTimeMillis());
        }
        
        long unix = InteractiveChat.keyTime.get(rawMessageKey);
        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
        	InteractiveChat.cooldownbypass.put(unix, new HashSet<String>());
        }
        
        ProcessCommandsResult commandsender = ProcessCommands.process(basecomponent);
        Optional<PlayerWrapper> sender = Optional.empty();
        if (commandsender.getSender() != null) {
        	Player bukkitplayer = Bukkit.getPlayer(commandsender.getSender());
        	if (bukkitplayer != null) {
        		sender = Optional.of(new PlayerWrapper(bukkitplayer));
        	} else {
        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(commandsender.getSender()));
        	}
        }
        if (!sender.isPresent()) {
        	sender = SenderFinder.getSender(basecomponent, rawMessageKey);
        }
        basecomponent = commandsender.getBaseComponent();
        
        String text = basecomponent.toLegacyText();
        if (InteractiveChat.messageToIgnore.stream().anyMatch(each -> text.matches(each))) {
        	return component;
        }
        
        if (sender.isPresent()) {
        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
        }
        
        String server;
        if (sender.isPresent() && !sender.get().isLocal()) {
        	try {
				TimeUnit.MILLISECONDS.sleep(InteractiveChat.remoteDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	server = sender.get().getServer();
        } else {
        	server = PlayerWrapper.CURRENT_SERVER_REPRESENTATION;
        }
		
        if (InteractiveChat.usePlayerName) {
        	basecomponent = PlayernameDisplay.process(basecomponent, rawMessageKey, sender, unix);
        }
        
        if (InteractiveChat.AllowMention && sender.isPresent()) {
        	basecomponent = MentionDisplay.process(basecomponent, reciever, sender.get(), rawMessageKey, unix, !Bukkit.isPrimaryThread());
        }
        
        if (InteractiveChat.useItem) {
        	basecomponent = ItemDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
        }
        
        if (InteractiveChat.useInventory) {
        	basecomponent = InventoryDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
        }
        
        if (InteractiveChat.useEnder) {
        	basecomponent = EnderchestDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
        }
        
        List<ICPlaceholder> serverPlaceholderList = InteractiveChat.remotePlaceholderList.get(server);
        if (server.equals(PlayerWrapper.CURRENT_SERVER_REPRESENTATION) || serverPlaceholderList == null) {
        	serverPlaceholderList = InteractiveChat.placeholderList;
        }
        basecomponent = CustomPlaceholderDisplay.process(basecomponent, sender, reciever, rawMessageKey, serverPlaceholderList, unix);
        
        if (InteractiveChat.clickableCommands) {
        	basecomponent = CommandsDisplay.process(basecomponent);
        }
        
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
	        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 5))) {
	        	basecomponent = ChatComponentUtils.translatePluginFontFormatting(basecomponent);
	        }
        }
        
        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
        	InteractiveChat.keyTime.remove(rawMessageKey);
        	InteractiveChat.keyPlayer.remove(rawMessageKey);
        }, 5);
        		        
        basecomponent = InteractiveChat.filterUselessColorCodes ? ChatComponentUtils.cleanUpLegacyText(basecomponent, reciever) : ChatComponentUtils.respectClientColorSettingsWithoutCleanUp(basecomponent, reciever);       
        
        String json = ComponentSerializer.toString(basecomponent);
        if (InteractiveChat.sendOriginalIfTooLong && json.length() > 32767) {
        	String originalJson = ComponentSerializer.toString(originalComponent);
        	if (originalJson.length() > 32767) {
        		return "{\"text\":\"\"}";
        	} else {
        		return originalJson;
        	}
        }
        
		return json;
	}

}
