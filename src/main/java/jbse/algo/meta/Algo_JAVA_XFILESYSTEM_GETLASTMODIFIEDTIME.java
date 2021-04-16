package jbse.algo.meta;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getLastModifiedTime(File)} and
 * {@link java.io.WinNTFileSystem#getLastModifiedTime(File)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME extends Algo_JAVA_XFILESYSTEM_CHECKGETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected String methodName() {
    	return "getLastModifiedTime";
    }
    
    @Override
    protected Object invokeMethod(Method method, Object fileSystem, File f) 
    throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
    	return method.invoke(fileSystem, f);
    }
}
