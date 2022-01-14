package com.loohp.interactivechat.objectholders;

public class WrappedString {
	
	private String string;	

	public WrappedString(String string) {
		this.string = string;
	}
	
	public WrappedString() {
		this("");
	}

	public String getString() {
		return string;
	}

	public String setString(String string) {
		return this.string = string;
	}
	
	@Override
	public String toString() {
		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WrappedString)) {
			return false;
		}
		WrappedString other = (WrappedString) obj;
		if (string == null) {
			if (other.string != null) {
				return false;
			}
		} else if (!string.equals(other.string)) {
			return false;
		}
		return true;
	}

}
