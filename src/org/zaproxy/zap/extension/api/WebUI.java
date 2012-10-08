/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.API.Format;
import org.zaproxy.zap.extension.api.API.RequestType;

public class WebUI {
	
	private API api;

	public WebUI(API api) {
		this.api = api;
	}
	
	private ApiElement getElement(ApiImplementor impl, String name, RequestType reqType) throws ApiException {
		if (RequestType.action.equals(reqType) && name != null) {
			// Action form
			List<ApiAction> actionList = impl.getApiActions();
			ApiAction action = null;
			for (ApiAction act : actionList) {
				if (name.equals(act.getName())) {
					action = act;
					break;
				}
			}
			if (action == null) {
				throw new ApiException(ApiException.Type.BAD_ACTION);
			}
			return action;
		} else if (RequestType.other.equals(reqType) && name != null) {
			// Other form
			List<ApiOther> otherList = impl.getApiOthers();
			ApiOther other = null;
			for (ApiOther oth : otherList) {
				if (name.equals(oth.getName())) {
					other = oth;
					break;
				}
			}
			if (other == null) {
				throw new ApiException(ApiException.Type.BAD_OTHER);
			}
			return other;
		} else if (RequestType.view.equals(reqType) && name != null) {
			List<ApiView> viewList = impl.getApiViews();
			ApiView view = null;
			for (ApiView v : viewList) {
				if (name.equals(v.getName())) {
					view = v;
					break;
				}
			}
			if (view == null) {
				throw new ApiException(ApiException.Type.BAD_VIEW);
			}
			return view;
		} else {
			throw new ApiException(ApiException.Type.BAD_TYPE);
		}
	}
	
	private void appendElements(StringBuilder sb, String component, String type, List<ApiElement> elementList) {
		sb.append("<table>\n");
		for (ApiElement element : elementList) {
			List<String> params = element.getParamNames();
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href=\"http://zap/");
			sb.append(Format.UI.name());
			sb.append('/');
			sb.append(component);
			sb.append('/');
			sb.append(type);
			sb.append('/');
			sb.append(element.getName());
			sb.append("/\">");
			sb.append(element.getName());
			if (params != null) {
				sb.append(" (");
				for (String param : params) {
					sb.append(param);
					sb.append(' ');
				}
				sb.append(") ");
			}
			sb.append("</a>");
			sb.append("</td>");
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

	}

	public String handleRequest(String component, ApiImplementor impl,
			RequestType reqType, String name) throws ApiException {
		// Generate HTML UI
		//contentType = "text/html";
		StringBuilder sb = new StringBuilder();
		sb.append("<head>\n");
		sb.append("<title>");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</title>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		if (component != null && reqType != null) {
			sb.append("<script>\n");
			sb.append("function submitScript() {\n");
			sb.append("var format = document.getElementById('zapapiformat').value\n");
			sb.append("var url = 'http://zap/' + format + '/" + component + "/" + reqType.name() + "/" + name + "/'\n");
			sb.append("var form=document.getElementById('zapform');\n");
			sb.append("form.action = url;\n");
			sb.append("form.submit();\n");
			sb.append("}\n");
			sb.append("</script>\n");
		}
		sb.append("<h1>");
		sb.append("<a href=\"http://zap/");
		sb.append(Format.UI.name());
		sb.append("/\">");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</a>");
		sb.append("</h1>\n");
		
		if (impl != null) {
			sb.append("<h2>");
			sb.append("<a href=\"http://zap/");
			sb.append(Format.UI.name());
			sb.append("/");
			sb.append(component);
			sb.append("/\">");
			sb.append(Constant.messages.getString("api.html.component"));
			sb.append(component);
			sb.append("</a>");
			sb.append("</h2>\n");
			
			if (name != null) {
				ApiElement element = this.getElement(impl, name, reqType);
				
				List<String> params = element.getParamNames();
				sb.append("<h3>");
				sb.append(Constant.messages.getString("api.html." + reqType.name()));
				sb.append(element.getName());
				sb.append("</h3>\n");
				sb.append("<form id=\"zapform\" name=\"zapform\">");
				sb.append("<table>\n");
				sb.append("<tr><td>");
				sb.append(Constant.messages.getString("api.html.format"));
				sb.append("</td><td>\n");
				sb.append("<select id=\"zapapiformat\" name=\"zapapiformat\">\n");
				sb.append("<option>JSON</option>\n");
				sb.append("<option>HTML</option>\n");
				sb.append("<option>XML</option>\n");
				sb.append("</select>\n");
				sb.append("</td></tr>\n");
				
				if (params != null) {
					for (String param : params) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(param);
						sb.append("</td>");
						sb.append("<td>");
						sb.append("<input id=\"");
						sb.append(param);
						sb.append("\" name=\"");
						sb.append(param);
						sb.append("\"></input>");
						sb.append("</td>");
						sb.append("</tr>\n");
					}
				}
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("</td>");
				sb.append("<td>");
				sb.append("<input id=\"button\" value=\"");
				sb.append(element.getName());
				sb.append("\" type=\"button\" onclick=\"submitScript();\">");
				sb.append("</td>");
				sb.append("</tr>\n");
				sb.append("</table>\n");
				sb.append("</form>\n");

			} else {
				List<ApiElement> elementList = new ArrayList<ApiElement>();
				List<ApiView> viewList = impl.getApiViews();
				if (viewList != null && viewList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.views"));
					sb.append("</h3>\n");
					elementList.addAll(viewList);
					this.appendElements(sb, component, RequestType.view.name(), elementList);
				}

				List<ApiAction> actionList = impl.getApiActions();
				if (actionList != null && actionList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.actions"));
					sb.append("</h3>\n");
					elementList = new ArrayList<ApiElement>();
					elementList.addAll(actionList);
					this.appendElements(sb, component, RequestType.action.name(), elementList);
				}
				
				List<ApiOther> otherList = impl.getApiOthers();
				if (otherList != null && otherList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.others"));
					sb.append("</h3>\n");
					elementList = new ArrayList<ApiElement>();
					elementList.addAll(otherList);
					this.appendElements(sb, component, RequestType.other.name(), elementList);
				}
			}

		} else {
			sb.append("<h3>");
			sb.append(Constant.messages.getString("api.html.components"));
			sb.append("</h3>\n");
			Set<String> components = api.getImplementors().keySet();
			sb.append("<table>\n");
			for (String cmp : components) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<a href=\"http://zap/");
				sb.append(Format.UI.name());
				sb.append('/');
				sb.append(cmp);
				sb.append("/\">");
				sb.append(cmp);
				sb.append("</a>");
				sb.append("</td>");
				sb.append("</tr>\n");
			}
			sb.append("</table>\n");
		}
		sb.append("</body>\n");
		
		return sb.toString();
		
	}

}