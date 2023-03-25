

package SysFor;

import java.util.*;

/**
 * A single node in a {@link DecisionTree}. This node should only be used for internal
 * nodes. This class is extends for a {@link LeafNode} to include class support
 * information. A node stores a reference to its parent node (null for root), and
 * a list of all children nodes. It also stores an attribute information and the
 * split value for this node.
 *
 * <p>For example, suppose at the root of the tree the split attribute is a numerical
 * attribute A1, and the split condition is A1>5. Then the root <code>node</code>
 * will not store an attribute index or split value, but its left child would store
 * an attribute index of 1, and split value of ">5", and the right child would
 * store attribute index of 1, and split value of "<=5".</p>
 *
 * @author helengiggins
 * @version 1.0 29/11/2010
 * @see DecisionTree
 * @see LeafNode
 */
public class Node {

    /* instance data */
    /** the split attribute name */
    private String attrName;
    /** the attribute index */
    private int attrIndex;
    /** attribute split value */
    private String splitValue;
    /** attribute type, 'n or 'c' */
    char attrType;
    /** children of this node */
    private List<Node> children;
    /** parent of this node */
    private Node parent;
    /** flag for pruning, so we don't examine an 'unprunable' node twice */
    private boolean visited;

    /**
     * Class constructor initialises to default values.
     */
    public Node()
    {
        attrName = null;
        attrIndex = -1;
        splitValue = null;
        attrType = (char) -1;
        children = null;
        parent = null;
        visited=false;
    }

    /**
     * Class constructor that creates the node with the passed values.
     *
     * @param attrName the split attribute name
     * @param attrIndex the split attribute index
     * @param splitValue the split value, for example "<5" or "Syd"
     * @param attrType numerical ('n') or categorical ('c')
     * @param children the children of this node (null for leaf nodes)
     * @param parent the parent of this node (null for root)
     */
    public Node(String attrName, int attrIndex, String splitValue, char attrType,
            List<Node> children, Node parent)
    {
        this.attrName = attrName;
        this.attrIndex = attrIndex;
        this.splitValue = splitValue;
        this.attrType = attrType;
        this.children = children;
        this.parent = parent;
        visited=false;
    }

    /**
     * Sets all attribute values for this Node, that is, the attribute index
     * and split value, the children nodes and parent node.
     *
     * @param attrName the split attribute name
     * @param attrIndex the split attribute index
     * @param splitValue the split value, for example "<5" or "Syd"
     * @param attrType numerical ('n') or categorical ('c')
     * @param children the children of this node (null for leaf nodes)
     * @param parent the parent of this node (null for root)
     */
    public void setData(String attrName, int attrIndex, String splitValue,
                        char attrType, List<Node> children, Node parent)
    {
        this.attrName = attrName;
        this.attrIndex = attrIndex;
        this.splitValue = splitValue;
        this.attrType = attrType;
        this.children = children;
        this.parent = parent;
    }

    /**
     * Sets the children for this node.
     *
     * @param children the list of children for this node
     */
    public void setChildren(List<Node> children)
    {
        this.children = children;
    }

    /**
     * Sets the parent for this node.
     *
     * @param parent the parent of this node
     */
    public void setParent(Node parent)
    {
        this.parent = parent;
    }

    /**
     * Sets the attribute index for this node.
     *
     * @param attrIndex the new attribute index
     */
    public void setAttrIndex(int attrIndex)
    {
        this.attrIndex = attrIndex;
    }

    /**
     * Gets the name of the split attribute for this node.
     *
     * @return the name of the split attribute for this node
     */
    public String getAttrName()
    {
        return attrName;
    }

    /**
     * Gets the split attribute index for this node. Will return -1 if no split
     * at this attribute, e.g. root node.
     *
     * @return the split attribute index for this node
     */
    public int getAttrIndex()
    {
        return attrIndex;
    }

    /**
     * Gets the split value the this node.
     *
     * @return the split value for this node
     */
    public String getSplitValue()
    {
        return splitValue;
    }

    /**
     * Gets the type of the split attribute, that is, 'c' or 'n'.
     *
     * @return the type of the split attribute, that is, 'c' or 'n'
     */
    public char getAttrType()
    {
        return attrType;
    }

    /**
     * Gets the list of children nodes for this node. Note the list may be null,
     * especially for leaf nodes.
     *
     * @return the list of children nodes for this node
     */
    public List<Node> getChildren()
    {
        return children;
    }

    /**
     * Checks if the node has any children.
     *
     * @return true if the node has children, false otherwise
     */
    public boolean hasChildren()
    {
        if(children==null)
            return false;
        //if(children.isEmpty())
         //   return false;
        else
            return true;
    }

    /**
     * Returns the split attribute name for the first child node of this node.
     * For printing the tree, we want to know what value this node's
     * children were split on.
     * @return the split attribute name for the first child node of this node
     */
    public String getSplitAttribute()
    {
        if(hasChildren())
        {
            return children.get(0).getAttrName();
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the parent node of this node. Note may be null, especially for the
     * root node.
     * 
     * @return the parent node of this node
     */
    public Node getParent()
    {
        return parent;
    }

    /**
     * Has this node been visited, used for pruning
     *
     * @return node visited when true, false otherwise
     */
    public boolean isVisited()
    {
        return visited;
    }

    /**
     * Sets the node to visited
     */
    public void setVisited()
    {
        visited=true;
    }

    /**
     * Recursive method for getting the node level in the tree.
     *
     * @return the level of this node in the tree, root is level 0
     */
    public int getLevel()
    {
        /** check if we're at the root node */
        if (parent == null) {
            return 0;
        }
        return 1 + parent.getLevel();
    }

    /**
     * Return a new nodes which has a copy of all info in the node. Note, that
     * the children list, and parent node will be the same object references.
     * @param node node to the be copied
     * @return a copy of the passed node
     */
    public static Node copy(Node node)
    {
        Node copyNode = new Node(node.getAttrName(), node.getAttrIndex(),
                node.getSplitValue(), node.getAttrType(),
            node.getChildren(), node.getParent());
        return copyNode;
    }

    /**
     * Returns the current contents of the node as a <code>String</code>.
     *
     * @return the current contents of the node as a <code>String</code>
     */
    @Override
    public String toString()
    {
        String retStr = "Node: ";
        if(attrName!=null)
        {
           retStr+= "Attr-" + attrName + " SplitValue-"  + splitValue + " ParentSplitAttr-" +
                   parent.getAttrName() + " ParentSplitValue-" + parent.getSplitValue();
        }
        return retStr+="\n";
    }
    
}
