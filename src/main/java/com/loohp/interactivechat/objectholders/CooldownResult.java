package com.loohp.interactivechat.objectholders;

public class CooldownResult {
	
	private CooldownOutcome outcome;
	private long now;
	private long cooldownExpireTime;
	private ICPlaceholder placeholder;
	
	public CooldownResult(CooldownOutcome outcome, long now, long cooldownExpireTime, ICPlaceholder placeholder) {
		this.outcome = outcome;
		this.now = now;
		this.cooldownExpireTime = cooldownExpireTime;
		this.placeholder = placeholder;
	}

	public CooldownOutcome getOutcome() {
		return outcome;
	}
	
	public long getTimeNow() {
		return now;
	}

	public long getCooldownExpireTime() {
		return cooldownExpireTime;
	}
	
	public ICPlaceholder getPlaceholder() {
		return placeholder;
	}

	public static enum CooldownOutcome {
		
		ALLOW(true), 
		ALLOW_BYPASS(true),
		DENY_PLACEHOLDER(false), 
		DENY_UNIVERSAL(false);
		
		private boolean allowed;
		
		CooldownOutcome(boolean allowed) {
			this.allowed = allowed;
		}
		
		public boolean isAllowed() {
			return allowed;
		}

	}
	
}
