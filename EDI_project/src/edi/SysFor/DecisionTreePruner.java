

package SysFor;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class contains methods for pruning a {@link DecisionTree} once it has been
 * built.
 *
 * @author helengiggins
 * @version 1.0 13/12/2010
 */
public class DecisionTreePruner {

    /** instance variables */
    private static double minDiff = 0.001;
    /** the binomial coefficients for current N and E pair */
    private static BigDecimal [] coefficients;
    /**
     * Class constructor.
     */
    public DecisionTreePruner()
    {
        coefficients = null;
    }

    /**
     * Returns a pruned {@link DecisionTree} which has been pruned according to
     * the following criteria.
     *
     * <p>For each leaf node (having the same parent), calculate the expected
     * error and sum for each leaf. The predicted error is <code>P<sub>r</sub></code>
     * times the number of records in the leaf. Where <code>CF = sum i (0 to E) (N choose i)
     * P<sub>r</sub><sup>i</sup> (1-P<sub>r</sub>)<sup>N-i</sup></code>, where
     * <code>E</code> is the number of misclassified records, <code>N</code> is the total
     * number of records in the leaf, and <code>CF</code> is the user inputed confidence
     * factor. The predicted error is then summed for all sibling leaf nodes.
     * </p>
     * <p>Once the error is calculated for each leaf, we also calculate the error
     * assuming that the parent node was not split into leaf nodes. Finally, this
     * error is compared to the error for all leaf nodes. If the error for the
     * subtree replaced by a leaf is lower, then we prune the subtree.</p>
     * <p>Note, this method will at each step only try to prune at a node who has
     * all children as leaf nodes. If any of these nodes is pruned it will check again
     * until no more branches can be pruned</p>
     * <p>Note that at present non-pruned test nodes will be tested again in the
     * recursive call to prune the tree.</p>
     */
    public static DecisionTree pruneTree (DecisionTree tree, double confidenceFactor)
    {
        //TODO probably best to make a copy of the tree so we don't lose the original */
        boolean wasPruned = false; //flag if the tree is pruned at all in the process
        /** continue to prune while ever we find a subtree error */
        do{
            wasPruned = false; //reset for this pass
            /** list of parent nodes, who have only leaf nodes as children */
            List <Node> leafParents = getLeafParentList(tree);
            //System.out.println("num subtrees being examined: " + leafParents.size());
            /** for each parent node work out the error for all children (subtree), and the error
             * for the parent node if the subtree was collapsed (now a leaf node)
             */
            for(int currParent=0; currParent<leafParents.size(); currParent++)
            {
                Node parent = leafParents.get(currParent);
                /** check we are not trying to collapse the root
                 */
                if((!(parent==tree.getRootNode())))
                {
                    double leafError = getTotalLeafError(parent, confidenceFactor);
                    double parentError = getTotalError(parent, confidenceFactor);
                    if(leafError>parentError)
                    {
                        //System.out.println("Prune me!");
                        pruneSubtree(parent);
                        wasPruned = true;
                    }
                    else{
                        /* flag node as visited, so we don't examine again */
                        parent.setVisited();
                    }
                }
            }
            //System.out.println("#############");
        }
        while(wasPruned);
        /** return pruned tree */
        return tree;
    }

    /**
     * Prunes the leaf nodes of the passed parent node, collapsing the class supports
     * for each leaf back into the parent node. Note assumes all children of the passed
     * node are leaf nodes.
     *
     * @param parent parent node of the subtree we want pruned
     */
    private static void pruneSubtree(Node parent)
    {
        List<Node> children = parent.getChildren();
        int [] totalSupports = new int[0]; //will set size once in loop
        LeafNode leaf = new LeafNode();
        /** loop over the children nodes to sum all class supports */
        for(int currLeaf=0; currLeaf<children.size(); currLeaf++)
        {
            leaf = (LeafNode) children.get(currLeaf);//assuming all children are leaf nodes
            int [] classSupports = leaf.getClassSupports();
            if(currLeaf==0) //can now set size of array
            {
                totalSupports = new int[classSupports.length];
            }
            /** add the class support values to the total supports */
            for(int i=0; i<classSupports.length; i++)
            {
                totalSupports[i] += classSupports[i];
            }
        }
        /** create the leafNode and copy it's previous info over */
        LeafNode newLeaf = new LeafNode(totalSupports, leaf.getClassValues(),
                leaf.getClassName(), leaf.getClassIndex());
        newLeaf.setData(parent.getAttrName(), parent.getAttrIndex(), parent.getSplitValue(),
                parent.getAttrType(), null, parent.getParent());
        /** need to set as child for parent node */
        Node grandParent = parent.getParent();
        List <Node> childList = grandParent.getChildren();
        /* find currNode in the list and replace with Leaf node */
        for(int i=0; i<childList.size(); i++)
        {
            /** can check object references for equality */
            if(childList.get(i)==parent)
            {
                childList.set(i,newLeaf);
            }
        }

    }

