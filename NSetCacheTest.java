import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for NSetCache
 * @author varun
 *
 */

public class NSetCacheTest {
	
	NSetCache<Integer,String> cache = new NSetCache<Integer,String>(2,7,0);;

	@Before //executed before every test
	public void setUp() {
		cache.clear();
	}
	
	@Test
	public void testTyping() {		
		NSetCache<String,Integer> c2 = new NSetCache<String,Integer>(2,8,0);
		String st = "testVal";
		for(int i=0;i<8;i++){
			c2.put(i+st,i);
			assertEquals(i,c2.get(i+st));
		}
		
		NSetCache<CacheEntry,CacheEntry> c3= new NSetCache<CacheEntry,CacheEntry>(2,8,0);
		for(int i=0;i<8;i++){
			CacheEntry entry = new CacheEntry(i,i,i);
			CacheEntry entry2 = new CacheEntry(i,i,i);
			c3.put(entry,entry2);
			assertEquals(entry2,c3.get(entry));
		}
		
		
		
	}

	@Test
	public void testEmptyPut() {		
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		
	}
	
	@Test
	public void testGet() {		
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		for(int i=0;i<8;i++){
			assertEquals(i+st,cache.get(i));
		}
		assertNull("get nonexistent", cache.get(10));
	}
	
	@Test
	public void testRemove() {		
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		for(int i=0;i<4;i++){
			assertEquals(i+st,cache.remove(i));
		}
		assertNull("remove nonexistent", cache.remove(0));
		assertNull("remove nonexistent", cache.remove(10));
	}
	
	@Test
	public void testUpdatePut() {		
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("update put",cache.put(i,i+st));
		}
		for(int i=0;i<8;i++){
			assertEquals(i+st,cache.put(i,"new"+i+st));
		}
		
	}
	
	@Test
	public void testEvictionLRU() {		
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		
		//check LRU eviction 
		for(int i=8;i<16;i++){
			assertEquals((i-8)+st,cache.put(i,i+"testval"));
		}
		
		
		//check get timestamp update
		cache.clear();
		for(int i=0;i<2;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		assertEquals(0+st,cache.get(0));
		assertEquals(1+st,cache.put(1, 1+st));
		
	}
	
	@Test
	public void testEvictionMRU() {		
		cache.setEvictionAlgorithm(1);
		String st = "testVal";
		for(int i=0;i<8;i++){
			assertNull("empty put",cache.put(i,i+st));
		}
		
		//check MRU eviction 
		for(int i=8;i<16;i++){
			assertEquals((i-4)+st,cache.put(i,i+st));
		}
		cache.setEvictionAlgorithm(0);
	}
	
	@Test
	public void testClearAndSize() {		
		String st = "testVal";
		
		//test clear
		assertEquals(0,cache.size());
		
		//test size
		for(int i=0;i<9;i++){
			cache.put(i,i+st);
		}
		assertEquals(8,cache.size());
		cache.remove(0);
		cache.remove(6);
		assertEquals(7,cache.size());
	}
	
	@Test
	public void testCustomEvict(){
		class CustomCache<K,V> extends NSetCache<K,V> {

			public CustomCache(int setSize, int cacheSize) {
				super(setSize, cacheSize);
			}
			
			protected CacheEntry evictCustom(HashSet set){
				CacheEntry result = null;
				int minHash = Integer.MAX_VALUE;
				for(Object oEntry : set){
					CacheEntry entry = (CacheEntry)oEntry;
					if(entry.getHashKey() < minHash) {
						result = entry;
						minHash = entry.getHashKey(); 
					}
				}
				return result;
			}
		}

		CustomCache<Integer,String> custCache = new CustomCache<Integer,String>(10,100);
		custCache.setEvictionAlgorithm(2);
		String st = "testVal";
		for(int i=0;i<100;i++){
			assertNull("empty put",custCache.put(i,i+st));
		}
		assertEquals(0+st,custCache.put(110,10+st+10));

		}
	
	

}
