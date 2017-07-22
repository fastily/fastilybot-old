package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.util.WikiX;
import util.BotUtils;

/**
 * Pre-computes regexes for MTC!
 * 
 * @author Fastily
 *
 */
public class FCTRedirsForMTC
{
	/**
	 * The title to post the report to.
	 */
	private static String reportPage = "Wikipedia:MTC!/Redirects";

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
		Wiki wiki = BotUtils.getFastilyBot();
		
		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage(reportPage + "/IncludeAlso", NS.TEMPLATE));
		rawL.addAll(TallyLics.comtpl);

		MQuery.linksHere(wiki, true, new ArrayList<>(rawL)).forEach((k, v) -> {
			v.add(0, k); // original template is included in results
			output += FL.pipeFence(WikiX.stripNamespaces(wiki, v)) + "\n";
		});

		output += "</pre>";		
		
		wiki.edit(reportPage, output, "Update report");
	}
}