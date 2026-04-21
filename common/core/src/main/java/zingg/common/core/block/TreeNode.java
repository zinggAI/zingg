package zingg.common.core.block;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    T value;
    TreeNode<T> parent;
    List<TreeNode<T>> children = new ArrayList<>();

    public TreeNode(T value) {
        this.value = value;
    }

    public void addChild(TreeNode<T> child) {
        children.add(child);
        child.parent = this;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }
}
