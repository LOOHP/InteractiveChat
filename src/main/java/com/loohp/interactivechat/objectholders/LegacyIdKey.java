package com.loohp.interactivechat.objectholders;

public class LegacyIdKey {

    private byte id;
    private short damage;

    public LegacyIdKey(byte id, short damage) {
        this.id = id;
        this.damage = damage;
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public short getDamage() {
        return damage;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    public boolean isDamageDataValue() {
        return damage <= Byte.MAX_VALUE;
    }

}
