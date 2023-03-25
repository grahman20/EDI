/**
 * EDI uses two layers/steps of imputation namely the Early-Imputation step and the Advanced-Imputation step. 
 * In the early imputation step we first impute the missing values (both numerical and categorical) using existing techniques. 
 * The main goal of this step is to carry out an initial imputation and thereby refine the records having missing values 
 * so that they can be used in the second layer of imputation through an existing technique called DMI. 
 * The original DMI ignores the records having missing values. 
 * Therefore, we argue that if a data set has a huge number of missing values then 
 * the imputation accuracy of DMI may suffer significantly since it ignores a huge number of records.
 * 
 * <h2>Reference</h2>
 * 
 * Rahman, M. G. and Islam, M. Z. (2013): A Novel Framework Using Two Layers of Missing Value Imputation, In Proc. of the 11th Australasian Data Mining Conference (AusDM 13), Canberra, Australia, 13-15 November 2013
 *  
 * @author Gea Rahman <https://csusap.csu.edu.au/~grahman/>
 */

package edi;
import java.io.*;
import SysFor.*;
import java.util.*;
import java.text.DecimalFormat;
/**
 *
 * @author grahman
 */
public class EDI {

    /*
     * Global declaration
     */
    private int kNNI; //contains k value for the k-NN, usually k=10
    private int [][]MV;  //Maissing values, 0->no missing, 1->Missing
    private int []MR;  //Missing Records, 0->no missing, 1->Missing
    private int totalMissing;
    private int totalRecords;  //total records of the data file
    private int totalAttrs;  //total records of the data file
    private String [][]dataset;//contains dataset
    private String [][]datasetI;//contains data set after initial Imputation
    private int [] attrNType; //contain attributes type 0->Categorical, 1->Numerical
    private String [] attrSType; //contain attributes type c->Categorical, n->Numerical
    private String [] fStr; //contains format string of each numerical attr
    private String []tmpFiles;
    private int noOfLeaves; //contains a value about no. of leaves of a DT
    private String []Rules; //contains leaf information of a DT
    private int [][]LeafRecords;  //Records belong to each leaf
    private int []SizeOfEachLeaf;  //No. of records belong to each leaf
    private int []RL;  //contains a leaf id where a record belongs to.
    private int []FlgImpStatus;  //0->no imp required, 1->imp require but not not done, 2->imp done!
    private int tmpf;
    


 /**
 * Impute a given data set having missing values
 * and calls other necessary methods
 *
 * @param attrFile contains 2 lines attributes types and name information
 * @param dataFile data set having missing values to be imputed
 * @param outputFile filename of the imputed data set
 * @param userKNN contains the value of k for k-nearest neighbors
 * @param DMIopt a string about DMI option "NDMI" or "SDMI" , default value="SDMI"
 */

    public void runEDI(String attrFile, String dataFile,String outputFile,
            int userKNN,  String DMIopt)
    {
        tmpFiles=new String[100];
        tmpf=0;
        kNNI=userKNN;
        initialize(attrFile,dataFile);
        FileManager fileManager = new FileManager();
        
        if(totalMissing>0)
        {
            String datafilePImp = fileManager.changedFileName(dataFile, "_pImp");
            tmpFiles[tmpf]=datafilePImp;tmpf++;
            preImputation(attrFile, dataFile,datafilePImp, kNNI);
            if(DMIopt.equals("NDMI"))
            {
                mviDMINew ndmi=new mviDMINew();
                dataset=ndmi.runDMIimp(attrFile, datafilePImp,  MR, MV, totalMissing);
            }
            else
            {
                dmiSingleImputation(attrFile,dataFile,datafilePImp);
            }
            //print to file
            arrayToFile(dataset, totalRecords, totalAttrs, outputFile);
            
            //remove tmp files
            fileManager.removeListOfFiles(tmpFiles, tmpf);
        }
        else
        {
            System.out.println("There are NO such missing values in the dataset!!!");
        }
        
    }
 
/**
 * This method first builds a decision tree on the pre-imputed data set
 * then apply EMI for imputing numerical missing values
 * and kNNI for imputing categorical missing values
 *
 * @param attrFile contains 2 lines attributes types and name information
 * @param dataFile data set having missing values to be imputed
 * @param outputFile filename of the imputed data set
 */

