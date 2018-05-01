
/**
 * Wrapper class for key,value pairs inside NSetCache class.
 * @author varun
 *
 */
public class CacheEntry {
	
	private int key;
	private long timestamp;
	private Object value;
	
	
	/**
	 * 
	 * @param hashed key object for CacheEntry tracking and equivalence
	 * @param value object
	 * @param timestamp for eviction algorithms
	 */
	public CacheEntry(int key, Object value, long timestamp) {
		this.key = key;
		this.value = value;
		this.timestamp = timestamp;
	}
	
	public Object getValue(){
		return value;
	}
	
	public int getHashKey(){
		return key;
	}
	
	public void updateTime(long now){
		timestamp = now;
	}
	
	public long getTime(){
		return timestamp;
	}
	
	public String toString() {
		return "("+key+","+value.toString()+")";
	}
	
	/**
	 * equivalence is defined by the key value
	 */
	@Override
	public boolean equals(Object other){
	   if(this==other){
	      return true;
	   }
	   if(other instanceof CacheEntry){
	       CacheEntry ce = (CacheEntry) other;
	       return this.key == ce.key;
	   }
	   return false;
	}
	
	/**
	 * equivalence is defined by the key value 
	 */
	@Override
	public int hashCode(){
		return key;
	}


}
