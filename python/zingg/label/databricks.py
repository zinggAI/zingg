"""Databricks adapter for the shared Zingg notebook labeling workflow."""

from . import label_training_data as _label_training_data


def label_training_data(zingg, args, dbutils, marked_dir):
    """Prepare the Databricks marked-record directory and show the label UI."""
    return _label_training_data(
        zingg,
        args,
        prepare_output_dir=lambda: dbutils.fs.mkdirs(marked_dir),
    )


__all__ = ["label_training_data"]