    private void dmiSingleImputation(String attrFile, String oridataFile,
            String preimpdataFile)
    {
        FileManager fileManager = new FileManager();
        //Generate name file from Dc
        String namesFile = fileManager.changedFileName(oridataFile, "_name");
        String gtest=fileManager.extractNameFileFromDataFile(new File(attrFile),
            new File(preimpdataFile),new File(namesFile));
        tmpFiles[tmpf]=namesFile;tmpf++;
        String treeFile = fileManager.changedFileName(oridataFile, "_tree");
        tmpFiles[tmpf]=treeFile;tmpf++;
        //creating decision tree
        DecisionTreeBuilder treeBuilder = new DecisionTreeBuilder(namesFile,
                preimpdataFile, treeFile, DecisionTreeBuilder.SEE5);
        treeBuilder.createDecisionTree();

       //create sub data set
        FindRecordForEachLeaf(treeFile);
        //impute each leaf
        ImputeLeafEMI();

        

        
    }
//Impute numerical missing values belonging to the leaves one by one using EMI
private void ImputeLeafEMI()
{
    for(int l=0;l<noOfLeaves;l++)
    {
        if(FlgImpStatus[l]==1)
        {
            String [][]tmpData=new String [SizeOfEachLeaf[l]][totalAttrs];
            int [][]MV1=new int [SizeOfEachLeaf[l]][totalAttrs];
            int []MR1=new int [SizeOfEachLeaf[l]];
            int totMiss=0;
            //create arrays for the new EMI
            for(int i=0;i<SizeOfEachLeaf[l];i++)
            {
                int tmprec=LeafRecords[l][i];
                for(int j=0;j<totalAttrs;j++)
                {
                    tmpData[i][j]=dataset[tmprec][j];
                    MV1[i][j]=MV[tmprec][j];
                    if(MV1[i][j]==1)
                    {
                       totMiss++;
                    }
                }
                MR1[i]=MR[tmprec];
            }
            //call new EMI to impute
            mviNewEMI nemi=new mviNewEMI();
            nemi.runNewEMI(tmpData, MV1,MR1, attrNType,totMiss, 0,1);

            //update the original data set
             for(int i=0;i<SizeOfEachLeaf[l];i++)
            {
                int tmprec=LeafRecords[l][i];
                if(MR[tmprec]==1)
                {
                    for(int j=0;j<totalAttrs;j++)
                    {
                        if(MV[tmprec][j]==1)
                        {
                           dataset[tmprec][j] =tmpData[i][j];
                        }
                    }
                }
            }
        }
    }
    
}


//the method finds the records belong to each leaf
private void FindRecordForEachLeaf(String treeFile)
{
    FileManager fileManager = new FileManager();
  //retrieving logic rules

    String []tmpRules = fileManager.readFileAsArray(new File(treeFile));
    noOfLeaves=tmpRules.length-1; //as first line contains attr information
    Rules=new String[noOfLeaves];
    for(int l=0;l<noOfLeaves;l++)
         Rules[l]=tmpRules[l+1];

    int noRecNotSatisfyAnyRule=0;
    if (noOfLeaves>0)
    {
        LeafRecords=new int[noOfLeaves][totalRecords];//Records belong to each leaf
        SizeOfEachLeaf=new int[noOfLeaves];  //No. of records belong to each leaf
        FlgImpStatus=new int[noOfLeaves];
        RL=new int[totalRecords];
        for(int l=0;l<noOfLeaves;l++)
        {
            SizeOfEachLeaf[l] = 0;FlgImpStatus[l]=0;
        }
        for(int i=0;i<totalRecords;i++)
        {
            int leafId=FindLeafId(dataset[i],attrNType,totalAttrs,Rules,noOfLeaves);
            RL[i]=leafId;
            if(leafId>=0)
            {
                LeafRecords[leafId][SizeOfEachLeaf[leafId]]=i;
                if(MR[i]==1)
                {
                   FlgImpStatus[leafId]=1;
                }
                SizeOfEachLeaf[leafId]++;
            }
            else
            {
//                System.out.println ("Record "+i+" is not satisfied by any logic rules!!!");
                noRecNotSatisfyAnyRule++;
             }
        }
        if(noRecNotSatisfyAnyRule>0)
        {
            System.out.println ("Total "+noRecNotSatisfyAnyRule+" records are not satisfied by any logic rules!!!");
        }
    }
}

/*
 * this will generate data file for each leaf
 */
private int FindLeafId(String []Record, int []attrType, int noAttr,
        String []rules, int noofRules)
    {
        int leafID=-1;
        for(int i=0;i<noofRules;i++)
        {
            if(isThisRecSatisfyRule(Record,attrType,noAttr,rules[i])==1)
            {
                leafID=i;break;
            }
        }
        return leafID;
    }
/*
 * this will check whether a record satisfy a rule
 */
public int isThisRecSatisfyRule(String []Record, int []attrType, int noAttr, String rule)
    {
        int flag=0;
        String dStr,rStr;
        int match=0, condition=0;
        StringTokenizer tokenizerRule= new StringTokenizer(rule, " \t\n\r\f");
        for(int i=0;i<noAttr-1;i++)
        {
            dStr=Record[i];
            rStr=tokenizerRule.nextToken();
            if(!rStr.equals("-")&&attrType[i]!=2)
            {
                condition++;
                if(isMissing(dStr)==0)
                {
                if(attrType[i]==0)   //for categorical
                {
                    if(rStr.equals(dStr))
                    {
                        match++;
                    }

                }
                else if(attrType[i]==1)  //for numerical
                {
                  double dVal=Double.parseDouble(dStr);
                  String dh;
                  dh=rStr.substring(1, rStr.length());
                  if(rStr.startsWith("G"))
                  {
                      double drul=Double.parseDouble(dh);
                      if(dVal>drul)match++;

                  }
                  else if(rStr.startsWith("L"))
                  {
                      double drul=Double.parseDouble(dh);
                      if(dVal<=drul)match++;
                  }
                  else if(rStr.startsWith("R"))
                  {
                      int indexOfComma = dh.lastIndexOf(",");
                      double leftDh=Double.parseDouble(dh.substring(0, indexOfComma-1));
                      double rightDh=Double.parseDouble(dh.substring(indexOfComma+1, dh.length()));
                      if(leftDh==rightDh)
                      {
                            if(dVal==rightDh)match++;
                      }
                     else if(leftDh<rightDh)
                      {
                          if(dVal>=leftDh && dVal<=rightDh)match++;
                     }
                      else
                      {
                           if(dVal>=rightDh && dVal<=leftDh)match++;
                      }

                  }
                }
                }
            }
        }
       if(match==condition) flag=1;   //record satisfied the rule
       return flag;
    }
/**
 * This method imputes missing values before imputing the data set by DMI
 * through applying EMI for imputing numerical missing values
 * and kNNI for imputing categorical missing values
 *
 * @param attrFile contains 2 lines attributes types and name information
 * @param dataFile data set having missing values to be imputed
 * @param outputFile filename of the imputed data set
 * @param userKNN contains the value of k for k-nearest neighbors
 */

