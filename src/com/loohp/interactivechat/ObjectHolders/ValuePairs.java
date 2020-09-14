package com.loohp.interactivechat.ObjectHolders;

public class ValuePairs<F, S> {
	
	private F first;
	private S second;
	
	public ValuePairs(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public F getFirst() {
		return first;
	}
	
	public S getSecond() {
		return second;
	}
	
}
