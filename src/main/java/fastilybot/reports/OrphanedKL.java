package fastilybot.reports;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wptoolbox.BStrings;
import fastily.wptoolbox.BotUtils;
import fastily.wptoolbox.WTP;

/**
 * Lists enwp files that are tagged keep local, but orphaned.
 * 
 * @author Fastily
 *
 */
public class OrphanedKL
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.retainAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE));

		wiki.edit("Wikipedia:Database reports/Orphaned free files tagged keep local", BotUtils.listify(BStrings.updatedAt, l, true),
				"Updating report");
	}
}