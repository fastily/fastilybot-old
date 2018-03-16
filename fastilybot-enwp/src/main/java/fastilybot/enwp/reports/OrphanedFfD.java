package fastilybot.enwp.reports;

import java.util.ArrayList;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastilybot.util.BStrings;
import fastilybot.util.BotUtils;
import fastilybot.util.WTP;
import fastily.jwiki.core.MQuery;

/**
 * Lists pages tagged for FfD with no corresponding FfD page/entry.
 * 
 * @author Fastily
 *
 */
public class OrphanedFfD
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		
		ArrayList<String> l = new ArrayList<>();
		MQuery.linksHere(wiki, false, WTP.ffd.getTransclusionSet(wiki, NS.FILE)).forEach((k, v) -> {
			if (!v.stream().anyMatch(s -> s.startsWith("Wikipedia:Files for discussion")))
				l.add(k);
		});

		wiki.edit(String.format("User:%s/Orphaned FfD", wiki.whoami()), BotUtils.listify(BStrings.updatedAt, l, true), String.format("Updating report (%d items)", l.size()));
	}
}