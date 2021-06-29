package com.loohp.interactivechat.utils.bossbar;

import java.util.Set;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.MCVersion;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;

public abstract class BossBarUpdater implements BossBar.Listener, AutoCloseable {
	
	protected BossBar bossbar;
	protected Player[] players;
	protected boolean isValid;
	
	public static BossBarUpdater update(BossBar bossbar, Player... players) {
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			return new BossBarUpdaterV1_16Impl(bossbar, players);
		} else if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_9)) {
			return new BossBarUpdaterV1_9Impl(bossbar, players);
		} else {
			throw new UnsupportedOperationException("Bossbars are not supported on this version of Minecraft.");
		}
	}
	
	public BossBarUpdater(BossBar bossbar, Player... players) {
		bossbar.addListener(this);
		this.bossbar = bossbar;
		this.players = players;
		this.isValid = true;
	}
	
	@Override
	public synchronized void close() {
		bossbar.removeListener(this);
		isValid = false;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public BossBar getBossBar() {
		return bossbar;
	}
	
	@Override
	public abstract void bossBarNameChanged(BossBar bar, Component oldName, Component newName);
	
	@Override
    public abstract void bossBarProgressChanged(BossBar bar, float oldProgress, float newProgress);

	@Override
    public abstract void bossBarColorChanged(BossBar bar, Color oldColor, Color newColor);

	@Override
    public abstract void bossBarOverlayChanged(BossBar bar, Overlay oldOverlay, Overlay newOverlay);

	@Override
    public abstract void bossBarFlagsChanged(BossBar bar, Set<Flag> flagsAdded, Set<Flag> flagsRemoved);

}
