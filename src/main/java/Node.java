import java.security.KeyPair;
import java.util.*;

public class Node<TK, TV> {
    public Color colour;
    public Node<TK, TV> left;
    public Node<TK, TV> right;
    public Node<TK, TV> parent;
    public Node<TK, TV> root;

    public TK key;
    public TV data;
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

    public Node<TK, TV> find(TK key) {
        var isFound = false;
        var temp = root;
        Node<TK, TV> item = null;
        var hash = key.hashCode();
        while (!isFound) {
            if (temp == null) {
                break;
            }

            if (hash < temp.hash) {
                temp = temp.left;
            } else if (hash > temp.hash) {
                temp = temp.right;
            }

            if (temp != null && hash == temp.hash) {
                isFound = true;
                item = temp;
            }
        }

        if (isFound) {
            return item;
        } else {
            return null;
        }
    }

    public void insert(TK key, TV item) {
        var node = find(key);

        if (node != null) {
            node.data = item;
            return;
        }

        var newItem = new Node<>(key, item);
        if (root == null) {
            root = newItem;
            root.colour = Color.Black;
            return;
        }

        Node<TK, TV> Y = null;
        var X = root;
        while (X != null) {
            Y = X;
            if (newItem.hash < X.hash) {
                X = X.left;
            } else {
                X = X.right;
            }
        }

        newItem.parent = Y;
        if (Y == null) {
            root = newItem;
        } else if (newItem.hash < Y.hash) {
            Y.left = newItem;
        } else {
            Y.right = newItem;
        }

        newItem.left = null;
        newItem.right = null;
        newItem.colour = Color.Red; //colour the new node red
        insertFixUp(newItem); //call method to check for violations and fix
    }

    private void inOrderDisplay(Node<TK, TV> current) {
        if (current != null) {
            inOrderDisplay(current.left);
            System.out.println(current.data.toString());
            inOrderDisplay(current.right);
        }
    }

    /// <summary>
    /// Left Rotate
    /// </summary>
    /// <param name="X"></param>
    /// <returns>void</returns>
    private void leftRotate(Node<TK, TV> X) {
        var Y = X.right; // set Y
        X.right = Y.left; //turn Y's left subtree into X's right subtree
        if (Y.left != null) {
            Y.left.parent = X;
        }

        if (Y != null) {
            Y.parent = X.parent; //link X's parent to Y
        }

        if (X.parent == null) {
            root = Y;
        }

        if (X.parent != null && X == X.parent.left) {
            X.parent.left = Y;
        } else if (X.parent != null) {
            X.parent.right = Y;
        }

        Y.left = X; //put X on Y's left
        if (X != null) {
            X.parent = Y;
        }

    }

    /// <summary>
    /// Rotate Right
    /// </summary>
    /// <param name="Y"></param>
    /// <returns>void</returns>
    private void rightRotate(Node<TK, TV> Y) {
        // right rotate is simply mirror code from left rotate
        var X = Y.left;
        Y.left = X.right;
        if (X.right != null) {
            X.right.parent = Y;
        }

        if (X != null) {
            X.parent = Y.parent;
        }

        if (Y.parent == null) {
            root = X;
        }

        assert Y.parent != null;
        if (Y == Y.parent.right) {
            Y.parent.right = X;
        }

        if (Y == Y.parent.left) {
            Y.parent.left = X;
        }

        X.right = Y; //put Y on X's right
        if (Y != null) {
            Y.parent = X;
        }
    }


    private void insertFixUp(Node<TK, TV> item) {
        //Checks Red-Black Tree properties
        while (item != root && item.parent.colour == Color.Red) {
            /*We have a violation*/
            if (item.parent == item.parent.parent.left) {
                var Y = item.parent.parent.right;
                if (Y != null && Y.colour == Color.Red) //Case 1: uncle is red
                {
                    item.parent.colour = Color.Black;
                    Y.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    item = item.parent.parent;
                } else //Case 2: uncle is black
                {
                    if (item == item.parent.right) {
                        item = item.parent;
                        leftRotate(item);
                    }

                    //Case 3: recolour & rotate
                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    rightRotate(item.parent.parent);
                }
            } else {
                //mirror image of code above
                Node<TK, TV> X = null;

                X = item.parent.parent.left;
                if (X != null && X.colour == Color.Black) //Case 1
                {
                    X.colour = Color.Red;
                    item.parent.parent.colour = Color.Black;
                    item = item.parent.parent;
                } else //Case 2
                {
                    if (item == item.parent.left) {
                        item = item.parent;
                        rightRotate(item);
                    }

                    //Case 3: recolour & rotate
                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    leftRotate(item.parent.parent);
                }
            }

            root.colour = Color.Black; //re-colour the root black as necessary
        }
    }

