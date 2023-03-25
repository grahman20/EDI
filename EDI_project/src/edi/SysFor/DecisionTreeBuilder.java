

package SysFor;

import java.io.*;
import java.util.List;

/**
 * Manages the creation of decision trees.
 *
 * @author helengiggins
 * @version 1.0 30/11/2010
 * @see Dataset
 * @see DecisionTree
 */
public class DecisionTreeBuilder {

    /* instance variables */

    /** decision tree building method C5.0 */
    public static final int SEE5 = 0;
    /** decision tree building method Explore */
    public static final int EXPLORE = 1;
    /** decision tree building method MDMT */
    public static final int MDMT = 2;
    /** multiple decision tree building method CS4 */
    public static final int CS4 = 3;
    /** the dataset we are building the decision tree on */
    Dataset dataset;
    /** used for managing reading and writing to file */
    FileManager fileManager;
    /** method for creating the decision tree */
    int method;
    /** the name of the output file */
    String fileOut;

    /**
     * Class constructor, initializes instance variables to default.
     */
    public DecisionTreeBuilder()
    {
        dataset = null;
        fileManager = new FileManager();
        method = -1;
        fileOut="";
    }

    /**
     * Class constructor.
     *
     * @param nFile the name of the names file containing attribute information
     * @param dFile the name of the data file to read the records from
     * @param fileOut the name of the output file to write the logic rules to
     * @param method indicates which method should be used to build the decision tree
     */
    public DecisionTreeBuilder(String nFile, String dFile, String fileOut, int method)
    {
        /** read name and data files and then create the dataset */
        fileManager = new FileManager();
        String [] nameFile = fileManager.readFileAsArray(new File(nFile));
        String [] dataFile = fileManager.readFileAsArray(new File(dFile));
        dataset = new Dataset(nameFile, dataFile);
        this.method = method;
        this.fileOut = fileOut;
    }

    /**
     * Calls appropriate method depending on which building method was passed
     * to parametric constructor. Will build the decision tree, output logic rules
     * to the output file and print tree to screen and a standard output file, based
     * on the name of the passed output file.
     */
    public void createDecisionTree()
    {
        //System.out.println(dataset); //will print the dataset

        /** determine which method we're using to build tree */
        if(method==SEE5)
        {
//            System.out.println("See5 decision tree being built.");//commented by gea
            buildSee5Tree(fileOut);
        }
        else if(method==EXPLORE)
        {
//            System.out.println("Explore decision tree being built");//commented by gea
            buildExploreTree(fileOut);
        }

    }

    public void createMultipleTrees()
    {
        if(method==SEE5)
        {
//            System.out.println("See5 multiple decision trees being built.");//commented by gea
            buildSee5MultiTrees(fileOut);
        }

    }

    /**
     * Builds a decision tree using the See5(C4.5) method. Writes
     * the resulting logic rules to the passed output file. Prunes the
     * tree once created.
     *
     * @param fileOut output file to which the logic rules are written
     */
    private void buildSee5Tree(String fileOut)
    {
        See5 see5 = new See5();
        DecisionTree tree = see5.buildTree(dataset);
        /** now prune the tree */
//        System.out.println("Now pruning the decision tree, this may take some time.\n");//commented by gea
        tree = DecisionTreePruner.pruneTree(tree, Constants.ConfidFactor);

//        System.out.println("@@@@@@@@@@@@@ Decision Tree after Pruning @@@@@@@@@@@@@@");//commented by gea
        /** print tree to screen */
//        System.out.println(DecisionTreeFunctions.readTreeForDisplay(tree));//commented by gea
//        System.out.println("@@@@@@@@@@@@@ Logic Rule for Prediction @@@@@@@@@@@@@@");//commented by gea
        /** print rules to screen */
//        System.out.println(DecisionTreeFunctions.readTreeForPrediction(tree, dataset));//commented by gea
        
        /** generate logic rules output to write to file */
        String output = DecisionTreeFunctions.readTreeForPrediction(tree, dataset);
        /** write logic rules to file, and display success message to user */
        File outFile = new File(fileOut);
        fileManager.writeToFile(outFile, output);//add by gea by commenting the following two line
//        System.out.println("Writing decision tree logic rules to file.");//commentd by gea
//        System.out.println(fileManager.writeToFile(outFile, output));//commentd by gea

        //commented by gea
        /** generate easy to read tree to write to file */
//        String treeOut = DecisionTreeFunctions.readTreeForDisplay(tree);
//        /** create a new file with 'tree-' in front of output file name */
//        String outFilePath = outFile.getPath();
//        int indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension
//        String pathBeforeExtension = outFilePath.substring(0,indexOfDot);
//        String treeOutName = pathBeforeExtension + "-tree.txt";
//        outFile = new File(treeOutName);
//        System.out.println("\nNow writing decision tree to file.");
//        System.out.println(fileManager.writeToFile(outFile, treeOut));
    }

