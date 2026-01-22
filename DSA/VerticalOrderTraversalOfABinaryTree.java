package DSA;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class VerticalOrderTraversalOfABinaryTree {

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(6);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(7);
//        root.left.right.right = new TreeNode(8);
//        root.right.left.right = new TreeNode(9);
//        root.right.right.right = new TreeNode(10);
//        root.left.right.right.left = new TreeNode(11);

        VerticalOrderTraversalOfABinaryTree obj = new VerticalOrderTraversalOfABinaryTree();
        List<List<Integer>> res = obj.verticalTraversal(root);

        System.out.print("[");
        for (int i = 0; i < res.size(); i++) {
            System.out.print("[");
            List<Integer> line = res.get(i);
            for (int j = 0; j < line.size(); j++) {
                System.out.print(line.get(j));
                if (j != line.size() - 1) System.out.print(", ");
            }
            System.out.print("]");
            if (i != res.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    List<List<Integer>> res = new ArrayList<>();

    Map<Integer, List<Location>> locations = new TreeMap<>();

    static class Location implements Comparable<Location>{
        int val;
        int row;
        int col;

        public Location(int val, int row, int col){
            this.val = val;
            this.row=row;
            this.col = col;
        }

        @Override
        public int compareTo(Location other){
            return this.row - other.row==0 ? this.val-other.val : this.row - other.row ;
        }

    }

    public List<List<Integer>> verticalTraversal(TreeNode root) {
        traverse(root, 0,0);
        locations.forEach(
                (col, location) -> {
                    location.sort(Location::compareTo);
                    res.add(
                            location.stream().map(a -> a.val).collect(Collectors.toList())
                    );
                }
        );
        return res;
    }

    private void traverse(TreeNode node , int row, int col){
        if(node==null)
            return;
        locations.computeIfAbsent(col, k -> new ArrayList<>())
                .add(new Location(node.val, row, col));
        traverse(node.left, row+1,col-1);
        traverse(node.right, row+1,col+1);
    }
}
