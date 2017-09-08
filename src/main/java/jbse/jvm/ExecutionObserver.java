package jbse.jvm;

/**
 * Interface for state observers. Observers are notified 
 * by the engine as their registration event arises.
 * 
 * @author Pietro Braione
 */
@FunctionalInterface
public interface ExecutionObserver {
    public void update(Engine e);
}