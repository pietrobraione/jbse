package jbse.mem;

import static jbse.common.Type.isCat_1;
import static jbse.common.Type.UNKNOWN;
import static jbse.mem.Slot.slotMayReceiveValueWeak;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jbse.bc.LocalVariableTable;
import jbse.bc.LocalVariableTable.Row;
import jbse.mem.exc.InvalidSlotException;
import jbse.val.DefaultValue;
import jbse.val.Value;

/**
 * Class representing a local variable memory area.
 */
class LocalVariablesArea implements Cloneable {
    /** The local variable table for the method. */
    private final LocalVariableTable lvt;

    /** The local variable type table for the method. */
    private final LocalVariableTable lvtt;

    /** Values in the memory area, accessible by slot. */
    private TreeMap<Integer, Value> values = new TreeMap<>();

    /**
     * Constructor.
     * 
     * @param lvt a {@link LocalVariableTable}, the local variable table.
     * @param lvtt a {@link LocalVariableTable}, the local variable type table.
     */
    LocalVariablesArea(LocalVariableTable lvt, LocalVariableTable lvtt) {
        this.lvt = lvt;
        this.lvtt = lvtt;
    }

    /**
     * Initializes the local variables by an array 
     * of {@link Value}s.
     * 
     * @param args a {@link Value}{@code []}; The 
     *        local variables are initialized in sequence 
     *        with these values. If there are less values 
     *        in {@code args} than local variables in this 
     *        object, the remaining variables are initialized 
     *        to {@link DefaultValue}s.
     * @throws InvalidSlotException when there are 
     *         too many {@code arg}s or some of their types are 
     *         incompatible with their respective slots types.
     */
    void setArgs(Value[] args) throws InvalidSlotException {
        int nargs = (args == null ? 0 : args.length);
        int j = 0;
        int slot = 0;
        while (slot < this.lvt.getSlots()) {
            final Value val = (j < nargs) ? args[j] : DefaultValue.getInstance();
            set(slot, 0, val);
            ++j;
            slot += isCat_1(val.getType()) ? 1 : 2;
        }
    }

    /**
     * Stores a value into a specific slot of the local variable area.
     * 
     * @param slot an {@code int}, the slot of the local variable.
     * @param currentProgramCounter the current program counter; if the local variable
     *        table contains type information about the local variable, this
     *        is used to check type conformance of the access.
     * @param val the {@link Value} to be stored.  
     * @throws InvalidSlotException when {@code slot} is out of range or
     *         {@code val}'s type is incompatible with the slot's type.
     */
    void set(int slot, int currentProgramCounter, Value val) throws InvalidSlotException {
        final int nslots = (isCat_1(val.getType()) ? 1 : 2);
        if (slot < 0 || slot > this.lvt.getSlots() - nslots) {
            throw new InvalidSlotException("Slot number " + slot + " is out of range.");
        }

        final Row r = this.lvt.row(slot, currentProgramCounter);
        if (!localVariableMayReceiveValue(r, val)) {
            throw new InvalidSlotException("Slot number " + slot + " has wrong type.");
        }

        if (nslots == 2) {
            this.values.remove(slot + 1);
        }

        //stores val at slot
        this.values.put(slot, val);
    }
    
    private static boolean localVariableMayReceiveValue(Row r, Value val) {
        if (r == null) {
        	return true;
        } else {
        	return slotMayReceiveValueWeak(r.descriptor, val.getType());
        }
    }

    /**
     * Returns the value of a local variable.
     * 
     * @param slot an {@code int}, the slot of the local variable.
     * @return a {@link Value}, the one stored in the local variable.
     * @throws InvalidSlotException if {@code slot} is not a valid slot number.
     */
    Value get(int slot) throws InvalidSlotException {
        Value retVal = this.values.get(slot);

        //the next case denotes, e.g., we wrote a cat2 value at slot x
        //and we try to read at slot x+1. 
        // TODO investigate the JVM spec and decide what to do.
        if (retVal == null) {
            throw new InvalidSlotException("Slot " + slot + " was not written.");
        }

        //the next case should never happen in verified code, however 
        //the JVM specification does not explicitly exclude it. 
        if (DefaultValue.getInstance().equals(retVal))
            ;
        /* TODO Should we throw InvalidSlotException, or rather return 
         * the true default value instead, which can be inferred from the 
         * bytecode??? Note that the latter case would require deep changes 
         * and perhaps is best done inside the load bytecode algorithm classes. 
         * Here we chose to return the DefaultValue, in order to better support 
         * prettyprinting. Externally no support to DefaultValue handling 
         * is implemented, but it shouldn't permeate with well-behaving 
         * code so this can be added to the todo list without much concern.
         */

        return retVal;
    }

    /**
     * Returns all the slots of the local variable area.
     * 
     * @return a {@link Set}{@code <}{@link Integer}{@code >} 
     *         containing all the valid slot numbers of this local variable
     *         area.
     */
    Set<Integer> slots() {
        return this.values.keySet();
    }

    /**
     * Returns the name of a local variable as declared in the 
     * debug information of the class.
     *  
     * @param slot the number of the slot of a local variable.
     * @param currentProgramCounter the current program counter.
     * @return a {@link String} containing the name of the local
     *         variable at {@code slot} as from the available debug 
     *         information, or {@code null} if no debug information is 
     *         available for the {@code (slot, curPC)} combination.
     */
    String getLocalVariableDeclaredName(int slot, int currentProgramCounter) {
        final LocalVariableTable.Row r = this.lvt.row(slot, currentProgramCounter);
        return (r == null ? null : r.name);
    }

    /**
     * Builds a local variable.
     * 
     * @param slot the number of the slot.
     * @param currentProgramCounter the current program counter.
     * @return a {@link Variable} at position {@code slot}.
     * @throws InvalidSlotException if {@code slot} is invalid.
     */
    Variable buildLocalVariable(int slot, int currentProgramCounter) throws InvalidSlotException {
        //gets the value
        final Value val = get(slot);

        //finds static information
        final LocalVariableTable.Row lvtRow = this.lvt.row(slot, currentProgramCounter);
        final LocalVariableTable.Row lvttRow = this.lvtt.row(slot, currentProgramCounter);
        final Variable retVal;
        if (lvtRow == null) {
            retVal = new Variable("" + UNKNOWN, null, "__LOCAL[" + slot + "]", val);
        } else {
            retVal = new Variable(lvtRow.descriptor, (lvttRow == null ? null : lvttRow.descriptor), lvtRow.name, val);
        }
        return retVal;
    }

    @Override
    public LocalVariablesArea clone() {
        final LocalVariablesArea o;
        try {
            o = (LocalVariablesArea) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        final TreeMap<Integer, Value> valuesCopy = new TreeMap<>(this.values);        
        o.values = valuesCopy;
        return o;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[");
        int j = 0;
        for (Map.Entry<Integer, Value> e : this.values.entrySet()) {
            buf.append(e.getKey());
            buf.append(":");
            buf.append(e.getValue());
            if (j < this.values.size() - 1) {
                buf.append(", ");
            }
            ++j;
        }
        buf.append("]");
        return buf.toString();
    }
}