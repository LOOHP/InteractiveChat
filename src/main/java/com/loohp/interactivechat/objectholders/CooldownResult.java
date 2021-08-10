package com.loohp.interactivechat.objectholders;

public class CooldownResult {
	
	private CooldownOutcome outcome;
	private long cooldownExpireTime;
	private ICPlaceholder placeholder;
	
	public CooldownResult(CooldownOutcome outcome, long cooldownExpireTime, ICPlaceholder placeholder) {
		this.outcome = outcome;
		this.cooldownExpireTime = cooldownExpireTime;
		this.placeholder = placeholder;
	}

	public CooldownOutcome getOutcome() {
		return outcome;
	}

	public long getCooldownExpireTime() {
		return cooldownExpireTime;
	}
	
	public ICPlaceholder getPlaceholder() {
		return placeholder;
	}

	public static enum CooldownOutcome {
		
		ALLOW(true), 
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
