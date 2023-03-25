

package SysFor;

import java.util.*;
/**
 * Stores the root element of a decision tree and has methods for traversing
 * and printing a decision tree based on the nodes currently stored in the tree.
 *
 * @author helengiggins
 * @version 1.0 1/12/2010
 * @see Node
 * @see LeafNode
 */
public class DecisionTree {

    /* instance data */
    /** the root node of the tree */
    private Node rootNode;
    
    /**
     * Class constructor sets a rootNode null. 
     */
    public DecisionTree()
    {
        rootNode = null;
    }
    
    /**
     * Class constructor that fixes the root node at creation.
     * 
     * @param rootNode the root element of the decision tree
     */
    public DecisionTree(Node rootNode)
    {
        this.rootNode = rootNode;
    }

    /**
     * Returns the root element of the decision tree.
     *
     * @return the root element of the decision tree
     */
    public Node getRootNode()
    {
        return rootNode;
    }

    /**
     * Returns the current number of nodes stored in the decision tree.
     *
     * @return the current number of nodes stored in the decision tree
     */
    public int size()
    {
        /** walk the tree to find size */
        return levelOrderTraversal().size();
    }

    public static DecisionTree copyTree(DecisionTree tree)
    {
        DecisionTree copyTree = new DecisionTree();
        /** need to copy all nodes in the tree */
        List<Node> treeList = tree.levelOrderTraversal();
        /** going from the root to leaf, bfs order, copy the
         * nodes in the tree.
         */
        Node root = Node.copy(treeList.get(0));
        //TODO need to finish this method
        return copyTree;
    }

    /**
     * Performs a breadthFirst traversal of the tree and returns a list
     * of nodes, in the order they are visited in the search. This
     *
     * @return the tree nodes as a list, in level order (root to leaf)
     */
    public List<Node> levelOrderTraversal()
    {
        /** create two list, one to store the nodes in order for return,
         * the other will act as a queue when we perform a breadthFirst
         * traversal of the tree
         */
        List<Node> nodeList = new LinkedList();
        LinkedList<Node> queue = new LinkedList();
        /** add the root node to the queue. While queue is not empty
         * dequeue a node, add to nodeList and add all children of this
         * node to the queue.
         */
        queue.add(rootNode);
        //while queue not empty
        while(!queue.isEmpty())
        {
            Node currNode = queue.removeFirst(); //get next node from queue
            nodeList.add(currNode); //add the node to the return list (visit node)
            
            List<Node> children = currNode.getChildren();
            if(children!=null)
            {
                //add all children of currNode to the queue
                for(int i=0; i<children.size(); i++)
                {
                    Node currChild = children.get(i);
                    queue.add(currChild);
                }
            }
        }
        return nodeList;
    }

    

    /**
     * Performs a depthFirst preorder traversal of the tree, adding nodes to the returned
     * list when they are visited. Visits nodes in the following order:
     * <ol>
     *  <li>Visit the root</li>
     *  <li>Traverse the left subtree</li>
     *  <li>Traverse the right subtree.</li>
     * </ol>
     *
     * @return the tree nodes as a list, in pre order (root to leaf)
     */
    public List<Node> preOrderTraversal()
    {
        /** create one list to store the nodes in order for return,
         * and create a stack to push nodes on until we get to a leaf
         */
        List<Node> nodeList = new LinkedList();
        Stack<Node> stack = new Stack();
        /** push the root node onto the stack, then
         * for each node in the stack, get children
         * nodes and push onto the stack and then
         * print the current node
         */
        stack.push(rootNode);
        Node currNode;
        while(!stack.isEmpty())
        {
            currNode = stack.pop();
            List<Node> children = currNode.getChildren();
            if(children!=null)
            {
                //add all children of currNode to the stack
                for(int i=children.size()-1; i>=0; i--)
                {
                    Node currChild = children.get(i);
                    stack.add(currChild);
                }
            }
            nodeList.add(currNode); //add the node to the return list (visit node)
            //System.out.println(currNode);//Testing
        }
        return nodeList;
    }
    /**
     * Performs a depthFirst preorder traversal of the tree, adding leaf nodes to the returned
     * list when they are visited. Visits nodes in the following order:
     * <ol>
     *  <li>Visit the root</li>
     *  <li>Traverse the left subtree</li>
     *  <li>Traverse the right subtree.</li>
     * </ol>
     *
     * @return the tree leaf nodes as a list, in pre order (dfs)
     */
    public List<Node> preOrderTraversalLeavesOnly()
    {
        /** create one list to store the nodes in order for return,
         * and create a stack to push nodes on until we get to a leaf
         */
        List<Node> nodeList = new LinkedList();
        Stack<Node> stack = new Stack();
        /** push the root node onto the stack, then
         * for each node in the stack, get children
         * nodes and push onto the stack and then
         * print the current node
         */
        stack.push(rootNode);
        Node currNode;
        while(!stack.isEmpty())
        {
            currNode = stack.pop();
            List<Node> children = currNode.getChildren();
            if(children!=null)
            {
                //add all children of currNode to the stack
                for(int i=children.size()-1; i>=0; i--)
                {
                    Node currChild = children.get(i);
                    stack.add(currChild);
                }
            }
            /** only add leaf nodes to the list */
            if(currNode instanceof LeafNode){
                nodeList.add(currNode); //add the node to the return list (visit node)
            }
            //System.out.println(currNode);//Testing
        }
        return nodeList;
    }
}
