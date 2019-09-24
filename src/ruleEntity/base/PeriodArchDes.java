package ruleEntity.base;

import static ruleEntity.base.PeriodReqArch.getPeriod;
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
 * 阶段间接口的周期的一致性---sysml、aadl
 */
public class PeriodArchDes {
	public static void excute() {
		Map<String, Component> componentListSimulink = new HashMap<>();
		Map<String, Channel> channelListSimulink = new HashMap<>();
		parseXML("simulink0726.xml", componentListSimulink, channelListSimulink);

		Map<String, Component> componentListAADL = new HashMap<>();
		Map<String, Channel> channelListAADL = new HashMap<>();
		parseXML("aadl0726.xml", componentListAADL, channelListAADL);

		List<String> componentList = keySet(componentListAADL);
		for (String componentId : componentList) {
			Component component = componentListAADL.get(componentId);
			String componentName = component.getAttr("name");
			if (componentId.equals("system"))
				continue;
			String sql = "select * from mapping where aadl_id=" + componentId;
			String simulinkId = connect(sql, "simulink");
			if (simulinkId == null)
				System.out.println("组件" + componentName + ":在子系统设计中没有对应的组件与之对应");
			else {
				List<Linkpoint> linkpointList = component.getLinkpointList();
				if (linkpointList != null) {
					Component componentSimulink = componentListSimulink.get(simulinkId);
					for (Linkpoint linkpoint : linkpointList) {
						String linkpointName = linkpoint.getAttr("name");
						String period = linkpoint.getAttr("period");
						String periodSimulink = null;
						if (period == null)
							System.out.println("架构模型中，接口" + linkpointName + "没有定义period");
						else {
							periodSimulink = getPeriod(componentSimulink, linkpointName);
							System.out.println(periodSimulink);
							if (periodSimulink == null)
								System.out.println("架构模型中，接口" + linkpointName + "在子系统设计中没有对应");
							else if (periodSimulink.equals("接口" + linkpointName + ":在子系统模型中没有定义period"))
								System.out.println(periodSimulink);
							else if (!(period.equals(periodSimulink)))
								System.out.println("架构模型中，接口" + linkpointName + "的period与子系统设计模型中对应接口周期不一致");

						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		excute();
	}
}
