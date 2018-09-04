package fastilybot.reports;

import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wptoolbox.BotUtils;
import fastilybot.shared.Settings;

/**
 * Looks for files without a license tag.
 * 
 * @author Fastily
 *
 */
public class MissingFCT
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String rPage = "Wikipedia:Database reports/Files without a license tag";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 8);
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 5));
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 6));
		
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file"));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore"))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));
		
		HashSet<String> lcl = new HashSet<>(wiki.getLinksOnPage("User:FastilyBot/License categories"));
		MQuery.getCategoriesOnPage(wiki, l).forEach((k, v) -> {
			if(v.isEmpty() || v.stream().anyMatch(lcl::contains))
				l.remove(k);
		});

		wiki.edit(rPage, BotUtils.listify(Settings.updatedAt, l, true), "Updating report");
	}
}