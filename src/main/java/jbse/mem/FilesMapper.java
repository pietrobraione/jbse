package jbse.mem;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * Class mapping file identifiers (descriptors/handles) to 
 * (meta-level) open files.
 * 
 * @author Pietro Braione
 */
final class FilesMapper implements Cloneable {
	private static final String NULL_FILE_POSIX = "/dev/null";
	private static final String NULL_FILE_WINDOWS = "NUL";
	
    //gets reflectively some fields for later access
    private static final Field FIS_PATH;
    private static final Field FOS_PATH;
    private static final Field RAF_PATH;
    private static final Field FIS_IN;
    private static final Field FOS_OUT;
    private static final Field FILEDESCRIPTOR_FD;
    private static final Field FILEDESCRIPTOR_HANDLE;
    private static final FileInputStream INPUT_NULL;
    private static final FileOutputStream OUTPUT_NULL;
    private static final FileOutputStream ERROR_NULL;
    static {
        //these are present on all the operating systems
        try {
            FIS_PATH = FileInputStream.class.getDeclaredField("path");
            FOS_PATH = FileOutputStream.class.getDeclaredField("path");
            RAF_PATH = RandomAccessFile.class.getDeclaredField("path");
            FIS_IN = FilterInputStream.class.getDeclaredField("in");
            FOS_OUT = FilterOutputStream.class.getDeclaredField("out");
            FILEDESCRIPTOR_FD = FileDescriptor.class.getDeclaredField("fd");
        } catch (NoSuchFieldException | SecurityException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    	
    	//this is present only on Windows
    	Field fileDescriptorHandle;
        try {
        	fileDescriptorHandle = FileDescriptor.class.getDeclaredField("handle");
        } catch (NoSuchFieldException e) {
        	//we are not on Windows
        	fileDescriptorHandle = null;
        }
        FILEDESCRIPTOR_HANDLE = fileDescriptorHandle;
        
        //sets all Fields accessible
    	FIS_PATH.setAccessible(true);
    	FOS_PATH.setAccessible(true);
        FIS_IN.setAccessible(true);
        FOS_OUT.setAccessible(true);
        FILEDESCRIPTOR_FD.setAccessible(true);
    	if (FILEDESCRIPTOR_HANDLE != null) {
    		FILEDESCRIPTOR_HANDLE.setAccessible(true);
    	}
    	
    	//opens the null file
		try {
			if (FILEDESCRIPTOR_HANDLE == null) {
				//macOS and Linux
				INPUT_NULL = new FileInputStream(NULL_FILE_POSIX);
				OUTPUT_NULL = new FileOutputStream(NULL_FILE_POSIX);
				ERROR_NULL = new FileOutputStream(NULL_FILE_POSIX);
			} else {
				//Windows
				INPUT_NULL = new FileInputStream(NULL_FILE_WINDOWS);
				OUTPUT_NULL = new FileOutputStream(NULL_FILE_WINDOWS);
				ERROR_NULL = new FileOutputStream(NULL_FILE_WINDOWS);
			}
		} catch (FileNotFoundException e) {
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Class used to wrap {@link RandomAccessFile} information.
     * 
     * @author Pietro Braione
     *
     */
    private static final class RandomAccessFileWrapper {
        final RandomAccessFile raf;
        final String modeString;
        
        RandomAccessFileWrapper(RandomAccessFile raf, String modeString) {
            this.raf = raf;
            this.modeString = modeString;
        }
    }
        
    /** Maps file descriptors/handles to (meta-level) open files. */
    private HashMap<Long, Object> files = new HashMap<>();
    
    /** The file descriptor/handle of the (standard) input. */
    private final long inFileId;
    
    /** The file descriptor/handle of the (standard) output. */
    private final long outFileId;
    
    /** The file descriptor/handle of the (standard) error. */
    private final long errFileId;

    FilesMapper() {
        try {
            //gets the stdin
            FileInputStream in = null;
            for (InputStream is = System.in; (is instanceof FilterInputStream) || (is instanceof FileInputStream); is = (InputStream) FIS_IN.get(is)) {
                if (is instanceof FileInputStream) {
                    in = (FileInputStream) is;
                    break;
                }
            }
            
            //if in == null, considers the null file
            //as the stdin
            if (in == null) {
            	in = INPUT_NULL;
            }
            
        	//gets the stdin identifier 
            if (FILEDESCRIPTOR_HANDLE == null) {
            	this.inFileId = ((Integer) FILEDESCRIPTOR_FD.get(in.getFD())).longValue();
            } else {
            	this.inFileId = ((Long) FILEDESCRIPTOR_HANDLE.get(in.getFD())).longValue();
            }
            
            //registers the stdin
            setFile(this.inFileId, in);
            
            //gets the stdout
            FileOutputStream out = null;
            for (OutputStream os = System.out; (os instanceof FilterOutputStream) || (os instanceof FileOutputStream); os = (OutputStream) FOS_OUT.get(os)) {                
                if (os instanceof FileOutputStream) {
                    out = (FileOutputStream) os;
                    break;
                }
            }
            
            //if out == null, considers the null file
            //as the stdout
            if (out == null) {
            	out = OUTPUT_NULL;
            }
            
            //gets the stdout identifier
            if (FILEDESCRIPTOR_HANDLE == null) {
            	this.outFileId = ((Integer) FILEDESCRIPTOR_FD.get(out.getFD())).longValue();
            } else {
            	this.outFileId = ((Long) FILEDESCRIPTOR_HANDLE.get(out.getFD())).longValue();
            }
            
            //registers the stdout
            setFile(this.outFileId, out);
            
            //gets the stderr
            FileOutputStream err = null;
            for (OutputStream os = System.err; (os instanceof FilterOutputStream) || (os instanceof FileOutputStream); os = (OutputStream) FOS_OUT.get(os)) {
                if (os instanceof FileOutputStream) {
                    err = (FileOutputStream) os;
                    break;
                }
            }
            
            //if err == null, considers the null file
            //as the stderr
            if (err == null) {
            	err = ERROR_NULL;
            }
            
            //gets the stderr identifier
            if (FILEDESCRIPTOR_HANDLE == null) {
            	this.errFileId = ((Integer) FILEDESCRIPTOR_FD.get(err.getFD())).longValue();
            } else {
            	this.errFileId = ((Long) FILEDESCRIPTOR_HANDLE.get(err.getFD())).longValue();
            }
            
            //registers the stderr
            setFile(this.errFileId, err);
        } catch (IllegalArgumentException | IllegalAccessException | 
        InvalidInputException | IOException e) {
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Returns the file stream associated to a open file.
     * 
     * @param id a {@code long}, either a file descriptor cast to {@code long} 
     *        (if we are on a UNIX-like platform) or a file handle (if we are on Windows).
     * @return a {@link FileInputStream}, or a {@link FileOutputStream}, or a {@link RandomAccessFile}, or
     *         {@code null} if {@code id} is not the descriptor/handle
     *         of an open file previously associated with a call to {@link #setFile(long, Object)}.
     */
    public Object getFile(long id) {
        final Object obj = this.files.get(Long.valueOf(id));
        if (obj instanceof RandomAccessFileWrapper) {
            return ((RandomAccessFileWrapper) obj).raf;
        } else {
            return obj;
        }
    }
    
    /**
     * Associates a {@link FileInputStream} to an open file id.
     * 
     * @param id a {@code long}, either a file descriptor cast to {@code long} 
     *        (if we are on a UNIX-like platform) or a file handle (if we are on Windows).
     * @param file a {@link FileInputStream}.
     * @throws InvalidInputException if {@code file == null}.
     */
    public void setFile(long id, FileInputStream file) throws InvalidInputException {
        if (file == null) {
            throw new InvalidInputException("Invoked " + FilesMapper.class.getCanonicalName() + ".setFile with a null FileInputStream file parameter.");
        }
    	this.files.put(Long.valueOf(id), file);
    }
    
    /**
     * Associates a {@link FileOutputStream} to an open file id.
     * 
     * @param id a {@code long}, either a file descriptor cast to {@code long} 
     *        (if we are on a UNIX-like platform) or a file handle (if we are on Windows).
     * @param file a {@link FileOutputStream}.
     * @throws InvalidInputException if {@code file == null}.
     */
    public void setFile(long id, FileOutputStream file) throws InvalidInputException {
        if (file == null) {
            throw new InvalidInputException("Invoked " + FilesMapper.class.getCanonicalName() + ".setFile with a null FileOutputStream file parameter.");
        }
        this.files.put(Long.valueOf(id), file);
    }
    
    /**
     * Associates a {@link RandomAccessFile} to an open file id.
     * 
     * @param id a {@code long}, either a file descriptor cast to {@code long} 
     *        (if we are on a UNIX-like platform) or a file handle (if we are on Windows).
     * @param file a {@link RandomAccessFile}.
     * @param modeString a {@link String}, 
     * @throws InvalidInputException if {@code file == null} or {@code modeString == null}.
     */
    public void setFile(long id, RandomAccessFile file, String modeString) throws InvalidInputException {
        if (file == null || modeString == null) {
            throw new InvalidInputException("Invoked " + FilesMapper.class.getCanonicalName() + ".setFile with a null RandomAccessFile file or String modeString parameter.");
        }
        this.files.put(Long.valueOf(id), new RandomAccessFileWrapper(file, modeString));
    }
    
    /**
     * Removes an open file descriptor and its associated file stream.
     * 
     * @param id a {@code long}, the identifier of the open file to remove
     *        (if it is not a previously associated open file descriptor
     *        the method does nothing).
     */
    public void removeFile(long id) {
        this.files.remove(Long.valueOf(id));
    }
    
    @Override
    protected FilesMapper clone() {
    	final FilesMapper o;
    	try {
    		o = (FilesMapper) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    	
        try {
            o.files = new HashMap<>();
            for (Map.Entry<Long, Object> entry : this.files.entrySet()) {
                if (entry.getValue() instanceof FileInputStream) {
                    final FileInputStream fisClone;
                    if (entry.getKey() == this.inFileId) {
                        fisClone = (FileInputStream) entry.getValue();
                    } else {
                        final FileInputStream fisThis = (FileInputStream) entry.getValue();
                        final String path = (String) FIS_PATH.get(fisThis);
                        fisClone = new FileInputStream(path);
                        fisClone.skip(fisThis.getChannel().position());
                    }
                    o.files.put(entry.getKey(), fisClone);
                } else if (entry.getValue() instanceof FileOutputStream) {
                    final FileOutputStream fosClone;
                    if (entry.getKey() == this.outFileId ||  entry.getKey() == this.errFileId) {
                        fosClone = (FileOutputStream) entry.getValue();
                    } else {
                        final FileOutputStream fosThis = (FileOutputStream) entry.getValue();
                        final String path = (String) FOS_PATH.get(fosThis);
                        fosClone = new FileOutputStream(path);
                    }
                    o.files.put(entry.getKey(), fosClone);
                } else { //entry.getValue() instanceof RandomAccessFileWrapper
                    final RandomAccessFileWrapper rafWrapperThis = (RandomAccessFileWrapper) entry.getValue();
                    final String path = (String) RAF_PATH.get(rafWrapperThis.raf);
                    final RandomAccessFile rafClone = new RandomAccessFile(path, rafWrapperThis.modeString);
                    o.files.put(entry.getKey(), new RandomAccessFileWrapper(rafClone, rafWrapperThis.modeString));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        return o;
    }
    
    @Override
    protected void finalize() {
        //closes all files except stdin/out/err
        for (Map.Entry<Long, Object> fileEntry : this.files.entrySet()) {
            final long fileId = fileEntry.getKey();
            if (fileId == this.inFileId || fileId == this.outFileId || fileId == this.errFileId) {
                continue;
            }
            final Object file = fileEntry.getValue();
            try {
                if (file instanceof FileInputStream) {
                    ((FileInputStream) file).close();
                } else if (file instanceof FileOutputStream) {
                    ((FileOutputStream) file).close();
                } else { //file instanceof RandomAccessFileWrapper
                    ((RandomAccessFileWrapper) file).raf.close();
                }
            } catch (IOException e) {
                //go on with the next file
            }
        }
    }
}