    public void delete(TK key) {
        //first find the node in the tree to delete and assign to item pointer/reference
        var item = find(key);
        Node<TK, TV> X = null;
        Node<TK, TV> Y = null;

        if (item == null) {
            System.out.println("Nothing to delete!");
            return;
        }

        if (item.left == null || item.right == null) {
            Y = item;
        } else {
            Y = treeSuccessor(item);
        }

        if (Y.left != null) {
            X = Y.left;
        } else {
            X = Y.right;
        }

        if (X != null) {
            X.parent = Y;
        }

        if (Y.parent == null) {
            root = X;
        } else if (Y == Y.parent.left) {
            Y.parent.left = X;
        } else {
            Y.parent.left = X;
        }

        if (Y != item) {
            item.data = Y.data;
        }

        if (Y.colour == Color.Black) {
            deleteFixUp(X);
        }

    }

    /// Checks the tree for any violations after deletion and performs a fix
    /// </summary>
    /// <param name="X"></param>
    private void deleteFixUp(Node<TK, TV> X) {
        while (X != null && X != root && X.colour == Color.Black) {
            if (X == X.parent.left) {
                var W = X.parent.right;
                if (W.colour == Color.Red) {
                    W.colour = Color.Black; //case 1
                    X.parent.colour = Color.Red; //case 1
                    leftRotate(X.parent); //case 1
                    W = X.parent.right; //case 1
                }

                if (W.left.colour == Color.Black && W.right.colour == Color.Black) {
                    W.colour = Color.Red; //case 2
                    X = X.parent; //case 2
                } else if (W.right.colour == Color.Black) {
                    W.left.colour = Color.Black; //case 3
                    W.colour = Color.Red; //case 3
                    rightRotate(W); //case 3
                    W = X.parent.right; //case 3
                }

                W.colour = X.parent.colour; //case 4
                X.parent.colour = Color.Black; //case 4
                W.right.colour = Color.Black; //case 4
                leftRotate(X.parent); //case 4
                X = root; //case 4
            } else //mirror code from above with "right" & "left" exchanged
            {
                var W = X.parent.left;
                if (W.colour == Color.Red) {
                    W.colour = Color.Black;
                    X.parent.colour = Color.Red;
                    rightRotate(X.parent);
                    W = X.parent.left;
                }

                if (W.right.colour == Color.Black && W.left.colour == Color.Black) {
                    W.colour = Color.Black;
                    X = X.parent;
                } else if (W.left.colour == Color.Black) {
                    W.right.colour = Color.Black;
                    W.colour = Color.Red;
                    leftRotate(W);
                    W = X.parent.left;
                }

                W.colour = X.parent.colour;
                X.parent.colour = Color.Black;
                W.left.colour = Color.Black;
                rightRotate(X.parent);
                X = root;
            }
        }

        if (X != null)
            X.colour = Color.Black;
    }

    private Node<TK, TV> minimum(Node<TK, TV> X) {
        while (X.left.left != null) {
            X = X.left;
        }

        if (X.left.right != null) {
            X = X.left.right;
        }

        return X;
    }

    private Node<TK, TV> treeSuccessor(Node<TK, TV> X) {
        if (X.left != null) {
            return minimum(X);
        } else {
            var Y = X.parent;
            while (Y != null && X == Y.right) {
                X = Y;
                Y = Y.parent;
            }

            return Y;
        }
    }

    public List<Map.Entry<TK, TV>> ToList() {
        var res = new ArrayList<Map.Entry<TK, TV>>() {
        };

        addToList(res, root);

        return res;
    }

    private void addToList(Collection<Map.Entry<TK, TV>> list, Node<TK, TV> node) {
        while (true) {
            if (node == null) {
                return;
            }

            addToList(list, node.left);
            list.add(Map.entry(node.key, node.data));
            node = node.right;
        }
    }

    public Iterator<Map.Entry<TK, TV>> getIterator() {
        return ToList().iterator();
    }

}








