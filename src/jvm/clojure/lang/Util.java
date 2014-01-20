/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Apr 19, 2008 */

package clojure.lang;

import java.lang.ref.Reference;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.SoftReference;
import java.lang.ref.ReferenceQueue;

public class Util{
static public boolean equiv(Object k1, Object k2){
	if(k1 == k2)
		return true;
	if(k1 != null)
		{
		if(k1 instanceof Number && k2 instanceof Number)
			return Numbers.equal((Number)k1, (Number)k2);
		else if(k1 instanceof IPersistentCollection || k2 instanceof IPersistentCollection)
			return pcequiv(k1,k2);
		return k1.equals(k2);
		}
	return false;
}

public interface EquivPred{
    boolean equiv(Object k1, Object k2);
}

static EquivPred equivNull = new EquivPred() {
        public boolean equiv(Object k1, Object k2) {
            return k2 == null;
        }
    };

static EquivPred equivEquals = new EquivPred(){
        public boolean equiv(Object k1, Object k2) {
            return k1.equals(k2);
        }
    };

static EquivPred equivNumber = new EquivPred(){
        public boolean equiv(Object k1, Object k2) {
            if(k2 instanceof Number)
                return Numbers.equal((Number) k1, (Number) k2);
            return false;
        }
    };

static EquivPred equivColl = new EquivPred(){
        public boolean equiv(Object k1, Object k2) {
            if(k1 instanceof IPersistentCollection || k2 instanceof IPersistentCollection)
                return pcequiv(k1, k2);
            return k1.equals(k2);
        }
    };

static public EquivPred equivPred(Object k1){
    if(k1 == null)
        return equivNull;
    else if (k1 instanceof Number)
        return equivNumber;
    else if (k1 instanceof String || k1 instanceof Symbol)
        return equivEquals;
    else if (k1 instanceof Collection || k1 instanceof Map)
        return equivColl;
    return equivEquals;
}

static public boolean equiv(long k1, long k2){
	return k1 == k2;
}

static public boolean equiv(Object k1, long k2){
	return equiv(k1, (Object)k2);
}

static public boolean equiv(long k1, Object k2){
	return equiv((Object)k1, k2);
}

static public boolean equiv(double k1, double k2){
	return k1 == k2;
}

static public boolean equiv(Object k1, double k2){
	return equiv(k1, (Object)k2);
}

static public boolean equiv(double k1, Object k2){
	return equiv((Object)k1, k2);
}

static public boolean equiv(boolean k1, boolean k2){
	return k1 == k2;
}

static public boolean equiv(Object k1, boolean k2){
	return equiv(k1, (Object)k2);
}

static public boolean equiv(boolean k1, Object k2){
	return equiv((Object)k1, k2);
}

static public boolean equiv(char c1, char c2) {
    return c1 == c2;
}

static public boolean pcequiv(Object k1, Object k2){
	if(k1 instanceof IPersistentCollection)
		return ((IPersistentCollection)k1).equiv(k2);
	return ((IPersistentCollection)k2).equiv(k1);
}

static public boolean equals(Object k1, Object k2){
	if(k1 == k2)
		return true;
	return k1 != null && k1.equals(k2);
}

static public boolean identical(Object k1, Object k2){
	return k1 == k2;
}

static public Class classOf(Object x){
	if(x != null)
		return x.getClass();
	return null;
}

static public int compare(Object k1, Object k2){
	if(k1 == k2)
		return 0;
	if(k1 != null)
		{
		if(k2 == null)
			return 1;
		if(k1 instanceof Number)
			return Numbers.compare((Number) k1, (Number) k2);
		return ((Comparable) k1).compareTo(k2);
		}
	return -1;
}

/* May optionally be used as the last mixing step.  Is a little bit
 * faster than mix, as it does no further mixing of the resulting
 * hash.  For the last element this is not necessary as the hash is
 * thoroughly mixed during finalization anyway. */
static public int murmurHash3MixLast(int hash, int data){
	int k = data;
	k *= 0xcc9e2d51;
	k = Integer.rotateLeft(k, 15);
	k *= 0x1b873593;
	return (hash ^ k);
}

/* Mix in a block of data into an intermediate hash value. */
static public int murmurHash3Mix(int hash, int data){
	int h = murmurHash3MixLast(hash, data);
	h = Integer.rotateLeft(h, 13);
	return (h * 5 + 0xe6546b64);
}

/** Force all bits of the hash to avalanche. Used for finalizing the hash. */
static public int murmurHash3Avalanche(int hash){
	int h = hash;
	h ^= h >>> 16;
	h *= 0x85ebca6b;
	h ^= h >>> 13;
	h *= 0xc2b2ae35;
	h ^= h >>> 16;
	return h;
}

/* Finalize a hash to incorporate the length and make sure all bits
 * avalanche. */
static public int murmurHash3FinalizeHash(int hash, int length){
	return murmurHash3Avalanche(hash ^ length);
}

/* Compute the hash of a product */
static public int murmurHash3ProductHash(Object x, int seed){
	APersistentVector v = (APersistentVector) x;
	int arr = v.count();
	int h = seed;
	int i = 0;
	while (i < arr) {
	    h = murmurHash3Mix(h, Util.hasheq(v.nth(i)));
	    i += 1;
	}
	return murmurHash3FinalizeHash(h, arr);
}

/* Compute the hash of a string */
static public int murmurHash3StringHash(String str, int seed){
	if (str == null) return 0;
	int h = seed;
	int i = 0;
	int len = str.length();
	while (i + 1 < len) {
		int data = (((int) str.charAt(i)) << 16) + ((int) str.charAt(i + 1));
		h = murmurHash3Mix(h, data);
		i += 2;
	}
	if (i < len)
		h = murmurHash3MixLast(h, (int) str.charAt(i));
	return murmurHash3FinalizeHash(h, len);
}

/* Compute a hash that is symmetric in its arguments - that is a hash
 * where the order of appearance of elements does not matter.  This is
 * useful for hashing sets, for example. */
static public int murmurHash3UnorderedHash(Object xs, int seed){
	int a = 0;
	int b = 0;
	int n = 0;
	int c = 1;
	for (ISeq s = RT.seq(xs); s != null; s = s.next()) {
		int h = Util.hasheq(s.first());
		a += h;
		b ^= h;
		if (h != 0)
			c *= h;
		n += 1;
	}
	int h = seed;
	h = murmurHash3Mix(h, a);
	h = murmurHash3Mix(h, b);
	h = murmurHash3MixLast(h, c);
	return murmurHash3FinalizeHash(h, n);
}

/* Compute a hash that depends on the order of its arguments. */
static public int murmurHash3OrderedHash(Object xs, int seed){
	int n = 0;
	int h = seed;
	for (ISeq s = RT.seq(xs); s != null; s = s.next()) {
		h = murmurHash3Mix(h, Util.hasheq(s.first()));
		n += 1;
	}
	return murmurHash3FinalizeHash(h, n);
}

/* Compute the hash of an array. */

static public int murmurHash3ArrayHash(Object a[], int seed){
	int h = seed;
	int i = 0;
	while (i < a.length) {
		h = murmurHash3Mix(h, Util.hasheq(a[i]));
		i += 1;
	}
	return murmurHash3FinalizeHash(h, a.length);
}

//public static final int arraySeed       = 0x3c074a61;
public static final int stringSeed      = 0xf7ca7fd2;
//public static final int productSeed     = 0xcafebabe;
//public static final int symmetricSeed   = 0xb592f7ae;
//public static final int traversableSeed = 0xe73a8b15;
public static final int seqSeed         = hasheq("Seq");
public static final int mapSeed         = hasheq("Map");
public static final int setSeed         = hasheq("Set");

static public int hash(Object o){
	if(o == null)
		return 0;
	return o.hashCode();
}

public static int hasheq(Object o){
	if(o == null)
		return 0;
	if(o instanceof IHashEq)
		return dohasheq((IHashEq) o);	
	if(o instanceof Number)
		return Numbers.hasheq((Number)o);
	return o.hashCode();
}

private static int dohasheq(IHashEq o) {
	return o.hasheq();
}

static public int hashCombine(int seed, int hash){
	//a la boost
	seed ^= hash + 0x9e3779b9 + (seed << 6) + (seed >> 2);
	return seed;
}

static public boolean isPrimitive(Class c){
	return c != null && c.isPrimitive() && !(c == Void.TYPE);
}

static public boolean isInteger(Object x){
	return x instanceof Integer
			|| x instanceof Long
	        || x instanceof BigInt
			|| x instanceof BigInteger;
}

static public Object ret1(Object ret, Object nil){
		return ret;
}

static public ISeq ret1(ISeq ret, Object nil){
		return ret;
}

static public <K,V> void clearCache(ReferenceQueue rq, ConcurrentHashMap<K, Reference<V>> cache){
		//cleanup any dead entries
	if(rq.poll() != null)
		{
		while(rq.poll() != null)
			;
		for(Map.Entry<K, Reference<V>> e : cache.entrySet())
			{
            Reference<V> val = e.getValue();
			if(val != null && val.get() == null)
				cache.remove(e.getKey(), val);
			}
		}
}

static public RuntimeException runtimeException(String s){
	return new RuntimeException(s);
}

static public RuntimeException runtimeException(String s, Throwable e){
	return new RuntimeException(s, e);
}

/**
 * Throw even checked exceptions without being required
 * to declare them or catch them. Suggested idiom:
 * <p>
 * <code>throw sneakyThrow( some exception );</code>
 */
static public RuntimeException sneakyThrow(Throwable t) {
    // http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
	if (t == null)
		throw new NullPointerException();
	Util.<RuntimeException>sneakyThrow0(t);
	return null;
}

@SuppressWarnings("unchecked")
static private <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
	throw (T) t;
}

}