    /**
     * Return the list of all leaf node's parents. That is, the list of nodes one
     * away from a leaf node where all children are leaf nodes.
     *
     * @param tree the decision tree
     * @return the list of all leaf node's parents
     */
    private static List<Node> getLeafParentList(DecisionTree tree)
    {
        /** traverse the tree to return a list of leaf nodes */
        List<Node> leavesList = tree.preOrderTraversalLeavesOnly();
        //System.out.println("num leaf nodes: " + leavesList.size());
        List<Node> parentList = new ArrayList<Node>();
        /** loop over the leaves and get a list of parent nodes */
        for(int i=0; i<leavesList.size(); i++)
        {
            Node currLeaf = leavesList.get(i);
            Node currParent = currLeaf.getParent();
            /** only look at parent nodes not already visited (examined and rejected) */
            if(!currParent.isVisited())
            {
                /** need to check if all children are leaf nodes */
                List<Node> children = currParent.getChildren();
                /** check if leaf is the first child of the parent */
                Node firstChild = children.get(0);
                if(firstChild==currLeaf)
                {
                    /* we will add a parent only from the first child */
                    boolean allLeafNodes = true;
                    /** loop over all children of parent and check they are leaves */
                    for(int currChild =0; currChild<children.size(); currChild++)
                    {
                        if(!(children.get(currChild) instanceof LeafNode))
                        {
                            allLeafNodes=false; //child of parent not a leaf, we will not add
                            break; //no need to look any further
                        }
                    }
                    /** if all children of current parent node are leaves, add to list */
                    if(allLeafNodes)
                    {
                        /** need to check if we have this parent already */
                        //boolean found = false;
                        /* start at end of list, as most likely the parent is last added */

                        /*for(int j=(parentList.size()-1); j>=0;j--)
                        {

                            if(parentList.get(j).equals(currParent))
                            {
                                found = true;
                                break; /** found this node, no need to look further */
                          //  }
                       // }
                       // if(!found) /** add the parent if not already in the list */
                        //{*/
                        parentList.add(currParent);
                        //}
                    }
                }
            }
        }
        //System.out.println(parentList);
        return parentList;
    }

    /**
     * For a parent of all leaf nodes, find the total error, where we calculate the
     * error for the node as if it were a leaf node.
     *
     * @param parent a parent of all leaf nodes
     * @param confidenceFactor the confidence factor
     * @return the total error for a parent node, as if it was a leaf node
     */
    private static double getTotalError(Node parent, double confidenceFactor)
    {
        int [] NandE = getNAndECounts(parent);
        int N = NandE[0];
        int E = NandE[1];
        /** get binomial coefficients list for N and E */
        //System.out.print("N: "+ N +" E" + E);
        //coefficients = getBinomialCoefficients(N,E); //get all n choose i values
        //double Pr = estimatePr(confidenceFactor, E, N, minDiff);
        double Pr = estimatePr(confidenceFactor, E, N);
        if(Pr==-1.0)
        {
           System.out.println("@@@@@@ERROR: Probability in pruning not calculated " + parent);
        }
        //System.out.println("totalError: " + Pr*N);
        return Pr*N;
    }

