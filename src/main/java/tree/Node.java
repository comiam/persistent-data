package tree;

public class Node<TK, TV> {

    public TK key;
    public TV data;
    public Node<TK, TV> parent;
    public Node<TK, TV> left;
    public Node<TK, TV> right;

    Color colour;
    public int hash;


    public Node(TK key, TV data) {
        this.key = key;
        this.data = data;
        hash = key.hashCode();
    }

    public Node(Color colour) {
        this.colour = colour;
    }

    public Node(TK key, TV data, Color colour) {
        this.key = key;
        this.data = data;
        this.colour = colour;
        hash = key.hashCode();
    }
}

