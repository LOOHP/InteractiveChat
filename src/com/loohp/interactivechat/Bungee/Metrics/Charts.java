package com.loohp.interactivechat.Bungee.Metrics;

import java.util.concurrent.Callable;

import com.loohp.interactivechat.Bungee.InteractiveChatBungee;

public class Charts {
	
	public static void setup(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_plugin_messages_relayed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	long amount = InteractiveChatBungee.pluginMessagesCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));
		
	}

}
