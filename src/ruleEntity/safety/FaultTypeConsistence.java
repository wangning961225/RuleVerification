package ruleEntity.safety;

import entity.Channel;
import entity.Component;
import entity.ExceptionXML;
import entity.Propagation;
import utils.XMLParseUtil;
import java.util.*;

import static ruleEntity.safety.FaultType.getExceptionIdList;
import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;
    /**
        * 系统架构模型定义的错误类型在需求模型的错误类型列表中都有对应的定义
    */

public class FaultTypeConsistence {

    public static void excute() {
        Map<String, Component> componentListAADL = new HashMap<>();
        Map<String, Channel> channelListAADL = new HashMap<>();
        parseXML("aadl(6).xml", componentListAADL, channelListAADL);

        List<String> componentList = keySet(componentListAADL);

        for (String componentId : componentList) {
            String componentName = componentListAADL.get(componentId).getAttr("name");
            if(componentId.equals("system"))
                continue;
            String componentType=componentListAADL.get(componentId).getAttr("type");
            String sql="select * from mapping where aadl_id="+componentId;

            String sysmlId=connect(sql,"sysml");

            boolean isHardware="device".equals(componentType);

            if(sysmlId==null && !isHardware){
                System.out.println("AADL模型中的组件"+componentName+"没有sysml的组件与之对应");
            }
            else if(!isHardware) {
                List<String> errorTypes = sysmlTypes(sysmlId);

                Map<String, Propagation> propagationList = componentListAADL.get(componentId).getPropagationList();

                List<String> propagationIds = keySet(propagationList);

                for (String progationId : propagationIds) {
                    String exception = propagationList.get(progationId).getAttr("fault");
                    if (!errorTypes.isEmpty() && !errorTypes.contains(exception)) {
                        System.out.println("组件" + componentName + "AADL中定义的错误类型" + exception + "在需求模型中没有体现");
                    }
                }
            }
        }
    }

    public static List<String> sysmlTypes(String sysmlId){
        List<String> exceptionList=new ArrayList<>();

        Map<String, Component> componentListSysml = new LinkedHashMap<>();
        Map<String, Channel> channelListSysml = new LinkedHashMap<>();
        parseXML("sysml(4).xml", componentListSysml, channelListSysml);

        Map<String, ExceptionXML> exceptions = componentListSysml.get(sysmlId).getExceptionList();
        List<String> exceptionIdList = getExceptionIdList(exceptions);
        for (String exceptionId : exceptionIdList) {
            exceptionList.add(exceptions.get(exceptionId).getAttr("name"));
        }

        return exceptionList;
    }

    public static void main(String[] args){
        excute();
    }
}