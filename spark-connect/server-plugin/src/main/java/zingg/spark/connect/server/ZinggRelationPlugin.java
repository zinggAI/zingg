package zingg.spark.connect.server;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.connect.plugin.RelationPlugin;
import org.apache.spark.sql.connect.planner.SparkConnectPlanner;

import scala.Option;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;
import zingg.spark.core.executor.SparkLabeller;
import zingg.spark.connect.proto.ZinggCommand;

/**
 * Spark Connect RelationPlugin that returns the unmarked training pairs for the
 * interactive label / findAndLabel loop as a queryable relation.
 *
 * Unlike a CommandPlugin (which can only signal handled/failed), a RelationPlugin
 * returns a LogicalPlan -- so Spark streams the resulting rows back to the client,
 * which is exactly the two-way channel the label loop needs. The server side here
 * only produces the pairs (via the existing Labeller.getUnmarkedRecords()); the
 * actual yes/no/skip marking happens client-side, where a human is, and the marked
 * pairs are written back separately.
 *
 * Registration (server side):
 *   --conf spark.connect.extensions.relation.classes=zingg.spark.connect.server.ZinggRelationPlugin
 *
 * The request travels as a ZinggCommand packed into Relation.extension. Only the
 * label / findAndLabel phases are handled here; anything else returns None so
 * other relation plugins get a turn.
 */
public class ZinggRelationPlugin implements RelationPlugin {

	public static final Log LOG = LogFactory.getLog(ZinggRelationPlugin.class);

	@Override
	public Option<LogicalPlan> transform(org.sparkproject.connect.protobuf.Any relation, SparkConnectPlanner planner) {
		Any any;
		try {
			any = Any.parseFrom(relation.toByteArray());
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Malformed Spark Connect relation extension", e);
		}
		if (!any.is(ZinggCommand.class)) {
			// Not ours -- let any other registered RelationPlugin take a turn.
			return Option.empty();
		}

		ZinggCommand zinggCommand;
		try {
			zinggCommand = any.unpack(ZinggCommand.class);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Failed to unpack ZinggCommand relation", e);
		}

		String phase = zinggCommand.getPhase();
		if (!phase.equals("label") && !phase.equals("findAndLabel")) {
			// This relation plugin only serves the labelling phases.
			return Option.empty();
		}

		try {
			LogicalPlan plan = unmarkedPairsPlan(zinggCommand, planner.session());
			return Option.apply(plan);
		} catch (ZinggClientException e) {
			throw new RuntimeException("Zingg relation for phase '" + phase + "' failed", e);
		}
	}

	private LogicalPlan unmarkedPairsPlan(ZinggCommand zinggCommand, SparkSession session)
			throws ZinggClientException {
		IZArgs args = ZinggProtoConverters.toJavaArguments(zinggCommand.getArgs());
		ClientOptions options = ZinggProtoConverters.toJavaClientOptions(zinggCommand.getPhase(),
				zinggCommand.getOptions());

		LOG.info("Producing unmarked pairs for Zingg phase '" + zinggCommand.getPhase()
				+ "' via Spark Connect relation plugin");

		SparkLabeller labeller = new SparkLabeller();
		labeller.init(args, session, options);

		ZFrame<Dataset<Row>, Row, Column> unmarked = labeller.getUnmarkedRecords();
		if (unmarked == null) {
			throw new ZinggClientException("No unmarked training pairs found -- run findTrainingData first.");
		}
		return unmarked.df().queryExecution().logical();
	}
}
