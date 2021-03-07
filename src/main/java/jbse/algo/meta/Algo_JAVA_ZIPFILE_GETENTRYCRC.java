package jbse.algo.meta;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getEntryCrc(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETENTRYCRC extends Algo_JAVA_ZIPFILE_GETENTRY_STARTS {
	public Algo_JAVA_ZIPFILE_GETENTRYCRC() {
		super("getEntryCrc", true);
	}
	
	@Override
	protected void setToPush(Object retVal) {
		final long l = ((Long) retVal).longValue();
		this.toPush = this.ctx.getCalculator().valLong(l);
	}
}
