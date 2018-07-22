package pbftSimulator;

public class Utils {
	
	public static boolean reachMajority(int n) {
		return n == 2 * getMaxTorelentNumber(Settings.N) + 1;
	}
	
	public static int getMaxTorelentNumber(int n) {
		if(n/3*3 == n) {
			return n/3 - 1;
		}
		return n/3;
	}
}
