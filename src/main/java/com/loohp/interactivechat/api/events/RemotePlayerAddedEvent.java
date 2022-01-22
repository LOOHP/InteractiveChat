package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;

/**
 * Called when a remote player is added
 *
 * @author LOOHP
 */
public class RemotePlayerAddedEvent extends RemotePlayerEvent {

    public RemotePlayerAddedEvent(ICPlayer player) {
        super(player);
    }

}
