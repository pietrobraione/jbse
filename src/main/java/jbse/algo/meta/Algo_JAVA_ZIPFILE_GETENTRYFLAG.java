package jbse.algo.meta;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getEntryFlag(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETENTRYFLAG extends Algo_JAVA_ZIPFILE_GETENTRY_STARTS {
	public Algo_JAVA_ZIPFILE_GETENTRYFLAG() {
		super("getEntryFlag", true);
	}
	
	@Override
	protected void setToPush(Object retVal) {
		final int i = ((Integer) retVal).intValue();
		this.toPush = this.ctx.getCalculator().valInt(i);
	}
}
