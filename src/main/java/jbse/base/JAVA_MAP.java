package jbse.base;

import static jbse.meta.Analysis.assume;
import static jbse.meta.Analysis.ignore;
import static jbse.meta.Analysis.isResolvedByExpansion;
import static jbse.meta.Analysis.isSymbolic;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class JAVA_MAP<K, V>  extends AbstractMap<K,V>
implements Map<K,V>, Cloneable, Serializable {

	private static final long serialVersionUID = 362498820763181265L; //same as HashMap

	private static abstract class Node { }

	private static class NodePair<KK, VV> extends Node {
		KK key;
		VV value;
		Node next;

		public int pairHashCode() {
			return (this.key == null ? 0 : this.key.hashCode()) ^
					(this.value == null ? 0 : this.value.hashCode());
		}
	}

	private static class NodeEmpty extends Node { }

	/**
	 * Caches whether this map is initial, i.e., whether it 
	 * represents the map as it was in the initial state.
	 */
	private boolean isInitial;

	/**
	 * Used only when isInitial == true; the hash code
	 * of the (initial) map.
	 */
	private final int initialHashCode;

	/** 
	 * When isInitial == true, contains the keys that are assumed 
	 * not to be in the map; When isInitial == false, contains the
	 * keys that were assumed to be in the initial map, but were 
	 * later removed (since they cannot be removed from this.initialMap).
	 */ 
	private ArrayList<K> absentKeys;

	/** 
	 * Used only when isInitial == true; values that are assumed 
	 * not to be in the map. 
	 */ 
	private ArrayList<V> absentValues;

	/** 
	 * Used only when isInitial == false; the initial map that backs this map, if 
	 * this map is concrete, otherwise it is set to null.
	 */
	private JAVA_MAP<K, V> initialMap;

	/**
	 * The size of the map.
	 */
	private int size;

	/**
	 * The list of key/value pairs in the map, 
	 * either added (noninitial map) or assumed (initial map).
	 */
	private Node root;

	/** 
	 * The number of nodes in root.(next)*, excluded the
	 * final NodeEmpty.
	 */
	private int numNodes;

	// Constructors

	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16


	public JAVA_MAP(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " +
					initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " +
					loadFactor);
		this.isInitial = false;
		this.initialHashCode = 0;
		this.absentKeys = new ArrayList<>();
		this.absentValues = null;
		this.initialMap = null;
		this.size = 0;
		this.root = new NodeEmpty();
		this.numNodes = 0;
	}

	public JAVA_MAP(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public JAVA_MAP() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public JAVA_MAP(Map<? extends K, ? extends V> m) {
		this();
		putAll(m);
	}

	// Query Operations

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return (size() == 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		//checks if the key is on the list of absent keys
		if (this.absentKeys.contains((K) key)) {
			return false;
		}

		//if not absent, checks in the nodes
		if (key == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.key == null) {
					return true;
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (key.equals(np.key)) {
					return true;
				}
			}
		}

		//if not in the nodes, there are three cases 
		//1- the map is initial: branch and recheck 
		if (this.isInitial) {
			refineOnKeyAndBranch((K) key);
			return containsKey(key); //after refinement it will be either in this.absentKeys or in this.root.(next)*
		}
		//2- the map is not initial and is backed by an initial
		//   map (it is symbolic): search in the initial map
		if (this.initialMap != null) {
			return this.initialMap.containsKey(key);
		}
		//3- otherwise (concrete map) it is not in the map
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsValue(Object value) {
		//checks if the value is in the list of absent values
		if (this.absentValues != null && this.absentValues.contains(value)) {
			return false;
		}

		//if not absent, checks in the nodes
		if (value == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.value == null) {
					return true;
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (value.equals(np.value)) {
					return true;
				}
			}
		}

		//if not in the nodes there are three possible cases: 
		//1- the map is initial: branches and rechecks
		if (this.isInitial) {
			refineOnValueAndBranch((V) value);
			return containsValue(value); //after refinement it will be either in this.absentValues or in this.root.(next)*
		}
		//2- the map is not initial and is backed by an initial
		//   map (it is symbolic): search in the initial map
		if (this.initialMap != null) {
			return this.initialMap.containsValue(value);
		}
		//3- otherwise (concrete map) it is not in the map
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		//checks if the key is in the list of absent keys
		if (this.absentKeys.contains((K) key)) {
			return null;
		}

		//if not absent, checks in the nodes
		if (key == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.key == null) {
					return np.value;
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (key.equals(np.key)) {
					return np.value;
				}
			}
		}

		//if not in the nodes there are three cases: 
		//1- the map is initial: branches and rechecks
		if (this.isInitial) {
			refineOnKeyAndBranch((K) key);
			return get(key); //after refinement it will be either in this.absentKeys or in this.root.(next)*
		}
		//2- the map is not initial and is backed by an initial
		//   map (it is symbolic): search in the initial map
		if (this.initialMap != null) {
			return this.initialMap.get(key);
		}
		//3- otherwise (concrete map) it is not in the map
		return null;
	}

	private void addNode(K key, V value) {
		this.absentKeys.remove(key);
		final NodePair<K, V> np = new NodePair<>();
		np.key = key;
		np.value = value;
		np.next = this.root;
		this.root = np;
		++this.numNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put a value in an initial map.");
		}

		//looks for a matching NodePair in this.root.(next)*
		NodePair<K, V> matchingPair = null;
		if (key == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.key == null) {
					matchingPair = np;
					break;
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (key.equals(np.key)) {
					matchingPair = np;
					break;
				}
			}
		}

		//no matching NodePair
		if (matchingPair == null) {
			if (this.initialMap == null) {
				//the map is concrete, so it did not contain the key
				//before this put operation: add the new mapping, 
				//adjust the size and return null
				addNode(key, value);
				++this.size;
				return null;
			} else {
				//the map is symbolic, so there are two cases: 
				//either the key was, or it was not, in the initial map. 
				//This decision could generate a branch in symbolic execution.

				//if the key surely is not in the initial map, add the new mapping, 
				//adjust the size and return null
				if (this.initialMap.absentKeys.contains(key)) {
					addNode(key, value);
					++this.size;
					return null;
				}

				//if the key surely is in the initial map, add the new mapping and 
				//return the value it had in the initial map
				if (key == null) {
					for (Node nInitial = this.initialMap.root; nInitial instanceof JAVA_MAP.NodePair; nInitial = ((JAVA_MAP.NodePair<K, V>) nInitial).next) {
						final NodePair<K, V> npInitial = (JAVA_MAP.NodePair<K, V>) nInitial;
						if (npInitial.key == null) {
							addNode(key, value);
							return npInitial.value;
						}
					}
				} else {
					for (Node nInitial = this.initialMap.root; nInitial instanceof JAVA_MAP.NodePair; nInitial = ((JAVA_MAP.NodePair<K, V>) nInitial).next) {
						final NodePair<K, V> npInitial = (JAVA_MAP.NodePair<K, V>) nInitial;
						if (key.equals(npInitial.key)) {
							addNode(key, value);
							return npInitial.value;
						}
					}
				}

				//else, branch and repeat put operation
				this.initialMap.refineOnKeyAndBranch((K) key);
				return put(key, value);
			}
		} else {
			//matching NodePair found: just update it and 
			//return its previous value
			final V retVal = matchingPair.value;
			matchingPair.value = value;
			return retVal;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to remove a value from an initial map.");
		}

		//if it is already absent, returns null
		if (this.absentKeys.contains(key)) {
			return null;
		}

		//looks for a matching NodePair in this.root.(next)*
		NodePair<K, V> matchingPairPrev = null, matchingPair = null;
		if (key == null) {
			for (Node nPrev = null, n = this.root; n instanceof JAVA_MAP.NodePair; nPrev = n, n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.key == null) {
					matchingPairPrev = (JAVA_MAP.NodePair<K, V>) nPrev;
					matchingPair = np;
					break;
				}
			}
		} else {
			for (Node nPrev = null, n = this.root; n instanceof JAVA_MAP.NodePair; nPrev = n, n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (key.equals(np.key)) {
					matchingPairPrev = (JAVA_MAP.NodePair<K, V>) nPrev;
					matchingPair = np;
					break;
				}
			}
		}

		//no matching NodePair
		if (matchingPair == null) {
			if (this.initialMap == null) {
				//the map is concrete, so it did not contain the key
				//before this remove operation: just return null
				return null;
			} else {
				//the map is symbolic, so there are two cases: 
				//either the key was, or it was not, in the initial map. 
				//This decision could generate a branch in symbolic execution.

				//if the key surely is not in the initial map, return null
				if (this.initialMap.absentKeys.contains((K) key)) {
					return null;
				}

				//if the key surely is in the initial map, adjust size and
				//return the associated value
				if (key == null) {
					for (Node nInitial = this.initialMap.root; nInitial instanceof JAVA_MAP.NodePair; nInitial = ((JAVA_MAP.NodePair<K, V>) nInitial).next) {
						final NodePair<K, V> npInitial = (JAVA_MAP.NodePair<K, V>) nInitial;
						if (npInitial.key == null) {
							this.absentKeys.add((K) key);						
							--this.size;
							return npInitial.value;
						}
					}
				} else {
					for (Node nInitial = this.initialMap.root; nInitial instanceof JAVA_MAP.NodePair; nInitial = ((JAVA_MAP.NodePair<K, V>) nInitial).next) {
						final NodePair<K, V> npInitial = (JAVA_MAP.NodePair<K, V>) nInitial;
						if (key.equals(npInitial.key)) {
							this.absentKeys.add((K) key);						
							--this.size;
							return npInitial.value;
						}
					}
				}

				//else, branch and repeat remove operation
				this.initialMap.refineOnKeyAndBranch((K) key);
				return remove(key);
			}
		} else {
			//matching NodePair found: remove it, adjust
			//size and return the value
			this.absentKeys.add((K) key);						
			if (matchingPairPrev == null) {
				this.root = matchingPair.next;
			} else {
				matchingPairPrev.next = matchingPair.next;
			}
			--this.numNodes;
			--this.size;
			return matchingPair.value;
		}
	}


	// Bulk Operations

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put values in an initial map.");
		}
		//TODO find a lazier implementation, this is copied from AbstractMap; see also copy constructor
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to clear an initial map.");
		}
		this.size = 0;
		this.root = new NodeEmpty();
		this.numNodes = 0;
		this.initialMap = null; //my, that's rough! But it works.
	}


	// Views

	@Override
	public Set<K> keySet() {
		if (this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to take the key set of an initial map.");
		}
		return new JAVA_SET_KEY();
	}

	@Override
	public Collection<V> values() {
		if (this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to take the values collection of an initial map.");
		}
		return new JAVA_COLLECTION_VALUE();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to take the entry set of an initial map.");
		}
		return new JAVA_SET_ENTRY();
	}

	private class JAVA_SET_KEY implements Set<K> {
		private final JAVA_SET_ENTRY entrySet = new JAVA_SET_ENTRY();

		@Override
		public int size() {
			return this.entrySet.size();
		}

		@Override
		public boolean isEmpty() {
			return this.entrySet.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return JAVA_MAP.this.containsKey(o);
		}

		@Override
		public Iterator<K> iterator() {
			return new Iterator<K>() {
				private Iterator<Map.Entry<K, V>> entrySetIterator = entrySet.iterator();

				@Override
				public boolean hasNext() {
					return this.entrySetIterator.hasNext();
				}

				@Override
				public K next() {
					return this.entrySetIterator.next().getKey();
				}

				@Override
				public void remove() {
					this.entrySetIterator.remove();
				}
			};
		}

		@Override
		public Object[] toArray() {
			return toArray(OBJECT_ARRAY);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			//TODO find a lazier implementation, this is taken from the comments of AbstractCollection.toArray
			final int size = size();
			final ArrayList<K> list = new ArrayList<>(size);
			for (K e : this) {
				list.add(e);
			}
			return list.toArray(a);
		}

		@Override
		public boolean add(K e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			final boolean retVal = JAVA_MAP.this.containsKey(o);
			JAVA_MAP.this.remove(o);
			return retVal;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			//TODO find a lazier implementation
			if (c == null) {
				throw new NullPointerException();
			}
			for (Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends K> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<K> it = iterator();
			while (it.hasNext()) {
				if (!c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<K> it = iterator();
			while (it.hasNext()) {
				if (c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public void clear() {
			this.entrySet.clear();
		}
	}

	private class JAVA_COLLECTION_VALUE implements Collection<V> {
		private final JAVA_SET_ENTRY entrySet = new JAVA_SET_ENTRY();

		@Override
		public int size() {
			return this.entrySet.size();
		}

		@Override
		public boolean isEmpty() {
			return this.entrySet.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return JAVA_MAP.this.containsValue(o);
		}

		@Override
		public Iterator<V> iterator() {
			return new Iterator<V>() {
				private Iterator<Map.Entry<K, V>> entrySetIterator = entrySet.iterator();

				@Override
				public boolean hasNext() {
					return this.entrySetIterator.hasNext();
				}

				@Override
				public V next() {
					return this.entrySetIterator.next().getValue();
				}

				@Override
				public void remove() {
					this.entrySetIterator.remove();
				}
			};
		}

		@Override
		public Object[] toArray() {
			return toArray(OBJECT_ARRAY);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			//TODO find a lazier implementation, this is taken from the comments of AbstractCollection.toArray
			final int size = size();
			final ArrayList<V> list = new ArrayList<>(size);
			for (V e : this) {
				list.add(e);
			}
			return list.toArray(a);
		}

		@Override
		public boolean add(V e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			final Iterator<V> it = iterator();
			if (o == null) {
				while (it.hasNext()) {
					if (it.next() == null) {
						it.remove();
						return true;
					}
				}
			} else {
				while (it.hasNext()) {
					if (o.equals(it.next())) {
						it.remove();
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			//TODO find a lazier implementation
			if (c == null) {
				throw new NullPointerException();
			}
			for (Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<V> it = iterator();
			while (it.hasNext()) {
				if (!c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<V> it = iterator();
			while (it.hasNext()) {
				if (c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public void clear() {
			this.entrySet.clear();
		}
	}

	private class JAVA_SET_ENTRY implements Set<Map.Entry<K, V>> {
		@Override
		public int size() {
			return JAVA_MAP.this.size();
		}

		@Override
		public boolean isEmpty() {
			return JAVA_MAP.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (o == null) {
				return false;
			}
			if (!(o instanceof Map.Entry<?, ?>)) {
				return false;
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			if (!JAVA_MAP.this.containsKey(e.getKey())) {
				return false;
			}
			final Object mapValue = JAVA_MAP.this.get(e.getKey());
			if (mapValue == null) {
				return e.getValue() == null;
			} else {
				return mapValue.equals(e.getValue());
			}
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new Iterator<Map.Entry<K,V>>() {
				private boolean scanningInitialMap = false;
				private Node current = JAVA_MAP.this.root;

				@Override
				public boolean hasNext() {
					return (this.current instanceof JAVA_MAP.NodePair);
				}

				@SuppressWarnings("unchecked")
				@Override
				public Entry<K, V> next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					final NodePair<K, V> currentPair = (JAVA_MAP.NodePair<K, V>) this.current;
					final K key = currentPair.key;
					final V value = currentPair.value;
					final Entry<K, V> retVal = new Map.Entry<K, V>() {
						@Override
						public K getKey() {
							return key;
						}

						@Override
						public V getValue() {
							return value;
						}

						@Override
						public Object setValue(Object value) {
							final Object retVal = currentPair.value;
							currentPair.value = (V) value;
							return retVal;
						}

						@Override
						public boolean equals(Object obj) {
							if (obj == null) {
								return false;
							}
							if (this == obj) {
								return true;
							}
							if (!(obj instanceof Map.Entry<?, ?>)) {
								return false;
							}
							final Map.Entry<?, ?> e = (Map.Entry<?, ?>) obj;
							return (key == null ? e.getKey() == null : key.equals(e.getKey())) &&
									(value == null ? e.getValue() == null : value.equals(e.getValue()));
						}

						@Override
						public int hashCode() {
							return currentPair.pairHashCode();
						}
					};

					//move forward by one
					this.current = currentPair.next;

					if (this.scanningInitialMap) {
						//if we are in the initial map, we must also skip all entries that are overridden
						//by other (already emitted) ones - i.e., by the entries in JAVA_MAP.this.root.(next)*
						scanForNotAlreadyEmitted:
							while (this.current instanceof JAVA_MAP.NodePair) {
								final NodePair<K, V> npCurrent = (JAVA_MAP.NodePair<K, V>) this.current;
								final K keyCurrent = npCurrent.key;
								if (keyCurrent == null) {
									for (Node n = JAVA_MAP.this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
										final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
										if (np.key == null) {
											this.current = npCurrent.next;
											continue scanForNotAlreadyEmitted;
										}
									}
								} else {
									for (Node n = JAVA_MAP.this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
										final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
										if (keyCurrent.equals(np.key)) {
											this.current = npCurrent.next;
											continue scanForNotAlreadyEmitted;
										}
									}
								}
								break;
							}
					} else if (this.current instanceof JAVA_MAP.NodeEmpty && JAVA_MAP.this.initialMap != null) {
						//if we are at the end of JAVA_MAP.this.root.(next)*, continue 
						//with the initial map (if present)
						this.current = JAVA_MAP.this.initialMap.root;
						this.scanningInitialMap = true;
					}

					return retVal;
				}

				@SuppressWarnings("unchecked")
				@Override
				public void remove() {
					if (!hasNext()) {
						throw new IllegalStateException();
					}
					final NodePair<K, V> currentBeforeRemovalPair = (JAVA_MAP.NodePair<K, V>) this.current;
					final K key = currentBeforeRemovalPair.key;
					JAVA_MAP.this.remove(key);
					if (!this.scanningInitialMap) {
						//check if currentBeforeRemovalPair is still there
						for (Node n = JAVA_MAP.this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
							if (n == currentBeforeRemovalPair) {
								//still present
								return;
							}
						}

						//otherwise, skips the iterator by one
						this.current = currentBeforeRemovalPair.next;
					}
				}
			};
		}

		@Override
		public Object[] toArray() {
			return toArray(OBJECT_ARRAY);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			//TODO find a lazier implementation, this is copied from the comments of AbstractCollection.toArray
			final int size = size();
			final ArrayList<Map.Entry<K, V>> list = new ArrayList<>(size);
			for (Map.Entry<K, V> e : this) {
				list.add(e);
			}
			return list.toArray(a);
		}

		@Override
		public boolean add(Entry<K, V> e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Map.Entry<?, ?>) {
				final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				final Object key = e.getKey();
				final Object value = e.getValue();
				return JAVA_MAP.this.remove(key, value);
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			//TODO find a lazier implementation
			if (c == null) {
				throw new NullPointerException();
			}
			for (Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<Map.Entry<K, V>> it = iterator();
			while (it.hasNext()) {
				if (!c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			//TODO find a lazier implementation, this is taken from AbstractCollection
			Objects.requireNonNull(c);
			boolean modified = false;
			final Iterator<Map.Entry<K, V>> it = iterator();
			while (it.hasNext()) {
				if (c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public void clear() {
			JAVA_MAP.this.clear();
		}

	}

	// Comparison and hashing

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof Map<?, ?>)) {
			return false;
		}
		final Map<?, ?> m = (Map<?, ?>) o;
		//TODO find a lazier implementation; this is taken from the comments of Map.equals
		return entrySet().equals(m.entrySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public int hashCode() {
		if (this.isInitial) {
			return this.initialHashCode;
		}

		//calculates the hash code for the entries added
		//after the start of the symbolic execution
		int hashCode = 0;
		for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
			final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
			hashCode += np.pairHashCode();
		}

		//if the map is concrete, there's nothing else to do
		if (this.initialMap == null) {
			return hashCode;
		}

		//else, add also the has code of the initial map...
		hashCode += this.initialMap.initialHashCode;

		//...and subtract the hash codes of all the entries 
		//in the initial map that have been replaced after
		//the start of symbolic execution (this is the hard part).
		//The idea is to specialize the backing initial map so 
		//we can determine, for all the keys in the node list, whether 
		//they are or not in the initial map: then, subtract the hash
		//values for the entries that are present.

		//first, statically determine if there are any
		//keys in this.root.(next)* that are not refined in
		//the initial map
		final ArrayList<K> notRefined = new ArrayList<>();
		findNotRefinedNodes:
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (this.initialMap.absentKeys.contains(np.key)) {
					continue findNotRefinedNodes;
				}
				if (np.key == null) {
					for (Node nRefinement = this.initialMap.root; nRefinement instanceof JAVA_MAP.NodePair; nRefinement = ((JAVA_MAP.NodePair<K, V>) nRefinement).next) {
						final NodePair<K, V> npRefinement = (JAVA_MAP.NodePair<K, V>) nRefinement;
						if (npRefinement.key == null) {
							continue findNotRefinedNodes;
						}
					}
				} else {
					for (Node nRefinement = this.initialMap.root; nRefinement instanceof JAVA_MAP.NodePair; nRefinement = ((JAVA_MAP.NodePair<K, V>) nRefinement).next) {
						final NodePair<K, V> npRefinement = (JAVA_MAP.NodePair<K, V>) nRefinement;
						if (np.key.equals(npRefinement.key)) {
							continue findNotRefinedNodes;
						}
					}
				}
				notRefined.add(np.key);
			}

		//if there are any, then refine (for n keys generates 2^n branches!!!)
		if (notRefined.size() > 0) {
			//TODO does this ever happen??? Apparently either a map is concrete (no initial map) or is symbolic, and in this case every operation (get, put) that introduces a key also introduces a refinement on it in the initial map
			refineOnKeyCombinationsAndBranch(notRefined.toArray());
		}

		//finally, subtract from the hash code all the hashes of pairs
		//in the initial map
		for (Node nRefinement = this.initialMap.root; nRefinement instanceof JAVA_MAP.NodePair; nRefinement = ((JAVA_MAP.NodePair<K, V>) nRefinement).next) {
			final NodePair<K, V> npRefinement = (JAVA_MAP.NodePair<K, V>) nRefinement;
			hashCode -= npRefinement.pairHashCode();
		}

		return hashCode;
	}

	// Defaultable methods

	//TODO here we accept all the default implementations. Should we define lazier ones?

	// Private methods

	private static final Object[] OBJECT_ARRAY = new Object[0];

	/**
	 * Causes JBSE to internally throw an unexpected internal exception.
	 * 
	 * @param message a {@link String}, the message of the exception.
	 */
	private native static void metaThrowUnexpectedInternalException(String message);

	/**
	 * Initializes a map, if it is symbolic. 
	 * The method will also initialize the (symbolic <em>initial</em>) 
	 * map {@code this.initialMap} that backs the map, and that 
	 * represents the map as it was in the initial state. 
	 * 
	 * @param tthis the {@link JAVA_MAP} to initialize. While {@code tthis}
	 * is mutable, {@code tthis.initialMap} will be immutable, will be 
	 * shared by all the clones of {@code tthis}, and will be progressively 
	 * refined upon access to {@code tthis} introduces assumptions on the 
	 * initial content of {@code tthis}.
	 * 
	 * @throws IllegalArgumentException if this map is not symbolic.
	 */
	private static <KK, VV> void initSymbolic(JAVA_MAP<KK, VV> tthis) {
		if (!isSymbolic(tthis)) {
			throw new IllegalArgumentException("Attempted to invoke " + JAVA_MAP.class.getCanonicalName() + ".initSymbolic on a concrete map.");
		}
		assume(isResolvedByExpansion(tthis));
		assume(isResolvedByExpansion(tthis.initialMap));

		//initializes this
		tthis.isInitial = false;
		//origin.initialHashCode: doesn't care
		tthis.absentKeys = new ArrayList<>();
		//this.absentValues: doesn't care
		//this.initialMap: OK the symbolic value it already has
		tthis.size = tthis.initialMap.size;
		tthis.root = new NodeEmpty();
		tthis.numNodes = 0;

		tthis.initialMap.makeInitial();
		tthis.initialMap.isInitial = true;
		//this.initialMap.initialHashCode: OK the symbolic value it already has
		tthis.initialMap.absentKeys = new ArrayList<>();
		tthis.initialMap.absentValues = new ArrayList<>();
		tthis.initialMap.initialMap = null;
		//this.initialMap.size: OK the symbolic value it already has
		assume(tthis.initialMap.size >= 0);
		tthis.initialMap.root = new NodeEmpty();
		tthis.initialMap.numNodes = 0;
	}

	/**
	 * Makes this object initial.
	 */
	private native void makeInitial();

	/**
	 * Causes symbolic execution to branch on the two cases:
	 * A key is present/absent in an initial map. Can be invoked
	 * only if this map is initial.
	 * 
	 * @param key the key.
	 */
	private native void refineOnKeyAndBranch(K key);

	/**
	 * Causes symbolic execution to branch on the two cases:
	 * A value is present/absent in an initial map. Can be invoked
	 * only if this map is initial.
	 * 
	 * @param value the value.
	 */
	private native void refineOnValueAndBranch(V value);

	/**
	 * Causes symbolic execution to branch on the cases:
	 * A set of keys is present/absent in an initial map. 
	 * Can be invoked only if this map is initial.
	 * 
	 * @param keys an array of keys. If {@code keys.length == n}
	 *        then {@code 2^n} branches will be created for all
	 *        possible subsets of keys.
	 */
	private native void refineOnKeyCombinationsAndBranch(Object... keys);

	/**
	 * Upcalled by {@link #refineOnKeyAndBranch(Object)},
	 * {@link #refineOnKeyCombinationsAndBranch(Object...)}, 
	 * and {@link #refineOnValueAndBranch(Object)}. 
	 * Refines this object by assuming that a key/value pair
	 * is present in it. This object must be initial.
	 * 
	 * @param key the key.
	 * @param value the value.
	 */
	private void refineIn(K key, V value) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a JAVA_MAP that is not initial.");
		}
		if (this.absentKeys.contains(key)) {
			ignore(); //contradiction found
		}

		final NodePair<K, V> p = new NodePair<K, V>();
		p.key = key;
		p.value = value;
		p.next = this.root;
		this.root = p;
		++this.numNodes;
		assume(this.size >= this.numNodes);
	}

	/**
	 * Upcalled by {@link #refineOnKeyAndBranch(Object)},
	 * and {@link #refineOnKeyCombinationsAndBranch(Object...)}. 
	 * Refines this object by assuming that a key
	 * is not present in it. This object must be initial.
	 * 
	 * @param key the key.
	 */
	@SuppressWarnings("unchecked")
	private void refineOutKey(K key) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a JAVA_MAP that is not initial.");
		}
		if (key == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.key == null) {
					ignore(); //contradiction found
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (key.equals(np.key)) {
					ignore(); //contradiction found
				}
			}
		}

		this.absentKeys.add(key);
	}

	/**
	 * Upcalled by {@link #refineOnValueAndBranch(Object)}. 
	 * Refines this object by assuming that a value
	 * is not present in it. This object must be initial.
	 * 
	 * @param value the value.
	 */
	@SuppressWarnings("unchecked")
	private void refineOutValue(V value) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a JAVA_MAP that is not initial.");
		}
		if (value == null) {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (np.value == null) {
					ignore(); //contradiction found
				}
			}
		} else {
			for (Node n = this.root; n instanceof JAVA_MAP.NodePair; n = ((JAVA_MAP.NodePair<K, V>) n).next) {
				final NodePair<K, V> np = (JAVA_MAP.NodePair<K, V>) n;
				if (value.equals(np.value)) {
					ignore(); //contradiction found
				}
			}
		}

		this.absentValues.add(value);
	}
}
