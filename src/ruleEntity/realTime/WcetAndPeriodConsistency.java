package ruleEntity.realTime;

import entity.*;
import utils.PathNode;

import java.util.*;

import static utils.XMLParseUtil.parseXML;

/*
* Author：lankx
* 手动验证规则文档，实时性规则，第三条
* 对aadl模型进行验证
* */

public class WcetAndPeriodConsistency {
    public static void excute() {
        Stack<PathNode> longestPath = new Stack<>();
        Map<String, PathNode> pathNodeList = new HashMap<String, PathNode>();
        Map<String, Component> componentListAadl = new LinkedHashMap<>();
        Map<String, Channel> channelListAadl = new LinkedHashMap<>();

        parseXML("aadl(9).xml", componentListAadl, channelListAadl);

        for (String componentKey : componentListAadl.keySet()) {
            Component currentComponent = componentListAadl.get(componentKey);
            //currentComponent.attrsToString();
//            System.out.println(componentKey + " connect list size is " +
//                    currentComponent.getConnectionList().size() + " linkpoint size is " +
//                    currentComponent.getLinkpointList().size());

            if (currentComponent.getConnectionList().isEmpty() || currentComponent.getLinkpointList().isEmpty()) continue;

            for (Connection connection : currentComponent.getConnectionList()) {
                Linkpoint destLinkpoint = null;
                String destId = connection.getAttr("dest");
                String sourceId = connection.getAttr("source");

                for (Linkpoint linkpoint : currentComponent.getLinkpointList()) {
                    if (linkpoint.getAttr("id").equals(destId)) {
                        destLinkpoint = linkpoint;
                        break;
                    }
                }

                if (destLinkpoint != null && destLinkpoint.getAttr("direction").equals("out")) {
                    boolean foundSourceComponent = false;
                    for (String subComponentKey : currentComponent.getSubComponentList().keySet()) {
                        if (currentComponent.getSubComponentList().get(subComponentKey).getPortList().isEmpty()) continue;
                        for (Port port : currentComponent.getSubComponentList().get(subComponentKey).getPortList()) {
                            if (port.getAttr("id").equals(sourceId)) {
                                sourceId = subComponentKey;
                                foundSourceComponent = true;
                                break;
                            }
                        }
                        if (foundSourceComponent) break;
                    }

                    getPathNode(currentComponent.getConnectionList(),
                            currentComponent.getSubComponentList(),
                            pathNodeList);

                    float targetWcet = getSpecificPathWcet(pathNodeList, sourceId, longestPath);

//                    System.out.println("Linkpoint " + destLinkpoint.getAttr("id") + " " + destLinkpoint.getAttr("name") +
//                            " period is " + destLinkpoint.getAttr("period") +
//                            " , path wcet is " + targetWcet);
                    if (targetWcet > Float.valueOf(destLinkpoint.getAttr("period").substring(0, destLinkpoint.getAttr("period").length()-2))) {
                        System.out.println("数据" + destLinkpoint.getAttr("id") + " " + destLinkpoint.getAttr("name") +
                                "周期为" + destLinkpoint.getAttr("period") +
                                " , 生成该数据的子组件路径执行时间为" + targetWcet + "，大于该数据生成的周期，不满足实时性规则");
                    } else if (targetWcet == 0) {
                        System.out.println("数据" + destLinkpoint.getAttr("id") + " " + destLinkpoint.getAttr("name") +
                                "无法验证该条规则，因为其关联的路径有循环");
                    } else {
                        System.out.println("数据" + destLinkpoint.getAttr("id") + " " + destLinkpoint.getAttr("name") +
                                "周期为" + destLinkpoint.getAttr("period") +
                                " , 生成该数据的子组件路径执行时间为" + targetWcet + "，不大于该数据生成的周期，满足实时性规则");
                    }
                    if (!longestPath.isEmpty()) {
                        PathNode.outputLongestList(longestPath);
                        longestPath.clear();
                    }
                }
            }
        }
    }