    /**
     * For a set of all leaf nodes, find the total error, where we calculate the
     * error for each leaf individually and sum together.
     *
     * @param parent a parent of all leaf nodes
     * @param confidenceFactor the confidence factor
     * @return the total error for a set of leaf nodes
     */
    private static double getTotalLeafError(Node parent, double confidenceFactor)
    {
       /** get all children of current parent node */
       List<Node> children = parent.getChildren();
       double totalError = 0.0;
       for(int currLeaf=0; currLeaf<children.size(); currLeaf++)
       {
           /** get N and E for current leaf node and calculate Pr */
           int [] NandE = getNAndECounts(children.get(currLeaf));
           int N = NandE[0];
           int E = NandE[1];
           /** get binomial coefficients list for N and E */
           //coefficients = getBinomialCoefficients(N,E);
           //double Pr = estimatePr(confidenceFactor, E, N, minDiff);
           double Pr = estimatePr(confidenceFactor, E, N);
           if(Pr==-1.0)
           {
               System.out.println("ERROR: Probability in pruning not calculated " + children.get(currLeaf));
           }
           totalError+= Pr*N;

       }
       //System.out.println("totalLeafError: " + totalError);
       return totalError;
    }

    /**
     * Returns the total number of records for the node (N), and the number
     * misclassified (E). When a parent node this sums supports for all leaf nodes
     * to determine total number of records and dominant class.
     * 
     * @param node either a leaf node or parent of a leaf
     * @return N in array position 0 and E in array position 1
     */
    public static int[] getNAndECounts(Node node)
    {
       int [] returnVals = new int[2];
       /** if a leaf node */
       if(node instanceof LeafNode)
       {
           LeafNode leaf = (LeafNode) node;
           int [] classSupports = leaf.getClassSupports();
           int numRecords = 0; //will sum all supports
           int maxSupport = 0; //keep track of dominant support value
           /** loop and sum supports, and find max support */
           for(int i=0;i<classSupports.length; i++)
           {
               numRecords+=classSupports[i];
               if(classSupports[i]>maxSupport)
               {
                   maxSupport= classSupports[i];
               }
           }
           //System.out.println("Leaf n: " + numRecords + " e: " + (numRecords-maxSupport));
           returnVals[0] = numRecords;
           returnVals[1] =(numRecords-maxSupport);
           return returnVals;
       }
       /** parent of leaf, sum supports for all children to determine n and e */
       List<Node> children = node.getChildren();
       /** storing support totals for each class value */
       int [] supportSums = new int[0];
       int maxSupport = 0;  //max support of a class value (over all leaves)
       int numRecords = 0; //total records over all leaves
       //System.out.println(node);

       /** examine all leaf nodes and sum supports for each class value, as well as
        * total number of records for all leaves
        */
       for(int currLeaf=0; currLeaf<children.size(); currLeaf++)
       {
           Node temp = children.get(currLeaf);
           LeafNode leaf = (LeafNode) children.get(currLeaf);
           int [] currSupports = leaf.getClassSupports();
           if(currLeaf==0) /* on first child need to find size of class to set array */
           {
               int numClasses = currSupports.length;
               supportSums = new int[numClasses];
           }
           /** loop over all classes and add current leaf's support to the total for that class value */
           for(int i=0; i<currSupports.length; i++)
           {
               supportSums[i] += currSupports[i];
               numRecords+=currSupports[i];
               /** check for max support on last iteration */
               if(currLeaf==(children.size()-1) && supportSums[i]>maxSupport)
               {
                   maxSupport = supportSums[i];
               }
           }
       }
       //System.out.println("n: " + numRecords + " e: " + (numRecords-maxSupport));
       returnVals[0] = numRecords;
       returnVals[1] = (numRecords-maxSupport);
       return returnVals;
    }

    /**
     * Estimates the Pr for a given CF, E and N values.
     *
     * <p>Where <code>CF = sum i (0 to E) (N choose i)
     * P<sub>r</sub><sup>i</sup> (1-P<sub>r</sub>)<sup>N-i</sup></code>, where
     * <code>E</code> is the number of misclassified records, <code>N</code> is the total
     * number of records in the leaf, and <code>CF</code> is the user inputed confidence
     * factor.</p>
     *
     * @param cf confidence factor
     * @param e the number of misclassified records
     * @param n the total number of records in leaf
     * @param threshold user defined threshold for how close we need to be to CF when
     * estimating Pr.
     * @return an estimate of the Pr value
     */
    public static double estimatePr(double cf, int e, int n, double threshold)
    {
        double Pr = 0.0;
        /** check for simple case, when E = 0, that is, no misclassified records */
        if(e==0){
            //System.out.println("e=0");
            double pow = 1.0/n;
            Pr = 1.0 - Math.pow(cf, pow);
            return Pr;
        }
        /** else we need to try to guess Pr and check how close we can get to CF. */
        BigDecimal thres = new BigDecimal(threshold); //the furthest we can be away real Cf
        BigDecimal CF = new BigDecimal(cf);
        Pr = findConfidenceFactor(e, n, CF, thres, 0.0, 1.0);
        return Pr;
    }

