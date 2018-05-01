import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * N-Way Set Associative Cache implementation.
 * Design description and explanation included in the design document.
 * Works in conjunction with CacheEntry class.
 * 
 * @author varun
 *
 * @param <K> key type
 * @param <V> value type
 */

public class NSetCache<K,V> {
	
	private boolean debug = false;
	
	private enum ReplaceAlgo {LRU,MRU,CUSTOM};
	private ReplaceAlgo replaceAlgo;
	
	private int setSize; //the "N" in NSetCache
	private int numSets; //number of sets
	
	private long atomicCount = System.nanoTime();
	
	ArrayList<HashSet<CacheEntry>> cacheList;
	
	/**
	 * Constructs the cache and sets the eviction algorithm to Least Recently Used (the most standard option).
	 * cacheSize is scaled up to make a whole number of sets.
	 * @param setSize the "N" in N-Way Set Associative Cache. 1=Direct Mapped cacheSize=fully associative
	 * @param cacheSize Total number of key,value pair capacity desired. The size of the cache is defined by the user as there is no corresponding main memory.
	 */
	
	public NSetCache(int setSize, int cacheSize){
		this.setSize = setSize;
		if(setSize>cacheSize) cacheSize = setSize;
		cacheSize += cacheSize % setSize;
		this.numSets = cacheSize/setSize;
		
		cacheList = new ArrayList<HashSet<CacheEntry>>(numSets);
		for(int i=0;i<numSets;i++){
			cacheList.add(i,new HashSet<CacheEntry>(setSize));
		}
		
		this.setEvictionAlgorithm(0);
		
	}
	
	
	/**
	 * Constructs the with a specified replacement algorithm.
	 * cacheSize is scaled up to make a whole number of sets.
	 * @param setSize the "N" in N-Way Set Associative Cache. 1=Direct Mapped cacheSize=fully associative
	 * @param cacheSizee Total number of key,value pair capacity desired. The size of the cache is defined by the user as there is no corresponding main memory. 
	 * @param replacementAlgo Enter an integer to correspond to replacement algorithm. 0=LRU. 1=MRU. 2=CUSTOM Default=LRU
	 */
	public NSetCache(int setSize, int cacheSize, int replacementAlgo){
		this.setSize = setSize;
		if(setSize>cacheSize) cacheSize = setSize;
		cacheSize += cacheSize % setSize;
		this.numSets = cacheSize/setSize;
		
		cacheList = new ArrayList<HashSet<CacheEntry>>(numSets);
		for(int i=0;i<numSets;i++){
			cacheList.add(i,new HashSet<CacheEntry>(setSize));
		}
		
		this.setEvictionAlgorithm(replacementAlgo);
	}
	
	
	/**
	 * Get method. Key object, must be the same (defined by hashcode) as entered
	 * Update the timestamp for the entry (for eviction algorithm).
	 * @param key requires same typing as set on cache declaration. 
	 * @return value Object if in the cache, or null
	 */
	public Object get(K key) {
		int hashKey = createKey(key);
		HashSet<CacheEntry> set = cacheList.get(getSetIndex(key));
		for(CacheEntry entry : set){
			if(entry.getHashKey()==hashKey){
				entry.updateTime(this.atomicCount++);
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Places object in the cache. 3 cases:
	 * 1. There is already an entry with the same key in the cache, replace and return the old value.
	 * 2. No entry with the same key and room in the set, add and return null.
	 * 3. No entry with the same key and no room in the set, evict based on algorithm, add, and return the old value.
	 * @param key type as defined by object declaration.
	 * @param value type as defined by object declaration.
	 * @return null if there is no replacement, previous value is there is replacement
	 */
	public Object put(K key, V value) {
		int setIndex = getSetIndex(key);
		CacheEntry newEntry = new CacheEntry(createKey(key),value,this.atomicCount++); 
		HashSet<CacheEntry> set = cacheList.get(setIndex);
		
		
		if(set.contains(newEntry)) { //cache hit, replace
			for(CacheEntry entry : set) {
				if(entry.equals(newEntry)) {
					set.remove(entry);
					set.add(newEntry);
					if(debug) System.out.println("update elt");
					return entry.getValue();
				}
			}
		} else if(set.size()<this.setSize) { //cache miss, has room
			if(debug) System.out.println("miss with room");
			set.add(newEntry);
		} else { //cache miss, full
			CacheEntry toEvict;
			switch(replaceAlgo) {
				case MRU:
					toEvict = evictMRU(set);
					break;
				case CUSTOM:
					toEvict = evictCustom(set);
					break;
				default:
					toEvict = evictLRU(set);
			}
			if(debug) System.out.println("evict"+toEvict);
			if(toEvict!=null){
				set.remove(toEvict);
				set.add(newEntry);
				return toEvict.getValue();
			} else{
				if(debug) System.out.println("evictprob");
			}
			
		}
		return null;
	}
	
	/**
	 * Remove the object from the cache given the specified key.
	 * @param key constrained by generics in cache declaration.
	 * @return The value that was previously in the cache, or null if the key is not found. 
	 */
	public Object remove(K key){
		int hashKey = createKey(key);
		HashSet<CacheEntry> set = cacheList.get(getSetIndex(key));
		for(CacheEntry entry : set){
			if(entry.getHashKey()==hashKey){
				set.remove(entry);
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @return returns how many objects are currently stored.
	 */
	public int size(){
		int size = 0;
		for(HashSet<CacheEntry> set : cacheList) {
			size+=set.size();
		}
		return size;
	}
	
	/**
	 * clear the cache of all entries
	 */
	public void clear() {
		for(HashSet<CacheEntry> set : cacheList) {
			set.clear();
		}
	}
	
	/**
	 * Compares internal timestamp of each entry.
	 * @param One of the sets from the cache.
	 * @return least recently used entry of the passed in set.
	 */
	private CacheEntry evictLRU(HashSet<CacheEntry> set){
		CacheEntry result = null;
		long minTime = Long.MAX_VALUE;
		for(CacheEntry entry : set){
			if(entry.getTime() < minTime) {
				result = entry;
				minTime = entry.getTime(); 
			}
		}
		return result;
	}
	
	/**
	 * Compares internal timestamp of each entry.
	 * @param One of the sets from the cache.
	 * @return most recently used entry of the passed in set.
	 */
	private CacheEntry evictMRU(HashSet<CacheEntry> set){
		CacheEntry result = null;
		long maxTime = 0;
		for(CacheEntry entry : set){
			if(entry.getTime() > maxTime) {
				result = entry;
				maxTime = entry.getTime(); 
			}
		}
		return result;
	}
	
	/**
	 * This method is meant to be overridden for the user to implement a custom replacement algorithm
	 * To do so, iterate through the set passed in a parameter. Relevant data for each CacheEntry is 
	 * available via public get methods. Due to overriding erasure issues in Java, the elements of the set
	 * are Objects and must be cast to CacheEntry to be accessed and returned. 
	 * An example custom algorithm is implemented (evict a random entry). 
	 * @param One of the sets from the cache.
	 * @return whichever the entry to evict is.
	 */
	protected CacheEntry evictCustom(HashSet set){
		if(debug) System.out.println("randomcall");
		int rand = new Random().nextInt(set.size());
		int count = 0;
		for(Object entry : set){
			if(count==rand) {
				return (CacheEntry)entry;
			}
			count++;
		}
		return null;
	}
	
	/**
	 * Offers the option to change the replacement algorithm after initialization.
	 * @param replacementAlgo An integer which corresponds to a replacement algorithm. 0=LRU. 1=MRU. 2=CUSTOM Default=LRU
	 */
	public void setEvictionAlgorithm(int replacementAlgo){
		switch(replacementAlgo) {
			case 0:
				replaceAlgo = ReplaceAlgo.LRU;
				break;
			case 1:
				replaceAlgo = ReplaceAlgo.MRU;
				break;
			case 2:
				replaceAlgo = ReplaceAlgo.CUSTOM;
				break;
			default:
				replaceAlgo = ReplaceAlgo.LRU;
		}
	}
	
	/**
	 * convenience method to determine which set to put a given object into. 
	 * @param key object
	 * @return an index denoting the set in the list of sets to select.
	 */
	private int getSetIndex(K key) {
		int hash = Math.abs(this.createKey(key));
		if(hash<numSets) {
			return hash;
		} else {
			return hash % numSets;
		}
		
	}
	
	/**
	 * no need to store the key object in each entry; just store the hashcode.
	 * can be overridden if different hashing is desired
	 * @param key
	 * @return integer hashcode
	 */
	protected int createKey(K key){
		return key.hashCode();
	}
	
	/**
	 * @return a string representation of the cache
	 */
	public String toString(){
		StringBuilder result = new StringBuilder();
		int line = 0;
		for(HashSet<CacheEntry> set : cacheList) {
			result.append(line+set.toString()+"\n");
			line++;
		}
		return result.toString();
	}
	

}
