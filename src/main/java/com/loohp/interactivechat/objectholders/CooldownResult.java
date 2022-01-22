package com.loohp.interactivechat.objectholders;

public class CooldownResult {

    private final CooldownOutcome outcome;
    private final long now;
    private final long cooldownExpireTime;
    private final ICPlaceholder placeholder;

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

    public enum CooldownOutcome {

        ALLOW(true),
        ALLOW_BYPASS(true),
        DENY_PLACEHOLDER(false),
        DENY_UNIVERSAL(false);

        private final boolean allowed;

        CooldownOutcome(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

    }

}
