

package SysFor;

import java.io.File;
import java.util.*;
/**
 * Methods for building a decision tree using the C4.5 (C5.0) method. Used in
 * <code>DecisionTreeBuilder</code>.
 *
 * <p>Version 1.1 changed to store split information as a {@link Split}.</p>
 *
 * @author helengiggins
 * @version 1.1 6/1/2011
 * @see DecisionTreeBuilder
 * @see DecisionTree
 */
public class See5 {

    /* instance data */
    /** the original dataset we're building the decision tree on */
    Dataset dataset;
    /** the decision tree being built */
    DecisionTree tree;

    /**
     * Class constructor, only initializes instance data to defaults.
     */
    public See5()
    {
        dataset = null;
        tree = null;
    }

    /**
     * Class constructor passed a dataset on creation.
     *
     * @param dataset the dataset we want to build the decision tree from
     */
    public See5(Dataset dataset)
    {
        this.dataset = dataset;
    }

    /**
     * Controls the overall construction of the decision tree. Assumes a
     * dataset has already been passed using the parametric constructor.
     *
     * @return the decision tree generated from the data set using C5.0 algorithm
     */
    public DecisionTree buildTree()
    {

       /** create and store the root node for the decision tree */
       Node root = new Node();
       tree = new DecisionTree(root);
       /**
        * this method will run until the tree is built
        */
       //splitTree(dataset, tree.getRootNode());
       iterativeSplitTree(dataset, tree.getRootNode());
//       System.out.println("@@@@@@@@@@@@@ Decision Tree before Pruning @@@@@@@@@@@@@@");//commented by Gea
       /** print tree to screen */
//       System.out.println(DecisionTreeFunctions.readTreeForDisplay(tree));//commented by Gea
       /*testing */
       //int [] attrs = DecisionTreeFunctions.getSplitAttributesForTree(tree, dataset);
       //for(int i=0;  i<attrs.length; i++)
       //    System.out.println(attrs[i]);
       return tree;
    }

    /**
     * Controls the overall construction of the decision tree. Assumes the
     * dataset has not yet been passed.
     *
     * @param dataset
     * @return the decision tree generated from the data set using C5.0 algorithm
     */
    public DecisionTree buildTree(Dataset dataset)
    {
        this.dataset = dataset;
        //System.out.println("!!!!!!!!!!!!");
//        System.out.println(dataset);//commented by Gea
        return this.buildTree();
    }

    /**
     * Given a current partition of the dataset, initially the full dataset,
     * this method will calculate a split point and then create a node in the
     * decision tree for that split. It will then partition the dataset, based
     * on the split value, and continue to generate the subtrees, iteratively,
     * until no further split points can be found.
     * <p>Updated so that Datasets are written to file when not being accessed
     * directly</p>
     *
     * @param dataset the dataset we are building the tree on
     * @param rootNode the root node for the tree we're building
     */
    private void iterativeSplitTree(Dataset dataset, Node rootNode)
    {
        FileManager fileManager = new FileManager(); //used to read and write to file
        /** this flag will keep track of whether or not any split points
         * were found in the current level
         */
        boolean splitFound = false;
        int level =0; //current tree level
        /**
         * store the files of each current partitions of the dataset for the current level,
         * this will start out storing only one partition, the whole dataset
         */
        List <File> currentPartitions = new ArrayList<File>();
        File initDataset = new File("data.ds");
        fileManager.writeDatasetToFile(initDataset, dataset);
        currentPartitions.add(initDataset);
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
            /** Step 1: for each currentPartition calculate a split point 
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
            //System.out.println("Root nodes at level " + level + " " + currentRootNodes.size());
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
                //System.out.println("currPart: " + currPart);
                /* read current partition from file */
                File currFile = currentPartitions.get(currPart);
                Dataset currPartition = fileManager.readDatasetFromFile(currFile);
                //System.out.println(currFile + " " + currPartition.getNumRecords());
                currSplits[currPart]=calculateSplit(currPartition);
                //System.out.println("split calculated");
            }
            /** create two temp lists to store the files for new partitions and associated
             * tree nodes for the next level (iteration). These will be set to 
             * currentPartitions and currentRootNodes at the end of the do-while
             */
            List <File> nextLevelPartitions = new ArrayList<File>();
            List <Node> nextLevelRootNodes = new ArrayList<Node>();

