

package SysFor;

import java.util.*;

/**
 * Contains methods for building {@link DecisionTree} using the Explore method.
 * Extends {@link See5} so we can use some of the underlying methods.
 *
 * @author helengiggins
 * @version 1.0 31/01/11
 */
public class Explore extends See5 {

    /* instance data */
    /** the original dataset we're building the decision tree on */
    //Dataset dataset; //in See5
    /** the decision tree being built */
    //DecisionTree tree; //in See5

    /**
     * Class constructor, only initializes instance data to defaults.
     */
    public Explore()
    {
        dataset = null;
        tree = null;
    }

    /**
     * Class constructor passed a dataset on creation.
     *
     * @param dataset the dataset we want to build the decision tree from
     */
    public Explore(Dataset dataset)
    {
        this.dataset = dataset;
    }

    /**
     * Controls the overall construction of the decision tree. Assumes a
     * dataset has already been passed using the parametric constructor.
     *
     * @return the decision tree generated from the data set using Explore algorithm
     */
    @Override
    public DecisionTree buildTree()
    {

        /** create and store the root node for the decision tree */
       Node root = new Node();
       tree = new DecisionTree(root);
       /**
        * this method will run until the tree is built
        */
       iterativeSplitTree(dataset, tree.getRootNode());
       System.out.println("@@@@@@@@@@@@@ Decision Tree before Pruning @@@@@@@@@@@@@@");
       /** print tree to screen */
       System.out.println(DecisionTreeFunctions.readTreeForDisplay(tree));
        return tree;
    }

    /**
     * Controls the overall construction of the decision tree. Assumes the
     * dataset has not yet been passed.
     *
     * @param dataset
     * @return the decision tree generated from the data set using Explore algorithm
     */
    @Override
    public DecisionTree buildTree(Dataset dataset)
    {
        this.dataset = dataset;
        return this.buildTree();
    }

