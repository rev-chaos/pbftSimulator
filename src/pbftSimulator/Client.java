package pbftSimulator;

import java.util.HashMap;
import java.util.Map;

import pbftSimulator.message.CliTimeOutMsg;
import pbftSimulator.message.Message;
import pbftSimulator.message.ReplyMsg;
import pbftSimulator.message.RequestMsg;

public class Client {
	
	public static final int PROCESSING = 0;		//没有收到f+1个reply
	
	public static final int STABLE = 1;			//已经收到了f+1个reply
	
	public int id;								//客户端编号
	
	public int v;								//视图编号
	
	public Map<Long, Integer> reqStats;			//request请求状态
	
	public Map<Long, Message> reqMsgs;			//request消息（删除已经达到stable状态的request消息）
	
	public Map<Long, Map<Integer, Message>> repMsgs;	//reply消息（删除已经达到stable状态的reply消息）
	
	public long accTime;						//累积确认时间
	
	public int netDlys[];						//与各节点的基础网络时延
	
	public String receiveTag = "CliReceive";
	
	public String sendTag = "CliSend";
	
	public Client(int id, int[] netDlys) {
		this.id = id;
		this.netDlys = netDlys;
		reqStats = new HashMap<>();
		reqMsgs = new HashMap<>();
		repMsgs = new HashMap<>();
	}
	
	public void msgProcess(Message msg) {
		msg.print(receiveTag);
		switch(msg.type) {
		case Message.REPLY:
			receiveReply(msg);
			break;
		case Message.CLITIMEOUT:
			receiveCliTimeOut(msg);
			break;
		default:
			System.out.println("【Error】消息类型错误！");
		}
		
	}
	
	public void sendRequest(long time) {
		//避免时间重复
		while(reqStats.containsKey(time)) {
			time++;
		}
		int priId = v % Simulator.RN;
		Message requestMsg = new RequestMsg("Message", time, id, id, priId, time + netDlys[priId]);
		Simulator.sendMsg(requestMsg, sendTag);
		reqStats.put(time, PROCESSING);
		reqMsgs.put(time, requestMsg);
		repMsgs.put(time, new HashMap<>());
		//发送一条Timeout消息，以便将来检查是否发生超时
		setTimer(time, time);
	}
	
	public void receiveReply(Message msg) {
		ReplyMsg repMsg = (ReplyMsg)msg;
		long t = repMsg.t;
		//如果这条消息对应的request消息不存在或者已经是stable状态，那就忽略这条消息
		if(!reqStats.containsKey(t) || reqStats.get(t) == STABLE) {
			return;
		}
		//否则就将这条reply消息包含到缓存中
		saveReplyMsg(repMsg);
		//判断是否满足f+1条件，如果满足就设定主节点编号，累加确认时间并清理缓存
		if(isStable(repMsg)) {
			v = repMsg.v;
			accTime += repMsg.rcvtime - t;
			reqStats.put(t, STABLE);
			reqMsgs.remove(t);
			repMsgs.remove(t);
			System.out.println("【Stable】客户端"+id+"在"+t
					+"时间请求的消息已经得到了f+1条reply，进入稳态，共耗时"+(repMsg.rcvtime - t)+"毫秒,此时占用带宽为"+Simulator.inFlyMsgLen+"B");
		}
	}
	
	public void receiveCliTimeOut(Message msg) {
		CliTimeOutMsg cliTimeOutMsg = (CliTimeOutMsg)msg;
		long t = cliTimeOutMsg.t;
		//如果这条消息对应的request消息不存在或者已经是stable状态，那就忽略这条消息
		if(!reqStats.containsKey(t) || reqStats.get(t) == STABLE) {
			return;
		}
		//否则给所有的节点广播request消息
		for(int i = 0; i < Simulator.RN; i++) {
			Message requestMsg = new RequestMsg("Message", t, id, id, i, cliTimeOutMsg.rcvtime + netDlys[i]);
			Simulator.sendMsg(requestMsg, sendTag);
		}
		//发送一条Timeout消息，以便将来检查是否发生超时
		setTimer(t, cliTimeOutMsg.rcvtime);
	}
	
	/**
     * 去重缓存reply消息
     * @param msg reply消息
     */
	public void saveReplyMsg(ReplyMsg msg) {
		Map<Integer, Message> rMap = repMsgs.get(msg.t);
		for(Integer i : rMap.keySet()) {
			if(i == msg.i && ((ReplyMsg)rMap.get(i)).v >= msg.v) {
				return;
			}
		}
		repMsgs.get(msg.t).put(msg.i, msg);
	}
	
	/**
	 * 判断请求消息是否已经达到稳定状态（即收到了f+1条reply消息）
	 * @param msg 请求消息
	 * @return	是否达到稳态的判断结果
	 */
	public boolean isStable(ReplyMsg msg) {
		Map<Integer, Message> rMap = repMsgs.get(msg.t);
		int cnt = 0;
		for(Integer i : rMap.keySet()) {
			if(((ReplyMsg)rMap.get(i)).v == msg.v && ((ReplyMsg)rMap.get(i)).r == msg.r) {
				cnt++;
			}
		}
		if(cnt > Utils.getMaxTorelentNumber(Simulator.RN)) return true;
		return false;
	}
	
	/**
     * 根据数组下标获取客户端Id
     * @param index 表示客户端在数组中的下标
     * @return 返回客户端id
     */
	public static int getCliId(int index) {
		return index * (-1) - 1;
	}
	
	/**
     * 根据客户端Id获取数组下标
     * @param id 表示客户端id
     * @return 返回数组下标
     */
	public static int getCliArrayIndex(int id) {
		return (id + 1) * (-1);
	}
	
	public int stableMsgNum() {
		int cnt = 0;
		if(reqStats == null) return cnt;
		for(long t : reqStats.keySet()) 
			if(reqStats.get(t) == STABLE) 
				cnt++;
		return cnt;
	}
	
	public void setTimer(long t, long time) {
		Message timeoutMsg = new CliTimeOutMsg(t, id, id, time + Simulator.CLITIMEOUT);
		Simulator.sendMsg(timeoutMsg, "ClientTimeOut");
	}
}
