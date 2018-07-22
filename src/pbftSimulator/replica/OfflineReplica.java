package pbftSimulator.replica;

import java.util.Queue;

import pbftSimulator.Message;
import pbftSimulator.Settings;

public class OfflineReplica extends Replica{
	
	public OfflineReplica(int id, boolean isByzt, int[] netDlys) {
		super(id, isByzt, netDlys);
	}
	
	public void msgProcess(Message msg, Queue<Message> msgQue) {
		msg.print(Settings.disconnectTag);
		return;
	}
	
	
}
