

package SysFor;

import java.util.*;

/**
 * Methods for manipulating a {@link DecisionTree}. Includes functions for
 * outputting a tree to file.
 *
 * @author helengiggins
 * @version 1.0 1/12/2010
 * @see DecisionTree
 * @see Node
 * @see LeafNode
 */
public class DecisionTreeFunctions {

    /**
     * From a {@link DecisionTree} generates a {@link String} representation
     * of the tree, displaying in preorder (traversal using DFS). This format
     * is designed for printing in a user-friendly manner. Not useful for
     * prediction.
     *
     * @param tree the decision tree to be printed
     * @return the tree in printing format
     */
    public static String readTreeForDisplay(DecisionTree tree)
    {
        String retStr = "";
        /** get the tree as a list, outputted in preorder (DFS) */
        List<Node> preOrderList = tree.preOrderTraversal();
        //int level = 0; //keep track of which level we are on in tree
        int tabWidth = 2; //will be used to tab across for each level
        while(!preOrderList.isEmpty())
        {
            Node currNode = preOrderList.remove(0); //get next node in list
            /** generate tab string, so easier to see levels */
            int level = currNode.getLevel();
            int numSpaces = level * tabWidth;
            String tabStr = "";
            for(int i=0; i<numSpaces; i++){tabStr+=" ";}
            retStr += tabStr + "Level (" + level + ") " ;
            /* check if not a leaf node
             * 
             * print the attribute we're slitting on (stored at child level)
             * and the split value. So root will not have a split value.
             */
            if(currNode.hasChildren())
            {
                retStr+= currNode.getSplitAttribute();
                String split = currNode.getSplitValue();
                if(split!=null) //in case we're at the root node
                {
                   retStr+= " when " + split;
                }
                retStr+="\n";
            }
            /** just be sure we have a leaf node, before we try and cast */
            if(currNode instanceof LeafNode) //leaf node
            {
                LeafNode currAsLeaf = (LeafNode) currNode;
                retStr+= currAsLeaf.getClassSupportInfo() + " when " + currNode.getSplitValue() + "\n";
            }
        }
        return retStr;
    }

    /**
     * From a {@link DecisionTree} generates a {@link String} representation
     * of the logic rules derived from the decision tree. Methods generates the rules
     * by reading the path from each leaf node to the root. Each rule will show the
     * class value supports for all records that fall into that leaf.
     *
     * <p>Format for the output String is as follows.</p>
     * <ul>
     * <li>First line shows attribute type for each attribute, 0=categorical, 1=numerical, 2=class</li>
     * <li>Each following line gives a logic rule. There is a value for each attribute. If the
     * current attribute is not involved in the logic rule there will be a '-' to indicate this.
     * For class attribute, there is a support set, where it shows each attribute value, and how
     * many records fall into that value, .e.g. for class status {good;200,bad;5}</li>
     * </ul>
     *
     * @param tree the decision tree we want to extract logic rules from
     * @param dataset the original dataset that the tree was built from
     * @return the logic rules for each leaf node in the decision tree
     */
    public static String readTreeForPrediction(DecisionTree tree, Dataset dataset)
    {       
        /* create a List for writing the logic rules to as we discover them */
        ArrayList <String []> logicRules = new ArrayList();
        /** get the list of nodes, in breadthFirst traversal order */
        List<Node> treeNodes = tree.levelOrderTraversal();

        /** move through the list to find all leaf nodes */
        while(!treeNodes.isEmpty())
        {
            Node currNode = treeNodes.remove(0); //take node from front of list
            if(currNode instanceof LeafNode)
            {
                /* find the logic rule for this leaf */
                logicRules.add(findLogicRule((LeafNode)currNode, dataset));
            }
        }
        String retStr = "";

        /** First line of file needs to be the attribute types for each
         * attribute, where 0=categorical, 1=numerical, 2=class
         */
        int numAttrs = dataset.getNumberOfAttributes();
        for(int i=0; i<numAttrs; i++)
        {
            retStr+=String.valueOf(dataset.getAttributeTypeForPrediction(i)) + " ";
        }
        retStr+="\n";
        /** loop over all logic rules and print to string.
         *NOTE: Need to insert class supports at classIndex
         */
        for(int i=0; i<logicRules.size(); i++)
        {
            String [] currRule = logicRules.get(i);
            /** loop over and print to str. 
             */
            for(int j=0; j<currRule.length; j++)
            {
                retStr+=currRule[j]+ " ";
            }
            retStr+="\n";
        }
        return retStr;
    }


