package fastilybot;

import java.util.ArrayList;

import fastily.jwiki.core.Wiki;
import fastily.wptoolbox.BotUtils;
import fastily.wptoolbox.WGen;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * CLI interface which makes it easy to launch enwp bots/reports
 * 
 * @author Fastily
 *
 */
@Command(name = "BMgr", description = "FastilyBot Bot Manager", version = "BMgr 0.0.1", mixinStandardHelpOptions = true)
public class BMgr implements Runnable
{
	/**
	 * Flag which activates the WGen utility.
	 */
	@Option(names = "--wgen", description = "Runs the WGen credential management utility")
	private boolean runWGen;

	/**
	 * Runs the specified bot tasks. Accepts single and multiple params (separated by ',').
	 */
	@Option(names = "-b", split = ",", description = "Triggers these bot task numbers")
	private ArrayList<Integer> botNums;

	/**
	 * Runs the specified report tasks. Accepts single and multiple params (separated by ',').
	 */
	@Option(names = "-r", split = ",", description = "Triggers these bot report numbers")
	private ArrayList<Integer> repNums;

	/**
	 * No public constructors
	 */
	private BMgr()
	{

	}

	/**
	 * Main driver
	 * 
	 * @param args Program arguments
	 */
	public static void main(String[] args)
	{
		CommandLine.run(new BMgr(), args);
	}

	/**
	 * Runs actual bot tasks
	 */
	@Override
	public void run()
	{		
		if (runWGen)
		{
			WGen.main(new String[0]);
			return;
		}
		
		if(botNums == null && repNums == null)
		{
			CommandLine.usage(this, System.err);
			return;
		}

		Wiki wiki = BotUtils.getUserWP("FastilyBot");
		
		if (botNums != null && !botNums.isEmpty())
		{
			Bots b = new Bots(wiki);
			
			for (int i : botNums)
				try
				{
					switch (i)
					{
						case 1:
							b.mtcHelper();
//							MTCHelper.main(pArgs);
							break;
						case 2:
							b.removeBadMTC();
//							RemoveBadMTC.main(pArgs);
							break;
						case 3:
							b.brokenSPI();
//							BrokenSPI.main(pArgs);
							break;
						case 4:
							b.unflagOI();
//							UnflagOI.main(pArgs);
							break;
						case 5:
							b.findLicConflict();
//							FindLicConflict.main(pArgs);
							break;
						case 6:
							b.ddNotifier();
//							DDNotifier.main(pArgs);
							break;
						case 7:
							b.findCommonsFFD();
//							FindCommonsFFD.main(pArgs);
							break;
						case 8:
							b.findDelComFFD();
//							FindDelComFFD.main(pArgs);
							break;
						case 9:
							b.findKeptComFFD();
//							FindKeptComFFD.main(pArgs);
							break;
						case 10:
							b.flagOI();
//							FlagOI.main(pArgs);
							break;
						case 11:
							b.dateNowCommons();
//							DateNowCommons.main(pArgs);
							break;
						case 12:
							b.ffdNotifier();
//							FFDNotifier.main(pArgs);
							break;
						default:
							System.err.println("ERROR: Not a valid task number: " + i);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
		}

		if (repNums != null && !repNums.isEmpty())
		{
			Reports r = new Reports(wiki);
			
			for (int i : repNums)
				try
				{
					switch (i)
					{
//						case 1:
//							r.untaggedDD();
//							UntaggedDD.main(pArgs);
//							break;
						case 2:
							r.orphanedFFD();
//							OrphanedFfD.main(pArgs);
							break;
						case 3:
							r.tallyLics();
//							TallyLics.main(pArgs);
							break;
						case 4:
							r.mtcRedirs();
//							MTCRedirs.main(pArgs);
							break;
//						 case 5:
//						 	r.freeLics();
//						 	break;
						case 6:
							r.orphanedKL();
//							OrphanedKL.main(pArgs);
							break;
						case 7:
							r.oversizedFU();
//							OversizedFU.main(pArgs);
							break;
						case 8:
							r.fprodSum();
//							FprodSum.main(pArgs);
							break;
						case 9:
							r.missingFCT();
//							MissingFCT.main(pArgs);
							break;
						case 10:
							r.dupeOnCom();
//							DupeOnCom.main(pArgs);
							break;
						default:
							System.err.println("ERROR: Not a valid report number: " + i);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
		}
	}
}