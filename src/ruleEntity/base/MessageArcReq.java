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
 * 系统架构模型与需求模型间数据的追踪关系检查
 */
public class MessageArcReq {
	public static void excute() {
		Map<String, Component> componentListSysml = new HashMap<>();
		Map<String, Channel> channelListSysml = new HashMap<>();
		parseXML("sysml(4).xml", componentListSysml, channelListSysml);

		Map<String, Component> componentListAADL = new HashMap<>();
		Map<String, Channel> channelListAADL = new HashMap<>();
		parseXML("aadl(6).xml", componentListAADL, channelListAADL);

		List<String> componentList = keySet(componentListSysml);
		for (String componentId : componentList) {
			Component component = componentListSysml.get(componentId);
			String componentName = component.getAttr("name");
			if (componentId.equals("system"))
				continue;
			String sql = "select * from mapping where sysml_id=" + componentId;
			String aadlId = connect(sql, "aadl");

			if (aadlId == null)
				System.out.println("组件" + componentName + "在系统架构模型中没有对应的组件与之对应");
			else {

				// System.out.println(componentId + " " + aadlId);-587841842 161145622

				List<Linkpoint> linkpointList = component.getLinkpointList();
				if (linkpointList != null) {
					Component componentAADL = componentListAADL.get(aadlId);
					for (Linkpoint linkpoint : linkpointList) {
						String linkpointName = linkpoint.getAttr("name");
						String datatype = linkpoint.getAttr("datatype");
						String datatypeAADL = null;
						if (datatype == null)
							System.out.println("需求模型中，接口" + linkpointName + "没有定义datatype");
						else {
							datatypeAADL = getDatatype(componentAADL, linkpointName);
							if (datatypeAADL == null)
								System.out.println("需求模型中，接口" + linkpointName + "在架构设计中没有对应");
							else if (datatypeAADL.equals("接口" + linkpointName + "在架构模型中没有定义datatype"))
								System.out.println(datatypeAADL);
							else if (!(datatype.equals(datatypeAADL)))
								System.out.println("需求模型中，接口" + linkpointName + "的datatype与架构模型中对应接口周期不一致");

						}
					}
				}
			}
		}
	}

	public static String getDatatype(Component componentAADL, String linkpointName) {
		List<Linkpoint> linkpointList = componentAADL.getLinkpointList();
		boolean haveLink = false;
		if (linkpointList != null) {
			for (Linkpoint linkpoint : linkpointList) {
				String name = linkpoint.getAttr("name");
				String datatype = linkpoint.getAttr("datatype");
				if (name.equals(linkpointName) && datatype != null) {
					return datatype;
				} else if (name.equals(linkpointName))
					haveLink = true;
			}
			if (haveLink)
				return "接口" + linkpointName + ":在架构模型中没有定义datatype";
		}
		return null;
	}

	public static void main(String[] args) {
		excute();
	}
}
