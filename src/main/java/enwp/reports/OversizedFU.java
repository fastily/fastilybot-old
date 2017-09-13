package enwp.reports;

import java.util.HashSet;
import java.util.List;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;

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

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, "report7");

		removeListFromHS(l,
				wiki.getCategoryMembers("Category:Wikipedia non-free file size reduction requests for manual processing"));
		removeListFromHS(l, wiki.getCategoryMembers("Category:Wikipedia non-free file size reduction requests"));
		removeListFromHS(l, wiki.getCategoryMembers("Category:Non-free images tagged for no reduction"));
		removeListFromHS(l, wiki.whatTranscludesHere("Template:Orphaned non-free revisions", NS.FILE));
		removeListFromHS(l, wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		removeListFromHS(l, wiki.whatTranscludesHere("Template:Ffd", NS.FILE));

		wiki.edit(String.format("Wikipedia:Database reports/Large fair-use images", wiki.whoami()), BotUtils.listify(BotUtils.updatedAt, l, true),
				"Updating report");
	}
	
	/**
	 * Removes a List from a HashSet. Copies a List into a HashSet and then removes it from {@code l}
	 * 
	 * @param l The HashSet to remove elements contained in {@code toRemove} from
	 * @param toRemove The List of items to remove from {@code l}
	 */
	private static void removeListFromHS(HashSet<String> l, List<String> toRemove)
	{
		l.removeAll(new HashSet<>(toRemove));
	}
}