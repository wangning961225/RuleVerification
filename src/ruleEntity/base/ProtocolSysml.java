package ruleEntity.base;

import entity.Channel;
import entity.Component;
import entity.Linkpoint;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

public class ProtocolSysml {
    public static void excute() {
        Map<String, Component> componentListSysml = new HashMap<>();
        Map<String, Channel> channelListSysml = new HashMap<>();
        parseXML("sysml0726.xml", componentListSysml, channelListSysml);

        List<String> componentList = keySet(componentListSysml);
        List<String> channelList = keySet(channelListSysml);
        Map<String, String> linkNameMap = new LinkedHashMap<>();
        Map<String, String> protocolMap = new LinkedHashMap<>();
        for (String componentId : componentList) {
            List<Linkpoint> linkpointList = componentListSysml.get(componentId).getLinkpointList();
            if (linkpointList != null) {
                for (Linkpoint linkpoint : linkpointList) {
                    String id = linkpoint.getAttr("id");
                    String name = linkpoint.getAttr("name");
                    linkNameMap.put(id, name);
                    if (linkpoint.getAttr("protocol") != null) {
                        String protocol = linkpoint.getAttr("protocol");
                        protocolMap.put(linkpoint.getAttr("id"), protocol);
                    }
                }
            }
        }
//        System.out.println(protocolMap);
//        System.out.println(linkNameMap);


        for (String channelId : channelList) {
            Channel channel = channelListSysml.get(channelId);
            String sourceId = channel.getAttr("source");
            String destId = channel.getAttr("dest");
            String sourceProtocol = protocolMap.get(sourceId);
            String destProtocol = protocolMap.get(destId);
            if (!(sourceProtocol == null && destProtocol == null)) {
                if (sourceProtocol == null) {
                    if (linkNameMap.get(sourceId) != null)
                        System.out.println("接口" + linkNameMap.get(sourceId) + ":没有设置通信协议");
                } else if (destProtocol == null) {
                    if (linkNameMap.get(destId) != null)
                        System.out.println("接口" + linkNameMap.get(destId) + ":没有设置通信协议");
                } else if (!(sourceProtocol.equals(destProtocol)))
                    System.out.println("接口:" + linkNameMap.get(sourceId) + "和接口:"
                            + linkNameMap.get(destId) + "的通信协议不一致");


            }
        }
    }

    public static void main(String[] args) {
        excute();
    }

}
