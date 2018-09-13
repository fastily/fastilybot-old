package fastilybot;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.reflect.TypeToken;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.WParser;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.core.WParser.WTemplate;
import fastily.jwiki.dwrap.PageSection;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.wptoolbox.BotUtils;
import fastily.wptoolbox.DateUtils;
import fastily.wptoolbox.WTP;
import okhttp3.HttpUrl;

/**
 * Fastily's Wikipedia Reports
 * 
 * @author Fastily
 *
 */
public class Reports
{
	/**
	 * The type representation for a map like [ String : Boolean ].
	 */
	private static final Type strBoolHM = new TypeToken<HashMap<String, Boolean>>(){}.getType();
	
	/**
	 * The main Wiki object to use
	 */
	private Wiki wiki;

	/**
	 * Constructor, takes a logged-in Wiki object to enwp.
	 * 
	 * @param wiki The Wiki object to use
	 */
	protected Reports(Wiki wiki)
	{
		this.wiki = wiki;
	}

	/**
	 * Lists enwp files with a duplicate on Commons.
	 */
	public void dupeOnCom()
	{
		String rPage = "Wikipedia:Database reports/Local files with a duplicate on Commons";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 1);
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(Settings.updatedAt, MQuery.exists(wiki, true, l), true), "Updating report");
	}

	/**
	 * Lists files nominated for deletion via file PROD.
	 */
	public void fprodSum()
	{
		// constants
		DateTimeFormatter dateInFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssz");
		Pattern filePRODRegex = Pattern.compile(WTP.fprod.getRegex(wiki));
		StringBuffer reportText = new StringBuffer("{{/header}}\n" + Settings.updatedAt
				+ "{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;\"\n! Date\n! File\n! Reason\n! Use count\n");

		ArrayList<String> fl = wiki.getCategoryMembers("Category:All files proposed for deletion", NS.FILE);

		// logic
		HashMap<String, Integer> counts = new HashMap<>();
		MQuery.fileUsage(wiki, fl).forEach((k, v) -> counts.put(k, v.size()));

		ArrayList<String> fails = new ArrayList<>();
		MQuery.getPageText(wiki, fl).forEach((k, v) -> {
			try
			{
				WTemplate t = WParser.parseText(wiki, BotUtils.extractTemplate(filePRODRegex, v)).getTemplates().get(0);

				reportText.append(String.format("|-%n| %s%n| [[:%s]]%n| %s%n | %d%n",
						DateUtils.iso8601dtf.format(ZonedDateTime.parse(t.get("timestamp").toString() + "UTC", dateInFmt)), k,
						t.get("concern").toString(), counts.get(k)));
			}
			catch (Throwable e)
			{
				fails.add(k);
				e.printStackTrace();
			}
		});

		reportText.append("|}\n");

		if (!fails.isEmpty())
			reportText.append(BotUtils.listify("\n== Possibly Malformed ==\n", fails, true));

		wiki.edit(String.format("User:%s/File PROD Summary", wiki.whoami()), reportText.toString(), "Updating report");
	}

	/**
	 * Looks for files without a license tag.
	 */
	public void missingFCT()
	{
		String rPage = "Wikipedia:Database reports/Files without a license tag";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 8);
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 5));
		l.removeAll(BotUtils.fetchLabsReportAsFiles(wiki, 6));

		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file"));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore"))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		HashSet<String> lcl = new HashSet<>(wiki.getLinksOnPage("User:FastilyBot/License categories"));
		MQuery.getCategoriesOnPage(wiki, l).forEach((k, v) -> {
			if (v.isEmpty() || v.stream().anyMatch(lcl::contains))
				l.remove(k);
		});

		wiki.edit(rPage, BotUtils.listify(Settings.updatedAt, l, true), "Updating report");
	}

	/**
	 * Precomputes regexes for MTC!
	 */
	public void mtcRedirs()
	{
		String reportPage = "Wikipedia:MTC!/Redirects";

		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage(reportPage + "/IncludeAlso", NS.TEMPLATE));
		
		HashMap<String, Boolean> m = GSONP.gson.fromJson(wiki.getPageText("User:FastilyBot/Free License Tags/Raw"), strBoolHM);
		m.forEach((k, v) -> {
			if(v)
				rawL.add(k);
		});

		StringBuilder b = new StringBuilder(
				"<!-- This is a bot-generated regex library for MTC!, please don't change, thanks! -->\n<pre>\n");
		MQuery.linksHere(wiki, true, new ArrayList<>(rawL)).forEach((k, v) -> {
			v.add(0, k); // original template is included in results
			b.append(FL.pipeFence(wiki.nss(v)) + "\n");
		});

		b.append("</pre>");

		wiki.edit(reportPage, b.toString(), "Updating report");
	}

	/**
	 * Lists pages tagged for FfD with no corresponding FfD page/entry.
	 */
	public void orphanedFFD()
	{
		ArrayList<String> l = new ArrayList<>();
		MQuery.linksHere(wiki, false, WTP.ffd.getTransclusionSet(wiki, NS.FILE)).forEach((k, v) -> {
			if (!v.stream().anyMatch(s -> s.startsWith("Wikipedia:Files for discussion")))
				l.add(k);
		});

		wiki.edit(String.format("User:%s/Orphaned FfD", wiki.whoami()), BotUtils.listify(Settings.updatedAt, l, true),
				String.format("Updating report (%d items)", l.size()));
	}

	/**
	 * Lists enwp files that are tagged keep local, but orphaned.
	 */
	public void orphanedKL()
	{
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.retainAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE));

		wiki.edit("Wikipedia:Database reports/Orphaned free files tagged keep local", BotUtils.listify(Settings.updatedAt, l, true),
				"Updating report");
	}

	/**
	 * Lists oversized (> 450x450) non-free bitmap images on enwp.
	 */
	public void oversizedFU()
	{
		String rPage = "Wikipedia:Database reports/Large fair-use images";

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 7);
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BotUtils.listify(Settings.updatedAt, l, true), "Updating report");
	}

	/**
	 * Counts up free license tags and checks if a Commons counterpart exists.
	 */
	public void tallyLics()
	{		
		// constants
		String reportPage = String.format("User:%s/Free License Tags", wiki.whoami());
		
		// refresh license tag cache
		ArrayList<String> rawTL = FL.toAL(wiki.getLinksOnPage(reportPage + "/Sources", NS.CATEGORY).stream()
				.flatMap(cat -> wiki.getCategoryMembers(cat, NS.TEMPLATE).stream()).filter(s -> !s.endsWith("/sandbox")));
		rawTL.removeAll(wiki.getLinksOnPage(reportPage + "/Ignore"));
		
		HashMap<String, Boolean> enwpOnCom = MQuery.exists(BotUtils.getCommons(wiki), rawTL);
		wiki.edit(reportPage + "/Raw", GSONP.gson.toJson(enwpOnCom, strBoolHM), "Updating report");	

		// Generate transclusion count table
		Collections.sort(rawTL);
		
		StringBuffer dump = new StringBuffer(Settings.updatedAt
				+ "\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;width:100%;\" \n! # !! Name !! Transclusions !! Commons? \n");
		
		int i = 0;
		for (String s : rawTL)
			try
			{
				Matcher m = Pattern.compile("(?<=\\<p\\>)\\d+(?= transclusion)")
						.matcher(BotUtils.httpGET(HttpUrl.parse("https://tools.wmflabs.org/templatecount/index.php?lang=en&namespace=10")
								.newBuilder().addQueryParameter("name", wiki.nss(s)).build()));
				
				dump.append( String.format("|-%n|%d ||{{Tlx|%s}} || %d ||[[c:%s|%b]] %n", ++i, wiki.nss(s), m.find() ? Integer.parseInt(m.group()) : -1, s,
						enwpOnCom.get(s)));
				
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}			
		
		dump.append("|}");
		
		wiki.edit(reportPage, dump.toString(), "Updating report");
	}
	
	/**
	 * Reports files in daily deletion categories which were untagged since the previous run.
	 * 
	 * @throws Throwable On IO error
	 */
	public void untaggedDD() throws Throwable
	{
		String rPage = "Wikipedia:Database reports/Recently Untagged Files for Dated Deletion";
		int maxOldReports = 50;
		Path ddFL = Paths.get("WPDDFiles.txt");

		HashSet<String> l = FL.toSet(
				wiki.getLinksOnPage(rPage + "/Config", NS.CATEGORY).stream().flatMap(s -> wiki.getCategoryMembers(s, NS.FILE).stream()));

		if (!Files.exists(ddFL))
		{
			BotUtils.writeStringsToFile(ddFL, l);
			return;
		}

		HashSet<String> cacheList = FL.toSet(Files.lines(ddFL));
		cacheList.removeAll(l);

		String text = wiki.getPageText(rPage);
		ArrayList<PageSection> sections = wiki.splitPageByHeader(rPage);
		if (sections.size() > maxOldReports) // TODO: hack, fixme
		{
			sections = new ArrayList<>(sections.subList(1, maxOldReports));
			StringBuilder s = new StringBuilder();
			for (PageSection ps : sections)
				s.append(ps.text);

			text = s.toString();
		}
		wiki.edit(rPage, BotUtils.listify("== ~~~~~ ==\n", MQuery.exists(wiki, true, cacheList), true) + text, "Updating report");

		BotUtils.writeStringsToFile(ddFL, l);
	}
}