    private static String [] findLogicRule(LeafNode leafNode, Dataset dataset)
    {
        /** create an array to store logic rule, size of number of attributes.
         */
        String [] logicRule = new String[dataset.getNumberOfAttributes()];

        /** fill array with "-", which is default for an attribute not
         *  contributing to the logic rule
         */
        for(int i=0; i<logicRule.length; i++)
        {
            logicRule[i] = "-";
        }

        /** We now want to find all of the attributes and split values from the
         * leaf node to the root. We do this by getting the parent node of this
         * node, until its empty (root node).
         */
        Node currNode = leafNode;
        int currAttrIndex;
        char attrType;
        String splitValue;

        /** while parent not null, we are not at root yet
         * NOTE: assumes tree has at least 1 split point
         */
        do{
            currAttrIndex = currNode.getAttrIndex();
            attrType = currNode.getAttrType();
            splitValue = currNode.getSplitValue();
            
            /** check attribute type */
            if(attrType=='c')
            {
               /** a categorical attribute may appear more once in a logic rule (for Explore),
                * also need to check for !Syd type values.
                */
                logicRule[currAttrIndex]= convertCategoricalSplitValue(splitValue, logicRule[currAttrIndex], currAttrIndex, dataset);
            }
            if(attrType=='n')
            {
                /* for numerical attributes need to check for disjoint or
                 * overlapping ranges, since one attribute can be tested multiple
                 * times for one logic rule
                 */
                logicRule[currAttrIndex]= convertNumericalSplitValue(splitValue,
                        logicRule[currAttrIndex], currAttrIndex, dataset);
            }
            /** get parent node, and its split info*/
            currNode = currNode.getParent();
        }while(currNode!=null);

        /** add class support info into class attribute */
        logicRule[leafNode.getClassIndex()] = leafNode.getClassSupportInfo();

        return logicRule;
    }

