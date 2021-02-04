package jbse.base;

import static jbse.meta.Analysis.assume;
import static jbse.meta.Analysis.ignore;
import static jbse.meta.Analysis.isResolvedByExpansion;
import static jbse.meta.Analysis.isSymbolic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Model class for class {@link java.util.LinkedHashMap}. 
 * 
 * @author Pietro Braione
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class JAVA_LINKEDMAP<K, V> extends HashMap<K, V>
implements Map<K, V> {

	private static final long serialVersionUID = 3801124242820219131L; //same as LinkedHashMap

	private static abstract class NNode { }

	private static class NNodePair<KK, VV> extends NNode {
		KK key;
		VV value;
		NNode next;
		NNodePair<KK, VV> before;
		NNodePair<KK, VV> after;

		public int pairHashCode() {
			return (this.key == null ? 0 : this.key.hashCode()) ^
					(this.value == null ? 0 : this.value.hashCode());
		}
	}

	private static class NNodeEmpty extends NNode { }

	/**
     * The iteration ordering method for this linked hash map: <tt>true</tt>
     * for access-order, <tt>false</tt> for insertion-order.
	 */
	private final boolean accessOrder;

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
	 * this map is symbolic, otherwise it is set to null.
	 */
	private JAVA_LINKEDMAP<K, V> initialMap;

	/** 
	 * Used only when isInitial == true; the current map of which this map 
	 * is the initial one. It is the reverse link of initialMap.
	 */
	private JAVA_LINKEDMAP<K, V> currentMap;

	/**
	 * The size of the map.
	 */
	private int size;

	/**
	 * The list of key/value pairs in the map, 
	 * either added (noninitial map) or assumed (initial map).
	 */
	private NNode root;
	
    /**
     * The eldest key/value pair; if the map is initial
     * it is always null.
     */
	private NNodePair<K, V> head;
	
    /**
     * The youngest key/value pair; if the map is initial
     * it points to the youngest key/value pair that was
     * assumed to be in the initial map, and that was not
     * accessed (in the sense of the access order definition) 
     * after execution, if the order is access order.
     */
	private NNodePair<K, V> tail;

	/** 
	 * The number of nodes in root.(next)*, excluded the
	 * final NodeEmpty.
	 */
	private int numNodes;
	
	// Constructors

	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16


	public JAVA_LINKEDMAP(int initialCapacity, float loadFactor, boolean accessOrder) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " +
					initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " +
					loadFactor);
		this.accessOrder = accessOrder;
		this.isInitial = false;
		this.initialHashCode = 0;
		this.absentKeys = new ArrayList<>();
		this.absentValues = null;
		this.initialMap = null;
		this.currentMap = null;
		this.size = 0;
		this.root = new NNodeEmpty();
		this.head = null;
		this.tail = null;
		this.numNodes = 0;
	}
	
	public JAVA_LINKEDMAP(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, false);
	}


	public JAVA_LINKEDMAP(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public JAVA_LINKEDMAP() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public JAVA_LINKEDMAP(Map<? extends K, ? extends V> m) {
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
	private static <KK, VV> JAVA_LINKEDMAP.NNodePair<KK, VV> findNodeKey(JAVA_LINKEDMAP.NNode root, KK key) {
		if (key == null) {
			for (JAVA_LINKEDMAP.NNode nInitial = root; nInitial instanceof JAVA_LINKEDMAP.NNodePair; nInitial = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> npInitial = (JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial;
				if (npInitial.key == null) {
					return npInitial;
				}
			}
		} else {
			for (JAVA_LINKEDMAP.NNode nInitial = root; nInitial instanceof JAVA_LINKEDMAP.NNodePair; nInitial = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> npInitial = (JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial;
				if (key.equals(npInitial.key)) {
					return npInitial;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <KK, VV> JAVA_LINKEDMAP.NNodePair<KK, VV> findNodeValue(JAVA_LINKEDMAP.NNode root, VV value) {
		if (value == null) {
			for (JAVA_LINKEDMAP.NNode nInitial = root; nInitial instanceof JAVA_LINKEDMAP.NNodePair; nInitial = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> npInitial = (JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial;
				if (npInitial.value == null) {
					return npInitial;
				}
			}
		} else {
			for (JAVA_LINKEDMAP.NNode nInitial = root; nInitial instanceof JAVA_LINKEDMAP.NNodePair; nInitial = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> npInitial = (JAVA_LINKEDMAP.NNodePair<KK, VV>) nInitial;
				if (value.equals(npInitial.value)) {
					return npInitial;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		notifyMethodExecution();

		//checks if the key is on the list of absent keys
		if (this.absentKeys.contains((K) key)) {
			return false;
		}

		//if not absent, checks in the nodes
		if (findNodeKey(this.root, key) != null) {
			return true;
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
		if (findNodeValue(this.root, value) != null) {
			return true;
		}

		//if not in the nodes there are three possible cases: 
		//1- the map is initial: branches and rechecks
		if (this.isInitial) {
			refineOnValueAndBranch((V) value);
			return containsValue(value); //after refinement it will be either in this.absentValues or in this.root.(next)*
		}
		//2- the map is not initial and is backed by an initial
		//   map (it is symbolic): searches in the initial map
		if (this.initialMap != null) {
			return this.initialMap.containsValue(value);
		}
		//3- otherwise (concrete map) it is not in the map
		return false;
	}
	
	private void _afterNodeAccess(NNodePair<K, V> e) {
		final JAVA_LINKEDMAP<K, V> tthis = (this.isInitial ? this.currentMap : this);
		JAVA_LINKEDMAP.NNodePair<K, V> last = tthis.tail;
		if (tthis.accessOrder && e != last) {
			JAVA_LINKEDMAP.NNodePair<K, V> b = e.before, a = e.after;
			e.after = null;
			if (b == null) {
				tthis.head = a;
			} else {
				b.after = a;
			}
			if (a == null) {
				last = b;
			} else {
				a.before = b;
			}
			if (last == null) {
				tthis.head = e;
			} else {
				e.before = last;
				last.after = e;
			}
			tthis.tail = e;
			if (this.isInitial && this.tail == e) {
				this.tail = b;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private JAVA_LINKEDMAP.NNodePair<K, V> getNode(Object key) {
		//checks if the key is in the list of absent keys
		if (this.absentKeys.contains((K) key)) {
			return null;
		}

		//if not absent, checks in the nodes
		final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) findNodeKey(this.root, key);
		if (np != null) {
			return np;
		}

		//if not in the nodes there are three cases: 
		//1- the map is initial: branches and rechecks
		if (this.isInitial) {
			refineOnKeyAndBranch((K) key);
			return getNode(key); //after refinement it will be either in this.absentKeys or in this.root.(next)*
		}
		//2- the map is not initial and is backed by an initial
		//   map (it is symbolic): search in the initial map
		if (this.initialMap != null) {
			return this.initialMap.getNode(key);
		}
		//3- otherwise (concrete map) it is not in the map
		return null;
	}

	@Override
	public V get(Object key) {
		notifyMethodExecution();
		
		final JAVA_LINKEDMAP.NNodePair<K, V> np = getNode(key);
		if (np == null) {
			return null;
		} else {
			_afterNodeAccess(np);
			return np.value;
		}
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		notifyMethodExecution();
		
		final JAVA_LINKEDMAP.NNodePair<K, V> np = getNode(key);
		if (np == null) {
			return defaultValue;
		} else {
			_afterNodeAccess(np);
			return np.value;
		}
	}

	@SuppressWarnings("unchecked")
	private void addNode(K key, V value) {
		final JAVA_LINKEDMAP.NNodePair<K, V> p = new JAVA_LINKEDMAP.NNodePair<>();
		p.key = key;
		p.value = value;
		p.before = p.after = null;
		JAVA_LINKEDMAP.NNode n;
		for (n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair<?, ?>; n = ((JAVA_LINKEDMAP.NNodePair<?, ?>) n).next) {
			if (((JAVA_LINKEDMAP.NNodePair<?, ?>) n).next instanceof NNodeEmpty) {
				break;
			}
		}
		if (n instanceof NNodeEmpty) {
			p.next = this.root;
			this.root = p;
		} else {
			final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
			p.next = np.next;
			np.next = p;
		}
		_linkNodeLast(p);
		++this.numNodes;
	}
	
	private void _linkNodeLast(JAVA_LINKEDMAP.NNodePair<K, V> p) {
		if (this.isInitial) {
			final JAVA_LINKEDMAP.NNodePair<K, V> last = this.tail;
			this.tail = p;
			if (last == null) {
				p.before = null;
				p.after = this.currentMap.head;
				if (p.after != null) {
					p.after.before = p;
				}				
				this.currentMap.head = p; 
			} else {
				p.before = last;
				p.after = last.after;
				if (p.after != null) {
					p.after.before = p;
				}
				last.after = p;
			}
			if (this.currentMap.tail == last) {
				this.currentMap.tail = p;
			}
		} else {
			final JAVA_LINKEDMAP.NNodePair<K, V> last = this.tail;
			this.tail = p;
			if (last == null) {
				this.head = p;
			} else {
				p.before = last;
				last.after = p;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void overrideInitialNode(JAVA_LINKEDMAP.NNodePair<K, V> pOverridden, V value) {
		final JAVA_LINKEDMAP.NNodePair<K, V> p = new JAVA_LINKEDMAP.NNodePair<>();
		p.key = pOverridden.key;
		p.value = value;
		p.before = p.after = null;
		JAVA_LINKEDMAP.NNode n;
		for (n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair<?, ?>; n = ((JAVA_LINKEDMAP.NNodePair<?, ?>) n).next) {
			if (((JAVA_LINKEDMAP.NNodePair<?, ?>) n).next instanceof NNodeEmpty) {
				break;
			}
		}
		if (n instanceof NNodeEmpty) {
			p.next = this.root;
			this.root = p;
		} else {
			final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
			p.next = np.next;
			np.next = p;
		}
		_transferLinks(pOverridden, p);
		pOverridden.before = pOverridden.after = null;
		++this.numNodes;
	}
	
	private void _transferLinks(JAVA_LINKEDMAP.NNodePair<K, V> src, JAVA_LINKEDMAP.NNodePair<K, V> dst) {
		final JAVA_LINKEDMAP.NNodePair<K, V> b = dst.before = src.before;
		final JAVA_LINKEDMAP.NNodePair<K, V> a = dst.after = src.after;
        if (b == null) {
            head = dst;
        } else {
            b.after = dst;
        }
        if (a == null) {
            tail = dst;
        } else {
            a.before = dst;
        }
	}
	
	private void _afterNodeInsertion(boolean evict) {
		final JAVA_LINKEDMAP.NNodePair<K, V> first = this.head;
		final Entry<K, V> e = new Map.Entry<K, V>() {

			@Override
			public K getKey() {
				return first.key;
			}

			@Override
			public V getValue() {
				return first.value;
			}

			@Override
			public V setValue(V value) {
				throw new UnsupportedOperationException();
			}
		};
		if (evict && first != null && removeEldestEntry(e)) {
			final K key = first.key;
			doRemove(key);
		}
	}
	
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

	@Override
	public V put(K key, V value) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put a value in an initial map.");
		}
		return putVal(key, value, true);
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put a value in an initial map.");
		}
		V v = get(key);
		if (v == null) {
			v = putVal(key, value, true);
		}
		return v;
	}
	
	@SuppressWarnings("unchecked")
	private V putVal(K key, V value, boolean evict) {				
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put a value in an initial map.");
		}
		
		//looks for a matching NodePair in this.root.(next)*
		final JAVA_LINKEDMAP.NNodePair<K, V> matchingPair = (JAVA_LINKEDMAP.NNodePair<K, V>) findNodeKey(this.root, key);

		if (matchingPair == null) {
			//no matching NodePair
			if (this.initialMap == null) {
				//the map is concrete, so it did not contain the key
				//before this put operation: add the new mapping, 
				//adjust the size and return null
				this.absentKeys.remove(key);
				addNode(key, value);
				++this.size;
				_afterNodeInsertion(evict);
				return null;
			} else {
				//the map is symbolic, so there are two cases: either a 
				//mapping is present, or it is not present, from the initial map. 
				//This decision could generate a branch in symbolic execution.

				//if no mapping is surely contributed by the initial map, adds the 
				//new mapping in the current map, adjusts the size and returns 
				//null
				if (this.absentKeys.contains(key) || this.initialMap.absentKeys.contains(key)) {
					this.absentKeys.remove(key);
					addNode(key, value);
					++this.size;
					_afterNodeInsertion(evict);
					return null;
				}

				//otherwise, the initial map might contribute a mapping: if the 
				//key surely is in the initial map, adds a new mapping to the 
				//current map that overrides that in the initial map, and 
				//returns the value it had in the initial map
				final JAVA_LINKEDMAP.NNodePair<K, V> npInitial = (JAVA_LINKEDMAP.NNodePair<K, V>) findNodeKey(this.initialMap.root, key);
				if (npInitial != null) {
					overrideInitialNode(npInitial, value);
					return npInitial.value;
				}

				//else, branch and repeat putVal operation
				this.initialMap.refineOnKeyAndBranch((K) key);
				return putVal(key, value, evict);
			}
		} else {
			//matching NodePair found: just update it and 
			//return its previous value (note that here 
			//this.absentKeys does not contain key)
			final V retVal = matchingPair.value;
			matchingPair.value = value;
			_afterNodeAccess(matchingPair);
			return retVal;
		}
	}

	@Override
	public V remove(Object key) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to remove a value from an initial map.");
		}
		final JAVA_LINKEDMAP.NNodePair<K, V> np = doRemove(key);
		return (np == null ? null : np.value);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to remove a value from an initial map.");
		}
        final Object curValue = get(key);
        if (!Objects.equals(curValue, value) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        doRemove(key);
        return true;
	}
	
	private void _afterNodeRemoval(JAVA_LINKEDMAP.NNodePair<K, V> e) {
		final JAVA_LINKEDMAP.NNodePair<K, V> b = e.before, a = e.after;
		e.before = e.after = null;
		if (b == null) {
			this.head = a;
		} else {
			b.after = a;
		}
		if (a == null) {
			this.tail = b;
		} else {
			a.before = b;
		}
	}

	@SuppressWarnings("unchecked")
	private JAVA_LINKEDMAP.NNodePair<K, V> doRemove(Object key) {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to remove a value from an initial map.");
		}
		
		//if it is already absent, returns null
		if (this.absentKeys.contains(key)) {
			return null;
		}

		//looks for a matching NodePair in this.root.(next)*
		JAVA_LINKEDMAP.NNodePair<K, V> matchingPairPrev = null, matchingPair = null;
		if (key == null) {
			for (JAVA_LINKEDMAP.NNode nPrev = null, n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair; nPrev = n, n = ((JAVA_LINKEDMAP.NNodePair<K, V>) n).next) {
				final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
				if (np.key == null) {
					matchingPairPrev = (JAVA_LINKEDMAP.NNodePair<K, V>) nPrev;
					matchingPair = np;
					break;
				}
			}
		} else {
			for (JAVA_LINKEDMAP.NNode nPrev = null, n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair; nPrev = n, n = ((JAVA_LINKEDMAP.NNodePair<K, V>) n).next) {
				final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
				if (key.equals(np.key)) {
					matchingPairPrev = (JAVA_LINKEDMAP.NNodePair<K, V>) nPrev;
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

				//if the key surely is in the initial map, adjusts size and
				//returns the associated value
				final JAVA_LINKEDMAP.NNodePair<K, V> npInitial = (JAVA_LINKEDMAP.NNodePair<K, V>) findNodeKey(this.initialMap.root, key);
				if (npInitial != null) {
					this.absentKeys.add((K) key);						
					--this.size;
					_afterNodeRemoval(npInitial);
					return npInitial;
				}

				//else, branch and repeat doRemove operation
				this.initialMap.refineOnKeyAndBranch((K) key);
				return doRemove(key);
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
			_afterNodeRemoval(matchingPair);
			return matchingPair;
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
			putVal(e.getKey(), e.getValue(), false);
		}
	}

	@Override
	public void clear() {
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to clear an initial map.");
		}
		this.size = 0;
		this.root = new NNodeEmpty();
		this.head = null;
		this.tail = null;
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
			return JAVA_LINKEDMAP.this.containsKey(o);
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
			final boolean retVal = JAVA_LINKEDMAP.this.containsKey(o);
			JAVA_LINKEDMAP.this.remove(o);
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
			return JAVA_LINKEDMAP.this.containsValue(o);
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
			return JAVA_LINKEDMAP.this.size();
		}

		@Override
		public boolean isEmpty() {
			return JAVA_LINKEDMAP.this.isEmpty();
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
			if (!JAVA_LINKEDMAP.this.containsKey(e.getKey())) {
				return false;
			}
			final Object mapValue = JAVA_LINKEDMAP.this.get(e.getKey());
			if (mapValue == null) {
				return e.getValue() == null;
			} else {
				return mapValue.equals(e.getValue());
			}
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			/**
			 * This iterator iterates according to the order established by
			 * the linked list. When hitting initialMap.tail, it branches
			 * assuming another entry in the initial list, or that the initial
			 * list is complete. 
			 */
			return new Iterator<Map.Entry<K,V>>() {
				private NNodePair<K, V> nextNodeIterator = null;
				
				{
					findNextNode();
				}
				
				private void findNextNode() {
					//if the iterator is at JAVA_LINKEDMAP.this.initialMap.tail, 
					//branches to assume another entry in the initial map
					if (JAVA_LINKEDMAP.this.initialMap != null && this.nextNodeIterator == JAVA_LINKEDMAP.this.initialMap.tail) {
						//refines
						JAVA_LINKEDMAP.this.initialMap.refineOnFreshEntryAndBranch();
					}
					//advances this.current
					this.nextNodeIterator = (this.nextNodeIterator == null ? JAVA_LINKEDMAP.this.head : this.nextNodeIterator.after);
				}
				
				@Override
				public boolean hasNext() {
					return (this.nextNodeIterator != null);
				}

				@SuppressWarnings("unchecked")
				@Override
				public Map.Entry<K, V> next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}

					//builds the return value
					final JAVA_LINKEDMAP.NNodePair<K, V> currentPair = this.nextNodeIterator;
					final Map.Entry<K, V> retVal = new Map.Entry<K, V>() {
						@Override
						public K getKey() {
							return currentPair.key;
						}

						@Override
						public V getValue() {
							return currentPair.value;
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
							return (currentPair.key == null ? e.getKey() == null : currentPair.key.equals(e.getKey())) &&
									(currentPair.value == null ? e.getValue() == null : currentPair.value.equals(e.getValue()));
						}

						@Override
						public int hashCode() {
							return currentPair.pairHashCode();
						}
					};
					
					//move this.current forward
					findNextNode();

					return retVal;
				}

				@Override
				public void remove() {
					if (!hasNext()) {
						throw new IllegalStateException();
					}
					final JAVA_LINKEDMAP.NNodePair<K, V> nextNodeIteratorBeforeRemoval = this.nextNodeIterator;
					final K key = nextNodeIteratorBeforeRemoval.key;
					JAVA_LINKEDMAP.this.remove(key);
					//check if currentBeforeRemovalPair is still there
					for (JAVA_LINKEDMAP.NNodePair<K, V>  n = JAVA_LINKEDMAP.this.head; n != null; n = n.after) {
						if (n == nextNodeIteratorBeforeRemoval) {
							//still present
							return;
						}
					}

					//otherwise, skips the iterator by one
					this.nextNodeIterator = nextNodeIteratorBeforeRemoval.after;
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
		public boolean add(Map.Entry<K, V> e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Map.Entry<?, ?>) {
				final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				final Object key = e.getKey();
				final Object value = e.getValue();
				return JAVA_LINKEDMAP.this.remove(key, value);
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
		public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
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
			JAVA_LINKEDMAP.this.clear();
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
		for (JAVA_LINKEDMAP.NNode n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair; n = ((JAVA_LINKEDMAP.NNodePair<K, V>) n).next) {
			final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
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
			for (JAVA_LINKEDMAP.NNode n = this.root; n instanceof JAVA_LINKEDMAP.NNodePair; n = ((JAVA_LINKEDMAP.NNodePair<K, V>) n).next) {
				final JAVA_LINKEDMAP.NNodePair<K, V> np = (JAVA_LINKEDMAP.NNodePair<K, V>) n;
				if (this.initialMap.absentKeys.contains(np.key)) {
					continue findNotRefinedNodes;
				}
				if (findNodeKey(this.initialMap.root, np.key) != null) {
					continue findNotRefinedNodes;
				}
				notRefined.add(np.key);
			}

		//if there are any, then refine (for n keys generates 2^n branches!!!)
		if (notRefined.size() > 0) {
			/* 
			 * TODO does this ever happen??? Apparently either a map is concrete (no initial map) 
			 * or is symbolic, and in this case every operation (get, put) that introduces a key 
			 * also introduces a refinement on it in the initial map
			 */
			refineOnKeyCombinationsAndBranch(notRefined.toArray());
		}

		//finally, subtract from the hash code all the hashes of pairs
		//in the initial map
		for (JAVA_LINKEDMAP.NNode nRefinement = this.initialMap.root; nRefinement instanceof JAVA_LINKEDMAP.NNodePair; nRefinement = ((JAVA_LINKEDMAP.NNodePair<K, V>) nRefinement).next) {
			final JAVA_LINKEDMAP.NNodePair<K, V> npRefinement = (JAVA_LINKEDMAP.NNodePair<K, V>) nRefinement;
			hashCode -= npRefinement.pairHashCode();
		}

		return hashCode;
	}

	// Defaultable methods

	//TODO here we take all the default implementations from java.util.Map. Should we define lazier ones?
		
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		notifyMethodExecution();
		
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to invoke replaceAll on an initial map.");
		}
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                entry.setValue(v);
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to replace a value in an initial map.");
		}
        final JAVA_LINKEDMAP.NNodePair<K, V> curNode = getNode(key);
        final V curValue = (curNode == null ? null : curNode.value);
        if (!Objects.equals(curValue, oldValue) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
	}
	
	@Override
	public V replace(K key, V value) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to replace a value in an initial map.");
		}
        final JAVA_LINKEDMAP.NNodePair<K, V> curNode = getNode(key);
        V curValue = (curNode == null ? null : curNode.value);
        if (curValue != null || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		notifyMethodExecution();

		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to invoke computeIfAbsent on an initial map.");
		}
		Objects.requireNonNull(mappingFunction);
		final JAVA_LINKEDMAP.NNodePair<K, V> oldNode = getNode(key);
        if (oldNode == null || oldNode.value == null) {
            final V newValue = mappingFunction.apply(key);
            if (newValue != null) {
            	put(key, newValue);
            }
        	return newValue;
        } else {
        	_afterNodeAccess(oldNode);
        	return oldNode.value;
        }
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		notifyMethodExecution();

		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to invoke computeIfPresent on an initial map.");
		}
		Objects.requireNonNull(remappingFunction);
		final JAVA_LINKEDMAP.NNodePair<K, V> oldNode = getNode(key);
        if (oldNode != null && oldNode.value != null) {
            final V newValue = remappingFunction.apply(key, oldNode.value);
            if (newValue == null) {
            	doRemove(key);
            } else {
            	put(key, newValue);
            }
        	return newValue;
        } else {
        	_afterNodeAccess(oldNode);
        	return oldNode.value;
        }
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		notifyMethodExecution();

		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to invoke compute on an initial map.");
		}
		Objects.requireNonNull(remappingFunction);
		final JAVA_LINKEDMAP.NNodePair<K, V> oldNode = getNode(key);
        if (oldNode == null || oldNode.value == null) {
            final V newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
            	put(key, newValue);
            }
        	return newValue;
        } else {
            final V newValue = remappingFunction.apply(key, oldNode.value);
            if (newValue == null) {
            	doRemove(key);
            } else {
            	put(key, newValue);
            }
        	return newValue;
        }
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		notifyMethodExecution();
		
		if (this.isInitial) {
			//initial maps are immutable
			metaThrowUnexpectedInternalException("Tried to put a value in an initial map.");
		}
		Objects.requireNonNull(remappingFunction);
		final JAVA_LINKEDMAP.NNodePair<K, V> oldNode = getNode(key);
        if (oldNode != null) {
            final V v;
            if (oldNode.value == null) {
                v = value;
            } else {
                v = remappingFunction.apply(oldNode.value, value);
            }
            if (v == null) {
            	doRemove(key);
            } else {
                put(key, v);
            }
            return v;
        }
        if (value != null) {
        	put(key, value);
        }
        return value;
	}

	// Clone
	
	@SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        final JAVA_LINKEDMAP<K,V> result = (JAVA_LINKEDMAP<K,V>) super.clone();
        result.absentKeys = new ArrayList<>(this.absentKeys);
        result.root = new NNodeEmpty();
        NNodePair<K, V> dest = null;
        for (NNode src = this.root; src instanceof NNodePair<?, ?>; src = ((NNodePair<K, V>) src).next) {
        	final NNodePair<K, V> newNodePair = new NNodePair<>();
        	newNodePair.key = ((NNodePair<K, V>) src).key;
        	newNodePair.value = ((NNodePair<K, V>) src).value;
        	if (dest == null) {
        		newNodePair.next = result.root;
        		result.root = newNodePair;
        	} else {
        		newNodePair.next = dest.next;
        		dest.next = newNodePair;
        	}
        	dest = newNodePair;
        }
        return result;
	}

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
	 * @param tthis the {@link JAVA_LINKEDMAP} to initialize. While {@code tthis}
	 * is mutable, {@code tthis.initialMap} will be immutable, will be 
	 * shared by all the clones of {@code tthis}, and will be progressively 
	 * refined upon access to {@code tthis} introduces assumptions on the 
	 * initial content of {@code tthis}.
	 * 
	 * @throws IllegalArgumentException if this map is not symbolic.
	 */
	private static <KK, VV> void initSymbolic(JAVA_LINKEDMAP<KK, VV> tthis) {
		if (!isSymbolic(tthis)) {
			throw new IllegalArgumentException("Attempted to invoke " + JAVA_LINKEDMAP.class.getCanonicalName() + ".initSymbolic on a concrete map.");
		}
		assume(isResolvedByExpansion(tthis));
		assume(isResolvedByExpansion(tthis.initialMap));

		//initializes this
		tthis.isInitial = false;
		//tthis.initialHashCode: doesn't care
		tthis.absentKeys = new ArrayList<>();
		//tthis.absentValues: doesn't care
		//tthis.initialMap: OK the symbolic value it already has
		tthis.currentMap = null;
		tthis.size = tthis.initialMap.size;
		tthis.root = new NNodeEmpty();
		tthis.head = null;
		tthis.tail = null;
		tthis.numNodes = 0;

		tthis.initialMap.makeInitial();
		tthis.initialMap.isInitial = true;
		//tthis.initialMap.initialHashCode: OK the symbolic value it already has
		tthis.initialMap.absentKeys = new ArrayList<>();
		tthis.initialMap.absentValues = new ArrayList<>();
		tthis.initialMap.initialMap = null;
		tthis.initialMap.currentMap = tthis;
		//this.initialMap.size: OK the symbolic value it already has
		assume(tthis.initialMap.size >= 0);
		tthis.initialMap.root = new NNodeEmpty();
		tthis.initialMap.head = null;
		tthis.initialMap.tail = null;
		tthis.initialMap.numNodes = 0;
	}

	/**
	 * Makes this object initial.
	 */
	private native void makeInitial();

	/**
	 * Notifies the start of the execution of a method 
	 * to the decision procedure.
	 */
	private native void notifyMethodExecution();
	
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
	 * Causes symbolic execution to branch on the cases:
	 * A fresh entry is present/absent in an initial map. 
	 * Can be invoked only if this map is initial.
	 */
	private native void refineOnFreshEntryAndBranch();
	
	/**
	 * Triggered on reference resolution of a key, 
	 * assumes that the key does not resolve to another
	 * key.
	 * 
	 * @param key the key that is resolved.
	 */
	private static void onKeyResolution(Object key) {
		onKeyResolution0(key); //calls native implementation - alas, triggers may not be native
	}
	
	private static native void onKeyResolution0(Object key);
	
	/**
	 * Upcalled by {@link #onKeyResolution(Object)}, 
	 * because it is easier to perform the check at
	 * the base-level (and triggers with two parameters
	 * are currently unsupported).
	 * 
	 * @param map the {@link JAVA_LINKEDMAP} containing {@code key}.
	 * @param key the key that is resolved.
	 */
	@SuppressWarnings("unchecked")
	private static <KK, VV> void onKeyResolutionComplete(JAVA_LINKEDMAP<KK, VV> tthis, KK key) {
		if (!tthis.isInitial) {
			throw new IllegalArgumentException("Attempted to invoke " + JAVA_LINKEDMAP.class.getCanonicalName() + ".onKeyResolutionComplete on a JAVA_MAP that is not initial.");
		}
		int occurrences = 0;
		if (key == null) {
			for (JAVA_LINKEDMAP.NNode n = tthis.root; n instanceof JAVA_LINKEDMAP.NNodePair; n = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) n).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> np = (JAVA_LINKEDMAP.NNodePair<KK, VV>) n;
				if (np.key == null) {
					++occurrences;
					assume(occurrences <= 1);
				}
			}
		} else {
			for (JAVA_LINKEDMAP.NNode n = tthis.root; n instanceof JAVA_LINKEDMAP.NNodePair; n = ((JAVA_LINKEDMAP.NNodePair<KK, VV>) n).next) {
				final JAVA_LINKEDMAP.NNodePair<KK, VV> np = (JAVA_LINKEDMAP.NNodePair<KK, VV>) n;
				if (key.equals(np.key)) {
					++occurrences;
					assume(occurrences <= 1);
				}
			}
		}
	}
	
	/**
	 * Upcalled by {@link #refineOnKeyAndBranch(Object)},
	 * {@link #refineOnKeyCombinationsAndBranch(Object...)}, 
	 * {@link #refineOnValueAndBranch(Object)}, and
	 * {@link #refineOnFreshEntryAndBranch()} 
	 * Refines this object by assuming that a key/value pair
	 * is present in it. This object must be initial.
	 * 
	 * @param key the key.
	 * @param value the value.
	 */
	private void refineIn(K key, V value) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a " + JAVA_LINKEDMAP.class.getCanonicalName() + " that is not initial.");
		}
		if (this.absentKeys.contains(key)) {
			ignore(); //contradiction found
		}
		addNode(key, value);
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
	private void refineOutKey(K key) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a " + JAVA_LINKEDMAP.class.getCanonicalName() + " that is not initial.");
		}
		if (findNodeKey(this.root, key) != null) {
			ignore(); //contradiction found
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
	private void refineOutValue(V value) {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a " + JAVA_LINKEDMAP.class.getCanonicalName() + " that is not initial.");
		}
		if (findNodeValue(this.root, value) != null) {
			ignore(); //contradiction found
		}
		this.absentValues.add(value);
	}

	/**
	 * Upcalled by {@link #refineOnFreshEntryAndBranch()}. 
	 * Refines this object by assuming that no more entries
	 * are present in it. This object must be initial.
	 * 
	 * @param value the value.
	 */
	private void refineMapComplete() {
		if (!this.isInitial) {
			metaThrowUnexpectedInternalException("Tried to refine a " + JAVA_LINKEDMAP.class.getCanonicalName() + " that is not initial.");
		}
		assume(this.size == this.numNodes);
	}
}