    private static void getPathNode(List<Connection> connectionList,
                                    Map<String, Component> subComponentList,
                                    Map<String, PathNode> pathNodeList) {
        for (Connection connection : connectionList) {
            String sourceId = connection.getAttr("source");
            String destId = connection.getAttr("dest");
            for (String subComponentKey : subComponentList.keySet()) {
                if (subComponentList.get(subComponentKey).getPortList().isEmpty()) continue;
                for (Port port : subComponentList.get(subComponentKey).getPortList()) {
                    if (port.getAttr("id").equals(sourceId)) {
                        sourceId = subComponentKey;
                        break;
                    }
                    if (port.getAttr("id").equals(destId)) {
                        destId = subComponentKey;
                        break;
                    }
                }
            }
            if (subComponentList.get(destId) == null || subComponentList.get(sourceId) == null) continue;
            if (pathNodeList.get(destId) != null) pathNodeList.get(destId).setIsFirst(false);
            else {
                PathNode newDestNode = new PathNode(destId, subComponentList.get(destId).getAttr("wcet"),
                        subComponentList.get(destId).getAttr("name"), false);
                pathNodeList.put(destId, newDestNode);
            }
            if (pathNodeList.get(sourceId) != null) {
                pathNodeList.get(sourceId).getNextComponents().add(pathNodeList.get(destId));
            } else {
                PathNode newSourceNode = new PathNode(sourceId, subComponentList.get(sourceId).getAttr("wcet"),
                        subComponentList.get(sourceId).getAttr("name"), true);
                newSourceNode.getNextComponents().add(pathNodeList.get(destId));
                pathNodeList.put(sourceId, newSourceNode);
            }

        }
    }

    private static float getSpecificPathWcet(Map<String, PathNode> pathNodeList,
                                             String targetLastId,
                                             Stack<PathNode> longestPath) {
        Stack<PathNode> pathNodeStack = new Stack<PathNode>();
        float maxPathWcet = 0;
        for (String pathNodeKeyString : pathNodeList.keySet()) {
            if (pathNodeList.get(pathNodeKeyString).getIsFirst()) {
                float maxPathWcetTemp = 0;
                float currentWcet = pathNodeList.get(pathNodeKeyString).getWcet();
                pathNodeStack.add(pathNodeList.get(pathNodeKeyString));
                maxPathWcetTemp = calculateSpecificWcet(maxPathWcetTemp, pathNodeList.get(pathNodeKeyString),
                        currentWcet, pathNodeStack, targetLastId, longestPath);
                //System.out.println(maxPathWcet + "max path wcet temp is " + maxPathWcetTemp);
                if (maxPathWcet < maxPathWcetTemp) maxPathWcet = maxPathWcetTemp;
                pathNodeStack.clear();
            }
        }

        return maxPathWcet;
    }

    private static float calculateSpecificWcet(float maxPathWcet,
                                          PathNode pathNode,
                                          float currentWcet,
                                          Stack<PathNode> pathNodeStack,
                                          String targetLastId,
                                          Stack<PathNode> longestPath) {
        if (pathNode.getNextComponents().size() <= 0) {
            if (pathNode.getId().equals(targetLastId)) {
                maxPathWcet = currentWcet;
                longestPath.clear();
                longestPath.addAll(pathNodeStack);
            }
            return maxPathWcet;
        }

        if (pathNodeStack.size() > 1) {
            pathNodeStack.pop();
            for (PathNode pathNNodeInPath : pathNodeStack) {
                if (pathNode.getId().equals(pathNNodeInPath.getId())) {
                    pathNodeStack.add(pathNode);
                    return maxPathWcet;
                }
            }
            pathNodeStack.add(pathNode);
        }
        for (PathNode nextPathNode : pathNode.getNextComponents()) {
            currentWcet += nextPathNode.getWcet();
            pathNodeStack.add(nextPathNode);
            //System.out.println(pathNode.getId());
            //System.out.println("current wcet is " + currentWcet);
            maxPathWcet = calculateSpecificWcet(maxPathWcet, nextPathNode, currentWcet,
                    pathNodeStack, targetLastId, longestPath);
            currentWcet -= nextPathNode.getWcet();
            pathNodeStack.pop();
        }
        return maxPathWcet;
    }

    public static void main(String[] args) {
        excute();
    }
}
