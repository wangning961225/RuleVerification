package ruleEntity.safety;

import entity.Channel;
import entity.Component;
import entity.State;
import entity.Transition;
import utils.XMLParseUtil;

import java.util.*;

import static ruleEntity.safety.FaultModuleSysml.faultDealModules;
import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

public class FaultDealSysmlSimulink {
    public static void excute() {
        Map<String, Component> componentListSysml = new HashMap<>();
        Map<String, Channel> channelListSysml = new HashMap<>();
        XMLParseUtil.parseXML("sysml(4).xml", componentListSysml, channelListSysml);

        List<String> componentList = keySet(componentListSysml);

        for (String componentId : componentList) {
            String componentName = componentListSysml.get(componentId).getAttr("name");
            List<String> faultDealModules = faultDealModules(componentId);
//            System.out.println(faultDealModules);
            if (!faultDealModules.isEmpty()) {
                Set<String> dealtypeList=new HashSet<>();
                String sql = "select * from mapping where sysml_id=" + componentId;
                String simulinkId = connect(sql, "simulink");
                if (simulinkId == null)
                    System.out.println("sysml模型中的组件" + componentName + "没有子系统设计的组件与之对应");
                else {
//                    System.out.println(simulinkId);
                    dealtypeList=dealList(simulinkId);
                    for(String faultType:faultDealModules){
                        if(!dealtypeList.contains(faultType))
                            System.out.println("组件"+componentName+":对应的故障类型"+faultType+"的处理模块在子系统设计中没有对应的实现");
                    }
                }
            }
        }
    }

    public static Set<String> dealList(String simulinkId) {
        Set<String> dealTyes = new HashSet<>();
        Map<String, Component> componentListSimulink = new HashMap<>();
        Map<String, Channel> channelListSimulink = new HashMap<>();
        parseXML("simulink(2).xml", componentListSimulink, channelListSimulink);

        List<Transition> transitionList = componentListSimulink.get(simulinkId).getTransitionList();
        Map<String, State> stateList = componentListSimulink.get(simulinkId).getStateList();

        List<String> stateIds = keySet(stateList);
//        System.out.println(stateIds.size());
        for (String stateId : stateIds) {
            State state = stateList.get(stateId);
            List<State> subStateList=stateList.get(stateId).getSubStateList();
//            System.out.println("subStateList"+subStateList);
            List<Transition> transList=stateList.get(stateId).getTransitionList();
//            for(Transition transition:transList){
//                System.out.println("dest"+transition.getAttr("dest"));
//            }

            if (state != null && state.getAttr("faultType") != null) {
                for (Transition transition : transitionList) {
                    if (transition.getAttr("source").equals(state.getAttr("id"))) {
//                        System.out.println(transition.getAttr("dest"));
                        String destId = transition.getAttr("dest");
                        State destState = stateList.get(destId);

                        if (destState != null && destState.getAttr("faultType") == null) {
                            dealTyes.add(state.getAttr("faultType"));
                        }
                    }
                }
            }
            if(!subStateList.isEmpty()){
//                System.out.println("----");
                Set<String> dealSubList=dealSubList(subStateList,transList,stateList);
//                System.out.println(dealSubList);
                dealTyes.addAll(dealSubList);
            }
        }
//        System.out.println(dealTyes);
        return dealTyes;
    }

    public static Set<String> dealSubList(List<State> subList,List<Transition> transitions,Map<String, State> stateList){
//        System.out.println(transitions.size());
        Set<String> dealSubList=new HashSet<>();
        for(State state:subList){
            if(state.getAttr("faultType")!=null){
                for (Transition transition : transitions) {
                    if (transition.getAttr("source").equals(state.getAttr("id"))) {
//                        System.out.println("faultState"+state);

                        String destId = transition.getAttr("dest");
//                        System.out.println(destId);
                        for(State subState:subList){
                            if(subState.getAttr("id").equals(destId) && subState.getAttr("faultType")==null)
                                dealSubList.add(state.getAttr("faultType"));
                        }
                    }
                }
            }
        }
        return dealSubList;
    }

    public static void main(String[] args) {
        excute();
    }
}
