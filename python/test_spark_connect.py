from zingg_v2.client import Zingg, Arguments, ClientOptions
from pyspark.sql.connect.session import SparkSession


if __name__ == "__main__":
    spark = SparkSession.builder.remote("sc://localhost").getOrCreate()
    print(hasattr(spark, "_jvm"))
    opts = ClientOptions(None)
    args = Arguments.createArgumentsFromJSON(fileName="../examples/febrl/config.json", phase="peekModel")
    zingg = Zingg(args=args, options=opts)
    zingg.execute()