    /**
     * Builds a single decision tree using the Explore algorithm and writes the
     * logic rules for prediction results to the passed output file. Decision
     * trees are written to the same file name with "-tree" added into the end.
     *
     * @param fileOut name of output file
     */
    private void buildExploreTree(String fileOut)
    {
        Explore explore = new Explore();
        DecisionTree tree = explore.buildTree(dataset);
        /** now prune the tree */
        System.out.println("Now pruning the decision tree, this may take some time.\n");
        tree = DecisionTreePruner.pruneTree(tree, Constants.ConfidFactor);

        System.out.println("@@@@@@@@@@@@@ Decision Tree after Pruning @@@@@@@@@@@@@@");
        /** print tree to screen */
        System.out.println(DecisionTreeFunctions.readTreeForDisplay(tree));
        System.out.println("@@@@@@@@@@@@@ Logic Rule for Prediction @@@@@@@@@@@@@@");
        /** print rules to screen */
        System.out.println(DecisionTreeFunctions.readTreeForPrediction(tree, dataset));

        /** generate logic rules output to write to file */
        String output = DecisionTreeFunctions.readTreeForPrediction(tree, dataset);
        /** write logic rules to file, and display success message to user */
        File outFile = new File(fileOut);
        System.out.println("Writing decision tree logic rules to file.");
        System.out.println(fileManager.writeToFile(outFile, output));
        /** generate easy to read tree to write to file */
        String treeOut = DecisionTreeFunctions.readTreeForDisplay(tree);
        /** create a new file with 'tree-' in front of output file name */
        String outFilePath = outFile.getPath();
        int indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension
        String pathBeforeExtension = outFilePath.substring(0,indexOfDot);
        String treeOutName = pathBeforeExtension + "-tree.txt";
        outFile = new File(treeOutName);
        System.out.println("\nNow writing decision tree to file.");
        System.out.println(fileManager.writeToFile(outFile, treeOut));
    }

