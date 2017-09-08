package enwp.reports;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.text.StrUtil;
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

		BotUtils.removeListFromHS(l,
				wiki.getCategoryMembers("Category:Wikipedia non-free file size reduction requests for manual processing"));
		BotUtils.removeListFromHS(l, wiki.getCategoryMembers("Category:Wikipedia non-free file size reduction requests"));
		BotUtils.removeListFromHS(l, wiki.getCategoryMembers("Category:Non-free images tagged for no reduction"));
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:Orphaned non-free revisions", NS.FILE));
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:Ffd", NS.FILE));

		wiki.edit(String.format("Wikipedia:Database reports/Large fair-use images", wiki.whoami()), StrUtil.listify(BotUtils.updatedAt, l, true),
				"Updating report");
	}
}