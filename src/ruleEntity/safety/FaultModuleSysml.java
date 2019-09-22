package ruleEntity.safety;

import entity.Channel;
import entity.Component;
import entity.Propagation;
import utils.XMLParseUtil;

import java.util.*;

import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

/**
 * AADL中定义的错误类型在需求模型中没有对应的处理模块
 * <p>
 * 此处没有对硬件的故障以及映射做处理，本项目中sysml是对软件需求的建模语言
 */

public class FaultModuleSysml {
    public static void excute() {
        Map<String, Component> componentListAADL = new HashMap<>();
        Map<String, Channel> channelListAADL = new HashMap<>();
        parseXML("aadl(6).xml", componentListAADL, channelListAADL);

        List<String> componentList = keySet(componentListAADL);

        for (String componentId : componentList) {
            String componentName = componentListAADL.get(componentId).getAttr("name");
            if (componentId.equals("system"))
                continue;

            Map<String, Propagation> propagationList = componentListAADL.get(componentId).getPropagationList();
            String componentType = componentListAADL.get(componentId).getAttr("type");
            String sql = "select * from mapping where aadl_id=" + componentId;

            String sysmlId = connect(sql, "sysml");

            boolean isHardware = "device".equals(componentType);

            if (sysmlId == null && !isHardware) {
                System.out.println("AADL模型中的组件" + componentName + "没有sysml的组件与之对应");
            } else if (!isHardware) {
                List<String> faultDealModules = faultDealModules(sysmlId);
                List<String> propagationIds = keySet(propagationList);

                for (String progationId : propagationIds) {
                    String exception = propagationList.get(progationId).getAttr("fault");
                    if (!faultDealModules.isEmpty() && !faultDealModules.contains(exception)) {
                        System.out.println("组件" + componentName + ":AADL中定义的错误类型" +
                                exception + "在需求模型中没有对应的处理模块");
                    }
                }
            }
        }
    }

    public static List<String> faultDealModules(String sysmlId) {
        List<String> faultDealModules = new ArrayList<>();
        Map<String, Component> componentListSysml = new HashMap<>();
        Map<String, Channel> channelListSysml = new HashMap<>();
        XMLParseUtil.parseXML("sysml(4).xml", componentListSysml, channelListSysml);

        Map<String, Component> subComponentList = componentListSysml.get(sysmlId).getSubComponentList();
        List<String> subList = keySet(subComponentList);
        for (String subId : subList) {
            Component subComponent = subComponentList.get(subId);
            String type = subComponent.getAttr("type");
            if (type != null && "faultTask".equals(type)) {
                faultDealModules.add(subComponent.getAttr("faulttype"));
            }
        }

//        System.out.println(faultDealModules);
        return faultDealModules;
    }

    public static void main(String[] args) {
//        faultDealModules("161145622");
        excute();
    }
}
