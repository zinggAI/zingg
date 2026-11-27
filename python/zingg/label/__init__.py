"""Shared interactive labeling workflow for Zingg notebooks."""

from typing import Any, Callable, Optional


LABEL_VALUES = {
    "Uncertain": 2,
    "Match": 1,
    "No Match": 0,
}


def _apply_pair_labels(candidate_pairs, selected_labels):
    updated_pairs = candidate_pairs.copy()
    for cluster_id, selected_label in selected_labels.items():
        if selected_label not in LABEL_VALUES:
            raise ValueError(f"Unknown label: {selected_label}")
        updated_pairs.loc[updated_pairs["z_cluster"] == cluster_id, "z_isMatch"] = LABEL_VALUES[selected_label]
    return updated_pairs


def count_labeled_pairs(marked_pairs):
    cluster_ids = marked_pairs["z_cluster"]
    labels = marked_pairs["z_isMatch"]
    return (
        marked_pairs.loc[labels == LABEL_VALUES["Match"], "z_cluster"].nunique(),
        marked_pairs.loc[labels == LABEL_VALUES["No Match"], "z_cluster"].nunique(),
        marked_pairs.loc[labels == LABEL_VALUES["Uncertain"], "z_cluster"].nunique(),
        cluster_ids.nunique(),
    )


def _save_labels(zingg, args, candidate_pairs, selected_labels, prepare_output_dir):
    from ..client import getPandasDfFromDs

    labeled_pairs = _apply_pair_labels(candidate_pairs, selected_labels)
    if prepare_output_dir is not None:
        prepare_output_dir()
    zingg.writeLabelledOutputFromPandas(labeled_pairs, args)
    return count_labeled_pairs(getPandasDfFromDs(zingg.getMarkedRecords()))


def _pair_html(display_pairs, left_index, right_index):
    from base64 import b64encode
    from html import escape

    rows = []
    for column in display_pairs.columns:
        left_value = display_pairs.loc[left_index, column]
        right_value = display_pairs.loc[right_index, column]
        if column == "image_path":
            image_cells = []
            for image_path in (left_value, right_value):
                if image_path and str(image_path) != "nan":
                    with open(image_path, "rb") as image_file:
                        encoded_image = b64encode(image_file.read()).decode("ascii")
                    image_cells.append(f'<td><img src="data:image/png;base64,{encoded_image}"></td>')
                else:
                    image_cells.append("<td></td>")
            rows.append("<tr><td><b>image</b></td>" + "".join(image_cells) + "</tr>")
        else:
            rows.append(
                "<tr>"
                f'<td style="width:10%"><b>{escape(str(column))}</b></td>'
                f'<td style="width:45%">{escape(str(left_value))}</td>'
                f'<td style="width:45%">{escape(str(right_value))}</td>'
                "</tr>"
            )
    return '<table style="width:100%;border-collapse:collapse" border="1">' + "".join(rows) + "</table>"


def label_training_data(
    zingg: Any,
    args: Any,
    prepare_output_dir: Optional[Callable[[], Any]] = None,
):
    """Display unmarked pairs and save their labels when the user clicks Save."""
    from IPython.display import clear_output
    from ipywidgets import Button, HTML, Output, ToggleButtons, VBox

    from ..client import getPandasDfFromDs

    candidate_pairs = getPandasDfFromDs(zingg.getUnmarkedRecords())
    if candidate_pairs.empty:
        message = HTML(value="No unlabeled candidate pairs found. Run findTrainingData first.")
        return message

    if len(candidate_pairs.index) % 2:
        raise ValueError("Unmarked candidate records must contain two rows per pair.")

    hidden_columns = [
        column
        for column in ("z_zid", "z_prediction", "z_score", "z_isMatch", "z_zsource")
        if column in candidate_pairs.columns
    ]
    display_pairs = candidate_pairs.drop(columns=hidden_columns)
    pair_count = len(candidate_pairs.index) // 2
    pair_widgets = []
    selected_labels = {}

    for pair_index in range(pair_count):
        left_index = candidate_pairs.index[2 * pair_index]
        right_index = candidate_pairs.index[2 * pair_index + 1]
        cluster_id = candidate_pairs.loc[left_index, "z_cluster"]
        if candidate_pairs.loc[right_index, "z_cluster"] != cluster_id:
            raise ValueError("Each candidate pair must have matching z_cluster values.")

        label_widget = ToggleButtons(options=list(LABEL_VALUES), value=None, allow_none=True, button_style="info")

        def record_label(change, pair_id=cluster_id):
            if change["new"] is None:
                selected_labels.pop(pair_id, None)
            else:
                selected_labels[pair_id] = change["new"]

        label_widget.observe(record_label, names="value")
        pair_widgets.append(
            VBox(
                [
                    HTML(value=_pair_html(display_pairs, left_index, right_index)),
                    label_widget,
                    HTML(value="<br>"),
                ]
            )
        )

    save_button = Button(description="Save labels", icon="save", button_style="primary")
    status = Output()

    def save_selected_labels(_):
        with status:
            clear_output(wait=True)
            if len(selected_labels) != pair_count:
                print("Choose a label for every pair before saving.")
                return
            positive, negative, uncertain, total = _save_labels(
                zingg,
                args,
                candidate_pairs,
                selected_labels,
                prepare_output_dir,
            )
            print(f"Out of total {total} pairs,")
            print(f"You have accumulated {positive} pairs labeled as positive matches.")
            print(f"You have accumulated {negative} pairs labeled as not matches.")
            print(f"You have accumulated {uncertain} pairs labeled as uncertain.")
            print("If you need more pairs to label, re-run findTrainingData.")

    save_button.on_click(save_selected_labels)
    interface = VBox(
        [
            HTML(value=f"<h2>Indicate if each of the {pair_count} record pairs is a match or not</h2>"),
            *pair_widgets,
            save_button,
            status,
        ]
    )
    return interface


__all__ = ["LABEL_VALUES", "count_labeled_pairs", "label_training_data"]