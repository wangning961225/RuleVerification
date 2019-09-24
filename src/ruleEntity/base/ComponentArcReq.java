package ruleEntity.base;

import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entity.Channel;
import entity.Component;
import utils.XMLParseUtil;

/**
 * 系统架构模型与需求模型间组件的追踪关系检查
 */
public class ComponentArcReq {
	public static void excute() {
		Map<String, Component> componentListAADL = new LinkedHashMap<>();
		Map<String, Channel> channelListAADL = new LinkedHashMap<>();

		Map<String, Component> componentListSysml = new LinkedHashMap<>();
		Map<String, Channel> channelListSysml = new LinkedHashMap<>();

		XMLParseUtil.parseXML("aadl(6).xml", componentListAADL, channelListAADL);
		XMLParseUtil.parseXML("sysml(4).xml", componentListSysml, channelListSysml);

		List<String> componentSysmlList = keySet(componentListSysml);
//        System.out.println(componentSysmlList);

		for (String sysmlCom : componentSysmlList) {
			if (sysmlCom.equals("system"))
				continue;
			String componentName = componentListSysml.get(sysmlCom).getAttr("name");

			/*
			 * List<Map<String,Map<String, String>>> list=mapping(); String simulinkId="";
			 * for(Map<String,Map<String, String>> map:list){ if(map.get("sysml")!=null &&
			 * map.get("sysml").get("id")!=null &&
			 * map.get("sysml").get("id").equals(sysmlCom)){
			 * simulinkId=map.get("simulink").get("id"); } }
			 */

			String aadlId = "";

			String sql = "select * from mapping where sysml_id=" + sysmlCom;
//        System.out.println(sql);
			aadlId = connect(sql, "aadl");
			if (aadlId != null && !aadlId.isEmpty()) {
				String componentSysmlType = componentListSysml.get(sysmlCom).getAttr("type");
				String componentAADLType = componentListAADL.get(aadlId).getAttr("type");

				if (!componentSysmlType.equals(componentAADLType))
					System.out.println("需求模型中的组件" + componentName + "的组件类型与系统架构模型中的组件类型不一致");
			} else
				System.out.println("需求模型中的组件" + componentName + "在架构模型中的没有对应的组件！");
		}
	}

	public static void main(String[] args) {
		excute();
	}

}
