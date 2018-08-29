package fastilybot.reports;

import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wptoolbox.BStrings;
import fastily.wptoolbox.BotUtils;

/**
 * Lists enwp files with a duplicate on Commons.
 * 
 * @author Fastily
 *
 */
public class DupeOnCom
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = "Wikipedia:Database reports/Local files with a duplicate on Commons";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 1);
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		
		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(BStrings.updatedAt, MQuery.exists(wiki, true, l), true), "Updating report");
	}
}