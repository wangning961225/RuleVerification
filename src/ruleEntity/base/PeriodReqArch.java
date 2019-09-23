package ruleEntity.base;

import static utils.Dataconnect.connect;
import static utils.KeySet.keySet;
import static utils.XMLParseUtil.parseXML;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Channel;
import entity.Component;
import entity.Linkpoint;

/**
 * 阶段间接口的周期的一致性---ayaml、simulink
 */
public class PeriodReqArch {
	public static void excute() {
		Map<String, Component> componentListSysml = new HashMap<>();
		Map<String, Channel> channelListSysml = new HashMap<>();
		parseXML("sysml0726.xml", componentListSysml, channelListSysml);

		Map<String, Component> componentListAADL = new HashMap<>();
		Map<String, Channel> channelListAADL = new HashMap<>();
		parseXML("aadl0726.xml", componentListAADL, channelListAADL);

		List<String> componentList = keySet(componentListSysml);
		for (String componentId : componentList) {
			Component component = componentListSysml.get(componentId);
			String componentName = component.getAttr("name");
			if (componentId.equals("system"))
				continue;
			String sql = "select * from mapping where sysml_id=" + componentId;
			String aadlId = connect(sql, "aadl");
			if (aadlId == null)
				System.out.println("组件" + componentName + ":在系统架构中没有对应的组件与之对应");
			else {
				List<Linkpoint> linkpointList = component.getLinkpointList();
				if (linkpointList != null) {
					Component componentAADL = componentListAADL.get(aadlId);
					for (Linkpoint linkpoint : linkpointList) {
						String linkpointName = linkpoint.getAttr("name");
						String period = linkpoint.getAttr("period");
						String periodAADL = null;
						if (period == null)
							System.out.println("需求模型中，接口" + linkpointName + "没有定义period");
						else {
							periodAADL = getPeriod(componentAADL, linkpointName);
							if (periodAADL == null)
								System.out.println("需求模型中，接口" + linkpointName + "在架构设计中没有对应");
							else if (periodAADL.equals("接口" + linkpointName + ":在架构模型中没有定义period"))
								System.out.println(periodAADL);
							else if (!(period.equals(periodAADL)))
								System.out.println("需求模型中，接口" + linkpointName + "的period与架构模型中对应接口周期不一致");

						}
					}
				}
			}
		}
	}

	public static String getPeriod(Component componentAADL, String linkpointName) {
		List<Linkpoint> linkpointList = componentAADL.getLinkpointList();
		boolean haveLink = false;
		if (linkpointList != null) {
			for (Linkpoint linkpoint : linkpointList) {
				String name = linkpoint.getAttr("name");
				String period = linkpoint.getAttr("period");
				if (name.equals(linkpointName) && period != null) {
					return period;
				} else if (name.equals(linkpointName))
					haveLink = true;
			}
			if (haveLink)
				return "接口" + linkpointName + ":在架构模型中没有定义period";
		}
		return null;
	}

	public static void main(String[] args) {
		excute();
	}
}
