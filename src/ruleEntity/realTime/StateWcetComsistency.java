package ruleEntity.realTime;

import static utils.XMLParseUtil.parseXML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import entity.Channel;
import entity.Component;
import entity.State;
import entity.Transition;
import utils.PathNode;

/*
* Author：lankx
* 手动验证文档，实时性规则，第2条:子系统设计模型中，子系统的串行状态的wcet之和应该小于等于该子系统的wcet。
* */

public class StateWcetComsistency {

	public static void excute() {
		Stack<PathNode> longestPath = new Stack<>();
		Map<String, PathNode> pathNodeList = new HashMap<String, PathNode>();
		Map<String, Component> componentListSimulink = new LinkedHashMap<>();
		Map<String, Channel> channelListSimulink = new LinkedHashMap<>();

		parseXML("simulink0703.xml", componentListSimulink, channelListSimulink);

		for (String componentKey : componentListSimulink.keySet()) {
			Component currentComponent = componentListSimulink.get(componentKey);
//        	System.out.println(componentKey + " connect list size is " +
//        						componentListAadl.get(componentKey).getConnectionList().size());
			if (componentListSimulink.get(componentKey).getTransitionList().size() > 0) {
				// System.out.println("has connections");
				if (componentListSimulink.get(componentKey).getAttr("wcet") != null) {
					String wcetString = componentListSimulink.get(componentKey).getAttr("wcet");
					float wcet = Float.valueOf(wcetString.substring(0, wcetString.length() - 2));

					getPathNode(componentListSimulink.get(componentKey).getTransitionList(),
							componentListSimulink.get(componentKey).getStateList(), pathNodeList);

//                    if (pathNodeList.size() != 0) System.out.println(pathNodeList.size() + "\n");
//                    for (String pathNodeKeyString : pathNodeList.keySet()) {
//                        System.out.println(pathNodeKeyString + " " +
//                                pathNodeList.get(pathNodeKeyString).getNextComponents().size() + "  " +
//                                pathNodeList.get(pathNodeKeyString).getIsFirst() + " " +
//                                pathNodeList.get(pathNodeKeyString).getWcet());
//                    }

					// float maxPathWcet = getMaxPathWcet(pathNodeList);
					float targetWcet = getMaxPathWcet(pathNodeList, longestPath);
					pathNodeList = new HashMap<>();
					// System.out.println("max path wcet is " + getMaxPathWcet(pathNodeList));

					if (wcet < targetWcet) {
						System.out.println("子系统" + componentListSimulink.get(componentKey).getAttr("name") + "的wcet为"
								+ wcet + "，其状态最长无循环路径的wcet为" + targetWcet + "，大于该子系统的wcet，不满足实时性规则");
					} else {
						System.out.println("子系统" + componentListSimulink.get(componentKey).getAttr("name") + "的wcet为"
								+ wcet + "，其状态最长无循环路径的wcet为" + targetWcet + "，不大于该子系统的wcet，满足实时性规则");
					}
					PathNode.outputLongestList(longestPath);
					longestPath.clear();
				}
			}

			if (!currentComponent.getStateList().isEmpty()) {
				for (String stateKey : currentComponent.getStateList().keySet()) {
					State currentState = currentComponent.getStateList().get(stateKey);
					if (!currentState.getTransitionList().isEmpty()) {
						// System.out.println("has connections");
						String stateWcetString = currentState.getAttr("wcet");
						float stateWcet = Float.valueOf(stateWcetString.substring(0, stateWcetString.length() - 2));

						Map<String, State> subStateMap = new HashMap<>();
						for (State state : currentState.getSubStateList()) {
							subStateMap.put(state.getAttr("id"), state);
						}
						getPathNode(currentState.getTransitionList(), subStateMap, pathNodeList);

//                        if (pathNodeList.size() != 0) System.out.println(pathNodeList.size() + "\n");
//                        for (String pathNodeKeyString : pathNodeList.keySet()) {
//                            System.out.println(pathNodeKeyString + " " +
//                                    pathNodeList.get(pathNodeKeyString).getNextComponents().size() + "  " +
//                                    pathNodeList.get(pathNodeKeyString).getIsFirst() + " " +
//                                    pathNodeList.get(pathNodeKeyString).getWcet());
//                        }

						float targetWcet = getMaxPathWcet(pathNodeList, longestPath);
						if (stateWcet < targetWcet) {
							System.out.println("状态" + currentState.getAttr("name") + "的wcet为" + stateWcet
									+ "，其子状态最长无循环路径的wcet为" + targetWcet + "，大于该状态的wcet，不满足实时性规则");
						} else {
							System.out.println("状态" + currentState.getAttr("name") + "的wcet为" + stateWcet
									+ "，其子状态最长无循环路径的wcet为" + targetWcet + "，不大于该状态的wcet，满足实时性规则");
						}
						PathNode.outputLongestList(longestPath);
						longestPath.clear();
					}
					pathNodeList = new HashMap<>();
				}
			}
		}
	}

