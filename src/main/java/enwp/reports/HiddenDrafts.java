package enwp.reports;

import java.util.HashSet;

import fastily.jwiki.core.Wiki;
import fastily.wpkit.text.ReportUtils;
import util.BotUtils;

/**
 * Finds drafts on enwp which are not part of the AfC process.
 * 
 * @author Fastily
 *
 */
public class HiddenDrafts
{
	/**
	 * Main driver
	 * 
	 * @param args Program args, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();

		HashSet<String> l = BotUtils.fetchLabsReportList(wiki, "report8", "Draft:");
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:AFC submission"));
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:Mfd"));
		BotUtils.removeListFromHS(l, wiki.getCategoryMembers("Category:Candidates for speedy deletion"));

		wiki.edit(String.format("User:%s/Hidden Drafts", wiki.whoami()), ReportUtils.listify(BotUtils.updatedAt, l, false),
				"Updating report");
	}
}