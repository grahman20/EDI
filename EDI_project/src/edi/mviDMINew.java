/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi;
import java.io.*;
import SysFor.*;
//import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author Geaur Rahman
 * 19/05/2011
 */
public class mviDMINew {

   /** the decision tree builder */
    private DecisionTreeBuilder treeBuilder;
   
    private String gfileDataFileIn;//contains users data file name
    private String gattrInfo; //the attribute information file, used to generate name files
    private String [] attrNames; //contain attributes name
    private String [] attrType; // "n"->numerical, "c"->categorical
    private int [] attrNType; // 1->numerical, 0->categorical, 2->class (categorical)
    private String []treeFile;//contains tree file names
    private int noOfTree;//contains no. of tree created for the dataset
    int []leafLength;//contains file name for each leaf
    private int [] missingAttrs;//1->missing, 0->Available
    private int [] missingAttrsTree;//contains tree index of an attr which has a tree
    private int [] TreeAttrs;//contains atttribute index which has a tree
    private int noOfAttrs; // total no. of attributes of the data file
    private int noOfRecords; // total no. of attributes of the data file
    private int noOfMissingAttrs; // total no. of missing attributes of the data file
   
    private String []tempFileList;
    private int tempTotalFile;
    private int [][][]LeafRecords;  //Records belong to each leaf
    private int [][]SizeOfEachLeaf;  //No. of records belong to each leaf
    private int [][]RL;  //contains a leaf id where a record belongs to.
    private int [][]FlgImpStatus;  //0->no imp required, 1->imp require but not not done, 2->imp done!
    private String [][]logicRule;//contains logic rules for each leaf of a tree
    
    private String [][]dataset;//data set
    private int []MR;  //Missing records, 0->no missing, 1->Missing
    private int [][]MV;  //Missing values, 0->no missing, 1->Missing
    
    /*
     * this method will take control and call the appropriate method
     */

    public void runDMInew(String attrFile, String dataFile,String outputFile)
    {
//        FileManager fileManager=new FileManager();
//        String [][]tmpData=fileManager.readFileAs2DArray(new File(dataFile));
//        int records=tmpData.length;
//        int atttrs=tmpData[0].length;
//        int [][]mv=new int[records][atttrs];
//        int []mr=new int[records];
//        int totalMissingValues=0;
//        for(int i=0; i<records;i++)
//        {
//            int flg=0;
//            for (int j=0; j<atttrs;j++)
//            {
//                mv[i][j]=isMissing(tmpData[i][j]);
//                if(mv[i][j]==1)
//                {
//                    totalMissingValues++; flg=1;
//                }
//            }
//            mr[i]=flg;
//        }
//        runDMIimp(attrFile,dataFile,mr,mv,totalMissingValues);
//        arrayToFile(dataset,records,atttrs,outputFile);

    }

    // this method is used to print score to file
private void arrayToFile(String [][]data, int totRec, int totAttr,String outF)
{
        FileManager fileManager=new FileManager();
        File outFile=new File(outF);
        for(int i=0;i<totRec;i++)
        {
            String rec="";
            for(int j=0;j<totAttr;j++)
           {
            rec=rec+data[i][j]+", ";
           }
           if(i<totRec-1)
               rec=rec+"\n";
           if(i==0)
               fileManager.writeToFile(outFile, rec);
           else
               fileManager.appendToFile(outFile, rec);
        }
}

    /*
     * this method will take a preprocessed data set then again impute
     * the missing values using DMI approach
     * @param MR Missing records, 0->no missing, 1->Missing
     * @param MV Missing values, 0->no missing, 1->Missing
     * @param totalMV total Missing records
     */

