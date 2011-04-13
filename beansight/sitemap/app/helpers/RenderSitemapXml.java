/**
 * @author Dirk <dirkmdev@gmail.com>
 * @author Steren Giannini <steren.giannini@gmail.com> 
 */
package helpers;

import java.lang.reflect.Method;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import play.Logger;
import play.exceptions.UnexpectedException;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.RenderXml;
import play.utils.Java;
import controllers.Application;

public class RenderSitemapXml extends RenderXml {
    public RenderSitemapXml(Document doc) {
        super(doc);
    }

    protected static Document createSiteMapDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnexpectedException(e);
        }
        Document doc = builder.newDocument();

        // Create root (urlset) element
        Element root = doc.createElement("urlset");
        root.setAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
        root.setAttribute("xmlns:geo", "http://www.google.com/geo/schemas/sitemap/1.0");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String xsiVersion = "http://www.sitemaps.org/schemas/sitemap/0.9";
        String xsiXsd = "http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd";
        root.setAttribute("xsi:schemaLocation", xsiVersion + " " + xsiXsd);
        doc.appendChild(root);

        return doc;
    }
    
	protected static void addAnnotatedActions(Document doc) {
		Element root = doc.getDocumentElement();

		List<Method> insitemaps = Java.findAllAnnotatedMethods(Application.class, InSitemap.class);
		for(Method insitemap : insitemaps) {
			Logger.info(insitemap.getName());
			
			root.appendChild(createUrl(doc, Router.getFullUrl(Application.class.getName() + "." + insitemap.getName()), insitemap.getAnnotation(InSitemap.class).changefreq(), insitemap.getAnnotation(InSitemap.class).priority()));
		}
		
	}

    protected static Element createUrl(Document doc, String loc, String changefreq, Double priority) {
        Element url = doc.createElement("url");
        appendTextElement(url, "loc", loc);
        appendTextElement(url, "changefreq", changefreq);
        appendTextElement(url, "priority", priority);
        return url;
    }

    private static void appendTextElement(Element parent, String name, Object value) {
        if (value == null) {
            return;
        }

        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(name);
        parent.appendChild(child);
        child.appendChild(doc.createTextNode(value.toString()));
    }
}