    /**
     * Estimates the Pr for a given CF, E and N values. This version of the method
     * uses an approximation, rather than 'guessing' the value and then seeing
     * how close we are.
     *
     * <p>Where <code>CF = sum i (0 to E) (N choose i)
     * P<sub>r</sub><sup>i</sup> (1-P<sub>r</sub>)<sup>N-i</sup></code>, where
     * <code>E</code> is the number of misclassified records, <code>N</code> is the total
     * number of records in the leaf, and <code>CF</code> is the user inputed confidence
     * factor.</p>
     *
     * @param cf confidence factor
     * @param e the number of misclassified records
     * @param n the total number of records in leaf
     * @return an estimate of the Pr value
     */
    public static double estimatePr(double cf, int e, int n)
    {
        double Pr = 0.0;
        /** check for simple case, when E = 0, that is, no misclassified records */
        if(e==0){
            //System.out.println("e=0");
            double pow = 1.0/n;
            Pr = 1.0 - Math.pow(cf, pow);
            return Pr;
        }
        /** else we need to try to approximate Pr.  */
        double lookUpValue = (1-cf)/2.0;
        double c = PruningCValues.getCValue(lookUpValue);
        /** we sum i over the number of values in E, using a different approx. calc.
         * when i<=(N/2).
         */
        for(int i=0; i<=e; i++)
        {
            /*if(i<=(n/2.0))
            {
                Pr += calcApproxPrLowI(i, n, c);
            }
            else
            { */
                Pr += calcApproxPrHighI(i, n, c); //currently only using 'top' equation.
            //}
        }
        return Pr;
    }

    /**
     * To be used when i<=(N/2)
     *
     * Pr<sub>i</sub> = (A-B)+sqrt(B(2-(A-B)-A)), where A = (i+1+ (c^2/4))/(N+1), and B= (c^2/2)((i+1)/(N+1)^2)
     *
     * @param i current i value (when summing over E)
     * @param n total number of records (N)
     * @param c value for c, given from lookup table
     * @return Pr approximation value for the current i, for when i<=(N/2)
     */
    private static double calcApproxPrLowI(int i, int n, double c)
    {
        double A = i+1+(Math.pow(c,2)/4);
        A = A/(n+1);
        double B = Math.pow(c, 2)/2;
        B = B *((i+1)/Math.pow((n+1), 2));
        double Pr =(A-B) + Math.sqrt(B*(2-(A-B)-A));
        //System.out.println("i: " + i + " Pr: " + Pr);
        return Pr;
    }

    /**
     * To be used when i>(N/2).
     *
     * Pr<sub>i</sub> = (i + 1/2 + (c^2/2) + c * sqrt((i+1/2)(1-(i+1/2)/N)+(c^2/4)))/(N+c^2)
     *
     * @param i current i value (when summing over E)
     * @param n total number of records (N)
     * @param c value for c, given from lookup table
     * @return Pr approximation value for the current i, for when i>(N/2)
     */
    private static double calcApproxPrHighI(int i, int n, double c)
    {
        //(i + 1/2 + (c^2/2) + c * sqrt((i+1/2)(1-(i+1/2)/N)+(c^2/4)))/(N+c^2)
        double bottom = n+(c*c);
        double sqrtTerm = (i + 0.5)*(1-(i+0.5)/n)+((c*c)/4);
        sqrtTerm = Math.sqrt(sqrtTerm);
        double top = i+0.5+((c*c)/2+ c*sqrtTerm);
        double Pr = top/bottom;
        //System.out.println("i: " + i + " Pr: " + Pr);
        return Pr;
    }