    /**
     * This method converts a decsionTree split value into a prediction split value,
     * for numerical values.
     *
     * <p>Key</p>
     * <ul>
     *  <li>L in first position indicates less than or equal to</li>
     *  <li>G indicates greater than</li>
     *  <li>R indicates interval (range), or window between, and two values. First value &gt; and second value &lt;=</li>
     *  <li>M indicates multiple intervals, separated by a :</li>
     *
     * @param numSplit the numerical split value outputted from {@link DecisionTreeBuilder}
     * @param existingRule the current logic rule for this attribute, need to check for overlaps
     * @param attrIndex the index of the numerical attribute for this split
     * @param dataset needs reference to the original dataset so it can get numerical
     * domain values, for Explore splits.
     * @return the split value in prediction format
     */
    public static String convertNumericalSplitValue(String numSplit, String existingRule,
                       int attrIndex, Dataset dataset)
    {
        //TODO need to use the existingRule in bulding the current one, for when more than one
        //leaf from the same attribute
        //System.out.println(existingRule + " " + numSplit);
        char existFirstChar = existingRule.charAt(0);
        String predSplit = "";//the returned prediction format split
        /** get first char to determine what type of split we have */
        char firstChar = numSplit.charAt(0);

        /** if there is no existing rule */
        if(existFirstChar=='-')
        {
            /** if less than equal*/
            if(firstChar=='<')
            {
                /** need to ignore first two chars '<=' and append to an L */
                predSplit+="L" + numSplit.substring(2,numSplit.length());
            }
            else if(firstChar=='>') /** if greater than */
            {
                /** need to ignore first char '>' and append to a G */
                predSplit+="G" + numSplit.substring(1,numSplit.length());
            }
            else if(firstChar=='[') /** interval */
            {
                /** need to ignore first and last char '[]' and append to an R */
                predSplit+="R" + numSplit.substring(1,numSplit.length()-1);
            }
            else if(firstChar=='!') /** two intervals - lowDomain,(value1-1) & (value2+1),highDomain */
            {
                /** get the low and high domain values for the attribute */
                double lowDomain = dataset.getAttrLowDomain(attrIndex);
                double highDomain = dataset.getAttrHighDomain(attrIndex);
                /** need to get value1-1 and value2+1*/
                StringTokenizer tokens = new StringTokenizer(numSplit, "[],!");
                double value1 = Double.parseDouble(tokens.nextToken())-1;
                double value2 = Double.parseDouble(tokens.nextToken())+1;

                /** now need to check for the case that we have a
                 * 'window' in which one value is a low or high domain value
                 *
                 * e.g. attribute x has low domain 20, and high domain 100.
                 * the best split point is selected as [20,30] or ![20,30].
                 * In this case our 'M' is really and 'R'
                 */
                if(lowDomain>value1)
                {
                    /** there is no low interval */
                    predSplit+="R" + value2 + "," + highDomain;
                }
                if(value2>highDomain)
                {
                    /** there is no high interval */
                    predSplit+="R" + lowDomain + "," + value1;
                }
                else
                {
                    /** am assuming we can't have both scenarios above happen at
                     * once, so now put together the multiple range
                     */
                    predSplit+="M" + lowDomain + "," + value1 + ":" + value2 + "," + highDomain;
                }

            }
            else /** should not get to here - error! */
            {
                System.out.println("Problem in convertNumericalSplitValue");
            }
        }
        /** there is an existing rule for same attribute, this means we have more
         * possible outcomes to consider.
         */
        else
        {
            /** if less than equal for both, take the lower, which is the existing rule
             * since we examine tree from leaf to root
             */
            if(firstChar=='<' && existFirstChar=='L')
            {
                /** need to ignore first two chars '<=' and append to an L */
                predSplit+=existingRule;
            }
            /** if less than for curr, and greater for existing, we have a range
             * from value after existing to curr
             */
            else if(firstChar == '<' && existFirstChar == 'G')
            {
                ///** need to get value after existing value, since we're dealing with a greater than */
                String existingStr = existingRule.substring(1,existingRule.length());
                ///** find index of existing value, then get value at next index */
                //String valueAfterExist = dataset.getAttrValuePlusOne(attrIndex, existingStr);

                //predSplit+="R" + valueAfterExist
                //        + "," + numSplit.substring(2,numSplit.length());

                //HG changed to now use the value as a greater than. This is needed now, since the 'next'
                // value may not be consecutive.
                predSplit+="R" + existingStr
                        + "," + numSplit.substring(2,numSplit.length());

            }
            /** we already have a range, new split value is a less than or equal,
             * so we have reduced the range from existing bottom value to curr.
             */
            else if(firstChar=='<' && existFirstChar == 'R')
            {
                /** need to get curr value, since we're dealing with a less than equal */
                String currStr = numSplit.substring(2,numSplit.length());
                /** now need to get second range value */
                int commaIndex = existingRule.indexOf(",");
                String existSecValue = existingRule.substring(1, commaIndex); //get bottom value of existing
                //System.out.println("existSecValue: "+ existSecValue + " R" + existSecValue + "," + currStr);
                predSplit+= "R" + existSecValue + "," + currStr;
            }
            /** if greater for both, take the higher value, which is existing rule */
            else if(firstChar=='>' && existFirstChar == 'G')
            {
                predSplit+=existingRule;
            }
            /** if less than for existing, and greater for curr, we have a range
             * from curr+1 to existing
             */
            else if(firstChar=='>' && existFirstChar == 'L')
            {
                /** need to get value after curr value, since we're dealing with a greater than */
                String currStr = numSplit.substring(1,numSplit.length());
                ///** find index of curr value, then get value at next index */
                //String valueAfterCurr = dataset.getAttrValuePlusOne(attrIndex, currStr);

                //predSplit+="R" + valueAfterCurr
                predSplit+="R" + currStr
                        + "," + existingRule.substring(1,existingRule.length());
            }
            /** we already have a range, new split value is a greater than,
             * so we have reduced the range from curr+1 to existing top value
             */
            else if(firstChar=='>' && existFirstChar == 'R')
            {
                /** need to get value after curr value, since we're dealing with a greater than */
                String currStr = numSplit.substring(1,numSplit.length());
                /** find index of curr value, then get value at next index */
                //String valueAfterCurr = dataset.getAttrValuePlusOne(attrIndex, currStr);
                /** now need to get second range value */
                int commaIndex = existingRule.indexOf(",")+1; //add one so we ignore comma when getting value
                String existSecValue = existingRule.substring(commaIndex,existingRule.length());
                //System.out.println("existSecValue: "+ existSecValue + " R" + valueAfterCurr + "," + existSecValue);
                predSplit+="R"+ currStr + "," + existSecValue;
            }
            else /** should not get here! */
            {
                System.out.println("Problem in convertNumericalSplitValue");
            }

        }
        
        return predSplit;
    }

