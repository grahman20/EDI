

package SysFor;

/**
 * A leaf {@link Node} of a {@link DecisionTree}. This node has the additional
 * information relating to class supports. That is, for the data set partition
 * of records that 'fall' into this leaf node, count the number of records having
 * each class value. Also has information about the class itself.
 *
 * @author helengiggins
 * @version 1.0 30/11/2010
 * @see Node
 * @see DecisionTree
 */
public class LeafNode extends Node{

    /* instance data */
    /** class supports */
    private int [] classSupports;
    /** class values in matched array */
    private String [] classValues;
    /** the name of the class */
    private String className;
    /** the attrIndex for the class */
    private int classIndex;

    /**
     * Class constructor calls constructor of Node to set default values.
     */
    public LeafNode()
    {
        super();
        classSupports = null;
        classValues = null;
        className = null;
        classIndex = -1;
    }

    /**
     * Class constructor passed class information for the leaf node.
     *
     * @param classSupports number of records that fall into each class for this leaf
     * @param classValues the class values, linked with class support
     * @param className the class name
     * @param classIndex the attribute index for the class
     */
    public LeafNode(int [] classSupports, String [] classValues, String className,
                    int classIndex)
    {
        super();
        this.classSupports = classSupports;
        this.classValues = classValues;
        this.className = className;
        this.classIndex = classIndex;
    }

    /**
     * Gets the class support array.
     *
     * @return the class support array
     */
    public int [] getClassSupports()
    {
        return classSupports;
    }

    /**
     * Gets the class values.
     *
     * @return the class values
     */
    public String [] getClassValues()
    {
        return classValues;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Gets the class attribute index.
     *
     * @return the class attribute index
     */
    public int getClassIndex()
    {
        return classIndex;
    }

    /**
     * Sets the class attribute index.
     *
     * @param classIndex the new class index for this leaf
     */
    public void setClassIndex(int classIndex)
    {
        this.classIndex = classIndex;
    }

    /**
     * Makes a copy of the passed leaf node. Note that the parent reference is
     * only a shallow copy. All other values are a deep copy.
     *
     * @param leafNode node being copied
     * @return a copy of the passed node
     */
    public static LeafNode copy(LeafNode leafNode)
    {
        /** make copies of the arrays before adding */
        int [] classSupports = leafNode.getClassSupports();
        String [] classValues = leafNode.getClassValues();
        int [] newSupports = new int[classSupports.length];
        String [] newValues = new String[classSupports.length];
        for(int i=0; i<classSupports.length; i++)
        {
            newSupports[i]=classSupports[i];
            newValues[i]=classValues[i];
        }
        LeafNode copyNode = new LeafNode(newSupports, newValues, leafNode.getClassName(),
                    leafNode.getClassIndex());
        /** now copy the Node info over */
        copyNode.setData(leafNode.getAttrName(), leafNode.getAttrIndex(),
                leafNode.getSplitValue(), leafNode.getAttrType(), null, leafNode.getParent());
        return copyNode;
    }

    /**
     * Returns current contents of the leaf node as a {@link String}.
     *
     * @return current contents of the leaf node as a {@link String}
     */
    @Override
    public String toString()
    {
       String retStr = super.toString();
       retStr+= getClassSupportInfo(); //get class name and supports as String
       return retStr+="\n";
    }

    /**
     * Returns the class name, and class value supports.
     * 
     * @return the class name, and class value supports
     */
    public String getClassSupportInfo()
    {
        String retStr="";
        if(classSupports!= null)
        {
           retStr+="{";
           for(int i=0; i<classSupports.length-1; i++)
           {
              retStr +=classValues[i] + ";" + classSupports[i] +",";
           }
           /** deal with last class differently, so no extra space at end */
           retStr +=classValues[classSupports.length-1] + ";" +
                   classSupports[classSupports.length-1];
           retStr+="} ";
       }
        return retStr;
    }
}