    public static double findConfidenceFactor(int e, int n, BigDecimal CF,
            BigDecimal thres, double PrLow, double PrHigh)
    {

        //System.out.println("e: " + e + " n: " + n + " PrLow: " + PrLow + " PrHigh: " + PrHigh);
        /** generate random number between PrLow and PrHigh */
        double currPr = (PrHigh+PrLow)/2; //pick the mid point of our range
        /** get binomial coefficients for this n and e */
        BigDecimal currCF = calculateConfidenceFactor(e, n, currPr);
        BigDecimal diffCF = currCF.subtract(CF); //curr - goalCF
        
        BigDecimal absDiff = diffCF.abs();
        //System.out.println(PrLow + " " + PrHigh + " " + diffCF);

        /** check if we are within the required threshold, that is, close enough
         * to our confidence factor
         */
        if(absDiff.compareTo(thres) ==-1)
        {
            //System.out.println("found: " + absDiff);
            /** we are within the required range for the confidence factor, can return current Pr*/
            return currPr;
        }
        /** currCF is too low, we need to reduce Pr */
        if(diffCF.compareTo(new BigDecimal(0.0))==-1){
            return findConfidenceFactor(e, n, CF, thres, PrLow, currPr);

        }
        /** currCF is too high, prob need to increase */
        else if(diffCF.compareTo(new BigDecimal(0.0))==1){
            return findConfidenceFactor(e, n, CF, thres, currPr, PrHigh);
        }
        
        /** Error if we don't find the probabilty */
        System.out.println("@@@@@@@@Error: Confidence factor not calculated, -1.0");
        return -1.0;//error, should n ot get here
    }

    /**
     * Calculates the confidence factor value for a given P<sub>r</sub>.
     *
     * <p><code>CF= sum i (0 to E) (N choose i)
     * P<sub>r</sub><sup>i</sup> (1-P<sub>r</sub>)<sup>N-i</sup></code>, where
     * <code>E</code> is the number of misclassified records, <code>N</code> is the total
     * number of records in the leaf.</p>
     *
     * @param e E the number of records misclassified
     * @param n N the total number of records
     * @param Pr probability
     * @return
     */
    public static BigDecimal calculateConfidenceFactor(int e, int n, double Pr)
    {
        
        BigDecimal CF = new BigDecimal(0.0);
        BigDecimal PrDec = new BigDecimal(Pr);
        BigDecimal oneMinusPr = new BigDecimal((1.0-Pr));
        /** sum over number of misclassified records */
        for(int i=0; i<=e; i++)
        {
            BigDecimal nChooseI = coefficients[i];
            BigDecimal PrToPowI =  PrDec.pow(i);
            BigDecimal oneMinusPrToNMinusI = oneMinusPr.pow(n-i);
            /** now put together to get current sum term */
            BigDecimal currSum = nChooseI.multiply(PrToPowI);
            currSum = currSum.multiply(oneMinusPrToNMinusI);
            CF = CF.add(currSum);  
        }
        
        return CF;
    }

    public static BigDecimal [] getBinomialCoefficients(int n, int e)
    {
        BigDecimal [] coeffs = new BigDecimal [e+1];

        for(int i=0; i<=e; i++)
        {
            BigDecimal nChooseI = calculateBinomialCoefficient(n, i);
            coeffs[i] = nChooseI;
        }
        return coeffs;
    }

    /**
     * Calculates the binomial coefficient using the multiplicative
     * formula.
     *
     * @param n the number <code>n</code>
     * @param k the number <code>k</code>
     * @return n choose k
     */
    public static BigDecimal calculateBinomialCoefficient(int n, int k)
    {
        /** check for n=0 */
        if(n==0){ return new BigDecimal(0.0);}
        
        BigDecimal nChooseK = new BigDecimal(1.0); //return 1 when k=0
        for(int i=1; i<=k; i++)
        {
            double temp = (n-k+i)/(i*1.0);
            BigDecimal multiplic = new BigDecimal(temp);
            //System.out.println(multiplic);
            nChooseK = nChooseK.multiply(multiplic);
            //System.out.println(" " + nChooseK);
        }
        return nChooseK;
    }
}