    /**
     * Convert a split rule into a logic rule for a categorical attribute. Takes into
     * account the case where an attribute may be tested more than once using Explore
     * method.
     *
     * @param catSplit the categorical split value outputted from {@link DecisionTreeBuilder}
     * @param existingRule the existing logic rule for this attribute
     * @param attrIndex the index of the categorical attribute for this split
     * @param dataset needs reference to the original dataset so it can get
     * all attribute values, for Explore splits.
     * @return the split value in prediction format
     */
    private static String convertCategoricalSplitValue(String catSplit, String existingRule, int attrIndex, Dataset dataset)
    {
        /** NOTE: for Explore we may have a categorical attribute tested more than once.
         * e.g. !syd as split value, then !melb or melb.
         *
         * Need to also consider which order these will be tested. When creating the logic
         * rules, we go from leaf to root. So if existing rule has only one value, we will
         * return this. e.g. melb in example above.
         */

        String predSplit = "";//the returned prediction format split
        /** get first char to determine what type of split we have */
        char firstChar = catSplit.charAt(0);
        /** get first char of existing rule to see if we've tested this attribute before */
        char existFirstChar = existingRule.charAt(0);
        /** first check if there is an existing rule, i.e. we've tested this attribute more than once */
        if(existFirstChar!='-')
        {
            /**count how many tokens in existing rule, to see if a '!melb', or just 'melb' split */
            StringTokenizer tokens = new StringTokenizer(existingRule, " ,");
            if(tokens.countTokens()==1)
            {
                /** return the existing rule, it's closest to the leaf, so can't do better */
                return existingRule;
            }
            else
            {
                /** we have a !syd then !melb type rule, need to exclude current split value
                 * from the existing values
                 */
                String ommittedValue = catSplit.substring(1,catSplit.length()) + ",";
                /** find the starting index of ommittedValue */
                int startIndex = existingRule.indexOf(ommittedValue);
                int lengthOmmitted = ommittedValue.length();
                String front = existingRule.substring(0, startIndex); //get front half, minus ommittedValue
                String back = existingRule.substring(startIndex+lengthOmmitted, existingRule.length()); //get after ommitted value
                //System.out.println("ommittedValue: " + ommittedValue + " " + front + back);//testing
                return front+back;
            }
        }
        /** Not tested this attribute before,
         * if a ! we need to output all of the other values */
        else if(firstChar == '!')
        {
           /** get all attribute values for this attribute */
            String [] allAttrValues = dataset.getAllAttrValues(attrIndex);
            String ommittedValue = catSplit.substring(1,catSplit.length());
            /** loop over all attribute values for this attribute and
             * output all of them but ommittedValue
             */
            for(int i=0; i<allAttrValues.length; i++)
            {
                if(!allAttrValues[i].equals(ommittedValue))
                {
                    predSplit+=allAttrValues[i] + ",";
                }
            }
        }
        /** otherwise we have split on the one value, return it directly */
        else
        {
            return catSplit;
        }
        return predSplit;
    }

    /**
     * Given a {@link DecisionTree} and {@link Dataset} return the indices of all
     * split attributes in the tree.
     * @param tree a decision tree
     * @param dataset the dataset the tree was built from
     * @return the indices of the split attributes for tree
     */
    public static int [] getSplitAttributesForTree(DecisionTree tree, Dataset dataset)
    {
        /** create an array the size of the number of attributes in the dataset.
         * Get a list of the nodes in the tree, and iterate over them. When we find
         * an attribute used in a split, set the flag at it's index to 1.
         * Finally, loop over the attrFlags array to return on those attributes
         * 'switched on'.
         */
        int [] attrFlags = new int[dataset.getNumberOfAttributes()]; //will default to 0
        List <Node> treeNodes = tree.preOrderTraversal();
        int numAttrs=0; //keep track of how many attributes we find, to set size of return array
        for(int currNode=0; currNode<treeNodes.size(); currNode++)
        {
            Node node = treeNodes.get(currNode);
            int splitAttrInd = node.getAttrIndex();
            //System.out.println("splitAttrInd: " + splitAttrInd);
            /** check not the root node */
            if(splitAttrInd!=-1)
            {
                /** check if this attribute has not been flagged yet */
                if(attrFlags[splitAttrInd]==0)
                {
                   attrFlags[splitAttrInd]=1;
                   numAttrs++;
                }
            }
        }
        int [] splitAttrs = new int[numAttrs];
        int currPos=0; //the curr pos in splitAttrs array
        for(int currAttr=0; currAttr<attrFlags.length; currAttr++)
        {
            if(attrFlags[currAttr]==1)
            {
                splitAttrs[currPos] = currAttr;
                currPos++; //move along one position
            }
        }
        return splitAttrs;
    }

    public static DecisionTree correctAttributeIndices (DecisionTree tree, Dataset dataset)
    {
        List <Node> nodeList = tree.levelOrderTraversal();
        /** loop over all nodes in the tree and reset the attribute index for each node
         * to the value matching that in dataset, for the split attribute name
         */
        for(int nodeInd=0; nodeInd<nodeList.size(); nodeInd++)
        {
            Node currNode = nodeList.get(nodeInd);
            String splitName = currNode.getAttrName();
            /** find attrIndex for splitName, and reset index in node to this value.
             * Check it's not null, which will be the case for root node
             */
            if(splitName!=null)
            {
              int attrIndex = dataset.getAttributeIndex(splitName);
              currNode.setAttrIndex(attrIndex);
            }
            /** check if a leaf node, and adjust class index as well */
            if(currNode instanceof LeafNode)
            {
                LeafNode leafNode = (LeafNode) currNode;
                leafNode.setClassIndex(dataset.getClassIndex());
            }
        }
        return tree;
    }

}