    /**
     * Given a current partition of the dataset, initially the full dataset,
     * this method will calculate a split point and then create a node in the
     * decision tree for that split. It will then partition the dataset, based
     * on the split value, and continue to generate the subtrees, iteratively,
     * until no further split points can be found.
     *
     * @param startPartition the dataset we are building the tree on
     * @param rootNode the root node for the tree we're building
     */
    private void iterativeSplitTree(Dataset startPartition, Node rootNode)
    {
        /** this flag will keep track of whether or not any split points
         * were found in the current level
         */
        boolean splitFound = false;
        int level =0; //current tree level
        /**
         * store the current partitions of the dataset for the current level,
         * this will start out storing only one partition, the whole dataset
         */
        List <Dataset> currentPartitions = new ArrayList<Dataset>();
        currentPartitions.add(startPartition);
        /**
         * store the subtree nodes for the level above the one we're currently
         * creating, starts out storing the overall root node for the tree
         */
        List <Node> currentRootNodes = new ArrayList<Node>();
        currentRootNodes.add(rootNode);
        /** NOTE: the above two list are paired */

        /**
         * starting at tree level 0, try and find a new split point
         * for each partition
         */
        do{
            /**
             * Step 1: for each currentPartition calculate a split point
             *
             * Step 2: for each non-null split, create new decision tree nodes,
             *  and store to currentRootNodes list for the next iteration.
             *  Then partition the currentPartition into sub-partitions for each
             *  split point. Store these for next iteration.
             *  Set the splitFound flag to true
             *
             * Step 3: for each null split, we need to create a leaf node, this node
             *  will not be placed in the currentRootNodes list.
             */

            /** testing */
            //System.out.println("Root nodes at level " + level);
            //for(int i=0; i<currentRootNodes.size(); i++)
            //{
            //    System.out.print(currentRootNodes.get(i));
            //}

            splitFound = false; //reset flag: only set to true if we're able to split at this level
            level++;

            /** Step 1: for each currentPartition calculate a split point */
            int numParts = currentPartitions.size();
            Split [] currSplits = new Split[numParts]; //store the splits
            for(int currPart=0; currPart<numParts; currPart++)
            {
               currSplits[currPart]=calculateSplit(currentPartitions.get(currPart));
            }
            /** create two temp lists to store the new partitions and associated
             * tree nodes for the next level (iteration). These will be set to
             * currentPartitions and currentRootNodes at the end of the do-while
             */
            List <Dataset> nextLevelPartitions = new ArrayList<Dataset>();
            List <Node> nextLevelRootNodes = new ArrayList<Node>();

            /** loop over all splits to generate the current level of the tree.
             * Note: some/all may be null
             */
            for(int splitInd=0; splitInd<numParts; splitInd++)
            {
                Split split = currSplits[splitInd];
                Dataset currentPartition = currentPartitions.get(splitInd);
                Node currNode = currentRootNodes.get(splitInd);
                /** Step 2: for each non-null split, create new decision tree nodes,
                 * and store to currentRootNodes list for the next iteration.
                 *  Then partition the currentPartition into sub-partitions for each
                 *  split point. Store these for next iteration.
                 *  Set the splitFound flag to true
                 */
                if(split!=null)
                {
                    /** generate the dataset partitions created by the split,
                     * add them to the list for the next level, and create
                     * a new decision tree node for each partition, also add this
                     * to the list for next level.
                     */

                    /** check which type of attribute */
                    if(split.getAttrType()=='c')
                    {
                        /** categorical attribute */
                        int attrIndex = split.getAttrIndex();
                        Dataset [] partitions = currentPartition.generateDatasetPartitions(attrIndex, "all");
                        List <Node> children = new ArrayList<Node>();
                        currNode.setChildren(children); /** set children in the current root node */

                        /** store temp array of split nodes and partitions, only to be added to list
                         * for next level if we get through all partitions cleanly. That is
                         * if none of them are too small.
                         */
                        Dataset [] currSplitPartitions = new Dataset[partitions.length];
                        Node [] currSplitNodes = new Node [partitions.length];
                        boolean nullSplit = false; //so we can check at the end of the loop if we generated a null split
                        /** create nodes in the decision tree for each partition
                         *
                         * NOTE: At this step we need to check if any partition is smaller than the
                         * user input minimum. If so, we will reset split to null, and allow
                         * the method to pass through to turning this node into a leaf node.
                         */
                        for(int currPart=0; currPart<partitions.length; currPart++)
                        {
                            Dataset currDataset = partitions[currPart];
                            //System.out.println("currSplit: " + splitInd+ "  currPart: " + currPart + " level: " + level+
                            //        " numRec: " + currDataset.getNumRecords());

                            /** check for partition too small -see note above */
                            if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                            {
                                split=null;
                                //System.out.println( " Leaf currNode: "  + currNode );
                                currNode.setChildren(null);
                                nullSplit=true;
                                break;
                            }
                            /** create a new node */
                            Node child = new Node();
                            String attrName = currDataset.getAttributeName(attrIndex);
                            /** current index attribute value is the split value */
                            String splitValue = currDataset.getAttributeValue(attrIndex, currPart);
                            child.setData(attrName, attrIndex, splitValue,'c',null, currNode);
                            /** add to children list that was set in currNode */
                            children.add(child);
                            /** store current partition and node to temp lists for next
                             *  level
                             */
                            currSplitPartitions[currPart]=currDataset;
                            currSplitNodes[currPart]=child;
                            splitFound=true; //set flag to true, so we will go to the next level
                        }
                        if(!nullSplit)
                        {
                            /** store current partition and node to temp lists for next
                             *  level
                             */
                            for(int i=0;i<partitions.length; i++)
                            {
                                nextLevelPartitions.add(currSplitPartitions[i]);
                                nextLevelRootNodes.add(currSplitNodes[i]);
                            }
                        }

                    }
                     /** note need to also check for null here, in case split was reset in categorical part,
                      * for when a partition will be too small
                      */
                    if(split!=null && split.getAttrType()=='n')
                    {
                        /** numerical attribute */
                        int attrIndex = split.getAttrIndex();
                        String splitValue = String.valueOf(split.getValueIndex()); //attribute value index
                        /** pass the attribute index, and the split value index and
                         * generate the partitions of the dataset
                         */
                        Dataset [] partitions = currentPartition.generateDatasetPartitions(attrIndex, splitValue);
                        List <Node> children = new ArrayList<Node>();
                        currNode.setChildren(children); /** set children in the passed parent node */

                        /** store temp array of split nodes and partitions, only to be added to list
                         * for next level if we get through all partitions cleanly. That is
                         * if none of them are too small.
                         */
                        Dataset [] currSplitPartitions = new Dataset[partitions.length];
                        Node [] currSplitNodes = new Node [partitions.length];
                        boolean nullSplit = false; //so we can check at the end of the loop if we generated a null split

                        /** create nodes in the decision tree for each partition and
                         * add to list for next iteration
                         *
                         * NOTE: At this step we need to check if any leaf is smaller than the
                         * user input minimum. If so, we will reset partitions to null, and allow
                         * the method to pass through to turning this node into a leaf node.
                         */
                        for(int currPart=0; currPart<partitions.length; currPart++)
                        {
                            Dataset currDataset = partitions[currPart];
                            //System.out.println("N  currPart: " + currPart + " level: "
                            //        + level+" numRec: " + currDataset.getNumRecords());

                            /** check for partition too small -see note above */
                            if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                            {
                                split=null;
                                //System.out.println( " Leaf currNode: "  + currNode );
                                currNode.setChildren(null);
                                nullSplit=true;
                                break;
                            }
                            Node child = new Node();
                            String attrName = currDataset.getAttributeName(attrIndex);
                            /** need to create the split string for the node */
                            splitValue="";
                            if(currPart==0)
                            {
                                splitValue = "<=" + split.getSplitValue();
                            }
                            else
                            {
                                splitValue = ">" + split.getSplitValue();
                            }
                            child.setData(attrName, attrIndex, splitValue,'n',null, currNode);
                            /** add to children list that was set in currNode */
                            children.add(child);
                            /** store to temp arrays, only to be added to list for next level if we get to
                             * the end of this for loop
                             */
                            currSplitPartitions[currPart]=currDataset;
                            currSplitNodes[currPart]=child;
                            splitFound=true; //set flag to true, so we will go to the next level
                        }
                        if(!nullSplit)
                        {
                            /** store current partition and node to temp lists for next
                             *  level
                             */
                            for(int i=0;i<partitions.length; i++)
                            {
                                nextLevelPartitions.add(currSplitPartitions[i]);
                                nextLevelRootNodes.add(currSplitNodes[i]);
                            }
                        }
                    }

                }
                /** Step 3: for each null split, we need to create a leaf node, this node
                 * will not be placed in the currentRootNodes list.
                 */
                if(split==null)
                {
                    //System.out.println("currSplit: " + splitInd+ " level: " + level+
                    //                " Leaf");
                    //System.out.println("SPLIT null " + currNode);
                    int attrIndex = currNode.getAttrIndex(); //index of split attribute
                    String rawSplit = currNode.getSplitValue();
                    /** need to check in case we have not been able to make a split at root node */
                    if(rawSplit!=null)
                    {
                        /** remove first char to get split type <= or > */
                        char splitType = rawSplit.charAt(0);
                        StringTokenizer tokens = new StringTokenizer(rawSplit, "<>=");
                        String splitValue = tokens.nextToken();
                        int valueIndex = -1;
                        //System.out.println("LEAF: " + attrIndex + " "+ splitType + " " + splitValue);
                        //System.out.println(currentPartition);
                        int [] classSupports = new int[0];
                        int classIndex = currentPartition.getClassIndex(); //class attribute index
                        String className = currentPartition.getAttributeName(classIndex);
                        String [] classValues = currentPartition.getAllAttrValues(classIndex);
                        if(currNode.getAttrType()=='n')
                        {
                            if(splitType=='<'){
                                //System.out.println("no Split: n <");
                                valueIndex = currentPartition.getAttrValueIndex(attrIndex, splitValue);
                               classSupports = getClassSupportsForSplit(attrIndex, currentPartition,valueIndex, true);
                            }
                            if(splitType=='>'){
                                //Note: splitValue will not appear in the higher partition, so need to find index
                                //of the value one up from it.
                               valueIndex = currentPartition.getAttrValueIndexPlusOne(attrIndex, splitValue);
                               classSupports = getClassSupportsForSplit(attrIndex, currentPartition,valueIndex, false); //note removed +1
                            }

                        }
                        if(currNode.getAttrType()=='c')
                        {
                            valueIndex = currentPartition.getAttrValueIndex(attrIndex, rawSplit);
                            classSupports = currentPartition.getAttrValueClassSupports(attrIndex, valueIndex);
                        }
                        /** create the leafNode and copy it's previous info over */
                        LeafNode newLeaf = new LeafNode(classSupports, classValues, className, classIndex);
                        newLeaf.setData(currNode.getAttrName(), attrIndex, rawSplit, currNode.getAttrType(), null, currNode.getParent());
                        /** need to set as child for parent node */
                        Node parent = currNode.getParent();
                        List <Node> childList = parent.getChildren();
                        //System.out.println("childList: " + childList.size());
                        /* find currNode in the list and replace with Leaf node */
                        for(int i=0; i<childList.size(); i++)
                        {
                            /** can check object references for equality */
                            if(childList.get(i)==currNode)
                            {
                                childList.set(i,newLeaf);
                            }
                        }
                    }
                }

            }//end loop over current partitions
            currentPartitions = nextLevelPartitions;
            currentRootNodes = nextLevelRootNodes;
            //System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }while(splitFound);
    }

    /**
     * Given a current partition of the dataset calculates the split value. Uses
     * Explore calculations.
     *
     * @param currentPartition a current partition of the dataset
     * @return the best split for this partition
     */
    private Split calculateSplit(Dataset currentPartition)
    {

        /** check that we have enough records in the current partition
         * to proceed. First of the 3 termination conditions checked.
         */
        //System.out.println("**********************************");
        if(currentPartition.getNumberOfRecords()< Constants.MIN_RECORDS)
        {
            //System.out.println("NO SPLIT - part<minRecords");
            return null;
        }
        /** store the number of Attributes in the current partition */
        int numAttributes = currentPartition.getNumberOfAttributes();
        /** calculate the entropy of the whole data set I(D). */
        double I_D = calculateWholeDataEntropy(currentPartition);
        /** check for this entropy being 0. This will flag that all records fall
         * into 1 class value, and hence no information. No need to go any further.
         * 2nd termination condition checked.
         */
        if(I_D==0.0)
        {
            //System.out.println("NO SPLIT - zero entropy");
            //System.out.println(currentPartition);
            return null;
        }
        System.out.println("I(D): " + I_D);
        /** store the current best Ultimate Gain Ratio, its attrIndex and split value */
        double bestGainRatio = Double.NEGATIVE_INFINITY;
        int bestAttrIndex = -1;
        int splitValueIndex = -1;
        int splitValueIndexHigh = -1; //used for numerical range

        /** loop over all attributes to find the best gain ratio */
        for(int currAttrIndex = 0; currAttrIndex < numAttributes; currAttrIndex++)
        {
            /** find the number of values for this attribute */
            int attrSize =  currentPartition.getNumberOfAttributeValues(currAttrIndex);
            /** get the size of each partition for this attribute's values */
            int [] attrValueDomains = currentPartition.getPartitionSizes(currAttrIndex);

            /** if categorical attribute, and not the class attribute */
            if(currentPartition.getAttributeType(currAttrIndex)=='c' &&
                    currentPartition.getClassIndex() != currAttrIndex)
            {

                /** calculating the weighted entropy and other value is straightforward
                 * 1) calculate entropy for each partition based on an attribute value Di
                 * 2) calculate weighted entropy for value, dividing by UltimateGainRatioMultiplier
                 * 3) min weighted entropy amongst all values is selected
                 * 4) calculate gain
                 * 5) calculate split for this attribute
                 * 6) calculate gain ratio
                 * 7) calculate ultimate gain ratio
                 */

                /** individual entropy for each attribute value - unweighted */
                double entropyD_i = 0;
                /** min weighted entropy for all values */
                double weightedEntropy =Double.POSITIVE_INFINITY; //default to positive infinity as we want min

                /** 1) calculate entropy for each partition based on an attribute value Di */
                /** 2) calculate weighted entropy for each attribute value and find minimum */
                /** store values for partitions when we find a new min weighted entropy */
                int splitIndex = -1;
                int [] bestPartSizes = new int[2];
                /** looking at all attribute values */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    entropyD_i = GeneralFunctions.calculateEntropy(classSupports, attrValueDomains[attrValueIndex]);
                    //System.out.println("attr: "+ currAttrIndex + " val: " + attrValueIndex + " entD_i: " + entD_i);
                    double w_entropyD_i = calculateWeightedEntropy(attrValueDomains[attrValueIndex], entropyD_i);
                    /** check if we have a new minimum weighted entropy with current value
                     *  3) min weighted entropy amongst all values is selected
                     */
                    if(w_entropyD_i < weightedEntropy)
                    {
                        weightedEntropy = w_entropyD_i;
                        splitIndex = attrValueIndex;
                        bestPartSizes[0] = attrValueDomains[attrValueIndex]; //size for current value
                        bestPartSizes[1] = currentPartition.getNumberOfRecords()-bestPartSizes[0]; //total number of records - current value size
                    }

                }
                
                /** 4) calculate gain */
                double gain = I_D - weightedEntropy;
                /** 5) calculate split for this attribute */
                double split = calculateSplit(bestPartSizes,currentPartition.getNumberOfRecords());
                /** 6) calculate gain ratio */
                double gainRatio;
                /** need to check for divide by zero, i the case that split is 0, i.e. all records have same attribute values */
                if(split==0.0 || gain<=0.0)
                {
                    gainRatio = 0.0; //enough to set gainRatio to zero
                }
                else
                {
                    gainRatio = gain / split;
                }
                /** 7) calculate ultimate gain ratio */
                double UGRMult = this.ultimateGainRatioMultiplier(attrValueDomains[splitIndex]);
                double UGR = 0.0;
                if(UGRMult!=Double.NEGATIVE_INFINITY)
                {
                    UGR = gainRatio * UGRMult;
                }
                System.out.println("UltimateGainRatio: " + UGR + " Split" + split + " " + currAttrIndex + " " + ultimateGainRatioMultiplier(attrValueDomains[splitIndex]));
                /** check if this is a better ultimate gain ratio than previous  */
                if(UGR > bestGainRatio)
                {
                    bestGainRatio = UGR;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = splitIndex;
                }
            }
            /** if numerical attribute, and not the class attribute */
            if(currentPartition.getAttributeType(currAttrIndex)=='n' &&
                    currentPartition.getClassIndex() != currAttrIndex)
            {
                 /** calculating the weighted entropy is less straightforward
                 * 1) calculate entropy for each partition Di,j based on splitting window between i and j
                 * 2) calculate weighted entropy for each window start value.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 * 6) calculate ultimate gain ratio
                 */

                /** 1) calculate entropy for each partition Di,j based on splitting window */

                /** set current lowest weighted entropy to positive infinity */
                double weightedEntropy = Double.POSITIVE_INFINITY;
                /** store values for partitions when we find a new min weighted entropy */
                int splitIndex = -1;
                int splitHighIndex = -1;
                int [] bestPartSizes = new int[2];

                for(int windStart=0; windStart<attrSize-1; windStart++)
                {

                    /* need to get total supports for all attribute values, these values
                     * will decrease by the supports for the current attribute attribute value
                     * when we move the split value along in the current window
                     */
                    int []  outsideSupports = getTotalClassSupports(currAttrIndex,currentPartition);
                    /**
                     * these values will increase by the supports for the current attribute attribute value
                     * when we move the split value along
                     */
                    int [] windowSupports = new int[outsideSupports.length];

                    int numRecords = currentPartition.getNumberOfRecords(); //total number of records
                    int windowSize = 0; /* will increase by the domain of the current split value */
                    int outsideSize = numRecords; /* will decrease by the domain of the current split value */
                    
                    /**
                     * Starting at the current window starting index (initially
                     * the first value), we move our sliding window, adding supports
                     * for the next value in the window, to the initially empty
                     * windowSupports, at the same time these supports are removed
                     * from the outside of window supports.
                     */
                    for(int attrValueIndex = windStart; attrValueIndex<attrSize; attrValueIndex++)
                    {
                        /** update the partition info for the next split point value */
                        /** get the number of records for each class value, for this attribute value Di*/
                        int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                        int sumClassSupports = 0;
                        for(int i=0; i<classSupports.length; i++)
                        {
                            outsideSupports[i]-=classSupports[i];
                            windowSupports[i]+=classSupports[i];
                            sumClassSupports+=classSupports[i];
                        }
                        windowSize+=sumClassSupports;
                        outsideSize-=sumClassSupports;
                        /** individual entropies for each partition */
                        double [] entropyD_i = new double[2];
                        int [] partitionSizes = new int[2];
                        entropyD_i[0] = GeneralFunctions.calculateEntropy(windowSupports, windowSize);
                        entropyD_i[1] = GeneralFunctions.calculateEntropy(outsideSupports, outsideSize);
                        partitionSizes[0] = windowSize;
                        partitionSizes[1] = outsideSize;
                        /** 2) calculate weighted entropy for attribute */
                        double currWeightedEntropy = calculateWeightedEntropy(partitionSizes,
                                    entropyD_i, numRecords);

                        /** check if we have a new minimum weighted entropy */
                        if(currWeightedEntropy<weightedEntropy)
                        {
                            splitIndex = windStart; //start of window
                            splitHighIndex = attrValueIndex; //end of window
                            bestPartSizes[0] = windowSize;
                            bestPartSizes[1] = outsideSize;
                            weightedEntropy = currWeightedEntropy;
                        }


                    }
                }
                /** 3) calculate gain
                 *     Note: for numerical attribute we 'penalise' them by subtracting log2(numAttrValue -1)/|D|
                 */
                double numPenalty = (Math.log(currentPartition.getNumberOfAttributeValues(currAttrIndex)-1)/Math.log(2))
                                    /currentPartition.getNumberOfRecords();
                //System.out.println("numPenalty: " + numPenalty + " attr: " + currAttrIndex);
                double gain = I_D - weightedEntropy-numPenalty; //weightedEntropy;
                /** 4) calculate split for this attribute, note that for numerical this uses the sizes of the
                 *     partitions for the best split point, not the number of records for each value.*/
                double split = calculateSplit(bestPartSizes,currentPartition.getNumberOfRecords());
                /** 5) calculate gain ratio */
                double gainRatio;
                /** need to check for divide by zero, i the case that split is 0, i.e. all records have same attribute values */
                if(split==0.0 || gain<=0.0)
                {
                    gainRatio = 0.0; //enough to set gainRatio to zero
                }
                else
                {
                    gainRatio = gain / split;
                }
                System.out.println("GainRatio: " + gainRatio + " Split " + split + " " + currAttrIndex +  " " + splitIndex + " " + weightedEntropy);
                /** check if this is a better gain ratio */
                if(gainRatio> bestGainRatio)
                {
                    bestGainRatio = gainRatio;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = splitIndex;
                }

            }

        }
        /** now return the info about the best split point */

        /** check if the gain ratio is higher than required minimum.
         *  3rd termination condition checked.
         */
        if(bestGainRatio<=Constants.MIN_GAIN_RATIO)
        {
            /** not sufficient gain ratio to make split */
            //System.out.println("NO SPLIT - gainRatio too low: " + bestGainRatio);
            return null;
        }
        Split split;
        /** check which type of attribute we have, categorical or numerical */
        if(splitValueIndex!=-1){
          /** numerical value, need to return best gain ratio, attribute index and split value index */
          //System.out.println("Split attr: " + bestAttrIndex + " SplitValue: " +
          //       currentPartition.getAttributeValue(bestAttrIndex, splitValueIndex) + " " + bestGainRatio);
          /** HG 1.1 >>
          double [] split = new double[3];
          split[0] = bestGainRatio;
          split[1] = bestAttrIndex;
          split[2] = splitValueIndex;
          return split;
           << **/
          split = new Split(bestAttrIndex, 'n', bestGainRatio, currentPartition.getAttributeValue(bestAttrIndex, splitValueIndex),
                  splitValueIndex, -1);
          return split;
        }
        else {
          /** categorical value - just need to return gain ratio and attribute index */
          //System.out.println("Split attr: " + bestAttrIndex);
          /** HG 1.1 >>
          double [] split = new double[2];
          split[0] = bestGainRatio;
          split[1] = bestAttrIndex;
          return split;
           << */
          /** note: for see5 categorical we do not have a split value */
          split = new Split(bestAttrIndex, 'c', bestGainRatio, "", -1, -1);
          return split;
        }

        //return null; //uncreachable
    }

