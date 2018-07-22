package pbftSimulator;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import pbftSimulator.replica.ByztReplica;
import pbftSimulator.replica.Replica;

public class Simulator {
	
	public static Comparator<Message> timeCmp = new Comparator<Message>(){
		@Override
		public int compare(Message c1, Message c2) {
			return (int) (c1.getTimestamp() - c2.getTimestamp());
		}
	};
	
	public static void main(String[] args) {
		//消息优先队列（按消息计划被处理的时间戳排序）
		Queue<Message> msgQue = new PriorityQueue<>(timeCmp);
		
		//初始化包含f个恶意节点的N个replicas
		int[][] netDlys = netDlyBtwRpInit(Settings.N);
		boolean[] byzts = byztDistriInit(Settings.N, Settings.f);
		Replica[] reps = new Replica[Settings.N];
		for(int i = 0; i < Settings.N; ++i) {
			if(byzts[i]) {
				reps[i] = new ByztReplica(i, byzts[i], netDlys[i]);
			}else {
				reps[i] = new Replica(i, byzts[i], netDlys[i]);
			}
		}
		
		//客户端发送request消息
		Message[] reqMsg = reqMsgInit(Settings.reqNum, Settings.avrReqInvl);
		for(Message msg : reqMsg) {
			msgQue.add(msg);
		}
		
		//节点处理消息
		while(!msgQue.isEmpty()) {
			Message msg = msgQue.poll();
			reps[msg.getRcvId()].msgProcess(msg, msgQue);
			if(Settings.getNetDelay(msgQue, 0) > Settings.collapseDelay) {
				System.out.println("【Error】网络传输消息数量为"+msgQue.size()+",系统网络时延已超过"+Settings.collapseDelay/1000+"秒，已崩溃！");
				break;
			}
		}
		System.out.println("【The end】");
	}
	
	/*
	 * 随机初始化replicas节点之间的网络传输延迟
	 * int n 表示节点总数
	 */
	public static int[][] netDlyBtwRpInit(int n){
		int[][] ltcs = new int[n][n];
		Random rand = new Random();
		for(int i = 0; i < n; ++i) 
			for(int j = 0; j < n; ++j) 
				if(i < j && ltcs[i][j] == 0) {
					ltcs[i][j] = Settings.baseDlyBtwRp + rand.nextInt(Settings.dlyRngBtwRp);
					ltcs[j][i] = ltcs[i][j];
				}
		return ltcs;
	}
	
	/*
	 * 随机初始化replicas节点与客户端的网络传输延迟
	 * int n 表示节点总数
	 */
	public static int[] netDlyBtwRpAndCliInit(int n){
		int[] ltcs = new int[n];
		Random rand = new Random();
		for(int i = 0; i < n; ++i) 
			ltcs[i] = Settings.baseDlyBtwRpAndCli + rand.nextInt(Settings.dlyRngBtwRpAndCli);
		return ltcs;
	}
	
	/*
	 * 随机初始化replicas节点中的恶意节点分布，恶意节点返回true
	 * int n 表示初始化的节点数量
	 * int f 表示恶意节点的数量
	 */
	public static boolean[] byztDistriInit(int n, int f) {
		boolean[] byzt = new boolean[n];
		Random rand = new Random();
		while(f > 0) {
			int i = rand.nextInt(n);
			if(!byzt[i]) {
				byzt[i] = true;
				--f;
			}
		}
		return byzt;
	}
	
	/*
	 * 请求消息初始化,平均
	 * int k表示请求消息总数
	 * int avrInvl表示request请求的平均时间间隔
	 */
	public static Message[] reqMsgInit(int k, int avrInvl) {
		Message[] msgs = new Message[Settings.reqNum*Settings.N];
		int n = 0;
		Random rand = new Random();
		int timestamp = 0;
		int[] ltcs = netDlyBtwRpAndCliInit(Settings.N);
		for(int i = 0; i < Settings.reqNum; ++i) {
			//向每个节点发送request请求
			for(int j = 0; j < Settings.N; ++j) {
				msgs[n++] = new Message(Message.Request, "message"+i, 
						-1, j, 0, 0, timestamp + ltcs[j]);
			}
			timestamp += rand.nextInt(avrInvl);
		}
		return msgs;
	}
	
}
