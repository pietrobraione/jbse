package jbse.mem;

import static jbse.common.Util.unsafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * Maps memory addresses to several internal structures
 * (memory blocks, inflaters...)
 * 
 * @author Pietro Braione
 *
 */
final class MemoryAddressesMapper implements Cloneable {
    /**
     * Class that stores the information about a raw memory
     * block allocated to support {@link sun.misc.Unsafe}
     * raw allocation methods.
     * 
     * @author Pietro Braione
     */
	private static final class MemoryBlock {
        /** The base address of the memory block. */
        final long address;
        
        /** The size in bytes of the memory block. */
        final long size;
        
        MemoryBlock(long address, long size) {
            this.address = address;
            this.size = size;
        }        
    }
    
    /**
     * Class that stores information about an open
     * zip file to support {@link java.util.zip.ZipFile}
     * and {@link java.util.jar.JarFile} native methods.
     * 
     * @author Pietro Braione
     */
    private static final class ZipFile {
        /** 
         * The address of a jzfile C data structure for the
         * entry. 
         */
        final long jzfile;
        
        /** The name of the file. */
        final String name;
        
        /** The file opening mode. */
        final int mode;
        
        /** When the file was modified. */
        final long lastModified;
        
        /** Should we use mmap? */
        final boolean usemmap;
        
        ZipFile(long jzfile, String name, int mode, long lastModified, boolean usemmap) {
            this.jzfile = jzfile;
            this.name = name;
            this.mode = mode;
            this.lastModified = lastModified;
            this.usemmap = usemmap;
        }
    }
    
    /**
     * Class that stores the information about an open
     * zip file's entries to support {@link java.util.zip.ZipFile}
     * and {@link java.util.jar.JarFile} native methods.
     * 
     * @author Pietro Braione
     */
    private static final class ZipFileEntry {
        /** 
         * The address of a jzentry C data structure for the
         * entry. 
         */
        final long jzentry;
        
        /** 
         * The (base-level) jzfile for the file this entry
         * belongs to. 
         */
        final long jzfile;
        
        /** The name of the entry. */
        final byte[] name;
        
        ZipFileEntry(long jzentry, long jzfile, byte[] name) {
            this.jzentry = jzentry;
            this.jzfile = jzfile;
            this.name = name.clone();
        }
    }
    
    private static final class Inflater {
        final long address;
        
        final boolean nowrap;
        
        final byte[] dictionary;
        
        Inflater(long address, boolean nowrap, byte[] dictionary, int off, int len) {
            this.address = address;
            this.nowrap = nowrap;
            if (dictionary == null) {
                this.dictionary = null;
            } else {
                this.dictionary = new byte[len];
                System.arraycopy(dictionary, off, this.dictionary, 0, len);
            }
        }
        
        Inflater(long address, boolean nowrap) {
            this(address, nowrap, null, 0, 0);
        }
    }
    
    /** Maps memory addresses to (meta-level) allocated memory blocks. */
    private HashMap<Long, MemoryBlock> allocatedMemory = new HashMap<>();
    
    /** 
     * Maps (base-level) jzfile C structure addresses to 
     * (meta-level) open zip files. 
     */
    private HashMap<Long, ZipFile> zipFiles = new HashMap<>();
    
    /** 
     * Maps (base-level) jzentry C structure addresses to 
     * (meta-level) open zip file entries.
     */
    private HashMap<Long, ZipFileEntry> zipFileEntries = new HashMap<>();
    
    /** Maps (base-level) inflater addresses to (meta-level) inflaters. */
    private HashMap<Long, Inflater> inflaters = new HashMap<>();
    
    /**
     * Registers a raw memory block.
     * 
     * @param address a {@code long}, the base address of the memory block.
     * @param size a {@code long}, the size in bytes of the memory block.
     * @throws InvalidInputException if 
     *         {@code address} is already a registered memory block base 
     *         address, or if {@code size <= 0}.
     */
    public void addMemoryBlock(long address, long size) throws InvalidInputException {
        if (this.allocatedMemory.containsKey(address)) {
            throw new InvalidInputException("Tried to add a raw memory block with an already known address");
        }
        if (size <= 0) {
            throw new InvalidInputException("Tried to add a raw memory block with a nonpositive size");
        }
        this.allocatedMemory.put(address, new MemoryBlock(address, size));
    }
    