	private static void getPathNode(List<Transition> transitionList, Map<String, State> stateList,
			Map<String, PathNode> pathNodeList) {
		for (Transition transition : transitionList) {
			String sourceId = transition.getAttr("source");
			String destId = transition.getAttr("dest");
			if (stateList.get(destId) == null || stateList.get(sourceId) == null)
				continue;
			if (pathNodeList.get(destId) != null)
				pathNodeList.get(destId).setIsFirst(false);
			else {
				PathNode newDestNode = new PathNode(destId, stateList.get(destId).getAttr("wcet"),
						stateList.get(destId).getAttr("name"), false);
				pathNodeList.put(destId, newDestNode);
			}
			if (pathNodeList.get(sourceId) != null) {
				pathNodeList.get(sourceId).getNextComponents().add(pathNodeList.get(destId));
			} else {
				PathNode newSourceNode = new PathNode(sourceId, stateList.get(sourceId).getAttr("wcet"),
						stateList.get(sourceId).getAttr("name"), true);
				newSourceNode.getNextComponents().add(pathNodeList.get(destId));
				pathNodeList.put(sourceId, newSourceNode);
			}

		}
	}

	private static float getMaxPathWcet(Map<String, PathNode> pathNodeList, Stack<PathNode> longestPath) {
		// System.out.println(pathNodeList.size());
		Stack<PathNode> pathNodeStack = new Stack<PathNode>();
		float maxPathWcet = 0;
		for (String pathNodeKeyString : pathNodeList.keySet()) {
			if (pathNodeList.get(pathNodeKeyString).getIsFirst()) {
				float maxPathWcetTemp = 0;
				float currentWcet = pathNodeList.get(pathNodeKeyString).getWcet();
				pathNodeStack.add(pathNodeList.get(pathNodeKeyString));
				maxPathWcetTemp = calculateMaxWcet(maxPathWcetTemp, pathNodeList.get(pathNodeKeyString), currentWcet,
						pathNodeStack, longestPath);
				// System.out.println(maxPathWcet + "max path wcet temp is " + maxPathWcetTemp);
				if (maxPathWcet < maxPathWcetTemp)
					maxPathWcet = maxPathWcetTemp;
				pathNodeStack.clear();
			}
		}

		return maxPathWcet;
	}

	/*
	 * 该函数用于计算pathNode为起点的最长的wcet maxPathWcet: 计算得到的执行时间最长路径的wcet pathNode:
	 * 从该节点开始计算之后的路径长度 currentWcet: 到达当前节点的最长的wcet pathNodeStack: 包括当前节点的路径中所有节点的id
	 */
	private static float calculateMaxWcet(float maxPathWcet, PathNode pathNode, float currentWcet,
			Stack<PathNode> pathNodeStack, Stack<PathNode> longestPath) {
//        System.out.println("path node id is " + pathNode.getId() + " " + pathNode.getName() + " " + pathNode.getNextComponents().size());
		if (pathNode.getNextComponents().size() <= 0) {
			System.err.println(maxPathWcet + " current wcet is " + currentWcet);
			if (currentWcet >= maxPathWcet) {
				maxPathWcet = currentWcet;
				longestPath.clear();
				longestPath.addAll(pathNodeStack);
				// PathNode.outputLongestList(longestPath);
			}
			// System.err.println("max wcet is " + maxPathWcet);
			return maxPathWcet;
		}

		if (pathNodeStack.size() > 1) {
			pathNodeStack.pop();
			for (PathNode pathNodeInPath : pathNodeStack) {
				if (pathNode.getId().equals(pathNodeInPath.getId())) {
					if (pathNode.getName().equalsIgnoreCase("idle") && currentWcet >= maxPathWcet) {
						maxPathWcet = currentWcet;
						longestPath.clear();
						longestPath.addAll(pathNodeStack);
					}
					pathNodeStack.add(pathNode);
					return maxPathWcet;
				}
			}
			pathNodeStack.add(pathNode);
		}
		for (PathNode nextPathNode : pathNode.getNextComponents()) {
			currentWcet += nextPathNode.getWcet();
			pathNodeStack.add(nextPathNode);
			maxPathWcet = calculateMaxWcet(maxPathWcet, nextPathNode, currentWcet, pathNodeStack, longestPath);
			currentWcet -= nextPathNode.getWcet();
			pathNodeStack.pop();
		}
		return maxPathWcet;
	}

	public static void main(String[] args) {
		excute();
	}
}
