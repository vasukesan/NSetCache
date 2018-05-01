import java.util.HashSet;

public class Main<K,V> extends NSetCache {

	public Main(int setSize, int cacheSize) {
		super(setSize, cacheSize);
	}
	
	protected CacheEntry evictCustom(HashSet set){
		for(Object entry : set) {
			return (CacheEntry)entry;
		}
		return null;
	}

	public static void main(String[] args) {
		Main<Integer,String> cache = new Main<Integer,String>(10,400);
		String st = "testVal";
		for(int i=0;i<250;i++){
			cache.put(i,i+st);
		}
		//System.out.println(cache.get(102));
		//System.out.println(cache);
	
		
	}

}
