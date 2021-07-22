package com.IanSloat.noodlebot.controllers.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A modified {@linkplain ArrayList} that contains additional methods useful for
 * gathering information about the {@linkplain GuildPermission} objects
 * contained within the list.
 */
public class GuildPermissions implements List<GuildPermission> {

	private ArrayList<GuildPermission> storage = new ArrayList<GuildPermission>();

	/**
	 * Checks whether this list contains a {@linkplain GuildPermission} with a
	 * specific key
	 * 
	 * @param key The key to check for
	 * @return True if the key was found in this list
	 */
	public boolean contains(String key) {
		boolean result = false;
		for (GuildPermission s : this) {
			if (s.getKey().equals(key)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Retrieves a permission entry from this list by key
	 * 
	 * @param key The key to search for
	 * @return The permission entry that contains the specified key, or null if no
	 *         entry contains this key
	 */
	public GuildPermission retrieveByKey(String key) {
		GuildPermission result = null;
		for (GuildPermission p : this) {
			if (p.getKey().equals(key)) {
				result = p;
				break;
			}
		}
		return result;
	}

	/**
	 * Retrieves every key found within this list
	 * 
	 * @return A {@linkplain List} of strings representing each key found within
	 *         this list
	 */
	public List<String> getKeys() {
		List<String> result = new ArrayList<String>();
		for (GuildPermission s : this) {
			if (!result.contains(s.getKey()))
				result.add(s.getKey());
		}
		return result;
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return storage.contains(o);
	}

	@Override
	public Iterator<GuildPermission> iterator() {
		return storage.iterator();
	}

	@Override
	public Object[] toArray() {
		return storage.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return storage.toArray(a);
	}

	@Override
	public boolean add(GuildPermission e) {
		return storage.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return storage.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return storage.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends GuildPermission> c) {
		return storage.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends GuildPermission> c) {
		return storage.addAll(index, c);
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
	public void clear() {
		storage.clear();
	}

	@Override
	public GuildPermission get(int index) {
		return storage.get(index);
	}

	@Override
	public GuildPermission set(int index, GuildPermission element) {
		return storage.set(index, element);
	}

	@Override
	public void add(int index, GuildPermission element) {
		storage.add(index, element);
	}

	@Override
	public GuildPermission remove(int index) {
		return storage.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return storage.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return storage.lastIndexOf(o);
	}

	@Override
	public ListIterator<GuildPermission> listIterator() {
		return storage.listIterator();
	}

	@Override
	public ListIterator<GuildPermission> listIterator(int index) {
		return storage.listIterator(index);
	}

	@Override
	public List<GuildPermission> subList(int fromIndex, int toIndex) {
		return storage.subList(fromIndex, toIndex);
	}

}
