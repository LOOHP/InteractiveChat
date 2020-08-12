package com.loohp.interactivechat.Utils;

public enum MCVersion {
	
	V1_16_2("1.16.2"),
	V1_16("1.16"),
	V1_15("1.15"),
	V1_14("1.14"),
	V1_13_1("1.13.1"),
	V1_13("1.13"),
	V1_12("1.12", true),
	V1_11("1.11", true),
	V1_10("1.10", true),
	V1_9_4("1.9.4", true),
	V1_9("1.9", true),
	V1_8_4("1.8.4", true, true),
	V1_8_3("1.8.3", true, true),
	V1_8("1.8", true, true),
	OUTDATED("Outdated", true, true, true);
	
	String name;
	boolean legacy;
	boolean old;
	boolean unsupported;
	
	MCVersion(String name) {
		this.name = name;
		this.legacy = false;
		this.old = false;
		this.unsupported = false;
	}
	
	MCVersion(String name, boolean legacy) {
		this.name = name;
		this.legacy = legacy;
		this.old = false;
		this.unsupported = false;
	}

	MCVersion(String name, boolean legacy, boolean old) {
		this.name = name;
		this.legacy = legacy;
		this.old = old;
		this.unsupported = false;
	}
	
	MCVersion(String name, boolean legacy, boolean old, boolean unsupported) {
		this.name = name;
		this.legacy = legacy;
		this.old = old;
		this.unsupported = unsupported;
	}
	
	public static MCVersion fromPackageName(String packageName) {
		if (packageName.contains("1_16_R2")) {
			return V1_16_2;
		} else if (packageName.contains("1_16_R1")) {
			return V1_16;
		} else if (packageName.contains("1_15_R1")) {
			return V1_15;
        } else if (packageName.contains("1_14_R1")) {
            return V1_14;
        } else if (packageName.contains("1_13_R2")) {
            return V1_13_1;
        } else if (packageName.contains("1_13_R1")) {
            return V1_13;
        } else if (packageName.contains("1_12_R1")) {
            return V1_12;
        } else if (packageName.contains("1_11_R1")) {
        	return V1_11;
        } else if (packageName.contains("1_10_R1")) {
        	return V1_10;
        } else if (packageName.contains("1_9_R2")) {
        	return V1_9_4;
        } else if (packageName.contains("1_9_R1")) {
        	return V1_9;
        } else if (packageName.contains("1_8_R3")) {
        	return V1_8_4;
        } else if (packageName.contains("1_8_R2")) {
        	return V1_8_3;
        } else if (packageName.contains("1_8_R1")) {
        	return V1_8;
        } else {
        	return OUTDATED;
        }
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isLegacy() {
		return legacy;
	}
	
	public boolean isOld() {
		return old;
	}
	
	public boolean isSupported() {
		return !unsupported;
	}
	
	public boolean isPost1_16() {
		return this.ordinal() <= MCVersion.V1_16.ordinal();
	}

}
