package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;

/**
 * Called when a remote player is remove
 *
 * @author LOOHP
 */
public class RemotePlayerRemovedEvent extends RemotePlayerEvent {

    public RemotePlayerRemovedEvent(ICPlayer player) {
        super(player);
    }

}
