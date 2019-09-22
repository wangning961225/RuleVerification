package ruleEntity.base;

import entity.Channel;
import entity.Component;
import entity.Linkpoint;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

public class ProtocolConsistency {
    public static void excute() {
        Map<String, Component> componentListSimulink = new HashMap<>();
        Map<String, Channel> channelListSimulink = new HashMap<>();
        
        Map<String, Component> componentListSysml = new HashMap<>();
        Map<String, Channel> channelListSysml = new HashMap<>();
        
        parseXML("simulink0726.xml", componentListSimulink, channelListSimulink);
        parseXML("sysml0726.xml", componentListSysml, channelListSysml);

        List<String> componentSimulinkList =keySet(componentListSimulink);
        List<String> channelSimulinkList =keySet(channelListSimulink);
        List<String> componentSysmlList = keySet(componentListSysml);
        List<String> channelSysmlList = keySet(channelListSysml);
        
        Map<String,String> SimulinklinkNameMap=new LinkedHashMap<>();
        Map<String,String> SimulinkprotocolMap=new LinkedHashMap<>();
        Map<String,String> SysmllinkNameMap=new LinkedHashMap<>();
        Map<String,String> SysmlprotocolMap=new LinkedHashMap<>();
        for(String componentId : componentSimulinkList){
            List<Linkpoint> linkpointList=componentListSimulink.get(componentId).getLinkpointList();
            if(linkpointList!=null){
                for(Linkpoint linkpoint:linkpointList){
                    String id=linkpoint.getAttr("id");
                    String name=linkpoint.getAttr("name");
                    SimulinklinkNameMap.put(id,name);
                    if(linkpoint.getAttr("protocol")!=null){
                        String protocol = linkpoint.getAttr("protocol");
                        SimulinkprotocolMap.put(linkpoint.getAttr("id"), protocol);
                    }
                }
            }
        }
        
        for (String componentId : componentSysmlList) {
            List<Linkpoint> linkpointList = componentListSysml.get(componentId).getLinkpointList();
            if (linkpointList != null) {
                for (Linkpoint linkpoint : linkpointList) {
                    String id = linkpoint.getAttr("id");
                    String name = linkpoint.getAttr("name");
                    SysmllinkNameMap.put(id, name);
                    if (linkpoint.getAttr("protocol") != null) {
                        String protocol = linkpoint.getAttr("protocol");
                        SysmlprotocolMap.put(linkpoint.getAttr("id"), protocol);
                    }
                }
            }
        }

        for (String SimulinkchannelId : channelSimulinkList) {
            Channel Simulinkchannel=channelListSimulink.get(SimulinkchannelId);
            String SimulinksourceId=Simulinkchannel.getAttr("source");
            String SimulinkdestId=Simulinkchannel.getAttr("dest");
            String SimulinksourceProtocol=SimulinkprotocolMap.get(SimulinksourceId);
            String SimulinkdestProtocol=SimulinkprotocolMap.get(SimulinkdestId);
            //通过映射表查找相关的组件id 但是不知道能不能查找到channelid
            String sql="select * from mapping where aadl_id="+SimulinkchannelId;
            String SysmlchannelId=connect(sql,"sysml");
            Channel Sysmlchannel=channelListSimulink.get(SysmlchannelId);
            String SysmlsourceId=Sysmlchannel.getAttr("source");
            String SysmldestId=Sysmlchannel.getAttr("dest");
            String SysmlsourceProtocol=SysmlprotocolMap.get(SysmlsourceId);
            String SysmldestProtocol=SysmlprotocolMap.get(SysmldestId);
            if(SimulinksourceProtocol!=null && SimulinkdestProtocol==SimulinksourceProtocol){
            	if(!(SysmlsourceProtocol==null && SysmldestProtocol==null)){
                    if(SysmlsourceProtocol==null)
                        System.out.println("与simulink接口"+SimulinklinkNameMap.get(SimulinksourceId)+"对应的接口"+SysmllinkNameMap.get(SysmlsourceId)+":没有设置通信协议");
                    if(SysmldestProtocol==null)
                        System.out.println("与simulink接口"+SimulinklinkNameMap.get(SimulinkdestId)+"对应的接口"+SysmllinkNameMap.get(SysmldestId)+":没有设置通信协议");
                    else if(!(SysmlsourceProtocol.equals(SimulinksourceProtocol)))
                        System.out.println("simulink和sysml对应的接口"+SimulinklinkNameMap.get(SimulinksourceId)+"通信协议不一致");
                }
    
            }
            
            
          
        }
    }

    public static void main(String[] args) {
        excute();
    }
}