    /**
     * Given a partition size, calculate the Ultimate Gain Ratio muliplier.
     * Currently set to log<sub>2</sub>(log<sub>2</sub> |D<sub>i</sub>|. Where
     * |D<sub>i</sub>| represents the number of records in the attribute partition
     * being examined (range for numerical, one value for categorical).
     *
     * @param partSize number of records containing i as value for the attribute, |D<sub>i</sub>|
     * @return log<sub>2</sub>(log<sub>2</sub> |D<sub>i</sub>|
     */
    private double ultimateGainRatioMultiplier(double partSize)
    {
        double result = Math.log(partSize)/ GeneralFunctions.Log2; //log2(partSize)
        result = Math.log(result)/GeneralFunctions.Log2; //log2(log2(partSize))
        return result;
    }

    /**
     * Calculates the weighted entropy, given an entropy and associated partition size.
     * Will divide entropy by the UGR multiplier (e.g. log2(log2(value))) of partition size.
     *
     * @param partSize the size of the partition
     * @param entropy the entropy of the partition
     * @return weighted entropy for a single value (cat), or range of values(num).
     */
    private double calculateWeightedEntropy(int partSize, double entropy)
    {
        /**
         *     I(Di)
         *     -----
         *    log2log2(|Di|)
         */
        double bottom = ultimateGainRatioMultiplier(partSize);
        return entropy/bottom;
    }

}
