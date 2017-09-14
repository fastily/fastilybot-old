package enwp.reports;

import java.util.HashSet;

import fastily.jwiki.core.Wiki;
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

		HashSet<String> l = BotUtils.fetchLabsReportSet(wiki, "report8", "Draft:");
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:AFC submission"));
		BotUtils.removeListFromHS(l, wiki.whatTranscludesHere("Template:Mfd"));
		BotUtils.removeListFromHS(l, wiki.getCategoryMembers("Category:Candidates for speedy deletion"));

		StringBuilder b = new StringBuilder(BotUtils.updatedAt + "\n{| class=\"wikitable sortable\"\n! No.\n! Name\n! Last Changed\n");
		int i = 0;
		for (String s : l)
			try
			{
				b.append(String.format("|-\n| %d\n| [[%s]]\n| %s\n", ++i, s, wiki.getRevisions(s, 1, false, null, null).get(0).timestamp));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

		b.append("|}");

		wiki.edit(String.format("User:%s/Hidden Drafts", wiki.whoami()), b.toString(), "Updating report");
	}
}