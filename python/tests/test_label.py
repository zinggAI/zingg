import sys
import types
import unittest
from unittest.mock import Mock, patch

from zingg.label import _apply_pair_labels, _save_labels, count_labeled_pairs, label_training_data


class Series(list):
    def __eq__(self, value):
        return [item == value for item in self]

    def nunique(self):
        return len(set(self))


class Loc:
    def __init__(self, frame):
        self.frame = frame

    def __getitem__(self, key):
        rows, column = key
        if isinstance(rows, list):
            return Series([row[column] for row, selected in zip(self.frame.rows, rows) if selected])
        return self.frame.rows[self.frame.index.index(rows)][column]

    def __setitem__(self, key, value):
        rows, column = key
        for row, selected in zip(self.frame.rows, rows):
            if selected:
                row[column] = value


class Frame:
    def __init__(self, rows):
        self.rows = [row.copy() for row in rows]
        self.index = list(range(len(self.rows)))
        self.loc = Loc(self)

    @property
    def columns(self):
        return list(self.rows[0]) if self.rows else []

    @property
    def empty(self):
        return not self.rows

    def __len__(self):
        return len(self.rows)

    def __getitem__(self, column):
        return Series([row[column] for row in self.rows])

    def copy(self):
        return Frame(self.rows)

    def drop(self, columns):
        return Frame([{key: value for key, value in row.items() if key not in columns} for row in self.rows])


class FakeHTML:
    def __init__(self, value):
        self.value = value


class FakeVBox:
    def __init__(self, children):
        self.children = children


class FakeToggleButtons:
    def __init__(self, **kwargs):
        self.value = kwargs["value"]
        self.observers = []

    def observe(self, callback, names):
        self.observers.append(callback)

    def choose(self, value):
        self.value = value
        for callback in self.observers:
            callback({"new": value})


class FakeButton:
    def __init__(self, **kwargs):
        self.callback = None

    def on_click(self, callback):
        self.callback = callback

    def click(self):
        self.callback(self)


class FakeOutput:
    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False


def install_notebook_stubs(frames):
    displayed = []
    widget_module = types.ModuleType("ipywidgets")
    widget_module.Button = FakeButton
    widget_module.HTML = FakeHTML
    widget_module.Output = FakeOutput
    widget_module.ToggleButtons = FakeToggleButtons
    widget_module.VBox = FakeVBox

    display_module = types.ModuleType("IPython.display")
    display_module.clear_output = Mock()
    display_module.display = displayed.append

    ipython_module = types.ModuleType("IPython")
    ipython_module.display = display_module

    client_module = types.ModuleType("zingg.client")
    client_module.getPandasDfFromDs = Mock(side_effect=lambda _: next(frames))

    modules = {
        "IPython": ipython_module,
        "IPython.display": display_module,
        "ipywidgets": widget_module,
        "zingg.client": client_module,
    }
    return modules, displayed, client_module


class LabelTests(unittest.TestCase):
    def test_apply_pair_labels_updates_both_rows_without_mutating_input(self):
        candidate_pairs = Frame(
            [
                {"z_cluster": 1, "z_isMatch": None},
                {"z_cluster": 1, "z_isMatch": None},
                {"z_cluster": 2, "z_isMatch": None},
                {"z_cluster": 2, "z_isMatch": None},
            ]
        )

        updated_pairs = _apply_pair_labels(candidate_pairs, {1: "Match", 2: "No Match"})

        self.assertEqual(updated_pairs["z_isMatch"], [1, 1, 0, 0])
        self.assertEqual(candidate_pairs["z_isMatch"], [None, None, None, None])

    def test_count_labeled_pairs_counts_unique_clusters(self):
        marked_pairs = Frame(
            [
                {"z_cluster": 1, "z_isMatch": 1},
                {"z_cluster": 1, "z_isMatch": 1},
                {"z_cluster": 2, "z_isMatch": 0},
                {"z_cluster": 2, "z_isMatch": 0},
                {"z_cluster": 3, "z_isMatch": 2},
                {"z_cluster": 3, "z_isMatch": 2},
            ]
        )

        self.assertEqual(count_labeled_pairs(marked_pairs), (1, 1, 1, 3))

    def test_empty_candidate_set_displays_message_without_saving(self):
        modules, displayed, _ = install_notebook_stubs(iter([Frame([])]))
        zingg = Mock()

        with patch.dict(sys.modules, modules):
            result = label_training_data(zingg, object())

        self.assertIsInstance(result, FakeHTML)
        self.assertIn("No unlabeled candidate pairs", result.value)
        self.assertEqual(displayed, [])
        zingg.writeLabelledOutputFromPandas.assert_not_called()

    def test_save_occurs_only_after_all_pairs_are_labeled_and_button_clicked(self):
        candidate_pairs = Frame(
            [
                {"z_cluster": 1, "z_isMatch": None, "name": "left"},
                {"z_cluster": 1, "z_isMatch": None, "name": "right"},
            ]
        )
        marked_pairs = Frame(
            [
                {"z_cluster": 1, "z_isMatch": 1},
                {"z_cluster": 1, "z_isMatch": 1},
            ]
        )
        modules, displayed, _ = install_notebook_stubs(iter([candidate_pairs, marked_pairs]))
        zingg = Mock()
        prepare_output_dir = Mock()

        with patch.dict(sys.modules, modules):
            interface = label_training_data(zingg, object(), prepare_output_dir)
            pair_widget = interface.children[1]
            label_widget = pair_widget.children[1]
            save_button = interface.children[-2]

            zingg.writeLabelledOutputFromPandas.assert_not_called()
            save_button.click()
            zingg.writeLabelledOutputFromPandas.assert_not_called()

            label_widget.choose("Match")
            zingg.writeLabelledOutputFromPandas.assert_not_called()
            save_button.click()

        prepare_output_dir.assert_called_once_with()
        zingg.writeLabelledOutputFromPandas.assert_called_once()
        self.assertEqual(zingg.writeLabelledOutputFromPandas.call_args.args[0]["z_isMatch"], [1, 1])

    def test_runtime_adapters_prepare_their_own_output_directory(self):
        from zingg.label import databricks, fabric

        dbutils = Mock()
        notebookutils = Mock()

        with patch("zingg.label.databricks._label_training_data") as shared_databricks:
            shared_databricks.side_effect = lambda zingg, args, prepare_output_dir: prepare_output_dir()
            databricks.label_training_data(object(), object(), dbutils, "/marked/db")
        dbutils.fs.mkdirs.assert_called_once_with("/marked/db")

        with patch("zingg.label.fabric._label_training_data") as shared_fabric:
            shared_fabric.side_effect = lambda zingg, args, prepare_output_dir: prepare_output_dir()
            fabric.label_training_data(object(), object(), notebookutils, "/marked/fabric")
        notebookutils.fs.mkdirs.assert_called_once_with("/marked/fabric")


if __name__ == "__main__":
    unittest.main()