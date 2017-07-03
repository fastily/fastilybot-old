package enwp.reports;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.WPStrings;
import fastily.wpkit.WTP;
import fastily.wpkit.tplate.ParsedItem;
import fastily.wpkit.tplate.Template;
import fastily.wpkit.util.TParse;
import fastily.wpkit.util.Toolbox;

/**
 * Lists files nominated for deletion via file PROD.
 * 
 * @author Fastily
 *
 */
public class FilePRODSum
{
	/**
	 * The Wiki to use
	 */
	private static Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The date input format (read from PROD template)
	 */
	private static DateTimeFormatter dateInFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssz");

	/**
	 * Matches the file PROD template.
	 */
	private static Pattern filePRODRegex = Pattern.compile(WTP.fprod.getRegex(wiki));

	/**
	 * The report text to output.
	 */
	private static String reportText = "{{/header}}\n" + WPStrings.updatedAt
			+ "{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;\"\n! Date\n! File\n! Reason\n ! Use count\n";

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> fl = wiki.getCategoryMembers("Category:All files proposed for deletion", NS.FILE);

		HashMap<String, Integer> counts = new HashMap<>();
		MQuery.fileUsage(wiki, fl).forEach((k, v) -> counts.put(k, v.size()));

		ArrayList<String> fails = new ArrayList<>();
		MQuery.getPageText(wiki, fl).forEach((k, v) -> {
			try
			{
				Template t = ParsedItem.parse(wiki, k, TParse.extractTemplate(filePRODRegex, v)).tplates.get(0);
				reportText += String.format("|-%n| %s%n| [[:%s]]%n| %s%n | %d%n",
						WPStrings.iso8601dtf.format(ZonedDateTime.parse(t.get("timestamp").toString() + "UTC", dateInFmt)), k,
						t.get("concern").toString(), counts.get(k));
			}
			catch (Throwable e)
			{
				fails.add(k);
			}
		});

		reportText += "|}\n";

		if (!fails.isEmpty())
			reportText += Toolbox.listify("\n== Failed to Parse ==\n", fails, true);

		wiki.edit(String.format("User:%s/File PROD Summary", wiki.whoami()), reportText, "Updating report");
	}
}