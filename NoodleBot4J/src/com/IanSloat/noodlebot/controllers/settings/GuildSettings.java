package com.IanSloat.noodlebot.controllers.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A modified {@linkplain ArrayList} that contains additional methods useful for
 * gathering information about the {@linkplain GuildSetting} objects contained
 * within the list.
 */
public class GuildSettings implements List<GuildSetting> {

	private ArrayList<GuildSetting> storage = new ArrayList<GuildSetting>();

	/**
	 * Checks whether this list contains a {@linkplain GuildSetting} with a specific
	 * key
	 * 
	 * @param key The key to check for
	 * @return True if the key was found in this list
	 */
	public boolean contains(String key) {
		boolean result = false;
		for (GuildSetting s : this) {
			if (s.getKey().equals(key)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Retrieves every key found within this list
	 * 
	 * @return A {@linkplain list} of strings representing each key found within
	 *         this list
	 */
	public List<String> getKeys() {
		List<String> result = new ArrayList<String>();
		for (GuildSetting s : this) {
			if (!result.contains(s.getKey()))
				result.add(s.getKey());
		}
		return result;
	}

	/**
	 * Retrieves every setting category found within this list
	 * 
	 * @return A {@linkplain list} of strings representing each setting category
	 *         found within this list
	 */
	public List<String> getCategories() {
		List<String> result = new ArrayList<String>();
		for (GuildSetting s : this) {
			if (!result.contains(s.getCategory()))
				result.add(s.getCategory());
		}
		return result;
	}

	/**
	 * Sorts all the entries in this list alphabetically
	 */
	public void sortAlphabetically() {
		Map<String, GuildSettings> bucket = new HashMap<>();
		List<String> categories = this.getCategories();
		Collections.sort(categories);
		categories.forEach(k -> bucket.put(k, new GuildSettings()));
		this.forEach(s -> bucket.get(s.getCategory()).add(s));
		bucket.forEach((cat, st) -> st.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle())));
		this.clear();
		bucket.forEach((cat, st) -> this.addAll(st));
	}

	@Override
	public boolean add(GuildSetting e) {
		return storage.add(e);
	}

	@Override
	public void add(int index, GuildSetting element) {
		storage.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends GuildSetting> c) {
		return storage.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends GuildSetting> c) {
		return storage.addAll(index, c);
	}

	@Override
	public void clear() {
		storage.clear();
	}

	@Override
	public boolean contains(Object o) {
		return storage.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return storage.containsAll(c);
	}

	@Override
	public GuildSetting get(int index) {
		return storage.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return storage.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public Iterator<GuildSetting> iterator() {
		return storage.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return storage.lastIndexOf(o);
	}

	@Override
	public ListIterator<GuildSetting> listIterator() {
		return storage.listIterator();
	}

	@Override
	public ListIterator<GuildSetting> listIterator(int index) {
		return storage.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return storage.remove(o);
	}

	@Override
	public GuildSetting remove(int index) {
		return storage.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return storage.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return storage.retainAll(c);
	}

	@Override
	public GuildSetting set(int index, GuildSetting element) {
		return storage.set(index, element);
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public List<GuildSetting> subList(int fromIndex, int toIndex) {
		return storage.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return storage.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return storage.toArray(a);
	}

}
