package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_UNIXFILESYSTEM;
import static jbse.common.Type.internalClassName;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getBooleanAttributes0(File)} and
 * {@link java.io.WinNTFileSystem#getBooleanAttributes(File)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX extends Algo_JAVA_XFILESYSTEM_CHECKGETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected String methodName() {
        final boolean isUnix = JAVA_UNIXFILESYSTEM.equals(internalClassName(this.fileSystemClass.getName()));
        return "getBooleanAttributes" + (isUnix ? "0" : "");
    }
    
    @Override
    protected Object invokeMethod(Method method, Object fileSystem, File f) 
    throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
    	return method.invoke(fileSystem, f);
    }
}
