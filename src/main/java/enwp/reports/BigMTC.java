package enwp.reports;

import fastily.jwiki.core.Wiki;
import fastily.wpkit.text.ReportUtils;
import util.BotUtils;

/**
 * Reports on the largest MTC files according to this
 * <a href="https://tools.wmflabs.org/fastilybot/r/report5.txt">database report</a>.
 * 
 * @author Fastily
 *
 */
public class BigMTC
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = BotUtils.getFastilyBot();

	/**
	 * The title to leave the report at
	 */
	private static String reportPage = String.format("User:%s/Largest MTC Files", wiki.whoami());

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		wiki.edit(reportPage, ReportUtils.listify(BotUtils.updatedAt, BotUtils.fetchLabsReportListAsFiles(wiki, "report5"), true),
				"Updating report");
	}
}