package jbse.algo.meta;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#startsWithLOC(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_STARTSWITHLOC extends Algo_JAVA_ZIPFILE_GETENTRY_STARTS {
	public Algo_JAVA_ZIPFILE_STARTSWITHLOC() {
		super("startsWithLOC", false);
	}
	
	@Override
	protected void setToPush(Object retVal) {
		final boolean b = ((Boolean) retVal).booleanValue();
		this.toPush = this.ctx.getCalculator().valInt(b ? 1 : 0);
	}
}
