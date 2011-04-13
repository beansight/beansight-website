/**
 * @author Steren Giannini <steren.giannini@gmail.com> 
 */
package helpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated action will appear in the generated sitemap, see
 * http://sitemaps.org
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InSitemap {
	/**
	 * How frequently the page is likely to change. This value provides general
	 * information to search engines and may not correlate exactly to how often
	 * they crawl the page. Valid values are:
	 * 
	 * always hourly daily weekly monthly yearly never
	 * 
	 */
	String changefreq() default "daily";

	/**
	 * The priority of this URL relative to other URLs on your site. Valid
	 * values range from 0.0 to 1.0. This value does not affect how your pages
	 * are compared to pages on other sites—it only lets the search engines know
	 * which pages you deem most important for the crawlers.
	 * 
	 */
	double priority() default 0.5;
}