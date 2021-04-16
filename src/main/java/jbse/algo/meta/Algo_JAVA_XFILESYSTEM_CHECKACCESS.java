package jbse.algo.meta;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#checkAccess(File, int)} and
 * {@link java.io.WinNTFileSystem#checkAccess(File, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_XFILESYSTEM_CHECKACCESS extends Algo_JAVA_XFILESYSTEM_CHECKGETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }
    
    @Override
    protected String methodName() {
    	return "getLength";
    }
    
    @Override
    protected Object invokeMethod(Method method, Object fileSystem, File f) 
    throws InvocationTargetException, IllegalAccessException, IllegalArgumentException, SymbolicValueNotAllowedException {
        //gets the access parameter
        final Primitive accessPrimitive = (Primitive) this.data.operand(2);
        if (accessPrimitive.isSymbolic()) {
            throw new SymbolicValueNotAllowedException("The int access parameter to invocation of method " + fileSystemClass.getName() + "." + methodName() + " is symbolic.");
        }
        final int access = ((Integer) ((Simplex) accessPrimitive).getActualValue()).intValue();
        
    	return method.invoke(fileSystem, f, access);
    }
}
