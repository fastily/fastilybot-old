package fastilybot;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastily.jwiki.core.MQuery;
import org.fastily.jwiki.core.NS;
import org.fastily.jwiki.core.WParser;
import org.fastily.jwiki.core.WParser.WTemplate;
import org.fastily.jwiki.core.Wiki;
import org.fastily.jwiki.util.FL;
import org.fastily.jwiki.util.GSONP;
import org.fastily.wptoolbox.Dates;
import org.fastily.wptoolbox.HTTP;
import org.fastily.wptoolbox.WTP;
import org.fastily.wptoolbox.WikiX;

import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;

/**
 * Fastily's Wikipedia Reports
 * 
 * @author Fastily
 *
 */
class Reports
{
	/**
	 * The type representation for a map like [ String : Boolean ].
	 */
	private static final Type strBoolHM = new TypeToken<HashMap<String, Boolean>>() {
	}.getType();

	/**
	 * Used as part of report headers.
	 */
	private static final String updatedAt = "This report updated at <onlyinclude>~~~~~</onlyinclude>\n";

	/**
	 * Default reason to use when updating reports
	 */
	private static final String updatingReport = "Updating report";

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
	 * Finds broken SPI pages on enwp and reports on them.
	 */
	public void brokenSPI()
	{
		String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";

		HashSet<String> spiCases = FL
				.toSet(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream().filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))));

		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI case status", NS.PROJECT));
		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI archive notice", NS.PROJECT));
		spiCases.removeAll(wiki.getLinksOnPage(report + "/Ignore"));

		ArrayList<String> l = new ArrayList<>();
		MQuery.resolveRedirects(wiki, spiCases).forEach((k, v) -> {
			if (k.equals(v)) // filter redirects
				l.add(v);
		});

		wiki.edit(report, BUtils.listify("{{/Header}}\n" + updatedAt, l, false), String.format("BOT: Update list (%d items)", l.size()));
	}

	/**
	 * Lists enwp files with a duplicate on Commons.
	 */
	public void dupeOnCom()
	{
		String rPage = "Wikipedia:Database reports/Local files with a duplicate on Commons";

		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 1);
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));
		l.removeAll(WikiX.getCommons().whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BUtils.listify(updatedAt, MQuery.exists(wiki, true, l), true), updatingReport);
	}

	/**
	 * Lists files nominated for deletion via file PROD.
	 */
	public void fprodSum()
	{
		// constants
		DateTimeFormatter dateInFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssz");
		Pattern filePRODRegex = Pattern.compile(WTP.fprod.getRegex(wiki));
		StringBuilder reportText = new StringBuilder(
				"{{/header}}\n" + updatedAt + "{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;\"\n! Date\n! File\n! Reason\n! Use count\n");

		ArrayList<String> fl = wiki.getCategoryMembers("Category:All files proposed for deletion", NS.FILE);

		// logic
		HashMap<String, Integer> counts = new HashMap<>();
		MQuery.fileUsage(wiki, fl).forEach((k, v) -> counts.put(k, v.size()));

		ArrayList<String> fails = new ArrayList<>();
		MQuery.getPageText(wiki, fl).forEach((k, v) -> {
			try
			{
				WTemplate t = WParser.parseText(wiki, WikiX.extractTemplate(filePRODRegex, v)).getTemplates().get(0);

				reportText.append(String.format("|-%n| %s%n| [[:%s]]%n| %s%n | %d%n", Dates.iso8601dtf.format(ZonedDateTime.parse(t.get("timestamp").toString() + "UTC", dateInFmt)), k,
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
			reportText.append(BUtils.listify("\n== Possibly Malformed ==\n", fails, true));

		wiki.edit(String.format("User:%s/File PROD Summary", wiki.whoami()), reportText.toString(), updatingReport);
	}

	/**
	 * Finds files nominated for daily deletion with malformed or expired nomination dates.
	 */
	public void impossibleDD()
	{
		String rPage = "Wikipedia:Database reports/Files for daily deletion with an impossible date";
		HashMap<String, String> rules = GSONP.gson.fromJson(wiki.getPageText("User:FastilyBot/Daily Deletion Categories"), BUtils.strStrHM);

		HashSet<String> out = new HashSet<>();

		// rootCat : groupCat
		rules.forEach((k, v) -> {
			HashSet<String> l = new HashSet<>(wiki.getCategoryMembers(v, NS.FILE));
			for (String cat : wiki.getCategoryMembers(k, NS.CATEGORY))
				if (cat.matches(".+?" + Dates.DMYRegex))
					l.removeAll(wiki.getCategoryMembers(cat, NS.FILE));

			if (!l.isEmpty())
				out.addAll(l);
		});

		wiki.edit(rPage, BUtils.listify(updatedAt, out, true), updatingReport);
	}

	/**
	 * Publishes a list of low-resolution free files.
	 */
	public void lowResFreeFiles()
	{
		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 10);
		l.removeAll(wiki.getCategoryMembers("Category:Wikipedia images available as SVG", NS.FILE));
		l.removeAll(wiki.getCategoryMembers("Category:All files proposed for deletion", NS.FILE));

		wiki.edit("Wikipedia:Database reports/Orphaned low-resolution free files", BUtils.listify(updatedAt, MQuery.exists(wiki, true, l), true), updatingReport);
	}

	/**
	 * Looks for files without a license tag.
	 */
	public void missingFCT()
	{
		String rPage = "Wikipedia:Database reports/Files without a license tag";

		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 8);
		l.removeAll(BUtils.fetchLabsReportAsFiles(wiki, 5));
		l.removeAll(BUtils.fetchLabsReportAsFiles(wiki, 6));

		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file"));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore"))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		HashSet<String> lcl = new HashSet<>(wiki.getLinksOnPage("User:FastilyBot/License categories"));
		MQuery.getCategoriesOnPage(wiki, l).forEach((k, v) -> {
			if (v.isEmpty() || v.stream().anyMatch(lcl::contains))
				l.remove(k);
		});

		wiki.edit(rPage, BUtils.listify(updatedAt, l, true), updatingReport);
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
			if (v)
				rawL.add(k);
		});

		StringBuilder b = new StringBuilder("<!-- This is a bot-generated regex library for MTC!, please don't change, thanks! -->\n<pre>\n");
		MQuery.linksHere(wiki, true, new ArrayList<>(rawL)).forEach((k, v) -> {
			v.add(0, k); // original template is included in results
			b.append(FL.pipeFence(wiki.nss(v)) + "\n");
		});

		b.append("</pre>");

		wiki.edit(reportPage, b.toString(), updatingReport);
	}

	/**
	 * Lists pdf files tagged non-free.
	 */
	public void nonFreePDFs()
	{
		wiki.edit("Wikipedia:Database reports/Non-free PDFs", BUtils.listify(updatedAt, BUtils.fetchLabsReportAsFiles(wiki, 15), true), updatingReport);
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

		wiki.edit("Wikipedia:Database reports/Files tagged for FfD missing an FfD nomination", BUtils.listify(updatedAt, l, true), updatingReport + String.format(" (%d items)", l.size()));
	}

	/**
	 * List orphaned file talk pages
	 */
	public void orphanedFileTalk()
	{
		HashSet<String> l = BUtils.fetchLabsReportSet(wiki, "report16", "File talk:");
		l.removeAll(wiki.getCategoryMembers("Category:Wikipedia orphaned talk pages that should not be speedily deleted", NS.FILE_TALK));

		wiki.edit("Wikipedia:Database reports/Orphaned file talk pages", BUtils.listify(updatedAt, l, false), updatingReport);
	}

	/**
	 * Lists enwp files that are tagged keep local, but orphaned.
	 */
	public void orphanedKL()
	{
		HashSet<String> l = WTP.orphan.getTransclusionSet(wiki, NS.FILE);
		l.retainAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE));

		wiki.edit("Wikipedia:Database reports/Orphaned free files tagged keep local", BUtils.listify(updatedAt, l, true), updatingReport);
	}

	/**
	 * Lists unused PDFs on enwp
	 */
	public void orphanedPDFs()
	{
		wiki.edit("Wikipedia:Database reports/Orphaned PDFs", BUtils.listify(updatedAt, FL.toAL(BUtils.fetchLabsReportAsFiles(wiki, 9).stream().filter(s -> s.toLowerCase().endsWith(".pdf"))), true),
				updatingReport);
	}

	/**
	 * Lists oversized (> 450x450) non-free bitmap images on enwp.
	 */
	public void oversizedFU()
	{
		String rPage = "Wikipedia:Database reports/Large fair-use images";

		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 7);
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));

		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, BUtils.listify(updatedAt, l, true), updatingReport);
	}

	/**
	 * Lists possibly unsourced free files on enwp.
	 */
	public void possiblyUnsourcedFiles()
	{
		wiki.edit("Wikipedia:Database reports/Free files without a machine-readable source", BUtils.listify(updatedAt, BUtils.fetchLabsReportAsFiles(wiki, 12), true), updatingReport);
	}

	/**
	 * Identifies enwp file description pages shadowing a Commons file or redirect
	 */
	public void shadowCommonsDescriptionPages()
	{
		String rPage = "Wikipedia:Database reports/File description pages shadowing a Commons file or redirect";

		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 11);
		for (String s : wiki.getLinksOnPage(rPage + "/Ignore", NS.CATEGORY))
			l.removeAll(wiki.getCategoryMembers(s, NS.FILE));

		wiki.edit(rPage, updatedAt + "\n" + String.join("\n", FL.toAL(l.stream().map(s -> "* {{No redirect|" + s + "}}"))), updatingReport);
	}

	/**
	 * Find all non-free files shadowing a Commons file (specifically titles where the respective files are not the same)
	 */
	public void shadowCommonsNonFree()
	{
		String rPage = "Wikipedia:Database reports/Non-free files shadowing a Commons file";

		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 13);
		l.retainAll(BUtils.fetchLabsReportAsFiles(wiki, 5));

		wiki.edit(rPage, BUtils.listify(updatedAt, l, true), updatingReport);
	}

	/**
	 * Counts up free license tags and checks if a Commons counterpart exists.
	 */
	public void tallyLics()
	{
		// constants
		String reportPage = String.format("User:%s/Free License Tags", wiki.whoami());

		// refresh license tag cache
		ArrayList<String> rawTL = FL
				.toAL(wiki.getLinksOnPage(reportPage + "/Sources", NS.CATEGORY).stream().flatMap(cat -> wiki.getCategoryMembers(cat, NS.TEMPLATE).stream()).filter(s -> !s.endsWith("/sandbox")));
		rawTL.removeAll(wiki.getLinksOnPage(reportPage + "/Ignore"));

		HashMap<String, Boolean> enwpOnCom = MQuery.exists(WikiX.getCommons(), rawTL);
		wiki.edit(reportPage + "/Raw", GSONP.gson.toJson(enwpOnCom, strBoolHM), "Updating report");

		// Generate transclusion count table
		Collections.sort(rawTL);

		StringBuilder dump = new StringBuilder(updatedAt + "\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;width:100%;\" \n! # !! Name !! Transclusions !! Commons? \n");

		int i = 0;
		for (String s : rawTL)
			try
			{
				Matcher m = Pattern.compile("\\d+(?= transclusion\\(s\\) found)")
						.matcher(HTTP.get(HttpUrl.parse("https://tools.wmflabs.org/templatecount/index.php?lang=en&namespace=10").newBuilder().addQueryParameter("name", wiki.nss(s)).build()));
				dump.append(String.format("|-%n|%d%n|{{Tlx|%s}}%n|%d%n|[[c:%s|%b]]%n", ++i, wiki.nss(s), m.find() ? Integer.parseInt(m.group()) : -1, s, enwpOnCom.get(s)));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

		dump.append("|}");

		wiki.edit(reportPage, dump.toString(), updatingReport);
	}
}
