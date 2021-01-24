package com.loohp.interactivechat.Utils;

public enum MCVersion {
	
	V1_16_4("1.16.4", 14),
	V1_16_2("1.16.2", 13),
	V1_16("1.16", 12),
	V1_15("1.15", 11),
	V1_14("1.14", 10),
	V1_13_1("1.13.1", 9),
	V1_13("1.13", 8),
	V1_12("1.12", 7),
	V1_11("1.11", 6),
	V1_10("1.10", 5),
	V1_9_4("1.9.4", 4),
	V1_9("1.9", 3),
	V1_8_4("1.8.4", 2),
	V1_8_3("1.8.3", 1),
	V1_8("1.8", 0),
	UNSUPPORTED("Unsupported", -1);
	
	private String name;
	private int shortNum;
	
	MCVersion(String name, int shortNum) {
		this.name = name;
		this.shortNum = shortNum;
	}
	
	public static MCVersion fromPackageName(String packageName) {
		if (packageName.contains("1_16_R3")) {
			return V1_16_4;
		} else if (packageName.contains("1_16_R2")) {
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
        	return UNSUPPORTED;
        }
	}
	
	public static MCVersion fromNumber(int number) {
		for (MCVersion version : values()) {
			if (version.shortNum == number) {
				return version;
			}
		}
		return UNSUPPORTED;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getNumber() {
		return shortNum;
	}
	
	public int compareWith(MCVersion version) {
		return this.shortNum - version.shortNum;
	}
	
	public boolean isOlderThan(MCVersion version) {
		return compareWith(version) < 0;
	}
	
	public boolean isOlderOrEqualTo(MCVersion version) {
		return compareWith(version) <= 0;
	}
	
	public boolean isNewerThan(MCVersion version) {
		return compareWith(version) > 0;
	}
	
	public boolean isNewerOrEqualTo(MCVersion version) {
		return compareWith(version) >= 0;
	}
	
	public boolean isBetweenInclusively(MCVersion v1, MCVersion v2) {
		int difference = v1.compareWith(v2);
		if (difference == 0) {
			return this.equals(v1);
		} else if (difference < 0) {
			return this.isNewerOrEqualTo(v1) && this.isOlderOrEqualTo(v2);
		} else {
			return this.isNewerOrEqualTo(v2) && this.isOlderOrEqualTo(v1);
		}
	}
	
	public boolean isLegacy() {
		return isOlderOrEqualTo(V1_12);
	}
	
	public boolean isOld() {
		return isOlderOrEqualTo(V1_8_4);
	}
	
	public boolean isSupported() {
		return this.shortNum >= 0;
	}

}
