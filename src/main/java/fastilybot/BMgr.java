package fastilybot;

import java.util.ArrayList;

import fastily.jwiki.util.WGen;
import fastilybot.bots.*;
import fastilybot.reports.*;
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

		String[] pArgs = new String[0];
		if (botNums != null && !botNums.isEmpty())
			for (int i : botNums)
				try
				{
					switch (i)
					{
						case 1:
							MTCHelper.main(pArgs);
							break;
						case 2:
							RemoveBadMTC.main(pArgs);
							break;
						case 3:
							BrokenSPI.main(pArgs);
							break;
						case 4:
							UnflagOI.main(pArgs);
							break;
						case 5:
							FindLicConflict.main(pArgs);
							break;
						case 6:
							DDNotifier.main(pArgs);
							break;
						case 7:
							FindCommonsFFD.main(pArgs);
							break;
						case 8:
							FindDelComFFD.main(pArgs);
							break;
						case 9:
							FindKeptComFFD.main(pArgs);
							break;
						case 10:
							FlagOI.main(pArgs);
							break;
						case 11:
							DateNowCommons.main(pArgs);
							break;
						case 12:
							FFDNotifier.main(pArgs);
							break;
						default:
							System.err.println("ERROR: Not a valid task number: " + i);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}

		if (repNums != null && !repNums.isEmpty())
			for (int i : repNums)
				try
				{
					switch (i)
					{
//						case 1:
//							UntaggedDD.main(pArgs);
//							break;
						case 2:
							OrphanedFfD.main(pArgs);
							break;
						case 3:
							TallyLics.main(pArgs);
							break;
						case 4:
							MTCRedirs.main(pArgs);
							break;
//						 case 5:
//						 	CountFfD.main(pArgs);
//						 	break;
						case 6:
							OrphanedKL.main(pArgs);
							break;
						case 7:
							OversizedFU.main(pArgs);
							break;
						case 8:
							FprodSum.main(pArgs);
							break;
						case 9:
							MissingFCT.main(pArgs);
							break;
						case 10:
							DupeOnCom.main(pArgs);
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