            /** loop over all splits to generate the current level of the tree.
             * Note: some/all may be null
             */
            for(int splitInd=0; splitInd<numParts; splitInd++)
            {
                Split split = currSplits[splitInd];
                /* read current partition from file */
                File currFile = currentPartitions.get(splitInd);
                Dataset currentPartition = fileManager.readDatasetFromFile(currFile);

                //Dataset currentPartition = currentPartitions.get(splitInd);
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
                        String fileNameStart = level + "-" + splitInd+"-";
                        List<File> partitions = currentPartition.generateDatasetPartitionsToFile(attrIndex, "all", fileNameStart);
                        List <Node> children = new ArrayList<Node>();
                        currNode.setChildren(children); /** set children in the current root node */

                        /** store temp array of split nodes and partitions, only to be added to list
                         * for next level if we get through all partitions cleanly. That is
                         * if none of them are too small.
                         */
                        List<File> currSplitPartitions = new ArrayList<File>();
                        Node [] currSplitNodes = new Node [partitions.size()];
                        boolean nullSplit = false; //so we can check at the end of the loop if we generated a null split
                        /** create nodes in the decision tree for each partition
                         *
                         * NOTE: At this step we need to check if any partition is smaller than the
                         * user input minimum. If so, we will reset split to null, and allow
                         * the method to pass through to turning this node into a leaf node.
                         */
                        for(int currPart=0; currPart<partitions.size(); currPart++)
                        {
                            /* read current partition from file */
                            File cFile = partitions.get(currPart);
                            Dataset currDataset = fileManager.readDatasetFromFile(cFile);
                            //Dataset currDataset = partitions[currPart];
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
                            //System.out.println("size: C " + currDataset.getNumberOfRecords());
                            currSplitPartitions.add(partitions.get(currPart)); //add file name to temp list
                            currSplitNodes[currPart]=child;
                            splitFound=true; //set flag to true, so we will go to the next level
                        }
                        if(!nullSplit)
                        {
                            /** store current partition and node to temp lists for next
                             *  level
                             */
                            for(int i=0;i<partitions.size(); i++)
                            {
                                nextLevelPartitions.add(currSplitPartitions.get(i));
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
                        String fileNameStart = level + "-" + splitInd+"-";
                        List<File> partitions = currentPartition.generateDatasetPartitionsToFile(attrIndex, splitValue, fileNameStart);
                        List <Node> children = new ArrayList<Node>();
                        currNode.setChildren(children); /** set children in the passed parent node */

                        /** store temp array of split nodes and partitions, only to be added to list
                         * for next level if we get through all partitions cleanly. That is
                         * if none of them are too small.
                         */
                        List<File> currSplitPartitions = new ArrayList<File>();
                        Node [] currSplitNodes = new Node [partitions.size()];
                        boolean nullSplit = false; //so we can check at the end of the loop if we generated a null split

                        /** create nodes in the decision tree for each partition and
                         * add to list for next iteration
                         *
                         * NOTE: At this step we need to check if any leaf is smaller than the
                         * user input minimum. If so, we will reset partitions to null, and allow
                         * the method to pass through to turning this node into a leaf node.
                         */
                        for(int currPart=0; currPart<partitions.size(); currPart++)
                        {
                            /* read current partition from file */
                            File cFile = partitions.get(currPart);
                            Dataset currDataset = fileManager.readDatasetFromFile(cFile);
                            //Dataset currDataset = partitions[currPart];
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
                            //currSplitPartitions[currPart]=currDataset;
                            //System.out.println("size: N " + currDataset.getNumberOfRecords());
                            currSplitPartitions.add(partitions.get(currPart)); //add file name to temp list
                            currSplitNodes[currPart]=child;
                            splitFound=true; //set flag to true, so we will go to the next level
                        }
                        if(!nullSplit)
                        {
                            /** store current partition and node to temp lists for next
                             *  level
                             */
                            for(int i=0;i<partitions.size(); i++)
                            {
                                nextLevelPartitions.add(currSplitPartitions.get(i));
                                //nextLevelPartitions.add(currSplitPartitions[i]);
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
     * Given a current partition of the dataset, initially the full dataset,
     * this method will calculate a split point and then create a node in the
     * decision tree for that split. It will then partition the dataset, based
     * on the split value, and call the method recursively to generate the subtree.
     *
     * @param currentPartition current partition of the dataset
     * @param currNode the node we are building the subtree from, initially the root node
     */
    private void splitTree(Dataset currentPartition, Node currNode)
    {
        //System.out.println("~~~~~~~~~~~~~~~~~Start splitTree " + currNode + " \n" + currentPartition.getNumRecords());
        /** this method will recursively call itself on the partitions generated
         * by splitting the dataset on an attribute value.
         */

        /* generate a split on the current partition of the dataset, initially
         * the whole dataset
         */
        //System.out.println("START SPLIT_TREE  &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        //System.out.println("current node: " + currNode);
        //System.out.println("currPartition: \n" + currentPartition);
        
        //double [] split =  calculateSplit(currentPartition); //HG 1.1
        Split split = calculateSplit(currentPartition);
        //System.out.println("Split: " + split);
        /* check if a split was made */
        if(split!=null)
        {
            /** generate the dataset partitions created by the split, and call
             * the method recursively on them
             */
             //System.out.println("GainRatio: " + split[0]);
            /** check which type of attribute */
            if(split.getAttrType()=='c')
            {
                /** categorical attribute */
                int attrIndex = split.getAttrIndex();
                Dataset [] partitions = currentPartition.generateDatasetPartitions(attrIndex, "all");
                List <Node> children = new ArrayList<Node>();
                currNode.setChildren(children); /** set children in the passed parent node */
                /** create nodes in the decision tree for each partition and
                 * make recursive call to split the child nodes
                 *
                 * NOTE: At this step we need to check if any partition is smaller than the
                 * user input minimum. If so, we will reset split to null, and allow
                 * the method to pass through to turning this node into a leaf node.
                 */
                for(int currPart=0; currPart<partitions.length; currPart++)
                {
                    //System.out.println("C  currPart: " + currPart + " level: " + currNode.getLevel());
                    Dataset currDataset = partitions[currPart];
                    //System.out.println("numRec: " + currDataset.getNumRecords());
                    /** check for partition too small -see note above */
                    if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                    {
                        split=null;
                        //System.out.println( " Leaf currNode: "  + currNode );
                        currNode.setChildren(null);
                        break;
                    }
                    Node child = new Node();
                    String attrName = currDataset.getAttributeName(attrIndex);
                    /** current index attribute value is the split value */
                    String splitValue = currDataset.getAttributeValue(attrIndex, currPart);
                    child.setData(attrName, attrIndex, splitValue,'c',null, currNode);
                    /** add to children list that was set in currNode */
                    children.add(child);
                    //System.out.println("split child: " + child);
                    splitTree(currDataset, child);
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
                /** create nodes in the decision tree for each partition and
                 * make recursive call to split the child nodes
                 *
                 * NOTE: At this step we need to check if any leaf is smaller than the
                 * user input minimum. If so, we will reset partitions to null, and allow
                 * the method to pass through to turning this node into a leaf node.
                 */
                for(int currPart=0; currPart<partitions.length; currPart++)
                {
                    Dataset currDataset = partitions[currPart];
                    //System.out.println("N  currPart: " + currPart + " level: " + currNode.getLevel());
                    //System.out.println("numRec: " + currDataset.getNumRecords());
                    //System.out.println("numRec: " + currDataset.getNumRecords());
                    /** check for partition too small -see note above */
                    if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                    {
                        split=null;
                        //System.out.println( " Leaf currNode: "  + currNode );
                        currNode.setChildren(null);
                        break;
                    }
                    Node child = new Node();
                    String attrName = currDataset.getAttributeName(attrIndex);
                    /**  */
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
                    //System.out.println("split child: " + child);
                    splitTree(currDataset, child);
                }
            }
        }
        if(split == null) /** no split made, need to change current tree node to be a leaf */
        {
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
            else
            {
               // System.out.println("Help!!!!");
            }
        }
        
        //System.out.println("end~~~~~~~~" + currNode);
    }

    /**
     * Given a current partition of the dataset calculates the split value.
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
        //System.out.println("I(D): " + I_D);
        /** store the current best Gain Ratio, its attrIndex and split value (for numerical) */
        double bestGainRatio = Double.NEGATIVE_INFINITY;
        int bestAttrIndex = -1;
        int splitValueIndex = -1;

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
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */
                /** individual entropies for each attribute value - unweighted */
                double [] entropyD_i = new double[attrSize];

                /** 1)calculate entropy for each partition based on an attribute value Di */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    double entD_i = GeneralFunctions.calculateEntropy(classSupports, attrValueDomains[attrValueIndex]);
                    //System.out.println("attr: "+ currAttrIndex + " val: " + attrValueIndex + " entD_i: " + entD_i);
                    entropyD_i[attrValueIndex] = entD_i;
                    

                }
                /** 2) calculate weighted entropy for attribute */
                double weightedEntropy = calculateWeightedEntropy(attrValueDomains,
                            entropyD_i, currentPartition.getNumberOfRecords());
                /** 3) calculate gain */
                double gain = I_D - weightedEntropy;
                /** 4) calculate split for this attribute */
                double split = calculateSplit(attrValueDomains,currentPartition.getNumberOfRecords());
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
                //System.out.println("GainRatio: " + gainRatio + " Split" + split + " " + currAttrIndex);
                /** check if this is a better gain ratio */
                if(gainRatio> bestGainRatio)
                {
                    bestGainRatio = gainRatio;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = -1; //will indicate its a categorical value
                }
            }
            /** if numerical attribute, and not the class attribute */
            if(currentPartition.getAttributeType(currAttrIndex)=='n' &&
                    currentPartition.getClassIndex() != currAttrIndex)
            {
                 /** calculating the weighted entropy is less straightforward
                 * 1) calculate entropy for each partition Di based on splitting point
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */

                /** 1) calculate entropy for each partition Di based on splitting point */

                /* need to get total supports for all attribute values, these values
                 * will decrease by the supports for the current attribute attribute value
                 * when we move the split value along
                 */
                int []  part2Supports = getTotalClassSupports(currAttrIndex,currentPartition);
                /** 
                 * these values will increase by the supports for the current attribute attribute value
                 * when we move the split value along 
                 */
                int [] part1Supports = new int[part2Supports.length];

                int numRecords = currentPartition.getNumberOfRecords(); //total number of records
                int currPart1Size = 0; /* will increase by the domain of the current split value */
                int currPart2Size = numRecords; /* will decrease by the domain of the current split value */
                /** set current lowest weighted entropy to positive infinity */
                double weightedEntropy = Double.POSITIVE_INFINITY;
                /** store values for partitions when we find a new min weighted entropy */
                int splitIndex = -1;
                int [] bestPartSizes = new int[2];
                /** looping over all attribute values for current attribute */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** update the partition info for the next split point value */
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    int sumClassSupports = 0;
                    for(int i=0; i<classSupports.length; i++)
                    {
                        part2Supports[i]-=classSupports[i];
                        part1Supports[i]+=classSupports[i];
                        sumClassSupports+=classSupports[i];
                    }
                    currPart1Size+=sumClassSupports;
                    currPart2Size-=sumClassSupports;
                    /** individual entropies for each partition */
                    double [] entropyD_i = new double[2];
                    int [] partitionSizes = new int[2];
                    entropyD_i[0] = GeneralFunctions.calculateEntropy(part1Supports, currPart1Size);
                    entropyD_i[1] = GeneralFunctions.calculateEntropy(part2Supports, currPart2Size);
                    partitionSizes[0] = currPart1Size;
                    partitionSizes[1] = currPart2Size;
                    /** 2) calculate weighted entropy for attribute */
                    double currWeightedEntropy = calculateWeightedEntropy(partitionSizes,
                                entropyD_i, numRecords);
                    /** check if we have a new minimum weighted entropy */
                    if(currWeightedEntropy<weightedEntropy)
                    {
                        splitIndex = attrValueIndex;
                        bestPartSizes[0] = currPart1Size;
                        bestPartSizes[1] = currPart2Size;
                        weightedEntropy = currWeightedEntropy;
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
                //System.out.println("GainRatio: " + gainRatio + " Split " + split + " " + currAttrIndex +  " " + splitIndex + " " + weightedEntropy);
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
     * Calculates the entropy of the whole data set. That is, <em>I(D)</em>.
     *
     * @return the entropy of the whole data set
     */
    protected double calculateWholeDataEntropy(Dataset currentPartition)
    {
        /** get the counts for how many records each class appears in */
       int [] totalSupports = currentPartition.getTotalClassSupports();
       /** calculate entropy by passing these freqs, and the total number of records.
        * Each prob will be calculated inside the method.
        */
       return GeneralFunctions.calculateEntropy(totalSupports, currentPartition.getNumberOfRecords());
    }

    /**
     * Calculates the weighted entropy, given a set of entropies, associated partition sizes, and
     * total dataset size.
     *
     * @param partSizes the size of each partition
     * @param entropies the entropy of each partition
     * @param totalSize total number of records in dataset
     * @return weighted entropy
     */
    protected double calculateWeightedEntropy(int [] partSizes, double [] entropies, int totalSize)
    {
        double weightedEntropy = 0.0;
        for(int i=0; i<partSizes.length; i++)
        {
            //System.out.println("partSize[i] " + partSizes[i] + " entropies[i] " + entropies[i] + " totalsize " + totalSize);
            /** check for divide by zero */
            if(partSizes[i]>0){
                weightedEntropy+= (partSizes[i]/(totalSize*1.0)) * entropies[i];

            }
        }
        //System.out.println("   weightedEntropy: " + weightedEntropy);
        return weightedEntropy;
    }

    /**
     * Given the partition sizes and total number of records in the current dataset,
     * calculate <code>split(D,T)</code>.
     * 
     * @param partSizes the sizes of each <code>D<subi</sub></code>
     * @param totalSize the total number of records
     * @return <code>split(D,T)</code>
     */
    protected double calculateSplit(int [] partSizes, int totalSize)
    {
        /** can calc using an entropy function */
        /*int x = 0;
        for(int i=0; i<partSizes.length; i++)
        {
           x+=partSizes[i];
           if(partSizes[i]==totalSize)
               System.out.println("All in one partition");
        }*/

         //System.out.println("part sum"  + x);
        double split = GeneralFunctions.calculateEntropy(partSizes, totalSize);
        //System.out.println("   split: " + split);
        return split;
    }

    /**
     * For a given attribute, sum the class supports for all attribute values.
     * This method is used in calculating split points for numerical attributes.
     *
     * @param attrIndex the attribute index
     * @param currentPartition current working dataset
     * @return the total class supports for all attribute values
     */
    protected int [] getTotalClassSupports(int attrIndex, Dataset currentPartition)
    {
        /** get number of class values */
        int numClasses = currentPartition.getNumberOfAttributeValues(currentPartition.getClassIndex());
        int [] supportTotals = new int[numClasses];
        int numAttrValues = currentPartition.getNumberOfAttributeValues(attrIndex);
        for(int i=0; i<numAttrValues; i++)
        {
            /** get the number of records for each class value, for current attribute value*/
            int [] classSupports = currentPartition.getAttrValueClassSupports(attrIndex, i);
            /** add these to the total sums for each class value */
            for(int j=0; j<numClasses; j++)
            {
                supportTotals[j]+=classSupports[j];
            }
        }
        return supportTotals;
    }

    /**
     * Return the class supports for a numerical split around an attribute index. The lower split
     * is for values less than or equal to valueIndex. Higher split is for values after these.
     *
     * @param attrIndex the attribute index
     * @param currentPartition the current dataset
     * @param valueIndex the index for the given attribute value
     * @param lower true if lower partition, false if higher partition
     * @return class supports for partition
     */
    protected int [] getClassSupportsForSplit(int attrIndex, Dataset currentPartition, int valueIndex, boolean lower)
    {
        if(valueIndex==-1)//error checking
        {
            System.out.println("----------ERROR: Value index -1");
        }
        /** check which side of the split <= or > valueIndex */
        if(lower)
        {
            /** lower partition <=valueIndex */
            /** get number of class values */
            int numClasses = currentPartition.getNumberOfAttributeValues(currentPartition.getClassIndex());
            int [] supportTotals = new int[numClasses];
            /** loop from first value up to and including valueIndex */
            for(int i=0; i<=valueIndex; i++)
            {
                /** get the number of records for each class value, for current attribute value*/
                int [] classSupports = currentPartition.getAttrValueClassSupports(attrIndex, i);
                /** add these to the total sums for each class value */
                for(int j=0; j<numClasses; j++)
                {
                    supportTotals[j]+=classSupports[j];
                }
            }
            return supportTotals;
        }
        else
        {
            /** higher partition >valueIndex */
            /** get number of class values */
            int numClasses = currentPartition.getNumberOfAttributeValues(currentPartition.getClassIndex());
            int [] supportTotals = new int[numClasses];
            int numAttrValues = currentPartition.getNumberOfAttributeValues(attrIndex);
            /** loop from value after the attrIndex to the end of values */
            for(int i=valueIndex; i<numAttrValues; i++)
            {
                /** get the number of records for each class value, for current attribute value*/
                int [] classSupports = currentPartition.getAttrValueClassSupports(attrIndex, i);
                /** add these to the total sums for each class value */
                for(int j=0; j<numClasses; j++)
                {
                    supportTotals[j]+=classSupports[j];
                }
            }
            return supportTotals;
        }
    }

    /**
     * Using the initial dataset, find up to the required number of alternate decision
     * trees. That is, alternate test attribute (with slightly lower GR).
     *
     * @return the trees for the multiple tree algorithm. Note
     */
    public DecisionTree [] generateMultipleTrees(Dataset dataset)
    {
        DecisionTree [] multTrees = new DecisionTree[Constants.NUM_TREES];

        Node currTreeStub = null;
        int currNumTree = 0; //keep track of how many new trees we've created so far.
        double currGRDiff = 0; //keep track of the current difference between best GR and test GR
        int treeLevel = 0; //which level of the tree are we currently building the stub at
        Dataset currentPartition = dataset; //set current partition to the initial dataset

        /** Start at the first level of the tree and see how many candidate trees we can build */
       
        int numToGenerate = Constants.NUM_TREES-currNumTree; //how many do we need to generate
        //System.out.println("numToGenerate:" + numToGenerate);

        /** find the best split points, that are within the user defined threshold for gain */
        Split [] topSplits = this.getTopSplitsForCurrentPartition(currentPartition,numToGenerate);
        /** now loop over the new candidate splits to generate new trees */
        /*
         * Added by Gea
         */
        if(topSplits==null)return multTrees;

        /*
         * End of addition
         */
        DecisionTree [] firstTrees = new DecisionTree[topSplits.length]; /* store these first gen tree, so we can build second level trees from these */
        for(int currSplitInd =0; currSplitInd<topSplits.length; currSplitInd++)
        {

           /** create and store the root node for the decision tree */
           multTrees[currNumTree] = new DecisionTree(new Node());
           firstTrees[currNumTree] = multTrees[currNumTree];//store a ref to these trees for next step
           //System.out.println(topSplits[currSplitInd].toString());
           createTreeFromSplit(currentPartition,topSplits[currSplitInd], multTrees[currNumTree].getRootNode());
           //System.out.println("############## Level " + treeLevel + " Tree " + (currNumTree+1));
           //System.out.println(DecisionTreeFunctions.readTreeForDisplay(multTrees[currNumTree]));
           /** increment tree count */
           currNumTree ++;
           numToGenerate--;
        }
        treeLevel++;

        /** looping over the first level trees, generate as many as needed at second level */
        for(int currFirstTree=0; currFirstTree<firstTrees.length; currFirstTree++)
        {
            /** Now need to check if we need to look at level 1 */
            if(numToGenerate>0)
            {
                //System.out.println("numToGenerate:" + numToGenerate);
                /** need to find the record partitions for the next round, based on
                 * the first split in the best tree from the tree generated at level 0.
                 */
                /** find split for the root node */
                currTreeStub = firstTrees[currFirstTree].getRootNode();//store the next best first tree root node
                /** check if the root node had a split point */
                if(currTreeStub.hasChildren()){
                    Node firstChildOfRoot = currTreeStub.getChildren().get(0);
                    char attrType = firstChildOfRoot.getAttrType();
                    int attrIndex = firstChildOfRoot.getAttrIndex();
                    StringTokenizer tokens = new StringTokenizer(firstChildOfRoot.getSplitValue(), "<>=");
                    String rawSplitValue = tokens.nextToken();
                    String splitValueIndex = String.valueOf(currentPartition.getAttrValueIndex(attrIndex, rawSplitValue));
                    //System.out.println("@@@ Root Split point - attrType: " + attrType + " attrIndex:" + attrIndex +
                    //        " splitValueIndex:" + splitValueIndex + " splitValue: " + firstChildOfRoot.getSplitValue() + " AttrName: "+ firstChildOfRoot.getAttrName() );

                    /** need to get partitions slightly differently for numerical and categorical attributes */
                    Dataset [] partitions = new Dataset[0];
                    if(attrType == 'c')
                    {
                        partitions= currentPartition.generateDatasetPartitions(attrIndex, "all");
                    }
                    else if(attrType == 'n')
                    {
                        partitions = currentPartition.generateDatasetPartitions(attrIndex, splitValueIndex);
                    }
                    /** for each partition, find the best splits, note the length may differ for each array */
                    Split [][] bestSplitsArray = new Split[partitions.length][];
                    for(int currPartInd = 0; currPartInd<partitions.length; currPartInd++)
                    {
                        //System.out.println("numRec: " + partitions[currPartInd].getNumberOfRecords());
                        bestSplitsArray[currPartInd] = getTopSplitsForCurrentPartition(partitions[currPartInd], numToGenerate);
                        if(bestSplitsArray[currPartInd]!=null){
                          //System.out.println(" bestSplitArray["+ currPartInd+"] " + bestSplitsArray[currPartInd].length);
                        }

                    }
                    /** now determine, from number of splits for each partition, how many trees to generate*/
                    int numTreesSecLevel = determineNumberOfTreesForSecondLevel(bestSplitsArray, partitions, dataset.getNumberOfRecords());
                    //System.out.println("num trees to generate at level 1: " + numTreesSecLevel);
                    /** generate the set of sub-trees from each partition */
                    Node [][] subTreeRoots = generateSubTreesAtSecondLevel(bestSplitsArray, partitions, numTreesSecLevel);

                    /** Now for each set of subtrees, we need to build into a tree
                     * Note: need to examine via y axis, rather than x axis, to get the tree number
                     */
                    for(int treeNum=0; treeNum<subTreeRoots[0].length; treeNum++)
                    {
                        Node newRoot = new Node(); //create a root node for the new tree
                        List<Node> rootChildren = new ArrayList<Node>(); //will add subtrees as we go to this list
                         /** for each partition from the original split point, tree root */
                        for(int partNum=0; partNum<subTreeRoots.length; partNum++)
                        {

                            /** get the info about the Split for the root node of the best tree, which
                             * this tree was based on
                             */
                            Node currSubTreeRoot = subTreeRoots[partNum][treeNum]; //root of current subtree

                            /** for the original level 0 tree, on which this tree is based, we need to find the split point
                             * from the root node, to the current subtree (at level 1).
                             */
                            Node childOfCurrTreeStub = currTreeStub.getChildren().get(partNum);
                            String splitAttrName = childOfCurrTreeStub.getAttrName();
                            int splitAttrIndex = childOfCurrTreeStub.getAttrIndex();
                            String splitValue = childOfCurrTreeStub.getSplitValue();
                            char splitAttrType = childOfCurrTreeStub.getAttrType();
                            //System.out.println("Tree:" + (treeNum+1) + " Part:" + partNum +
                            //        " " + subTreeRoots[partNum][treeNum].getSplitAttribute());
                            /** check if the subTree needs to be a leaf node */
                            if(!currSubTreeRoot.hasChildren())
                            {
                                //System.out.println("Leaf!");
                                /** need to check in case we have not been able to make a split at root node */
                                if(splitValue!=null)
                                {
                                    /** remove first char to get split type <= or > */
                                    char splitType = splitValue.charAt(0);
                                    StringTokenizer tokens2 = new StringTokenizer(splitValue, "<>=");
                                    String trueSplitValue = tokens2.nextToken();
                                    int valueIndex = -1;
                                    //System.out.println("LEAF: " + attrIndex + " "+ splitType + " " + splitValue);
                                    //System.out.println(currentPartition);
                                    int [] classSupports = new int[0];
                                    int classIndex = currentPartition.getClassIndex(); //class attribute index
                                    String className = currentPartition.getAttributeName(classIndex);
                                    String [] classValues = currentPartition.getAllAttrValues(classIndex);
                                    if(splitAttrType=='n')
                                    {
                                        if(splitType=='<'){
                                            //System.out.println("no Split: n <");
                                            valueIndex = currentPartition.getAttrValueIndex(attrIndex, trueSplitValue);
                                           classSupports = getClassSupportsForSplit(attrIndex, currentPartition,valueIndex, true);
                                        }
                                        if(splitType=='>'){
                                            //Note: splitValue will not appear in the higher partition, so need to find index
                                            //of the first value higher than it.
                                           valueIndex = currentPartition.getAttrValueIndexPlusOne(attrIndex, trueSplitValue);
                                           classSupports = getClassSupportsForSplit(attrIndex, currentPartition,valueIndex, false); //note removed +1
                                        }

                                    }
                                    if(splitAttrType=='c')
                                    {
                                        valueIndex = currentPartition.getAttrValueIndex(attrIndex, splitValue);
                                        classSupports = currentPartition.getAttrValueClassSupports(attrIndex, valueIndex);
                                    }
                                    /** create the leafNode and copy it's previous info over */
                                    LeafNode newLeaf = new LeafNode(classSupports, classValues, className, classIndex);
                                    newLeaf.setData(splitAttrName, splitAttrIndex, splitValue, splitAttrType, null, newRoot);
                                    rootChildren.add(newLeaf);
                                }
                            }
                            else
                            {
                                /** set split info for root into current subtree */
                               currSubTreeRoot.setData(splitAttrName, splitAttrIndex, splitValue,
                               splitAttrType, currSubTreeRoot.getChildren(), newRoot);
                               /** add this node to children list for root node */
                               rootChildren.add(currSubTreeRoot);
                            }

                        }
                        /** set children for root node, and set as root node for a new DecisionTree */
                        newRoot.setChildren(rootChildren);
                        multTrees[currNumTree] = new DecisionTree(newRoot);
                        /** print tree to screen */
                        //System.out.println("############## Level " + treeLevel + " BestNum " + currFirstTree + " Tree " + (currNumTree+1));
                        //System.out.println(DecisionTreeFunctions.readTreeForDisplay(multTrees[currNumTree]));
                        /** increment tree count */
                        currNumTree ++;
                        numToGenerate--;
                    }
                }
            }
        }
        return multTrees;
    }

    /**
     * Given the set of best split points for each partition at the second level of the tree, generate
     * the subtrees from these partitions. We do not generate a tree from the first set of splits
     * as these will give us the same tree generated at level 0. If we have an uneven
     * number of splits for different partitions, we revert to the best split for
     * any partition that has no more splits to go to.
     *
     * @param bestSplitsArray x axis is the partition number, y is the order from best to worst.
     * @param partitions the partitions of the dataset
     * @param numTreesSecLevel max number of trees to generate
     * @return the subtree root nodes. x axis is partition number, y axis is tree number
     */
    private Node [][] generateSubTreesAtSecondLevel (Split [][] bestSplitsArray,
            Dataset [] partitions, int numTreesSecLevel)
    {
        //System.out.println("partitions.length: " + partitions.length + " " + numTreesSecLevel);
        Node [][] subTreeRoots = new Node [partitions.length][numTreesSecLevel];
        /** loop each subtree/partition, and over number of trees to be generated
         */
        for(int partNum=0; partNum<partitions.length; partNum++)
        {
            /** need to check if any splits were generated for this partition */
            if(bestSplitsArray[partNum]!=null)
            {
                int numSplits=0;
                /** check if we have more than 1 split, if only 1, we've 
                 * already generated a tree with this split 
                 */
                if(bestSplitsArray[partNum].length>1)
                {
                    numSplits = bestSplitsArray[partNum].length-1;
                }
                 ///System.out.println("numSplits:" + numSplits);
                 /** check if this partition has enough splits to generate all subtrees */
                 if(numSplits>=numTreesSecLevel)
                 {
                    /** looping over the number that need to be generate
                     * start loop at 1, so we do not look at the first splits
                     */
                    for(int subTreeNum=1; subTreeNum<=numTreesSecLevel; subTreeNum++)
                    {
                       Node currRoot= new Node();
                        //System.out.println("Enough   partNum:" + partNum + " subTreeNum:" + subTreeNum
                        //    + " " + bestSplitsArray[partNum][subTreeNum]);
                        subTreeRoots[partNum][subTreeNum-1]=currRoot; //store the root of the subtree
                        //generate subtree from the current split
                        createTreeFromSplit(partitions[partNum],
                                bestSplitsArray[partNum][subTreeNum], currRoot);
                        //System.out.println(currRoot.getSplitAttribute());
                    }
                }
                /** we don't have enough splits, some will need to be reverted to
                 * the original best split for this partition
                 */
                else
                {
                    /** loop over those we do have, and then generate the rest as leaf nodes */
                    int numLeaves = numTreesSecLevel - numSplits;
                    //System.out.println("numLeaves: " + numSplits);
                    for(int splitNum=1; splitNum<=numSplits; splitNum++)
                    {
                        Node currRoot= new Node();
                        subTreeRoots[partNum][splitNum-1]=currRoot; //store the root of the subtree
                        //generate subtree from the current split
                        createTreeFromSplit(partitions[partNum],
                                bestSplitsArray[partNum][splitNum], currRoot);
                        //System.out.println(currRoot.getSplitAttribute());
                    }
                    /** revert to the original best node */
                    for(int leafNum =0; leafNum<numLeaves; leafNum++)
                    {
                        Node currRoot= new Node();
                        subTreeRoots[partNum][leafNum+numSplits]=currRoot; //store the root of the subtree
                        //generate subtree from the best split instead
                        createTreeFromSplit(partitions[partNum],
                                bestSplitsArray[partNum][0], currRoot);
                        
                    }

                    
                }
            }
            else //no splits, need to make a leaf node
            {
                /** return an empty node, which will be turned into a leaf at calling method */
                for(int subTreeNum=0; subTreeNum<numTreesSecLevel; subTreeNum++)
                {
                    Node currRoot= new Node();
                    subTreeRoots[partNum][subTreeNum]=currRoot; //store the root of the subtree
                }
            }
        }
        return subTreeRoots;
    }

    /**
     * Based on the number of records in each partition, and the number of splits
     * for that partition, we decide how many trees to generate at the second level
     * (level 1). Note we ignore the first split for each partition in the calculation,
     * since these splits are the same as for the original tree generated at level 0.
     * <p>Formula is: sum(for each partition) (numRecInPartition*numSplits)/totalRecords</p>
     *
     * @param bestSplits the best splits for each partition, in 2D array format
     * @param partitions the partitions of the dataset, based on the split at root
     * @param numRecords the number of records in the dataset
     * @return
     */
    private int determineNumberOfTreesForSecondLevel(Split [][] bestSplits, Dataset [] partitions, int numRecords)
    {
        /** find size of each partition, that is, number of records, as well as
         * the number of candidate splits for each partition, multiply them, and add
         * to running total. When finished, divide this by the total number of records.
         * Round result to get number of trees to be generated at level 1.
         */
        double sum = 0.0;
        for(int currPart =0; currPart<partitions.length; currPart++)
        {
            int recInCurrPart = partitions[currPart].getNumberOfRecords();
            int numSplits=0;
            /** need to check if we have more than 1 Split */
            if(bestSplits[currPart]!=null && bestSplits[currPart].length>1)
             numSplits= bestSplits[currPart].length-1; //subtract 1 to remove the best split
            sum+=recInCurrPart*numSplits;
            //System.out.println("sum: " + recInCurrPart + " " + numRecords);
        }
        double numTrees = sum/numRecords;
        return (int)Math.floor(numTrees);
    }

    /**
     * Will build a subtree from the current node, based on the {@link Split}.
     * This method makes a recursive call to SplitTree method, for each child node
     * generated from the initial split.
     *
     * @param currentPartition the current partition of records
     * @param split the split point
     * @param currNode the root node for the subtree
     */
    public void createTreeFromSplit(Dataset currentPartition, Split split, Node currNode)
    {
        //System.out.println(currentPartition.getNumberOfRecords() + " " +  split);
        /* check if a split was made */
        if(split!=null)
        {
            /** generate the dataset partitions created by the split, and call
             * the method recursively on them
             */
      
            /** check which type of attribute */
            if(split.getAttrType()=='c')
            {
                //System.out.println("cat");
                /** categorical attribute */
                int attrIndex = split.getAttrIndex();
                Dataset [] partitions = currentPartition.generateDatasetPartitions(attrIndex, "all");
                List <Node> children = new ArrayList<Node>();
                currNode.setChildren(children); /** set children in the passed parent node */
                /** create nodes in the decision tree for each partition and
                 * make recursive call to split the child nodes
                 *
                 * NOTE: At this step we need to check if any partition is smaller than the
                 * user input minimum. If so, we will reset split to null, and allow
                 * the method to pass through to turning this node into a leaf node.
                 */
                for(int currPart=0; currPart<partitions.length; currPart++)
                {
                    Dataset currDataset = partitions[currPart];
                    //System.out.println("numRec: " + currDataset.getNumRecords());
                    /** check for partition too small -see note above */
                    if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                    {
                        split=null;
                        currNode.setChildren(null);
                        break;
                    }
                    Node child = new Node();
                    String attrName = currDataset.getAttributeName(attrIndex);
                    /** current index attribute value is the split value */
                    String splitValue = currDataset.getAttributeValue(attrIndex, currPart);
                    child.setData(attrName, attrIndex, splitValue,'c',null, currNode);
                    /** add to children list that was set in currNode */
                    children.add(child);
                    iterativeSplitTree(currDataset, child);
                }

            }
             /** note need to also check for null here, in case split was reset in categorical part,
              * for when a partition will be too small
              */
            if(split!=null && split.getAttrType()=='n')
            {
                //System.out.println("num");
                /** numerical attribute */
                int attrIndex = split.getAttrIndex();
                String splitValue = String.valueOf(split.getValueIndex()); //attribute value index
                /** pass the attribute index, and the split value index and
                 * generate the partitions of the dataset
                 */
                Dataset [] partitions = currentPartition.generateDatasetPartitions(attrIndex, splitValue);
                List <Node> children = new ArrayList<Node>();
                currNode.setChildren(children); /** set children in the passed parent node */
                /** create nodes in the decision tree for each partition and
                 * make recursive call to split the child nodes
                 *
                 * NOTE: At this step we need to check if any leaf is smaller than the
                 * user input minimum. If so, we will reset partitions to null, and allow
                 * the method to pass through to turning this node into a leaf node.
                 */
                for(int currPart=0; currPart<partitions.length; currPart++)
                {
                    Dataset currDataset = partitions[currPart];
                    //System.out.println("numRec: " + currDataset.getNumRecords());
                    /** check for partition too small -see note above */
                    if(currDataset.getNumRecords()<Constants.MIN_RECORDS)
                    {
                        //System.out.println("currDataSize: " + currDataset.getNumRecords());
                        split=null;
                        currNode.setChildren(null);
                        break;
                    }
                    Node child = new Node();
                    String attrName = currDataset.getAttributeName(attrIndex);
                    /**  */
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
                    iterativeSplitTree(currDataset, child);
                }
            }
        }
        if(split == null) /** no split made, need to change current tree node to be a leaf */
        {

            //System.out.println("SPLIT null");
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
    }

    /**
     * Given a partition of the dataset, find the best gain ratios, in order. Note,
     * for numerical attributes we need to make sure that the potential good split point
     * is not too close to another good/best split point. Used in our multiple
     * tree method.
     *
     * @param currentPartition a dataset partition
     * @param numSplits
     * @return the top GainRatio split points, up to numSplits of them, ordered from highest
     * at position 0, to lowest
     */
    private Split [] getTopSplitsForCurrentPartition(Dataset currentPartition, int numSplits)
    {
        /** while finding the best GR, also store any GR within the threshold away from current best */
        ArrayList<Split> candidateSplits = new ArrayList<Split>();

        int currNumSplits = 0; //keep track of how many splits currently stored

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
        //System.out.println("I(D): " + I_D);
        /** store the current best Gain Ratio, its attrIndex and split value (for numerical) */
        double bestGainRatio = Double.NEGATIVE_INFINITY;
        int bestAttrIndex = -1;
        int splitValueIndex = -1;

        /** loop over all attributes to find the gain ratios */
        for(int currAttrIndex = 0; currAttrIndex < numAttributes; currAttrIndex++)
        {
            //System.out.println("------currAttrIndex: " + currAttrIndex);
            Split currSplit;
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
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */
                /** individual entropies for each attribute value - unweighted */
                double [] entropyD_i = new double[attrSize];

                /** 1)calculate entropy for each partition based on an attribute value Di */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    double entD_i = GeneralFunctions.calculateEntropy(classSupports, attrValueDomains[attrValueIndex]);
                    //System.out.println("attr: "+ currAttrIndex + " val: " + attrValueIndex + " entD_i: " + entD_i);
                    entropyD_i[attrValueIndex] = entD_i;


                }
                /** 2) calculate weighted entropy for attribute */
                double weightedEntropy = calculateWeightedEntropy(attrValueDomains,
                            entropyD_i, currentPartition.getNumberOfRecords());
                /** 3) calculate gain */
                double gain = I_D - weightedEntropy;
                /** 4) calculate split for this attribute */
                double split = calculateSplit(attrValueDomains,currentPartition.getNumberOfRecords());
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
                //System.out.println("GainRatio: " + gainRatio + " " + currAttrIndex);
                /** check if this is a better gain ratio */
                if(gainRatio> bestGainRatio)
                {
                    bestGainRatio = gainRatio;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = -1; //will indicate its a categorical value
                }
                /** store this split value if within threshold of current best gain
                 * and higher than minimum gain ratio
                 */
                if(Math.abs(bestGainRatio-gainRatio)<Constants.MT_THRESHOLD & gainRatio>Constants.MIN_GAIN_RATIO)
                {
                   currSplit = new Split(currAttrIndex, 'c', gainRatio, "", -1, -1);
                   candidateSplits.add(currSplit);  
                }

            }
            /** if numerical attribute, and not the class attribute */
            if(currentPartition.getAttributeType(currAttrIndex)=='n' &&
                    currentPartition.getClassIndex() != currAttrIndex)
            {
                 /** calculating the weighted entropy is less straightforward
                 * 1) calculate entropy for each partition Di based on splitting point
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */
                /** need to store all numerical splits and then we will see if they are
                 * good enough to use
                 */
                 ArrayList<Split> candNumSplits = new ArrayList<Split>();
                /** 1) calculate entropy for each partition Di based on splitting point */

                /* need to get total supports for all attribute values, these values
                 * will decrease by the supports for the current attribute attribute value
                 * when we move the split value along
                 */
                int []  part2Supports = getTotalClassSupports(currAttrIndex,currentPartition);
                /**
                 * these values will increase by the supports for the current attribute attribute value
                 * when we move the split value along
                 */
                int [] part1Supports = new int[part2Supports.length];

                int numRecords = currentPartition.getNumberOfRecords(); //total number of records
                int currPart1Size = 0; /* will increase by the domain of the current split value */
                int currPart2Size = numRecords; /* will decrease by the domain of the current split value */
                /** set current lowest weighted entropy to positive infinity */
                double weightedEntropy = Double.POSITIVE_INFINITY;
                /** store values for partitions when we find a new min weighted entropy */
                int splitIndex = -1;
                int [] bestPartSizes = new int[2];
                /** looping over all attribute values for current attribute */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** update the partition info for the next split point value */
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    int sumClassSupports = 0;
                    for(int i=0; i<classSupports.length; i++)
                    {
                        part2Supports[i]-=classSupports[i];
                        part1Supports[i]+=classSupports[i];
                        sumClassSupports+=classSupports[i];
                    }
                    currPart1Size+=sumClassSupports;
                    currPart2Size-=sumClassSupports;
                    /** individual entropies for each partition */
                    double [] entropyD_i = new double[2];
                    int [] partitionSizes = new int[2];
                    entropyD_i[0] = GeneralFunctions.calculateEntropy(part1Supports, currPart1Size);
                    entropyD_i[1] = GeneralFunctions.calculateEntropy(part2Supports, currPart2Size);
                    partitionSizes[0] = currPart1Size;
                    partitionSizes[1] = currPart2Size;
                    /** 2) calculate weighted entropy for attribute */
                    double currWeightedEntropy = calculateWeightedEntropy(partitionSizes,
                                entropyD_i, numRecords);
                    //System.out.println(" xx " +attrValueIndex + " " + currWeightedEntropy);
                    /** check if we have a new minimum weighted entropy */
                    if(currWeightedEntropy<weightedEntropy)
                    {
                        splitIndex = attrValueIndex;
                        bestPartSizes[0] = currPart1Size;
                        bestPartSizes[1] = currPart2Size;
                        weightedEntropy = currWeightedEntropy;
                        
                    }
                    /** 3) calculate gain
                    *     Note: for numerical attribute we 'penalise' them by subtracting log2(numAttrValue -1)/|D|
                     */
                    double numPenalty = (Math.log(currentPartition.getNumberOfAttributeValues(currAttrIndex)-1)/Math.log(2))
                                        /currentPartition.getNumberOfRecords();
                    double gain = I_D - currWeightedEntropy-numPenalty; //weightedEntropy;
                    /** 4) calculate split for this attribute, note that for numerical this uses the sizes of the
                     *     partitions for the best split point, not the number of records for each value.*/
                    double split = calculateSplit(partitionSizes,currentPartition.getNumberOfRecords());
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
                    //System.out.println("GainRatio: " + gainRatio + " Split "+ split + " " + currAttrIndex + " " + attrValueIndex);
                    /** check if this is a better gain ratio */
                    if(gainRatio> bestGainRatio)
                    {
                        
                        bestGainRatio = gainRatio;
                        bestAttrIndex = currAttrIndex;
                        splitValueIndex = attrValueIndex;
                    }
                    /** store this split value if within threshold of current best gain, 
                     * and greater than min gain ratio
                     */
                    if(Math.abs(bestGainRatio-gainRatio)<Constants.MT_THRESHOLD  & gainRatio>Constants.MIN_GAIN_RATIO)
                    {
                        currSplit = new Split(currAttrIndex, 'n', gainRatio, currentPartition.getAttributeValue(currAttrIndex, attrValueIndex),
                        attrValueIndex, -1);
                        candNumSplits.add(currSplit);
                    }
                }
                /** now we have all candidate splits for this numerical attribute,
                 * we need to cull based on how close they are to the best
                 */
                /** convert the list to an array and sort using a default java sort method */
               Split [] splitsArray = new Split[candNumSplits.size()];
               splitsArray = candNumSplits.toArray(splitsArray);
               Split x = new Split();
               Arrays.sort(splitsArray, x); //note: will sort in descending order due to compare method on Split
               //for(int j=0; j<splitsArray.length; j++)
                 //System.out.println("attr " + currAttrIndex + " :" + splitsArray[j]);
               /** now check which of the candidate splits are far enough away from bestSplit, and each other */
               if(splitsArray.length>0){
                   Split [] goodSplits = checkCandidateNumericalSplitPoints(splitsArray, attrSize);
                   //System.out.println("Attr " + currAttrIndex + " has " + goodSplits.length + " good splits");
                   for(int i=0; i<goodSplits.length; i++)
                   {
                       candidateSplits.add(goodSplits[i]);
                       //System.out.println("Good Split: " + goodSplits[i]);
                   }
               }
            }

        }
        /** now need to sort the list of potential Splits and discard any that might still be
         * outside of the required threshold (due to best GR improving since they were tested above.
         */
        //System.out.println("Best GR: " + bestGainRatio + " Size: " + candidateSplits.size() +  " numSplits: " + numSplits +"\n");

        Split [] bestSplits = getBestSplits(candidateSplits, numSplits, bestGainRatio);
        return bestSplits;
    }


    /**
     * This method is used by CS4 algorithm. It returns the best split for each
     * attribute on a given dataset. Note, it will not check if the gain ratio
     * is above the required minimum.
     *
     * @param currentPartition a dataset
     * @return ordered by attribute, the best splits for each attribute. Note, gain ratio
     * may be below the user defined threshold for some of these. Will return null
     * array in the event that the dataset is too small to split, or all records
     * have the same class value
     */
    public Split [] getAllAttributeSplits(Dataset currentPartition)
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

        /** create Split array to return */
        Split [] allAttrSplits = new Split[numAttributes];

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
        //System.out.println("I(D): " + I_D);


        /** loop over all attributes to find the best gain ratio for each attribute */
        for(int currAttrIndex = 0; currAttrIndex < numAttributes; currAttrIndex++)
        {

            /** store the current best Gain Ratio, its attrIndex and split value (for numerical) */
            double bestGainRatio = Double.NEGATIVE_INFINITY;
            int bestAttrIndex = -1;
            int splitValueIndex = -1;

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
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */
                /** individual entropies for each attribute value - unweighted */
                double [] entropyD_i = new double[attrSize];

                /** 1)calculate entropy for each partition based on an attribute value Di */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    double entD_i = GeneralFunctions.calculateEntropy(classSupports, attrValueDomains[attrValueIndex]);
                    //System.out.println("attr: "+ currAttrIndex + " val: " + attrValueIndex + " entD_i: " + entD_i);
                    entropyD_i[attrValueIndex] = entD_i;


                }
                /** 2) calculate weighted entropy for attribute */
                double weightedEntropy = calculateWeightedEntropy(attrValueDomains,
                            entropyD_i, currentPartition.getNumberOfRecords());
                /** 3) calculate gain */
                double gain = I_D - weightedEntropy;
                /** 4) calculate split for this attribute */
                double split = calculateSplit(attrValueDomains,currentPartition.getNumberOfRecords());
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
                //System.out.println("GainRatio: " + gainRatio + " Split" + split + " " + currAttrIndex);
                /** check if this is a better gain ratio */
                if(gainRatio> bestGainRatio)
                {
                    bestGainRatio = gainRatio;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = -1; //will indicate its a categorical value
                }
                /** now store the info about the best split point for the current attribute */
                Split catSplit = new Split(bestAttrIndex, 'c', bestGainRatio, "", -1, -1);
                allAttrSplits[currAttrIndex] = catSplit;
            }
            /** if numerical attribute, and not the class attribute */
            if(currentPartition.getAttributeType(currAttrIndex)=='n' &&
                    currentPartition.getClassIndex() != currAttrIndex)
            {
                 /** calculating the weighted entropy is less straightforward
                 * 1) calculate entropy for each partition Di based on splitting point
                 * 2) calculate weighted entropy for attribute.
                 * 3) calculate gain
                 * 4) calculate split for this attribute
                 * 5) calculate gain ratio
                 */

                /** 1) calculate entropy for each partition Di based on splitting point */

                /* need to get total supports for all attribute values, these values
                 * will decrease by the supports for the current attribute attribute value
                 * when we move the split value along
                 */
                int []  part2Supports = getTotalClassSupports(currAttrIndex,currentPartition);
                /**
                 * these values will increase by the supports for the current attribute attribute value
                 * when we move the split value along
                 */
                int [] part1Supports = new int[part2Supports.length];

                int numRecords = currentPartition.getNumberOfRecords(); //total number of records
                int currPart1Size = 0; /* will increase by the domain of the current split value */
                int currPart2Size = numRecords; /* will decrease by the domain of the current split value */
                /** set current lowest weighted entropy to positive infinity */
                double weightedEntropy = Double.POSITIVE_INFINITY;
                /** store values for partitions when we find a new min weighted entropy */
                int splitIndex = -1;
                int [] bestPartSizes = new int[2];
                /** looping over all attribute values for current attribute */
                for(int attrValueIndex = 0; attrValueIndex<attrSize; attrValueIndex++)
                {
                    /** update the partition info for the next split point value */
                    /** get the number of records for each class value, for this attribute value Di*/
                    int [] classSupports = currentPartition.getAttrValueClassSupports(currAttrIndex, attrValueIndex);
                    int sumClassSupports = 0;
                    for(int i=0; i<classSupports.length; i++)
                    {
                        part2Supports[i]-=classSupports[i];
                        part1Supports[i]+=classSupports[i];
                        sumClassSupports+=classSupports[i];
                    }
                    currPart1Size+=sumClassSupports;
                    currPart2Size-=sumClassSupports;
                    /** individual entropies for each partition */
                    double [] entropyD_i = new double[2];
                    int [] partitionSizes = new int[2];
                    entropyD_i[0] = GeneralFunctions.calculateEntropy(part1Supports, currPart1Size);
                    entropyD_i[1] = GeneralFunctions.calculateEntropy(part2Supports, currPart2Size);
                    partitionSizes[0] = currPart1Size;
                    partitionSizes[1] = currPart2Size;
                    /** 2) calculate weighted entropy for attribute */
                    double currWeightedEntropy = calculateWeightedEntropy(partitionSizes,
                                entropyD_i, numRecords);
                    /** check if we have a new minimum weighted entropy */
                    if(currWeightedEntropy<weightedEntropy)
                    {
                        splitIndex = attrValueIndex;
                        bestPartSizes[0] = currPart1Size;
                        bestPartSizes[1] = currPart2Size;
                        weightedEntropy = currWeightedEntropy;
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
                //System.out.println("GainRatio: " + gainRatio + " Split " + split + " " + currAttrIndex +  " " + splitIndex + " " + weightedEntropy);
                /** check if this is a better gain ratio */
                if(gainRatio> bestGainRatio)
                {
                    bestGainRatio = gainRatio;
                    bestAttrIndex = currAttrIndex;
                    splitValueIndex = splitIndex;
                }
                Split numSplit = new Split(bestAttrIndex, 'n', bestGainRatio, currentPartition.getAttributeValue(bestAttrIndex, splitValueIndex),
                  splitValueIndex, -1);
                allAttrSplits[currAttrIndex] = numSplit;
            }
            /** when class attribute, add an empty split, so we do not have any null entries */
            if(currentPartition.getClassIndex() == currAttrIndex)
            {
                Split classSplit = new Split();
                allAttrSplits[currAttrIndex] = classSplit;
            }

        }
        return allAttrSplits;
    }

    /**
     * Checks is a candidate split is 'good'. That is far enough away from other 'good'
     * splits. The set of 'good' splits is initially populated with the best split for
     * this attribute.
     * 
     * @param candNumSplits the list of candidate splits to be checked for 'goodness'
     * @param domain the domain size of the numerical attribute
     * @return
     */
    private Split [] checkCandidateNumericalSplitPoints(Split [] candNumSplits, int domain)
    {
        Split[] goodSplits = new Split [candNumSplits.length];
        int currPos =0; //need to keep track of where to add next best split
        /** find the split point for the best split, first in passed array and add to 'good'
         * split list
         */

        Split bestSplit = candNumSplits[0];
        goodSplits[0] = bestSplit;
        currPos ++;
        //System.out.println("BestSplitIndex "+ bestSplit.getAttrIndex() + ": " + bestSplit.getValueIndex() + " Domain: "+ domain);
        /** now loop over next best candidates and check if they pass our
         * test
         *
         * Dmin/|D|>MT_THETA
         */
        for(int i=1; i<candNumSplits.length; i++)
        {
            /** find the min distance between the current candidate and current best list */
            int Dmin = getMinDistance(goodSplits, candNumSplits[i], domain, currPos);
            /** check if far enough away from current best splits */
            if((Dmin/(1.0*domain))>Constants.MT_THETA)
            {
                goodSplits[currPos] = candNumSplits[i];
                currPos++;
            }
        }
        /** now only return array of size the number of values found */
        Split [] newGoodSplits = new Split[currPos];
        System.arraycopy(goodSplits, 0, newGoodSplits, 0, currPos);
        return newGoodSplits;
    }

    /**
     * We need to find the minimum distance between a new candidate split and the
     * current list of current best splits.
     *
     * @param goodSplits the current list of best splits
     * @param candSplit tbe split we're examining as a candidate to join the 'good' splits
     * @param domain the number of values for this numerical attribute
     * @param currSize how many good splits currently stored
     * @return the min distance between the split point for candidate and any splits in the
     * current good list
     */
    private int getMinDistance(Split [] goodSplits, Split candSplit, int domain, int currSize)
    {
        int Dmin=domain; //set the default min distance to the max distance for current attribute
        int candSplitIndex = candSplit.getValueIndex();
        /** loop over current best and find the min distance from the candidate to
         * any in this list
         */
        for(int i=0; i<currSize; i++)
        {
            int currSplitIndex = goodSplits[i].getValueIndex();
            int currDiff = Math.abs(currSplitIndex-candSplitIndex);
            if(currDiff<Dmin)
                Dmin = currDiff;
        }
        //System.out.println("Dmin: " + Dmin);
        return Dmin;
    }

    /**
     * Finds the required number of best splits. Checks that they satisfy the
     * requirements for a candidate split. That is, within the threshold of the
     * best gain ratio.
     *
     * @param candidateSplits unordered candidate splits, both numerical and categorical
     * @param numSplits number of splits we need, which corresponds to number of required trees
     * @param bestGainRatio the highest gain ratio at the current level
     * @return
     */
    private Split [] getBestSplits(ArrayList<Split> candidateSplits, int numSplits, double bestGainRatio)
    {
        /** convert the list to an array and sort using a default java sort method */
       Split [] splitsArray = new Split[candidateSplits.size()];
       splitsArray = candidateSplits.toArray(splitsArray);
       Split x = new Split();
       Arrays.sort(splitsArray, x); //note: will sort in descending order due to compare method on Split
       //for(int i=0; i<splitsArray.length; i++)
         //System.out.println(splitsArray[i].getGainRatio());
       /** now need to only get first numSplit number of Splits, or less if we do not
        * meet the requirement for the difference being within the user defined threshold,
        * or the size of the candidateSplits<numSplits.
        */
       int finalSize = 0;
       /** at worst only need to look at first 'numSplits' splits. */
       int maxNum = Math.min(candidateSplits.size(), numSplits);
       for(int i=0; i<maxNum; i++)
       {
           /** check if within threshold */
           //System.out.println("i:" + i + " splitsArray.length:" + splitsArray.length + " maxNum: " + maxNum);
           if(candidateSplits.size()>0 && bestGainRatio-splitsArray[i].getGainRatio()<Constants.MT_THRESHOLD)
           {
               /* this split is OK */
               finalSize++;
           }
           else
           {
              /** no need to look further, since they are sorted. */
               break;
           }
       }
       //System.out.println("finalSize: " + finalSize);
       /** now copy the first 'finalSize' Splits to a new array and return */
       Split [] finalSplits = new Split[finalSize];
       System.arraycopy(splitsArray, 0, finalSplits, 0, finalSize);
       return finalSplits;
    }

}
