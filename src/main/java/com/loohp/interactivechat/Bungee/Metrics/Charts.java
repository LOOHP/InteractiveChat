package com.loohp.interactivechat.Bungee.Metrics;

import java.util.concurrent.Callable;

import com.loohp.interactivechat.Bungee.InteractiveChatBungee;

public class Charts {
	
	public static void setup(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_plugin_messages_relayed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	long amount = InteractiveChatBungee.pluginMessagesCounter.getAndSet(0);
                return (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("servers_managed_by_bungeecord_with_interactivechat", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	long amount = InteractiveChatBungee.getBackendInteractiveChatInfo().values().stream().filter(each -> each.hasInteractiveChat()).count();
            	return (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }));
		
		metrics.addCustomChart(new Metrics.SimplePie("accurate_sender_parser_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	return InteractiveChatBungee.useAccurateSenderFinder ? "Enabled" : "Disabled";
	        }
	    }));
		
	}

}
