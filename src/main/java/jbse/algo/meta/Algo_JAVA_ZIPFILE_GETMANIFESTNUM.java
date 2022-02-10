package jbse.algo.meta;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getManifestNum(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETMANIFESTNUM extends Algo_JAVA_ZIPFILE_GETENTRY_STARTS {
	public Algo_JAVA_ZIPFILE_GETMANIFESTNUM() {
		super("getManifestNum", false);
	}
	
	@Override
	protected void setToPush(Object retVal) {
		final int i = ((Integer) retVal).intValue();
		this.toPush = this.ctx.getCalculator().valInt(i);
	}
}
