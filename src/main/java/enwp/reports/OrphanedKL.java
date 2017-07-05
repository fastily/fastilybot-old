package enwp.reports;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.text.WPStrings;
import fastily.wpkit.text.WTP;
import fastily.wpkit.util.Toolbox;

/**
 * Lists enwp files that are tagged keep local, but orphaned.
 * 
 * @author Fastily
 *
 */
public class OrphanedKL
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
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.retainAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE));

		wiki.edit(String.format("User:%s/Orphaned Keep Local", wiki.whoami()), Toolbox.listify(WPStrings.updatedAt, l, true),
				"Updating report");
	}
}