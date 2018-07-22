package pbftSimulator.replica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import pbftSimulator.Message;
import pbftSimulator.Settings;
import pbftSimulator.Status;
import pbftSimulator.Utils;

public class Replica {
	
	protected String receiveTag;
	
	protected String sendTag;
	
	protected String processTag;
	
	protected boolean isByzt;					// 是否是拜占庭节点
	
	protected int id; 								//当前节点的id
	
	protected HashMap<String, Status> statMap;	//消息状态map
	
	protected HashMap<String, ArrayList<Message>> msgCache;  //未处理消息缓存
	
	protected int netDlys[];				//与其他节点的网络延迟
	
	public Replica(int id, boolean isByzt, int[] netDlys) {
		this.id = id;
		this.isByzt = isByzt;
		statMap = new HashMap<>();
		msgCache = new HashMap<>();
		receiveTag = Settings.receiveTag;
		sendTag = Settings.sendTag;
		processTag = Settings.processTag;
		this.netDlys = netDlys;
	}
	
	public void msgProcess(Message msg, Queue<Message> msgQue) {
		msg.print(receiveTag);
		int type = msg.getType();
		switch(type) {
		case Message.Request:
			receiveRequest(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Preprepare:
			receivePreprepare(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Prepare:
			receivePrepare(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Commit:
			receiveCommit(msg, msgQue);
			break;
		case Message.ViewChg:
			receiveViewChange(msg, msgQue, msg.getTimestamp());
			break;
		case Message.NewView:
			receiveNewView(msg, msgQue, msg.getTimestamp());
			break;
		case Message.TimeOut:
			receiveTimeOut(msg, msgQue);
			break;
		default:
			System.out.println("【Error】消息类型错误！");
		}
	}
	
	public void receiveRequest(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		//产生一条Timeout消息，以便将来检查是否发生超时
		sendTimeOutMsg(msg, msgQue, id);
		requestProcess(msg, msgQue, procTime);
		//处理消息缓存中可能存在的后续消息
		postMsgProcess(msg, msgQue, Message.Preprepare, procTime);
	}
	
	public void receivePreprepare(Message msg, Queue<Message> msgQue, long procTime) {
		//检查消息是否合规，并做好相应的处理
		if(!preCheckMsg(msg)) return;
		preprepareProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Prepare, procTime);
	}
	
	public void receivePrepare(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		prepareProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Commit, procTime);
	}
	
	public void receiveCommit(Message msg, Queue<Message> msgQue) {
		if(!preCheckMsg(msg)) return;
		commitProcess(msg, msgQue);
	}
	
	public void receiveTimeOut(Message msg, Queue<Message> msgQue) {
		if(!preCheckMsg(msg)) return;
		timeOutProcess(msg, msgQue);
	}
	
	
	public void receiveViewChange(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		viewChangeProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.NewView, procTime);
	}
	
