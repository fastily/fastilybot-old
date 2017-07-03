package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WTP;
import fastily.wpkit.util.Toolbox;

/**
 * Finds and flags orphaned free media files on enwp.
 * 
 * @author Fastily
 *
 */
public class FlagOI
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashSet<String> l = Toolbox.fetchLabsReportListAsFiles(wiki, "report3");
		l.removeAll(WTP.orphan.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(Toolbox.fetchLabsReportListAsFiles(wiki, "report4"));
		l.removeAll(new HashSet<>(MQuery.exists(wiki, false, new ArrayList<>(l))));
		
		for(String s : l)
			wiki.addText(s, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false);
	}
}