    public void preImputation(String attrFile, String dataFile,
            String outputFile, int userKNN)
    {
        /*
         * Call EMI to impute numerical missing values
         * if the user select a mehtod EMI
         * default method is EMI
         */
        
         mviNewEMI nem=new mviNewEMI();
         nem.runNewEMI(dataset, MV, MR, attrNType, totalMissing, 0,0);
        
        
        /*
         * Impute categorical missing values by kNNI
         */
        kNN_ary knni=new kNN_ary();
        String []testRec=new String [totalAttrs];
        int []kID=new int [userKNN];
        int flgMCat=0;//flag to avoid to find kNN for a same record
        for(int i=0;i<totalRecords;i++)
        {
            if(MR[i]==1)
            {
                flgMCat=0;
                for(int j=0;j<totalAttrs;j++)
                {
                    if(MV[i][j]==1)
                    {
                        if(attrNType[j]==0 || (attrNType[j]==1 && isMissing(dataset[i][j])==1) )
                        {
                            if(flgMCat==0)
                            {
                                System.arraycopy(dataset[i], 0, testRec, 0, totalAttrs);
                                kID=knni.runkNN(attrNType, testRec, dataset,  userKNN,i);
                                flgMCat=1;
                            }
                            if(flgMCat==1)
                            {
                                if(attrNType[j]==1)
                                {
                                   dataset[i][j]=findMeanValue(kID,j);
                                }
                                else{
                                    dataset[i][j]=findModeValue(kID,j);
                                 }
                            }
                        }
                    }

                }
            }
        }

        arrayToFile(dataset,totalRecords,totalAttrs,outputFile);

    }
/*
 * The method finds the most frequent value of a catgorical
 * attribute within a data set (presented as an array)
 */
private String findModeValue(int[]kID, int attrPos)
{
    String PreVal="?";
    int tmp_rec=kID.length;
    String []tmpDomain=new String[tmp_rec];
    int []tmpCnt=new int[tmp_rec];
    int tmpDS=0;
    for (int i=0;i<tmp_rec;i++)
    {
        if(isMissing(dataset[kID[i]][attrPos])==0)
        {
            int flg=chkDomain(tmpDomain,tmpDS,dataset[kID[i]][attrPos]);
            if(flg==-1)
            {
                tmpDomain[tmpDS]=dataset[kID[i]][attrPos];
                tmpCnt[tmpDS]++;
                tmpDS++;
            }
            else
             {
                tmpCnt[flg]++;
             }
        }
    }
    int max=-1, mIndex=-1;
    for (int i=0;i<tmpDS;i++)
    {
        if(tmpCnt[i]>max)
        {
           max =tmpCnt[i];
           mIndex=i;
        }

    }
    if (mIndex>-1) PreVal=tmpDomain[mIndex];
//    System.out.println(PreVal);
    return PreVal;
}
/*
 * The method finds the most frequent value of a catgorical
 * attribute within a data set (presented as an array)
 */
private String findMeanValue(int[]kID, int attrPos)
{
    String PreVal="0.0";
    int tmp_rec=kID.length;
    double tot=0.0;
    int tmpDS=0;
    for (int i=0;i<tmp_rec;i++)
    {
        if(isMissing(dataset[kID[i]][attrPos])==0)
        {
            tot+=Double.parseDouble(dataset[kID[i]][attrPos]);
            tmpDS++;
        }
    }
    
    if (tmpDS>0) PreVal=(tot/(double)tmpDS)+"";
    return PreVal;
}
 
/**
 * The method is used to check whether or not a given value is already in the
 * domain list.
 * @param
 * tmpDomain-contains domain values of an attribute.
 * domainSize-the total number of values of the attribute
 * curVal- the current value is to be checked with existing domain values.
 * @return flag- is an integer value indicating Exist (1) or NOT exist(0)
 */
 private int chkDomain(String []tmpDomain,int domainSize, String curVal)
    {
        int flag=-1;
        for(int i=0;i<domainSize;i++)
        {
            if(curVal.equals(tmpDomain[i]))
            {
               flag=i; break;
            }
        }
        return flag;
    }


/*
 * this method is used to write an array into a file
 * 
 */

private void arrayToFile(String [][]data, int totRec, int totAttr,String outF)
{
        FileManager fileManager=new FileManager();
        File outFile=new File(outF);
        
        for(int i=0;i<totRec;i++)
        {
           String rec="";
            
           for(int j=0;j<totAttr;j++)
           {
                if(attrNType[j]==1 && isMissing(data[i][j])==0)
                {
                    DecimalFormat df = new DecimalFormat(fStr[j]);
                    rec=rec+df.format(Double.parseDouble(data[i][j]))+", ";
                }
                else{
                    rec=rec+data[i][j]+", ";
                }
           }
           if(i<totRec-1)
               rec=rec+"\n";
           if(i==0)
               fileManager.writeToFile(outFile, rec);
           else
               fileManager.appendToFile(outFile, rec);
        }
}

/**
 * the method is used to initialize the global variables
 * @Param dataFile- contains data set
 */
private void initialize(String attrFile, String dataFile)
    {
        getAttrType(attrFile); //set attr info
        FileManager fileManager = new FileManager();
        dataset=fileManager.readFileAs2DArray(new File(dataFile));
        totalRecords=dataset.length;
        totalAttrs=dataset[0].length;
        datasetI=new String[totalRecords][totalAttrs];
        MV=new int[totalRecords][totalAttrs];
        MR=new int[totalRecords];
        int flg;
        totalMissing=0;
        for(int i=0; i<totalRecords;i++)
         {
            flg=0;
            for (int j = 0; j < totalAttrs; j++)
             {
                MV[i][j]=isMissing(dataset[i][j]);
                if(MV[i][j]==1) {flg=1;totalMissing++;}
            }
            MR[i]=flg;
        }

        int []mDecP=fileManager.attrMaxDecimalPlaces(attrNType,dataset);
        fStr=new String[totalAttrs];
        for (int c=0;c<totalAttrs;c++)
        {
            fStr[c]="";
            if (attrNType[c]==1)
            {
                String fs="";
                for(int i=0;i<mDecP[c];i++)
                {
                    fs=fs+"0";
                }
                if (mDecP[c]>0)
                {
                    fStr[c] = "####0." + fs;}
                else
                {
                    fStr[c] = "####0";}
            }
        }


    }
/**
  * set attr info
  */
    private void getAttrType(String attrFile)
    {
         FileManager fileManager=new FileManager();
         String [][]tmpAty=fileManager.readFileAs2DArray(new File(attrFile));
         int nAttr=tmpAty[0].length;
         attrNType=new int[nAttr];
         attrSType=new String[nAttr];
         for(int i=0; i<nAttr;i++)
         {
             if(tmpAty[0][i].equals("1"))
             {
                 attrNType[i]=1;
                 attrSType[i]="n";
             }
            else
             {
                 attrNType[i]=0;
                 attrSType[i]="c";
             }
         }
    }
/**
  * this function will indicate whether or not a value is missing.
  *
  * @param oStr the string to be checked
  * @return ret an integer value 0->No missing, 1->Missing
  */

 private int isMissing(String oStr)
    {
       int ret=0;
       if(oStr.equals("")||oStr.equals("?")||oStr.equals("�"))
                     {
                         ret=1;
                    }
       return ret;
    }

}