    /**
     * Returns the base address of a memory block.
     * 
     * @param address a {@code long}, the address as known by this {@link State}
     *        (base-level address).
     * @return a {@code long}, the true base address of the memory block
     *         (meta-level address).
     * @throws InvalidInputException if {@code address} is not a memory block
     *         address previously registered by a call to {@link #addMemoryBlock(long, long) addMemoryBlock}.
     */
    public long getMemoryBlockAddress(long address) throws InvalidInputException {
        if (!this.allocatedMemory.containsKey(address)) {
            throw new InvalidInputException("Tried to get the address of a raw memory block corresponding to an unknown (base-level) address");
        }
        return this.allocatedMemory.get(address).address;
    }

    /**
     * Returns the size of a memory block.
     * 
     * @param address a {@code long}, the address as known by this {@link State}
     *        (base-level address).
     * @return a {@code long}, the size in bytes of the memory block.
     * @throws InvalidInputException if {@code address} is not a memory block
     *         address previously registered by a call to {@link #addMemoryBlock(long, long) addMemoryBlock}.
     */
    public long getMemoryBlockSize(long address) throws InvalidInputException {
        if (!this.allocatedMemory.containsKey(address)) {
            throw new InvalidInputException("Tried to get the size of a raw memory block corresponding to an unknown (base-level) address");
        }
        return this.allocatedMemory.get(address).size;
    }

    /**
     * Removes the registration of a memory block.
     * 
     * @param address a {@code long}, the address as known by this {@link State}
     *        (base-level address).
     * @throws InvalidInputException if {@code address} 
     *         is not a memory block address previously registered by a call to 
     *         {@link #addMemoryBlock(long, long) addMemoryBlock}.
     */
    public void removeMemoryBlock(long address) throws InvalidInputException {
        if (!this.allocatedMemory.containsKey(address)) {
            throw new InvalidInputException("Tried to remove a raw memory block corresponding to an unknown (base-level) address");
        }
        this.allocatedMemory.remove(address);
    }
    
    /**
     * Adds a zip file.
     * 
     * @param jzfile a {@code long}, the address of a jzfile C data structure
     *        for the open zip file.
     * @param name a {@link String}, the name of the file.
     * @param mode an {@code int}, the mode this zip file was opened.
     * @param lastModified a {@code long}, when this zip file was last modified.
     * @param usemmap a {@code boolean}, whether mmap was used when this zip file
     *        was opened.
     * @throws InvalidInputException if 
     *         {@code jzfile} was already added before, or
     *         {@code name == null}.
     */
    public void addZipFile(long jzfile, String name, int mode, long lastModified, boolean usemmap) 
    throws InvalidInputException {
        if (this.zipFiles.containsKey(jzfile)) {
            final ZipFile zf = this.zipFiles.get(jzfile);
            if (zf.name.equals(name) && zf.lastModified == lastModified) {
                //already opened, zlib reuses the allocated C data structure
                return;
            } else {
                throw new InvalidInputException("Tried to add a zipfile with an already existing jzfile address");
            }
        }
        if (name == null) {
            throw new InvalidInputException("Tried to add a zip file with null name");
        }
        final ZipFile zf = new ZipFile(jzfile, name, mode, lastModified, usemmap);
        this.zipFiles.put(jzfile, zf);
    }
    
    /**
     * Adds a zip file entry.
     * 
     * @param jzentry a {@code long}, the address of a jzentry C data structure
     *        for the open zip file entry.
     * @param jzfile a {@code long}, a jzfile address as known by this {@link State}
     *        (base-level address).
     * @param name a {@code byte[]}, the name of the entry.
     * @throws InvalidInputException if {@code jzfile} was 
     *         not added before by a call to
     *         {@link #addZipFile(long, String, int, long, boolean) addZipFile}, or
     *         {@code jzentry} was already added before, or
     *         {@code name == null}.
     */
    public void addZipFileEntry(long jzentry, long jzfile, byte[] name) throws InvalidInputException {
        if (!this.zipFiles.containsKey(jzfile)) {
            throw new InvalidInputException("Tried to add a zip file entry for an unknown zip file.");
        }
        if (this.zipFileEntries.containsKey(jzentry)) {
            throw new InvalidInputException("Tried to add an already existing zip file entry.");
        }
        if (name == null) {
            throw new InvalidInputException("Tried to add a zip file entry with null name.");
        }
        final ZipFileEntry zfe = new ZipFileEntry(jzentry, jzfile, name);
        this.zipFileEntries.put(jzentry, zfe);
    }
    
