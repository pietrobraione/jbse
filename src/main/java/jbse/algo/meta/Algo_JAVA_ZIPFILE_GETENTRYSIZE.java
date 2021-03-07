package jbse.algo.meta;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getEntrySize(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETENTRYSIZE extends Algo_JAVA_ZIPFILE_GETENTRY_STARTS {
	public Algo_JAVA_ZIPFILE_GETENTRYSIZE() { 
		super("getEntrySize", true);
	}
	
	@Override
	protected void setToPush(Object retVal) {
		final long l = ((Long) retVal).longValue();
		this.toPush = this.ctx.getCalculator().valLong(l);
	}
}
