package pbftSimulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import pbftSimulator.message.LastReply;
import pbftSimulator.message.PrePrepareMsg;
import pbftSimulator.replica.Replica;

public class test {
	
	public static void main(String[] args) {
		
		Queue<PrePrepareMsg> executeQ = new PriorityQueue<>(Replica.nCmp);
		executeQ.add(new PrePrepareMsg(3, 2, null, 0, 0, 0, 0));
		executeQ.add(new PrePrepareMsg(3, 4, null, 0, 0, 0, 0));
		executeQ.add(new PrePrepareMsg(3, 3, null, 0, 0, 0, 0));
		while(!executeQ.isEmpty()) {
			System.out.println(executeQ.poll().n);
		}
	}
}