    private void buildSee5MultiTrees(String fileOut)
    {
        See5 see5 = new See5();
        DecisionTree [] multTrees = see5.generateMultipleTrees(dataset);
        int NofTree=0;//No. of Tree//added by Gea
        File outFile = new File(fileOut);
        for(int currTree=0; currTree<multTrees.length; currTree++)
        {
            /** check if we have a tree at this position */
            if(multTrees[currTree]!=null)
            {
                NofTree++;
            }
        }
        
        fileManager.writeToFile(outFile, NofTree+"\n");//added by Gea

        /** empty multi tree and prediction rule files, since we use append later on */
//        System.out.println(fileManager.writeToFile(outFile, "")); //empty file //commented by Gea
//        String outFilePath = outFile.getPath(); //commented by Gea
//        int indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension //commented by Gea
//        String pathBeforeExtension = outFilePath.substring(0,indexOfDot); //commented by Gea
//        String treeOutName = pathBeforeExtension + "-tree.txt"; //commented by Gea
//        outFile = new File(treeOutName); //commented by Gea
        /** now prune the trees */
        for(int currTree=0; currTree<multTrees.length; currTree++)
        {
            /** check if we have a tree at this position */
            if(multTrees[currTree]!=null)
            {
                DecisionTree tree = multTrees[currTree];

//                 System.out.println("@@@@@@@@@@@@@ Decision Tree " + (currTree+1)+ " before Pruning @@@@@@@@@@@@@@"); //commented by Gea
                /** print tree to screen */
//                System.out.println(DecisionTreeFunctions.readTreeForDisplay(tree)); //commented by Gea
//                System.out.println("Now pruning decision tree " + (currTree+1)+ " , this may take some time.\n"); //commented by Gea
                tree = DecisionTreePruner.pruneTree(tree, Constants.ConfidFactor);

//                System.out.println("@@@@@@@@@@@@@ Decision Tree " + (currTree+1)+ " after Pruning @@@@@@@@@@@@@@"); //commented by Gea
                /** generate easy to read tree to write to file */
//                String treeOut = DecisionTreeFunctions.readTreeForDisplay(tree); //commented by Gea
                /** print tree to screen */
//                System.out.println(treeOut); //commented by Gea
//                System.out.println("@@@@@@@@@@@@@ Logic Rule for Prediction @@@@@@@@@@@@@@"); //commented by Gea
                /** generate logic rules output to write to file */
                String predOutput = DecisionTreeFunctions.readTreeForPrediction(tree, dataset);
                /** print rules to screen */
//                System.out.println(predOutput); //commented by Gea
                fileManager.appendToFile(outFile, "DT-GEA\n");//added by Gea
                fileManager.appendToFile(outFile, predOutput);//added by Gea
                
                /** write logic rules to file, and display success message to user */
                /** create a new file with '-x' in front of output file name, where x is the tree number */
//                outFile = new File(fileOut); //commented by Gea
//                outFilePath = outFile.getPath(); //commented by Gea
//                indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension //commented by Gea
//                pathBeforeExtension = outFilePath.substring(0,indexOfDot); //commented by Gea
//                treeOutName = pathBeforeExtension + "-" + (currTree+1) + ".txt"; //commented by Gea
//                File treeOutFile = new File(treeOutName); //commented by Gea
//                System.out.println("Writing decision tree logic rules to file."); //commented by Gea
//                System.out.println(fileManager.writeToFile(treeOutFile, predOutput)); //commented by Gea
                /** add prediction to all prediction file */
//                System.out.println(fileManager.appendToFile(outFile, "\nTree Number: " +(currTree+1)+"\n" + predOutput)); //commented by Gea
                
                /** create a new file with 'x-tree' in front of output file name */
//                outFilePath = treeOutFile.getPath(); //commented by Gea
//                indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension //commented by Gea
//                pathBeforeExtension = outFilePath.substring(0,indexOfDot); //commented by Gea
//                treeOutName = pathBeforeExtension + "-tree.txt"; //commented by Gea
//                treeOutFile = new File(treeOutName); //commented by Gea
//                System.out.println("\nNow writing decision tree to file."); //commented by Gea
//                System.out.println(fileManager.writeToFile(treeOutFile, treeOut)); //commented by Gea
                /** write tree to all trees file */
//                outFilePath = outFile.getPath(); //commented by Gea
//                indexOfDot = outFilePath.lastIndexOf("."); //to get position of file extension //commented by Gea
//                pathBeforeExtension = outFilePath.substring(0,indexOfDot); //commented by Gea
//                treeOutName = pathBeforeExtension + "-tree.txt"; //commented by Gea
//                outFile = new File(treeOutName); //commented by Gea
//                System.out.println(fileManager.appendToFile(outFile, "\nTree Number: " +(currTree+1)+"\n" + treeOut)); //commented by Gea
            }
        }
    }

    

}
