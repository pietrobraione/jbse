package jbse.mem;

/**
 * A Java object which may reside in the heap, 
 * i.e., either an instance of a class, or an array.
 */
public interface HeapObjekt extends Objekt {
	HeapObjekt clone();
}