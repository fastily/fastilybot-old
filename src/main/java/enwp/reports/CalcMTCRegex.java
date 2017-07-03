package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Pre-computes regexes for MTC!
 * 
 * @author Fastily
 *
 */
public class CalcMTCRegex
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The title to post the report to.
	 */
	private static String reportPage = "Wikipedia:MTC!/Regexes";

	/**
	 * The output text to be posted to the report.
	 */
	private static String output = "<!-- This is a bot-generated regex library for MTC!, please don't touch, thanks! -->\n<pre>\n";

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage(reportPage + "/IncludeAlso", NS.TEMPLATE));
		rawL.addAll(TallyLics.comtpl);

		MQuery.linksHere(wiki, true, new ArrayList<>(rawL)).forEach((k, v) -> {
			v.add(k); // original template is included in results
			output += String.format("%s;%s%n", k, FL.pipeFence(WikiX.stripNamespaces(wiki, v)));
		});

		output += "</pre>";

		wiki.edit(reportPage, output, "Update report");
	}
}