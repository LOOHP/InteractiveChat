package com.loohp.interactivechat.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class CollectionUtils {
	
	public static <T> void filter(Collection<T> collection, Predicate<T> predicate) {
		Iterator<T> itr = collection.iterator();
		while (itr.hasNext()) {
			if (!predicate.test(itr.next())) {
				itr.remove();
			}
		}
	}

}
