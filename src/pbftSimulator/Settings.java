package pbftSimulator;

import java.util.Queue;

public class Settings {

	public static final int N = 20;  						//replicas节点的数量
	
	public static final int f = 6;							//恶意节点的数量
	
	public static final int reqNum = 10;						//客户端的请求消息数量
	
	public static final int avrReqInvl = 1000; 				//请求消息的平均时间间隔(毫秒)
	
	public static final int timeout = 500;					//超时设定(毫秒)
	
	public static final int baseDlyBtwRp = 10;				//节点之间的基础网络时延
	
	public static final int dlyRngBtwRp = 50;				//节点间的网络时延扰动范围
	
	public static final int baseDlyBtwRpAndCli = 50;		//节点与客户端之间的基础网络时延
	
	public static final int dlyRngBtwRpAndCli = 150;		//节点与客户端之间的网络时延扰动范围
	
	public static final int bandwidth = 10000;				//节点间网络的额定消息负载(超过后时延呈指数级上升)
	
	public static final double factor = 1.05;					//超出额定负载后的指数基数
	
	public static final int collapseDelay = 10000;				//视为系统崩溃的网络时延
	
	public static final boolean showDetailInfo = false;		//是否显示完整的消息交互过程
	
	public static final String receiveTag = "Receive";
	
	public static final String sendTag = "Send";
	
	public static final String offlineTag = "Receive";
	
	public static final String processTag = "Process";
	
	public static final String disconnectTag = "Disconnect";
	
	public static final String confirmedTag = "Confirmed";
	
	public static final String btztProcessTag = "BtztProcess";
	
	public static final String btztReceiveTag = "BtztReceive";
	
	public static final String btztSendTag = "BtztSend";
	
	public static int remainConfirms = reqNum * (N - f);
	
	public static int getNetDelay(Queue<Message> msgQue, int basedelay) {
		int msgNum = msgQue.size();
		if(msgNum < bandwidth) {
			return basedelay;
		}else {
			return (int)Math.pow(factor, msgNum - bandwidth) + basedelay;
		}
	}
	
}
