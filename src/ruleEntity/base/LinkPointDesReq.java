package ruleEntity.base;

import entity.Channel;
import entity.Component;
import entity.Linkpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

public class LinkPointDesReq {
    public static void excute() {
        Map<String, Component> componentListSysml = new HashMap<>();
        Map<String, Channel> channelListSysml = new HashMap<>();
        parseXML("sysml0726.xml", componentListSysml, channelListSysml);
        
        Map<String, Component> componentListSimulink = new HashMap<>();
        Map<String, Channel> channelListSimulink = new HashMap<>();
        parseXML("simulink0726.xml", componentListSimulink, channelListSimulink);

        List<String> componentList = keySet(componentListSysml);
        for (String componentId : componentList) {
            Component component=componentListSysml.get(componentId);
            String componentName=component.getAttr("name");
            if(componentId.equals("system"))
                continue;
            String sql="select * from mapping where aadl_id="+componentId;
            String simulinkId=connect(sql,"simulink");
            if(simulinkId==null)
                System.out.println("组件"+componentName+":在子需求设计中没有对应的组件与之对应");
        }
    }
    public static void main(String[] args) {
        excute();
    }
}