    public String[][] runDMIimp(String attrFile, String dataFile,
            int []MR1, int [][]MV1, int totalMV)
    {
        MR=MR1;
        MV=MV1;
//        totalMissingValues=totalMV;
        initialize(attrFile,dataFile);
        CreateDTs();
        FindRecordForEachLeaf();
        ImputeLeafEMI();
     
        //remove tmp files
        FileManager fileManager=new FileManager();
        fileManager.removeListOfFiles(tempFileList, tempTotalFile);
        //remove tree files
         fileManager.removeListOfFiles(treeFile, noOfTree);


        return dataset;
    }



/*
 * this method initializes the MVI framework
 */
public void initialize(String attrFile, String dataFile)
    {
        gattrInfo=attrFile;//assign attribute file name
        gfileDataFileIn=dataFile;//assign data file name
        
        noOfRecords=MV.length;
        FileManager fileManager=new FileManager();
        String [][]tmpAty=fileManager.readFileAs2DArray(new File(attrFile));
        dataset=fileManager.readFileAs2DArray(new File(dataFile));
        noOfAttrs=tmpAty[0].length;
        attrNType=new int[noOfAttrs];
        attrType=new String[noOfAttrs];
        attrNames=new String[noOfAttrs];
        missingAttrs=new int[noOfAttrs];
        for(int i=0; i<noOfAttrs;i++)
         {
            attrNames[i]=tmpAty[1][i];
            if(tmpAty[0][i].equals("1"))
             {
                 attrNType[i]=1;
                 attrType[i]="n";
             }
            else
             {
                 
                 attrType[i]="c";
                 attrNType[i]=0;
             }
         }
        noOfMissingAttrs=0;
        for (int j=0; j<noOfAttrs;j++)
        {
            missingAttrs[j]=0;
            for(int i=0; i<noOfRecords;i++)
            {
                if (MV[i][j] == 1)
                {
                    missingAttrs[j]=1;
                    noOfMissingAttrs++;
                    break;
                }
            }
        }

      tempFileList=new String[50];
      tempTotalFile=0;
    }

/*
 * The method builds DT for each attrinbute having missing values.
 */
private void CreateDTs()
{
    FileManager fileManager = new FileManager();
    noOfTree=0;
    treeFile=new String[noOfMissingAttrs];
    missingAttrsTree=new int[noOfAttrs];
    leafLength=new int[noOfMissingAttrs];
    TreeAttrs=new int[noOfAttrs];
    String tmpDataF= fileManager.changedFileName(gfileDataFileIn, "_tmp");
    tempFileList[tempTotalFile]=tmpDataF;tempTotalFile++;
    String nameFile_DT= fileManager.changedFileName(gfileDataFileIn, "_tmpName");
    tempFileList[tempTotalFile]=nameFile_DT;tempTotalFile++;
    String tmpAttrF= fileManager.changedFileName(gattrInfo, "_tmp");
    tempFileList[tempTotalFile]=tmpAttrF;tempTotalFile++;
    for(int i=0;i<noOfAttrs;i++)
        {
            if(missingAttrs[i]==1)//1=missing, 0=no missing
            {
                if(attrType[i].equals("n"))
                {
                 //generalize
                    generalise(gfileDataFileIn, tmpDataF, i,noOfAttrs);
                }
                else
                {
                    fileManager.copyFile(gfileDataFileIn, tmpDataF);
                }
                setClassAttribute(gattrInfo, tmpAttrF, i,noOfAttrs);
                String gtest=fileManager.extractNameFileFromDataFile(new File(tmpAttrF),
            new File(tmpDataF),new File(nameFile_DT));
               //creating decision tree
               String nameFile_out= fileManager.changedFileName(gfileDataFileIn, "_"+attrNames[i]+"_DT");
               treeBuilder = new DecisionTreeBuilder(nameFile_DT,
                       tmpDataF, nameFile_out, DecisionTreeBuilder.SEE5);

               treeBuilder.createDecisionTree();
               treeFile[noOfTree]= nameFile_out;
               leafLength[noOfTree]=noOfRules(nameFile_out);
               missingAttrsTree[i]=noOfTree;
               TreeAttrs[noOfTree]=i;
//               if(noOfTree==0)
//                    leafStart[noOfTree]= noOfTree;
//               else
//                   leafStart[noOfTree]=leafLength[noOfTree-1]+ leafStart[noOfTree-1];
               noOfTree++;
            }
        }

}
//Impute numerical missing values belonging to the leaves one by one using EMI
private void ImputeLeafEMI()
{
    for(int t=0;t<noOfTree;t++)
    {
        if(attrNType[TreeAttrs[t]]==1)
        {
        for(int l=0;l<leafLength[t];l++)
        {
            if(FlgImpStatus[t][l]==1)
            {
            String [][]tmpData=new String [SizeOfEachLeaf[t][l]][noOfAttrs];
            int [][]MV1=new int [SizeOfEachLeaf[t][l]][noOfAttrs];
            int []MR1=new int [SizeOfEachLeaf[t][l]];
            int totMiss=0;
            //create arrays for the new EMI
            for(int i=0;i<SizeOfEachLeaf[t][l];i++)
            {
                int tmprec=LeafRecords[t][l][i];
                for(int j=0;j<noOfAttrs;j++)
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
            //
//            printStrArray("Before EMI: Tree:"+t+", Leaf:"+l,tmpData);
            //

            //call new EMI to impute
            mviNewEMI nemi=new mviNewEMI();
            nemi.runNewEMI(tmpData, MV1,MR1, attrNType,totMiss, 0,1);

            //
//            printStrArray("After EMI: Tree:"+t+", Leaf:"+l,tmpData);
            //

            //update the original data set
            for(int i=0;i<SizeOfEachLeaf[t][l];i++)
            {
                int tmprec=LeafRecords[t][l][i];
                if(MR[tmprec]==1 && MV[tmprec][TreeAttrs[t]]==1)
                {
                    if(isMissing(tmpData[i][TreeAttrs[t]])==0)
                    {
                    dataset[tmprec][TreeAttrs[t]] =tmpData[i][TreeAttrs[t]];
                    }
                }
            }
        }
       }
        }
    }
}
/**
  * the method prints the values of an given string array
  * @param
  * Msg- the message to be printed as a label.
  * sv-contains similarity values of the attribute categories
  */
private void printStrArray(String Msg, String [][]sv)
{
        System.out.print(Msg+"\n");
        for(int i=0;i<sv.length;i++)
        {
            for(int j=0;j<sv[0].length;j++)
            {
                System.out.print(sv[i][j]+",  ");
            }
            System.out.print("\n");
         }
}
//the method finds the records belong to each leaf
private void FindRecordForEachLeaf()
{
    FileManager fileManager = new FileManager();
  //retrieving logic rules
    
    if(noOfTree>0)
    {
        int maxLeaf=0;
        for(int t=0;t<noOfTree;t++)
        {
            if(leafLength[t]>maxLeaf)maxLeaf=leafLength[t];
        }
        LeafRecords=new int[noOfTree][maxLeaf][noOfRecords];//Records belong to each leaf
        SizeOfEachLeaf=new int[noOfTree][maxLeaf];  //No. of records belong to each leaf
        FlgImpStatus=new int[noOfTree][maxLeaf];
        RL=new int[noOfTree][noOfRecords];
        logicRule=new String[noOfTree][maxLeaf];
        String [][]MajorityVal=new String[noOfTree][maxLeaf];
        for(int t=0;t<noOfTree;t++)
        {
           if(leafLength[t]>0)
           {
           int atype[]=new int[noOfAttrs];
           for (int i=0;i<noOfAttrs;i++)        
                   atype[i]=attrNType[i];
           atype[TreeAttrs[t]]=2;

           String []tmpRules = fileManager.readFileAsArray(new File(treeFile[t]));
           for(int l=0;l<leafLength[t];l++)
           {
                 logicRule[t][l]=tmpRules[l+1];
                 if (attrNType[TreeAttrs[t]]!=1)
                 {
                     MajorityVal[t][l]=findMajorityClassValues(atype,noOfAttrs,logicRule[t][l]);
                  }
                 SizeOfEachLeaf[t][l] = 0;
                 FlgImpStatus[t][l]=0;
            }
           
           int noRecNotSatisfyAnyRule=0;
           for(int i=0;i<noOfRecords;i++)
           {
                int leafId=FindLeafId(dataset[i],atype,noOfAttrs,logicRule[t],leafLength[t]);
                RL[t][i]=leafId;
                if(leafId>=0)
                {
                    LeafRecords[t][leafId][SizeOfEachLeaf[t][leafId]]=i;

                    if(MV[i][TreeAttrs[t]]==1)
                    {
                        if (attrNType[TreeAttrs[t]]==1 )
                        {
                            FlgImpStatus[t][leafId] = 1;
                        }
                        else 
                        {
                            dataset[i][TreeAttrs[t]]= MajorityVal[t][leafId];
                        }
                    }
                    SizeOfEachLeaf[t][leafId]++;
                }
                else
                {
                    noRecNotSatisfyAnyRule++;
                }
            }
            if(noRecNotSatisfyAnyRule>0)
            {
//                System.out.println ("Total "+noRecNotSatisfyAnyRule+" records are not satisfied by any logic rules of tree"+t);
            }
            }
        }
    }
}
/*
 * this will find the majority class value of a leaf
 */
public String findMajorityClassValues(int []attrType, int noAttr, String rule)
    {
        String rStr,cv="?";
        String data="";
        StringTokenizer tokenizerRule= new StringTokenizer(rule, " \t\n\r\f");
        for(int i=0;i<noAttr;i++)
        {
            rStr=tokenizerRule.nextToken();
            if(!rStr.equals("-")&&attrType[i]==2)
            {
               data= rStr;
               break;
            }
        }
        String tcv="";
        if(!data.equals(""))
        {
            int max=0,cmax=0;
            String cVal="",temp="";
            StringTokenizer tokenizerData = new StringTokenizer(data, " {};,\t\n\r\f");
            int cnt=tokenizerData.countTokens();
            for(int i=0;i<cnt;i++)
            {
                cVal=tokenizerData.nextToken();
                tcv=cVal;
                i++;
                temp=tokenizerData.nextToken();
                cmax=Integer.parseInt(temp);
                if(cmax>max)
                {
                   max=cmax;
                   cv=cVal;
                }
            }
        }
        if(cv.equals("?"))
        {
            cv=tcv;
        }
       return cv;
    }
/*
 * this will generate data file for each leaf
 */
private int FindLeafId(String []Record, int []attrType, int noAttr,
        String []rules, int noofRules)
    {
        int leafID=0;
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
 * this method set attrpos as class attribute in the dtNameFile
 */
public void setClassAttribute(String srcNameFile, String dtNameFile,int attrPos,int totalAttr)
    {
        
        StringTokenizer tokenizer;
        FileManager fileManager_g = new FileManager();

        String [] nameFile_g = fileManager_g.readFileAsArray(new File(srcNameFile));
        File outFile = new File(dtNameFile);
        
        
        String val;
        String []attrInfoG=new String[totalAttr];
        String []attrName=new String[totalAttr];
        for(int i=0; i<2;i++)
         {
            
            tokenizer = new StringTokenizer(nameFile_g[i], " ,\t\n\r\f");
            for(int j=0; j<totalAttr;j++)
                {
                    val=tokenizer.nextToken();
                    if(i==0)
                    {
                    if(val.equals("2"))
                            val="0";
                    if(j==attrPos)
                             val="2";
                    attrInfoG[j]=val;
                    }
                    else
                    {
                      attrName[j]=val;
                    }
                }
            String rec="";
            for(int j=0; j<totalAttr;j++)
                {
                    if(i==0)
                    {
                        if(j==0)
                            rec=attrInfoG[j];
                        else
                            rec=rec+" "+attrInfoG[j];
                    }
                    else
                    {
                      if(j==0)
                            rec=attrName[j];
                        else
                            rec=rec+","+attrName[j];
                    }
                }
          
            if(i==0) 
            {
                rec=rec+"\n";
                fileManager_g.writeToFile(outFile, rec);
             }
            else
                fileManager_g.appendToFile(outFile, rec);
            }
     }

/*
 * this method is used to exchange data between i-th position (attrPos) and the last place
 */
public void exChangeAttrPosition(String dtFile,int attrPos,int totalAttr)
    {
        int i,j;
        StringTokenizer tokenizer;
        FileManager fileManager_g = new FileManager();
        String [] dataFile_g = fileManager_g.readFileAsArray(new File(dtFile));
        File outFile = new File(dtFile);
        int noOfRec=dataFile_g.length;
        String []attrName=new String[totalAttr];
        for(i=0; i<noOfRec;i++)
         {

            tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
            for(j=0; j<totalAttr;j++)
                 attrName[j]=tokenizer.nextToken();
           String gtemp;
           gtemp=attrName[attrPos];
           attrName[attrPos]=attrName[totalAttr-1];
           attrName[totalAttr-1]=gtemp;
           String rec="";
           for(j=0; j<totalAttr;j++)
               if(j==0)
                     rec=attrName[j];
               else
                     rec=rec+", "+attrName[j];
           rec=rec+"\n";
           if(i==0)
                fileManager_g.writeToFile(outFile, rec);
           else
                fileManager_g.appendToFile(outFile, rec);
         }
    }
/*
 * this method is used to generalise a numerical attribute (attrPos)
 * into log|domainsize of attrPos| categories
 */
public void generalise(String srcFile, String dtFile,int attrPos,int totalAttr)
    {
        int i,j;
        StringTokenizer tokenizer;
        FileManager fileManager = new FileManager();
        String [][] dataFile = fileManager.readFileAs2DArray(new File(srcFile));
        File outFile = new File(dtFile);
        int noOfRec=dataFile.length;
        double []domain=new double[noOfRec];
        int domainSize=0;
        String val;
        double cval;
        for(i=0; i<noOfRec;i++)
        {
            val=dataFile[i][attrPos];
            if(isMissing(val)==0)
            {
                cval= Double.parseDouble(val);
                if(chkDomain(domain,domainSize,cval)==0)
                {
                    domain[domainSize]=cval;
                    domainSize++;
                }
            }
        }
        int NofGroups;
        if(domainSize>2)
//            NofGroups=(int)Math.round(Math.log1p((double)domainSize));
            NofGroups=(int)Math.round(Math.sqrt((double)domainSize));
        else
            NofGroups=domainSize;
        //Sorting by using sort(double[] d) method.
        Arrays.sort(domain,0,domainSize);

       int groupSize=0;
       if(NofGroups>0)
       {
           groupSize=(int)domainSize/NofGroups;
           int rem=domainSize%NofGroups;
           if(rem>0)
           {
              if (rem>(int) NofGroups/2.0)
              {
                  NofGroups++;
                  rem=0;
              }
           }
           double []lowDomain=new double[NofGroups];
           double []highDomain=new double[NofGroups];
           for(i=0,j=0 ;i<domainSize && j<NofGroups;j++)
           {
               lowDomain[j]=domain[i];
               i=i+groupSize-1;
               if(i>=domainSize) i=domainSize-1;
               highDomain[j]=domain[i];
               i++;
           }
      if(rem>0)
            highDomain[j-1]=domain[domainSize-1];
      
      for(i=0; i<noOfRec;i++)
        {
           String rec="";
            val=dataFile[i][attrPos];
            if(isMissing(val)==1)val="0";
            cval= Double.parseDouble(val);
            int fg=-1;
            for(j=0;j<NofGroups;j++)
            {
                if(lowDomain[j]<=cval && cval<=highDomain[j])
                {
                    fg=j;break;
                }
            }
            String rng;
            if(fg==-1)
            {
               rng= cval+"-"+cval;
            }
            else
            {
                rng= lowDomain[fg]+"-"+highDomain[fg];
            }
           dataFile[i][attrPos]=rng;
           for(j=0;j<totalAttr;j++)
               rec=rec+dataFile[i][j]+", ";
           //write record to file
           if(i==0)
           {
               rec=rec+"\n";
               fileManager.writeToFile(outFile, rec);
           }
           else
           {
                if(i<noOfRec-1)  rec=rec+"\n";
                fileManager.appendToFile(outFile, rec);
            }
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
       if(oStr.equals("")||oStr.equals("?")||oStr.equals("�")||oStr.equals("NaN")||oStr.equals("  NaN"))
                     {
                         ret=1;
                    }
       return ret;
    }
private int chkDomain(double []domain,int domainSize, double curVal)
    {
        int flag=0;
        for(int i=0;i<domainSize;i++)
        {
            if(curVal==domain[i])
            {
               flag=1; break;
            }
        }
        return flag;
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
        for(int i=0;i<noAttr;i++)
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

 
 
/*
 * this will return the no. of rules for a specific tree
 */
public int noOfRules(String treeFile)
    {
        FileManager fileManager_g = new FileManager();
        String [] rules = fileManager_g.readFileAsArray(new File(treeFile));
        int noR=rules.length-1;
        if(noR<0) noR=0;
        return noR;
    }



}