	public void receiveNewView(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		newViewProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Preprepare, procTime);
	}
	
	public void requestProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		//新建状态
		statMap.put(msg.getCtx(), new Status(Status.Request, msg.getSeqId(), msg.getPriId(), msg.getTimestamp()));
		//如果是主节点，就发送preprepare消息和prepare消息，并更新状态
		if(id == msg.getPriId()) {
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);	
		}
	}
	
	public void preprepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		stat.setStage(Status.Preprepare);
		broadcastMsg(Message.Prepare, msg, msgQue, procTime);
	}
	
	public void prepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addPrepareVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			stat.setStage(Status.Prepare);
			broadcastMsg(Message.Commit, msg, msgQue, procTime);
		}
	}
	
	public void commitProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addCommitVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			stat.setStage(Status.Commit);
			stat.setEndTime(msg.getTimestamp());
			//清除已经进入稳定状态的消息缓存
			msgCache.remove(msg.getCtx());
			//打印输出消息
			System.out.println("【Confirmed】消息:"
					+msg.getCtx()+"在节点"+id+"("+(isByzt?"恶意节点":"正常节点")+")进入稳态！消息确定时延:"
					+(stat.getEndTime()-stat.getStartTime())
					+";消息序列:"+stat.getSeqId()
					+";主节点:"+stat.getPriId()
					);
			//更新全局控制变量的状态
			Settings.remainConfirms--;
		}
	}
	
	public void timeOutProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		broadcastMsg(Message.ViewChg, msg, msgQue, msg.getTimestamp());
	}
	
	public void viewChangeProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addViewChgVote(msg);
		if(Utils.reachMajority(voteNum)) {
			int rvcId = msg.getPriId();
			sendOneMsg(Message.NewView, msg, msgQue, rvcId, procTime);
			//更新状态为Request，更新primaryId和SequenceId
			stat.reset(msg.getSeqId(), msg.getPriId());
			//产生一条Timeout消息，以便将来检查是否发生超时
			sendTimeOutMsg(msg, msgQue, id);
		}
	}
	
	public void newViewProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addNewViewVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			//重置状态
			stat.reset(msg.getSeqId(), msg.getPriId());
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);
		}
	}
	
	public boolean preCheckMsg(Message msg) {
		String key = msg.getCtx();
		int type = msg.getType();
		//如果消息是request消息,且这个request请求已经处理过，那么就略过这条消息不处理
		if(type == Message.Request) {
			if(statMap.containsKey(key)) return false;
			else return true;
		}
		//如果不是request消息，但又没有这条消息的状态，那么将消息存到缓存中以后处理
		if(!statMap.containsKey(key)) {
			addMsgToCache(msg);
			return false;
		}  
		Status stat = statMap.get(key);
		//如果消息的sequenceId低于当前消息状态的sequenceId，略过这条消息不处理
		if(msg.getSeqId() < stat.getSeqId()) return false;
		//如果消息类型是ViewChg或者NewView或者Timeout
		//若消息已经进入稳态，则略过这条消息不处理
		if(type == Message.ViewChg || type == Message.NewView || type == Message.TimeOut) {
			if(stat.getStage() == Status.Commit) return false;
			return true;
		}
		//消息类型是Preprepare或者Prepare或者Commit
		//如果消息的主节点id与消息状态的主节点id不同或者seqId高于当前的消息状态的seqId
		//保存消息到消息缓存中等确定新的视图后再做处理
		if(msg.getPriId() != stat.getPriId() || msg.getSeqId() > stat.getSeqId()) {
			addMsgToCache(msg);
			return false;
		}
		//如果消息的类型低于当前的状态，忽略不做处理
		if(msg.getType() < stat.getStage()) return false;
		//如果消息的类型高于当前的状态，保存消息留待后续处理
		if(msg.getType() > stat.getStage()) {
			addMsgToCache(msg);
			return false;
		}
		return true;
	}
	
	public void postMsgProcess(Message msg, Queue<Message> msgQue, int msgType, long procTime) {
		ArrayList<Message> nextMsg = getMsgByType(msgType, msg);
		for(Message m : nextMsg) {
			switch(msgType) {
			case Message.Preprepare:
				receivePreprepare(m, msgQue, procTime);
				break;
			case Message.Prepare:
				receivePrepare(m, msgQue, procTime);
				break;
			case Message.Commit:
				receiveCommit(m, msgQue);
				break;
			case Message.NewView:
				receiveNewView(m, msgQue, procTime);
				break;
			default:
				break;
			}
			
		}
	}
	
	public void broadcastMsg(int type, Message msg, Queue<Message> msgQue, long procTime) {
		for(int i = 0; i < Settings.N; ++i) {
			Message newMsg = new Message(type, msg.getCtx(), id, i, 
					msg.getSeqId(), msg.getPriId(), 
					procTime + Settings.getNetDelay(msgQue, netDlys[i]));
			msgQue.add(newMsg);
			newMsg.print(sendTag);
		}
	}
	
	public void sendOneMsg(int type, Message msg, Queue<Message> msgQue, int rcvId, long procTime) {
		Message newMsg = new Message(type, msg.getCtx(), id, rcvId, 
				msg.getSeqId(), msg.getPriId(), 
				procTime + Settings.getNetDelay(msgQue, netDlys[rcvId]));
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	public void sendTimeOutMsg(Message msg, Queue<Message> msgQue, int rcvrId) {
		Message newMsg = new Message(Message.TimeOut, msg.getCtx(), id, rcvrId, 
				msg.getSeqId()+1, msg.getPriId()+1, 
				msg.getTimestamp() + Settings.timeout);
		msgQue.add(newMsg);
		newMsg.print("send");
	}
	
	/*
	 * 从消息缓存中获取指定消息内容的指定类型的消息
	 * 需要注意的是：发生viewChange后，需要将之前的相同类型的消息从cache中删去
	 */
	public ArrayList<Message> getMsgByType(int type, Message msg) {
		String key = msg.getCtx();
		ArrayList<Message> rstList = new ArrayList<>();
		if(!msgCache.containsKey(key)) {
			return rstList;
		}
		ArrayList<Message> msgList = msgCache.get(key);
		for(Message m: msgList) {
			//需要消息内容，primaryId，sequentId都一致才返回，
			//消息内容和消息发发送者id不会被篡改（有摘要和签名）
			if(type == m.getType() && msg.getPriId() == m.getPriId() 
					&& msg.getSeqId() == m.getSeqId()) {
				rstList.add(m);
			}
		}
		return rstList;
	}
	
	/*
	 * 向消息缓存中增加消息
	 */
	public void addMsgToCache(Message msg) {
		String key = msg.getCtx();
		if(!msgCache.containsKey(key)) {
			ArrayList<Message> list = new ArrayList<>();
			list.add(msg);
			msgCache.put(key, list);
			return;
		}
		//注意：如果有重复类型的消息，就不需要缓存
		ArrayList<Message> msgList = msgCache.get(key);
		boolean flag = true;
		for(Message m : msgList) {
			if(m.equals(msg)) {
				flag = false;
			}
		}
		if(flag) {
			msgList.add(msg);
		}
	}
	
}
