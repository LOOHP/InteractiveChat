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
		
	}

}
