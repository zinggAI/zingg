"""Generate a small Spark oracle for the checked-in Zingg classifier fixture."""
from pathlib import Path
import json
import shutil
import tempfile
from pyspark.sql import SparkSession
from pyspark.ml import PipelineModel

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT / "compat-zingg07-runtime/src/test/resources/external/zingg-v07-model-48cb157/classifier/best.model/bestModel"
OUTPUT = ROOT / "compat-zingg07-runtime/src/test/resources/external/zingg-v07-model-48cb157/spark-differential.json"
FEATURES = [f"z_sim{i}" for i in range(18)]
CASES = [
    {"name": "zeros", **{name: 0.0 for name in FEATURES}},
    {"name": "ones", **{name: 1.0 for name in FEATURES}},
    {"name": "ramp", **{name: (index + 1) / 18.0 for index, name in enumerate(FEATURES)}},
]

spark = SparkSession.builder.master("local[1]").appName("zingg-duckdb-fixture").config("spark.ui.enabled", "false").getOrCreate()
try:
    with tempfile.TemporaryDirectory(prefix="zingg-model-") as temp:
        local_model = Path(temp) / "bestModel"
        shutil.copytree(MODEL, local_model, ignore=shutil.ignore_patterns("*.crc", ".*"))
        model = PipelineModel.load(str(local_model))
        frame = spark.createDataFrame(CASES)
        result = model.transform(frame).select("name", "z_feature", "rawPrediction", "z_probability", "z_prediction").collect()
        payload = []
        for row in result:
            payload.append({
                "name": row["name"],
                "expandedHead": [float(value) for value in row["z_feature"].toArray()[:25]],
                "rawPrediction": [float(value) for value in row["rawPrediction"]],
                "probability": [float(value) for value in row["z_probability"]],
                "prediction": float(row["z_prediction"]),
            })
        OUTPUT.write_text(json.dumps({"sparkVersion": spark.version, "model": str(MODEL), "features": FEATURES, "cases": payload}, indent=2) + "\n", encoding="utf-8")
finally:
    spark.stop()