    /**
     * Checks whether an open zip file exists.
     * 
     * @param jzfile a {@code long}, the address of a jzentry C data structure
     *        as known by this {@link State} (base-level address).
     * @return {@code true} iff {@code jzfile} was added before by a call to
     *         {@link #addZipFile(long, String, int, long, boolean) addZipFile}.
     */
    public boolean hasZipFile(long jzfile) {
        return this.zipFiles.containsKey(jzfile);
    }
    
    /**
     * Checks whether an address of a jzentry C structure is present.
     * 
     * @param jzentry a {@code long}.
     * @return {@code true} iff {@code jzentry} is the true address of 
     *         a jzfile C structure (meta-level address).
     */
    public boolean hasZipFileEntryJzInverse(long jzentry) {
        for (ZipFileEntry entry : this.zipFileEntries.values()) {
            if (entry.jzentry == jzentry) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the base-level address of a jzentry C structure.
     * 
     * @param jzentry a {@code long}, the  address of a jzfile C structure
     *         (meta-level address).
     * @return a {@code long}, the base-level address corresponding to
     *         {@code jzentry}.
     * @throws InvalidInputException if {@code jzentry} is not the address
     *         of a jzentry data structure.
     */
    public long getZipFileEntryJzInverse(long jzentry) throws InvalidInputException {
        for (Map.Entry<Long, ZipFileEntry> entry : this.zipFileEntries.entrySet()) {
            if (entry.getValue().jzentry == jzentry) {
                return entry.getKey();
            }
        }
        throw new InvalidInputException("Tried to invert an unknown jzentry C structure address");
    }
    
    /**
     * Gets the address of a jzfile C structure.
     * 
     * @param jzfile a {@code long}, the address as known by this {@link State} 
     *        (base-level address).
     * @return a {@code long}, the true address of the jzfile C structure
     *         (meta-level address).
     * @throws InvalidInputException if {@code jzfile} was not added before by a call to
     *         {@link #addZipFile(long, String, int, long, boolean) addZipFile}.
     */
    public long getZipFileJz(long jzfile) throws InvalidInputException {
        if (!this.zipFiles.containsKey(jzfile)) {
            throw new InvalidInputException("Tried to get a jzfile for an unknown zip file");
        }
        return this.zipFiles.get(jzfile).jzfile;
    }
    
    /**
     * Gets the address of a jzentry C structure.
     * 
     * @param jzentry a {@code long}, the address as known by this {@link State} 
     *        (base-level address).
     * @return a {@code long}, the true address of the jzentry C structure
     *         (meta-level address).
     * @throws InvalidInputException if {@code jzentry} was not added before by a call to
     *         {@link #addZipFileEntry(long, long, byte[]) addZipFileEntry}.
     */
    public long getZipFileEntryJz(long jzentry) throws InvalidInputException {
        if (!this.zipFileEntries.containsKey(jzentry)) {
            throw new InvalidInputException("Tried to get a jzentry for an unknown zip file entry");
        }
        return this.zipFileEntries.get(jzentry).jzentry;
    }
    
    /**
     * Removes a zip file and all its associated entries.
     * 
     * @param jzfile a {@code long}, the address of a jzfile C structure as known 
     *        by this {@link State} (base-level address).
     * @throws InvalidInputException if {@code jzfile} was 
     *         not added before by a call to
     *         {@link #addZipFile(long, String, int, long, boolean) addZipFile}.
     */
    public void removeZipFile(long jzfile) throws InvalidInputException {
        if (!this.zipFiles.containsKey(jzfile)) {
            throw new InvalidInputException("Tried to remove an unknown zip file");
        }
        this.zipFiles.remove(jzfile);
        final HashSet<Long> toRemove = new HashSet<>();
        for (Map.Entry<Long, ZipFileEntry> entry : this.zipFileEntries.entrySet()) {
            if (entry.getValue().jzfile == jzfile) {
                toRemove.add(entry.getKey());
            }
        }
        for (long jzentry : toRemove) {
            this.zipFileEntries.remove(jzentry);
        }
    }
    
    /**
     * Removes a zip file entry.
     * 
     * @param jzentry a {@code long}, the address of a jzentry C structure as known 
     *        by this {@link State} (base-level address).
     * @throws InvalidInputException if {@code jzentry} 
     *         was not added before by a call to
     *         {@link #addZipFileEntry(long, long, byte[]) addZipFileEntry}.
     */
    public void removeZipFileEntry(long jzentry) throws InvalidInputException {
        if (!this.zipFileEntries.containsKey(jzentry)) {
            throw new InvalidInputException("Tried to remove an unknown zip file entry");
        }
        this.zipFileEntries.remove(jzentry);
    }
    
    /**
     * Registers an inflater.
     * 
     * @param address a {@code long}, the address of an inflater block.
     * @param nowrap a {@code boolean}, the {@code nowrap} parameter 
     *        to {@link java.util.zip.Inflater#init(boolean)}.
     * @throws InvalidInputException if 
     *         {@code address} was already registered.
     */
    public void addInflater(long address, boolean nowrap) throws InvalidInputException {
        if (this.inflaters.containsKey(address)) {
            throw new InvalidInputException("Tried to add an already registered inflater block address");
        }
        final Inflater inflater = new Inflater(address, nowrap);
        this.inflaters.put(address, inflater);
    }
    
    /**
     * Gets the address of an inflater block.
     * 
     * @param address a {@code long}, the address of an inflater block
     *        as known by this state (base-level address).
     * @return a {@code long}, the true address of the inflater block
     *         (meta-level address).
     * @throws InvalidInputException  if {@code address} was not previously
     *         registered.
     */
    public long getInflater(long address) throws InvalidInputException {
        if (!this.inflaters.containsKey(address)) {
            throw new InvalidInputException("Tried to get the address of an unknown inflater");
        }
        return this.inflaters.get(address).address;
    }
    
    /**
     * Stores the dictionary of an inflater.
     * 
     * @param address a {@code long}, the address of an inflater block
     *        as known by this state (base-level address).
     * @param dictionary a {@code byte[]} containing the dictionary.
     * @param ofst a {@code int}, the offset in {@code dictionary}
     *        where the dictionary starts.
     * @param len a {@code int}, the length of the dictionary.
     * @throws InvalidInputException if 
     *         {@code address} was not previously
     *         registered, or {@code dictionary == null}, or {@code ofst < 0}, 
     *         or {@code len < 0}, or {@code ofst >= dictionary.length}, or
     *         {@code ofst + len > dictionary.length}.
     */
    public void setInflaterDictionary(long address, byte[] dictionary, int ofst, int len) 
    throws InvalidInputException {
        if (!this.inflaters.containsKey(address)) {
            throw new InvalidInputException("Tried to set the dictionary of an unknown inflater");
        }
        if (dictionary == null || ofst < 0 || len < 0 || ofst >= dictionary.length || ofst + len > dictionary.length) {
            throw new InvalidInputException("Tried to set the dictionary of an inflater with wrong dictionary, offset or length");
        }
        final Inflater inflaterOld = this.inflaters.get(address);
        final Inflater inflaterNew = new Inflater(inflaterOld.address, inflaterOld.nowrap, dictionary, ofst, len);
        this.inflaters.put(address, inflaterNew);
    }
    
    /**
     * Removes a registered inflater.
     * 
     * @param address a {@code long}, the address of an inflater block
     *        as known by this state (base-level address).
     * @throws InvalidInputException if  
     *         {@code address} was not previously registered.
     */
    public void removeInflater(long address) throws InvalidInputException {
        if (!this.inflaters.containsKey(address)) {
            throw new InvalidInputException("Tried to remove an unknown inflater.");
        }
        this.inflaters.remove(address);
    }
    
    @Override
    protected MemoryAddressesMapper clone() {
        final MemoryAddressesMapper o;
        try {
            o = (MemoryAddressesMapper) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        //allocatedMemory
        o.allocatedMemory = new HashMap<>();
        for (Map.Entry<Long, MemoryBlock> entry : this.allocatedMemory.entrySet()) {
            final long baseLevelAddress = entry.getKey();
            final long oldMemoryBlockAddress = entry.getValue().address;
            final long size = entry.getValue().size;
            final long newMemoryBlockAddress = unsafe().allocateMemory(size);
            unsafe().copyMemory(oldMemoryBlockAddress, newMemoryBlockAddress, size);
            o.allocatedMemory.put(baseLevelAddress, new MemoryBlock(newMemoryBlockAddress, size));
        }
        
        //zipFiles
        o.zipFiles = new HashMap<>();
        try {
            final Method methodOpen = java.util.zip.ZipFile.class.getDeclaredMethod("open", String.class, int.class, long.class, boolean.class);
            methodOpen.setAccessible(true);
            for (Map.Entry<Long, ZipFile> entry : this.zipFiles.entrySet()) {
                final ZipFile zf = entry.getValue();
                final String name = zf.name;
                final int mode = zf.mode;
                final long lastModified = zf.lastModified;
                final boolean usemmap = zf.usemmap;
                final long jzfileNew = (long) methodOpen.invoke(null, name, mode, lastModified, usemmap);
                final ZipFile zfNew = new ZipFile(jzfileNew, name, mode, lastModified, usemmap);
                o.zipFiles.put(entry.getKey(), zfNew);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
                 NoSuchMethodException | SecurityException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        //zipFileEntries
        o.zipFileEntries = new HashMap<>();
        try {
            final Method methodGetEntry = java.util.zip.ZipFile.class.getDeclaredMethod("getEntry", long.class, byte[].class, boolean.class);
            methodGetEntry.setAccessible(true);
            for (Map.Entry<Long, ZipFileEntry> entry : this.zipFileEntries.entrySet()) {
                final ZipFileEntry zfe = entry.getValue();
                final long _jzfile = zfe.jzfile;
                final long jzfile = o.zipFiles.get(_jzfile).jzfile;
                final byte[] name = zfe.name;
                final long jzentryNew = (long) methodGetEntry.invoke(null, jzfile, name, true);
                final ZipFileEntry zfeNew = new ZipFileEntry(jzentryNew, _jzfile, name);
                o.zipFileEntries.put(entry.getKey(), zfeNew);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
                 IllegalArgumentException | InvocationTargetException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        //inflaters
        o.inflaters = new HashMap<>();
        try {
            final Method methodInit = java.util.zip.Inflater.class.getDeclaredMethod("init", boolean.class);
            methodInit.setAccessible(true);
            final Method methodSetDictionary = java.util.zip.Inflater.class.getDeclaredMethod("setDictionary", long.class, byte[].class, int.class, int.class);
            methodSetDictionary.setAccessible(true);
            for (Map.Entry<Long, Inflater> entry : this.inflaters.entrySet()) {
                final Inflater inf = entry.getValue();
                final long addressNew = (long) methodInit.invoke(null, inf.nowrap);
                final Inflater infNew;
                if (inf.dictionary == null) {
                    infNew = new Inflater(addressNew, inf.nowrap);
                } else {
                    methodSetDictionary.invoke(null, addressNew, inf.dictionary, 0, inf.dictionary.length);
                    infNew = new Inflater(addressNew, inf.nowrap, inf.dictionary, 0, inf.dictionary.length);
                }
                o.inflaters.put(entry.getKey(), infNew);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
                 IllegalArgumentException | InvocationTargetException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        return o;
    }
    
    @Override
    protected void finalize() {        
        //deallocates all memory blocks
        for (MemoryBlock memoryBlock : this.allocatedMemory.values()) {
        	unsafe().freeMemory(memoryBlock.address);
        }
    }
}
