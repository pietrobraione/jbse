package jbse.algo.meta;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getLength(File)} and
 * {@link java.io.WinNTFileSystem#getLength(File)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_XFILESYSTEM_GETLENGTH extends Algo_JAVA_XFILESYSTEM_CHECKGETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected String methodName() {
    	return "getLength";
    }
    
    @Override
    protected Object invokeMethod(Method method, Object fileSystem, File f) 
    throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
    	return method.invoke(fileSystem, f);
    }
}
