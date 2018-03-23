package fastilybot.reports;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastilybot.util.BStrings;
import fastilybot.util.BotUtils;

/**
 * Lists oversized (> 450x450) non-free bitmap images on enwp.
 * 
 * @author Fastily
 *
 */
public class OversizedFU
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = "Wikipedia:Database reports/Large fair-use images";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, "report7");
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		
		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(BStrings.updatedAt, l, true),
				"Updating report");
	}
}