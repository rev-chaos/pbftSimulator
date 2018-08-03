package pbftSimulator.replica;

public class ByztReplica extends Replica{
	
	public static final String BTZTPROCESSTAG = "BtztProcess";
	
	public static final String BTZTRECEIVETAG = "BtztReceive";
	
	public static final String BTZTSENDTAG = "BtztSend";
	
	public ByztReplica(int id, int[] netDlys, int[]netDlysToClis) {
		super(id, netDlys, netDlysToClis);
		receiveTag = BTZTRECEIVETAG;
		sendTag = BTZTSENDTAG;
	}
	
	
}