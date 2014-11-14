package mil.nga.giat.sfc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import mil.nga.giat.geowave.index.dimension.LatitudeDefinition;
import mil.nga.giat.geowave.index.dimension.LongitudeDefinition;
import mil.nga.giat.geowave.index.sfc.SFCDimensionDefinition;
import mil.nga.giat.geowave.index.sfc.SFCFactory;
import mil.nga.giat.geowave.index.sfc.SFCFactory.SFCType;
import mil.nga.giat.geowave.index.sfc.SpaceFillingCurve;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.StopWatch;

public class Benchmark
{
	public static void main(
			final String[] args ) {
		if (args.length != 2) {
			System.err.println("Usage: java mil.nga.giat.sfc.Benchmark <Output CSV File> <Iterations per cardinality>");
		}
		try (final FileWriter fw = new FileWriter(
				new File(
						args[0])); final CSVPrinter printer = new CSVPrinter(
				fw,
				CSVFormat.DEFAULT)) {

			// print the headers
			printer.printRecord(new Object[] {
				"Cardinality",
				"SFC",
				"Nanos"
			});
			final long numIterations = Long.parseLong(args[1]);

			// let's just assume cardinality for lat and lon is the same in each
			// dimension, but vary that cardinality
			for (int cardinality = 1; cardinality < 32; cardinality++) {

				for (final SFCType sfcType : SFCType.values()) {

					final long nanos = runTest(
							numIterations,
							cardinality,
							sfcType);
					printer.printRecord(new Object[] {
						cardinality,
						sfcType.toString(),
						nanos
					});
				}
			}
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static long runTest(
			final long iterations,
			final int cardinality,
			final SFCType sfcType ) {
		// make sure random numbers are the same for each sfc type, by seeding
		// with cardinality
		final Random rand = new Random(
				cardinality);
		final SpaceFillingCurve sfc = SFCFactory.createSpaceFillingCurve(
				new SFCDimensionDefinition[] {
					new SFCDimensionDefinition(
							new LongitudeDefinition(),
							cardinality),
					new SFCDimensionDefinition(
							new LatitudeDefinition(),
							cardinality)
				},
				sfcType);
		// just to make sure we don't capture any time associated with
		// generating the value use the stopwatch
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		stopWatch.suspend();
		for (long i = 0; i < iterations; i++) {
			final double lat = (rand.nextDouble() * 180) - 90;
			final double lon = (rand.nextDouble() * 360) - 180;
			final double[] values = new double[] {
				lon,
				lat
			};
			stopWatch.resume();
			sfc.getId(values);
			stopWatch.suspend();
		}
		return stopWatch.getNanoTime();
	}
}
