package pbftSimulator.replica;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import pbftSimulator.Message;
import pbftSimulator.Settings;
import pbftSimulator.Status;

public class ByztReplica extends Replica{
	//这个参数是为了防止出现死循环，限制byzantine节点无限次数地广播消息
	private Map<String, Integer> cntMap;
	
	public ByztReplica(int id, boolean isByzt, int[] netDlys) {
		super(id, isByzt, netDlys);
		receiveTag = Settings.btztReceiveTag;
		sendTag = Settings.btztSendTag;
		processTag = Settings.btztProcessTag;
		cntMap = new HashMap<>();
	}
	
	//拜占庭节点不是主节点的时候发送preprepare请求
	//是主节点的时候发送不正确的sequence
	public void requestProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(id != msg.getPriId()) {
			broadcastByztMsg(Message.Preprepare, msg, msgQue, msg.getSeqId(), id, procTime);	
		}else {
			broadcastByztMsg(Message.Preprepare, msg, msgQue, msg.getSeqId() + 1, id, procTime);
		}
	}
	
	//没有达到绝大多数条件也发送commit消息（只要收到prepareRequest就发送commit消息）
	public void prepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.Commit, msg, msgQue, procTime);
		}
	}
	
	//收到Commit消息什么都不做
	public void commitProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
	}
	
	public void timeOutProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.ViewChg, msg, msgQue, msg.getTimestamp());
		}
	}
	
	//没有达到绝大多数条件，就发送NewView到0节点
	public void viewChangeProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		sendOneMsg(Message.NewView, msg, msgQue, 0, procTime);
		sendTimeOutMsg(msg, msgQue, id);
	}
	
	//收到任何newView的消息，就广播prepare消息
	public void newViewProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);
		}
	}
	
	//对消息不做合规性检查
	public boolean preCheckMsg(Message msg) {
		String key = msg.getCtx();
		//如果所有诚实节点都已经confirmed了，就没有继续作恶的意义了
		if(Settings.remainConfirms <= 0) {
			return false;
		}
		//限制每条消息的最大广播次数，防止出现死循环
		if(!cntMap.containsKey(key)) {
			cntMap.put(key, 50 * Settings.N);
		}
		if(!statMap.containsKey(key)) {
			statMap.put(key, new Status(Status.Byzantine, 0, 0, 0L));
		} 
		return true;
	}
	
	public void broadcastByztMsg(int type, Message msg, Queue<Message> msgQue, int seqId, int priId, long procTime) {
		for(int i = 0; i < Settings.N; ++i) {
			Message newMsg = new Message(type, msg.getCtx(), id, i, 
					seqId, priId, procTime + (long)netDlys[i]);
			msgQue.add(newMsg);
			newMsg.print(sendTag);
		}
	}
	
	public void sendOneMsg(int type, Message msg, Queue<Message> msgQue, int rcvId, long procTime) {
		Message newMsg = new Message(type, msg.getCtx(), id, rcvId, 
				msg.getSeqId(), msg.getPriId(), 
				procTime + (long)netDlys[rcvId]);
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	//拜占庭节点故意提前发起viewChange
	public void sendTimeOutMsg(Message msg, Queue<Message> msgQue, int rcvrId) {
		Message newMsg = new Message(Message.TimeOut, msg.getCtx(), id, rcvrId, 
				msg.getSeqId()+1, msg.getPriId()+1, 
				msg.getTimestamp() + Settings.timeout/2);
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	public int remainCnt(Message msg) {
		String key = msg.getCtx();
		if(!cntMap.containsKey(key)) {
			return 0;
		}
		int cnt = cntMap.get(key);
		cntMap.put(key, cnt-1);
		return cnt;
	}
	
}
