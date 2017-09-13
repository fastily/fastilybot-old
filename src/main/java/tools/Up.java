package tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Stream;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.util.WikiX;
import util.BotUtils;

/**
 * Simple Wikimedia Commons Uploader
 * 
 * @author Fastily
 *
 */
public final class Up
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = BotUtils.getUserCOM("Fastily");

	/**
	 * The regex matching file extensions which can be uploaded to Commons
	 */
	private static String extRegex = WikiX.allowedFileExtsRegex(wiki);

	/**
	 * The String template for file description pages
	 */
	private static String infoT = "=={{int:filedesc}}==\n{{Information\n|description=%s\n|date=%s\n|source={{Own}}\n|"
			+ "author=~~~\n}}\n\n=={{int:license-header}}==\n{{Self|Cc-by-sa-4.0}}\n\n[[Category:%s]]\n[[Category:Files by %s]]";

	/**
	 * The template String for on-Wiki file names
	 */
	private static String fnBase = String.format("File:%%s %%d %s.%%s", DateTimeFormatter.ISO_DATE.format(LocalDate.now()));

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. PRECONDITION: These are valid directory paths
	 * @throws Throwable I/O Error
	 */
	public static void main(String[] args) throws Throwable
	{
		ArrayList<String> fails = new ArrayList<>();
		
		Stream.of(args).map(Paths::get).filter(Files::isDirectory).forEach(d -> {
			int i = 0;
			String name = d.getFileName().toString();

			try
			{
				for (Path f : FL.toSet(Files.list(d).filter(f -> Files.isRegularFile(f) && f.toString().matches(extRegex))))
					if (!wiki.upload(f, String.format(fnBase, name, ++i, getExt(f)),
							String.format(infoT, name,
									WikiX.iso8601dtf.format(ZonedDateTime.ofInstant(Files.getLastModifiedTime(f).toInstant(), ZoneOffset.UTC)), name,
									wiki.whoami()),
							""))
						fails.add(f.toString());
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});

		System.out.printf("Complete, with %s failures: %s%n", fails.size(), fails);
	}

	/**
	 * Extracts the file extension
	 * 
	 * @param f The file to get an extension for
	 * @return The file extension, without a '.'
	 */
	private static String getExt(Path f)
	{
		String x = f.getFileName().toString();
		return x.substring(x.lastIndexOf('.') + 1).toLowerCase();
	}
}