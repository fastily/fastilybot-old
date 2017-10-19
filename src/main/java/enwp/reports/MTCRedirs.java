package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import util.BotUtils;

/**
 * Precomputes regexes for MTC!
 * 
 * @author Fastily
 *
 */
public class MTCRedirs
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{		
		Wiki wiki = BotUtils.getFastilyBot();
		String reportPage = "Wikipedia:MTC!/Redirects";
		
		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage(reportPage + "/IncludeAlso", NS.TEMPLATE));
		rawL.addAll(TallyLics.comtpl);

		StringBuilder b = new StringBuilder("<!-- This is a bot-generated regex library for MTC!, please don't change, thanks! -->\n<pre>\n");
		MQuery.linksHere(wiki, true, new ArrayList<>(rawL)).forEach((k, v) -> {
			v.add(0, k); // original template is included in results
			b.append(FL.pipeFence(wiki.nss(v)) + "\n");
		});

		b.append("</pre>");	
		
		wiki.edit(reportPage, b.toString(), "Updating report");
	}
}