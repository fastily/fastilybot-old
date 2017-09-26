package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import util.BotUtils;

/**
 * Keeps track of daily FfD count in a graph on wiki.
 * 
 * @author Fastily
 *
 */
public class CountFfD
{
	/**
	 * The Wiki to use
	 */
	private static Wiki wiki = BotUtils.getFastilyBot();

	/**
	 * Location of {@code ffd-count.txt} for storage of results.
	 */
	private static Path graphData = Paths.get("ffd-count.txt");

	/**
	 * Stores the datasets to work with. {@code xl} is the x-axis (dates), and {@code yl} is the y-axis (ffd count)
	 */
	private static ArrayList<String> xl, yl;

	/**
	 * The maximum number of data points to graph.
	 */
	private static int maxSize = 31;

	/**
	 * Main driver
	 * 
	 * @param args Program args
	 * @throws Throwable On i/o error
	 */
	public static void main(String[] args) throws Throwable
	{
		// Read in data
		if (Files.exists(graphData))
		{
			List<String> l = Files.readAllLines(graphData);
			xl = new ArrayList<>(Arrays.asList(l.get(0).split(",")));
			yl = new ArrayList<>(Arrays.asList(l.get(1).split(",")));
		}
		else
		{
			xl = new ArrayList<>();
			yl = new ArrayList<>();
		}

		// Calculate today's FfD total
		int total = 0;
		Matcher m = Pattern.compile("(?m)^\\* \\[\\[Wikipedia.+?\\d+? remaining$")
				.matcher(wiki.getPageText("Wikipedia:Files for discussion"));
		while (m.find())
		{
			String t = m.group();
			total += Integer.parseInt(t.substring(t.indexOf('-') + 1, t.indexOf("remaining")).trim());
		}
		yl.add("" + total);

		// Get and use today's date
		LocalDate ld = LocalDate.now();
		xl.add(String.format("%d/%d", ld.getMonthValue(), ld.getDayOfMonth()));

		// Truncate to most recent (maxSize) results
		xl = truncateRS(xl);
		yl = truncateRS(yl);

		// Edit
		String x = String.join(",", xl), y = String.join(",", yl);
		wiki.edit(String.format("User:%s/FFDCount", wiki.whoami()),
				String.format(
						"{{Graph:Chart|width=1200|height=400|type=line|xAxisTitle=Date|yAxisTitle=Open FfDs|x=%s|y=%s}}%nUpdated ~~~~~", x,
						y),
				"Updating report");

		// Save results to disk for next run
		Files.write(graphData, FL.toSAL(x, y), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Returns, at most, the last {@code maxSize} elements of {@code l}.
	 * 
	 * @param l The List to work with
	 * @return A List with the last, at most, {@code maxSize} elements.
	 */
	private static ArrayList<String> truncateRS(ArrayList<String> l)
	{
		int length = l.size();
		return length > maxSize ? new ArrayList<>(l.subList(Math.max(length - maxSize, 0), length)) : l;
	}
}