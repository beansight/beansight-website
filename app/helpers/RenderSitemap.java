/**
 * @author Dirk <dirkmdev@gmail.com>
 * @author Steren Giannini <steren.giannini@gmail.com> 
 */

package helpers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Insight;
import models.User;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import controllers.Application;

import play.Logger;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.utils.Java;

public class RenderSitemap extends RenderSitemapXml {
	public RenderSitemap(List<User> users, List<Insight> insights) {
		super(getDocument(users, insights));
	}

	private static Document getDocument(List<User> users,
			List<Insight> insights) {

		Document doc = createSiteMapDocument();
		Element root = doc.getDocumentElement();
		
		addAnnotatedActions(doc);
		
		for(Insight i : insights) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("insightUniqueId", i.uniqueId);
			String loc = Router.getFullUrl("Application.showInsight", args);
			root.appendChild(createUrl(doc, loc, "daily", 0.9));
		}
		
		for(User u : users) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("userName", u.userName);
			String loc = Router.getFullUrl("Application.showUser", args);
			root.appendChild(createUrl(doc, loc, "weekly", 0.7));
		}

		return doc;
	}

}