package jbse.apps;

public interface Stats extends Timer {
	long countPushAssumption();
	long countClearAssumptions();
	long countAddAssumptions();
	long countSetAssumptions();
	long countGetAssumptions();
	long countIsSat();
	long countIsSatAliases();
	long countIsSatExpands();
	long countIsSatNull();
	long countIsSatInitialized();
	long countIsSatNotInitialized();
	long countGetModel();
	long countSimplify();
	long timePushAssumption();
	long timeClearAssumptions();
	long timeAddAssumptions();
	long timeSetAssumptions();
	long timeGetAssumptions();
	long timeIsSat();
	long timeIsSatAliases();
	long timeIsSatExpands();
	long timeIsSatNull();
	long timeIsSatInitialized();
	long timeIsSatNotInitialized();
	long timeGetModel();
	long timeSimplify();
	
	@Override
	default long getTime() {
		return timePushAssumption() +
		       timeClearAssumptions() +
		       timeAddAssumptions() +
		       timeSetAssumptions() +
		       timeGetAssumptions() +
		       timeIsSat() +
		       timeIsSatAliases() +
		       timeIsSatExpands() +
		       timeIsSatNull() +
		       timeIsSatInitialized() +
		       timeIsSatNotInitialized() +
		       timeGetModel() +
		       timeSimplify();
	}
}
