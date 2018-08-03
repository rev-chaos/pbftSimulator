package pbftSimulator.replica;

import pbftSimulator.message.Message;

public class OfflineReplica extends Replica{
	
	public OfflineReplica(int id, int[] netDlys, int[] netDlysToClis) {
		super(id, netDlys, netDlysToClis);
	}
	
	public void msgProcess(Message msg) {
		msg.print("Disconnect");
		return;
	}
	
	
}
