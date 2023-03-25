

package SysFor;

import java.util.*;
/**
 * Stores information about a decision tree split point. For See5 this will store
 * a gain ratio value, and for Explore will store an Ultimate Gain Ratio. In both
 * cases this value is referred to as gain ratio in this class.
 *
 * @author helengiggins
 * @version 1.0 20/12/2010
 */
public class Split implements Comparator{

    /** instance variables */
    /** the attribute index */
    private int attrIndex;
    /** the attribute type */
    private char attrType;
    /** the gain ratio or ultimate gain ratio for this split */
    private double gainRatio;
    /** the value for the split, can be a range for Explore */
    private String splitValue;
    /** the value index, or start value index for a range split */
    private int valueIndex;
    /** the end value index for a range split, for Explore numerical attributes only */
    private int upperValueIndex;


    /**
     * Class constructor set default values.
     */
    public Split()
    {
        attrIndex = -1;
        attrType = 'x';
        gainRatio = 0.0;
        splitValue = null;
        valueIndex = -1;
        upperValueIndex = -1;
    }

    /**
     * Class constructor.
     *
     * @param attrIndex the split attribute index
     * @param attrType the split attribute type
     * @param gainRatio the gain ratio, or UGR
     * @param splitValue the split value
     * @param valueIndex the split value index
     * @param upperValueIndex the split range upper value (Explore)
     */
    public Split(int attrIndex, char attrType, double gainRatio, String splitValue,
            int valueIndex, int upperValueIndex)
    {
        this.attrIndex = attrIndex;
        this.attrType = attrType;
        this.gainRatio = gainRatio;
        this.splitValue = splitValue;
        this.valueIndex = valueIndex;
        this.upperValueIndex = upperValueIndex;
    }

    /**
     * Returns the attribute index for the split.
     *
     * @return attribute index for the split
     */
    public int getAttrIndex()
    {
        return attrIndex;
    }

    /**
     * Return the attribute type for the attribute involved in this split.
     *
     * @return the attribute type
     */
    public char getAttrType()
    {
        return attrType;
    }

    /**
     * Returns the gain ratio (or UGR) for this split.
     *
     * @return the gain ratio (or UGR) for this split
     */
    public double getGainRatio()
    {
        return gainRatio;
    }

    /**
     * Returns the split value for the split. That is which value/s of the attribute
     * we are splitting on.
     *
     * @return the split value for the split
     */
    public String getSplitValue()
    {
        return splitValue;
    }

    /**
     * Returns the attribute value index of the split value. Note for See5 and
     * categorical attribute this value will be -1. For Explore numerical attribute
     * this represents the lower range value's index.
     *
     * @return the attribute value index of the split value
     */
    public int getValueIndex()
    {
        return valueIndex;
    }

    /**
     * Returns the attribute value index for the upper value in the range, for
     * Explore numerical attributes only.
     *
     * @return the attribute value index for the upper value in the range
     */
    public int getUpperValueIndex()
    {
        return upperValueIndex;
    }

    /**
     * Comparison method used for sorting splits. NOTE: This comparator
     * is opposite to what the standard should be, will result in descending
     * order, rather than the default ascending order.
     *
     * @param o1 the first {@link Split} object
     * @param o2 the second {@link Split} object
     * @return 0 when equal for gain ratio, 1 when o1&lt;o2, -1 when o1&gt;o2
     *
     */
    public int compare(Object o1, Object o2)
    {
        if(o1 instanceof Split & o2 instanceof Split)
        {
            Split s1 = (Split)o1;
            Split s2 = (Split)o2;
            if(s1.getGainRatio()<s2.getGainRatio())
                return 1; //for ascending order reverse the sign
            if(s1.getGainRatio()>s2.getGainRatio())
                return -1; //for ascending order reverse the sign
            else //should be equal
                return 0;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Split))
            return false;
        Split objSplit = (Split) obj;
        if(objSplit.getAttrIndex()!=attrIndex)
            return false;
        if(objSplit.getAttrType()!=attrType)
            return false;
        if(objSplit.gainRatio!= gainRatio)
            return false;
        if(!objSplit.splitValue.equals(splitValue))
            return false;
        if(objSplit.getValueIndex()!= valueIndex)
            return false;
        if(objSplit.getUpperValueIndex()!=upperValueIndex)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append("Split: ");
        str.append("attrIndex:");
        str.append(attrIndex);
        str.append(" splitValue:");
        str.append(splitValue);
        str.append(" gainRatio:");
        str.append(gainRatio);
        if(attrType=='n')
        {
            str.append(" splitValueIndex:");
            str.append(valueIndex);
        }
        return str.toString();
    }

}
