package pbftSimulator;

import java.util.Arrays;

public class test {
	
	public static void main(String[] args) {
		boolean[] b = Simulator.byztDistriInit(10, 3);
		System.out.println(Arrays.toString(b));
	}
}
