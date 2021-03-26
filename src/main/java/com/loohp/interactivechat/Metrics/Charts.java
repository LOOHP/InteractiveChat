package com.loohp.interactivechat.Metrics;

import java.util.concurrent.Callable;

import com.loohp.interactivechat.InteractiveChat;

public class Charts {
	
	public static void setup(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_placeholders", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return InteractiveChat.placeholderList.size();
            }
        }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_amount_of_messages_processing_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	long amount = InteractiveChat.messagesCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

		metrics.addCustomChart(new Metrics.SimplePie("bungeecord_mode", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.bungeecordMode ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("accurate_sender_parser_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.useAccurateSenderFinder ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("item_display_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.useItem ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("inventory_display_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.useInventory ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("inventory_display_layout", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	if (!InteractiveChat.useInventory) {
	        		return "Disabled";
	        	} else {
	        		return "Layout " + InteractiveChat.invDisplayLayout;
	        	}
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("enderchest_display_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.useEnder ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("clickable_command_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.clickableCommands ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("player_name_info_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.usePlayerName ? "Enabled" : "Disabled";
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("mention_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChat.allowMention ? "Enabled" : "Disabled";
	        }
	    }));
	}

}
