/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi;
import SysFor.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
/**
 *
 * @author grahman
 */
public class Module {


    /**
     * Generate a name file from a data file. Also need to input a two line file
     * containing the attribute types one first line, and attribute names on second.
     * Line 1 shows attribute type for each attribute,
     * 0=categorical, 1=numerical, 2=class e.g.
     * <p>l 0 1 2 0 1</p>
     * <p>Line 2 shows attribute names, note that names can be separated by spaces,
     * tabs, or commas. However, names need to be one word, and can contain any
     * character except for comma.</p>
     * @param attrInfo the file containing attribute info on two lines
     * @param dataFile the data file we are generating the name file for
     * @param outFile the filename for the new name file
     * @return an message, either indicating success, or describing error
     */
    public String extractDomainInfo(String attrF, String dataF, String outF)
    {
        FileManager fileManager = new FileManager();
        File attrInfo=new File(attrF);
        File dataFile=new File(dataF);
        File outFile=new File(outF);
        /** read the two files and store as arrays */
        String [] attrFile = fileManager.readFileAsArray(attrInfo, 2);
        String [] dataStrings = fileManager.readFileAsArray(dataFile);
        /** store return message */
        String retStr="Resulting domain information successfully written to " + outFile.getPath();
        /** read the first line of the attribute info file to determine the number
         * and type of attributes, as well as class attribute
         */
        int [] attrTypes=null; //will store attribute type for each attribute
        String [] attrNames = null; //store the names of each attribute
        int classIndex = -1; //the attribute index of the class attribute
        int numAttrs = -1; //the number of attributes
        double [] numHighDomain=null; //store highest value for numerical attrs
        double [] numLowDomain=null;  //store lowest value for numerical attrs
        double [] intervals = null; //store the minimum interval between values
        ArrayList [] catValues = null; //for each categorical attr store a list of string values
        ArrayList [] numValues = null; //store list of numerical values
        try{
            /** tokenize first line to find out how many attributes we have, and then
             * store their type, tokenize second line to get attribute names
             */
            StringTokenizer tokens = new StringTokenizer(attrFile[0], " ,\n\t"); //remove spaces, commas, tabs and newlines
            StringTokenizer tokensNames =new StringTokenizer(attrFile[1], " ,\n\t"); //remove spaces, commas, tabs and newlines
            numAttrs = tokens.countTokens();
            attrTypes = new int [numAttrs];
            attrNames = new String[numAttrs];
            /** initalize variables for storing value info */
            numHighDomain = new double[numAttrs];
            numLowDomain = new double[numAttrs];
            intervals = new double[numAttrs];
            catValues = new ArrayList[numAttrs];
            numValues = new ArrayList[numAttrs];
            /** read attribute info, and initialize appropriate variables*/
            for(int currAttr=0; currAttr<numAttrs; currAttr++)
            {
                int currAttrType = Integer.parseInt(tokens.nextToken());//get attr type
                attrTypes[currAttr]=currAttrType;
                attrNames[currAttr]=tokensNames.nextToken(); //get attr name
                /** check for attribute type */
                if(currAttrType==2)//class attribute
                {
                    classIndex = currAttr;
                    catValues[currAttr] = new ArrayList<String>();
                }
                else if(currAttrType==0)//categorical attribute
                {
                    catValues[currAttr] = new ArrayList<String>();
                }
                else //numerical attribute
                {
                    numValues[currAttr] = new ArrayList<Double>();
                    numHighDomain[currAttr] = Double.NEGATIVE_INFINITY;//defaults
                    numLowDomain[currAttr] = Double.POSITIVE_INFINITY;//defaults
                }
            }
        }
        catch(Exception e)
        {
            retStr="There was a problem extracting data from the attribute info file.\n"+
                    "Please check the file and try again, no name file created.\n"+ e.toString();
            return retStr;
        }
        /**
         * Now we know how many attributes and types, we need to find the ranges and values.
         * For categorical attributes, we are building a list of values. For numerical
         * values, we are trying to find highest and lowest value, and storing all values
         * to an Integer list. Later we will sort these list to determine the minimum interval
         * between values, and the values for categorical.
         */
        for(int currRec=0; currRec<dataStrings.length; currRec++)
        {
            //System.out.println("rec: " + currRec + "dataStrings " + dataStrings.length + "\n" + dataStrings[currRec]);
            /**for each record, we need to tokenize the values for each attribute */

              StringTokenizer tokens = new StringTokenizer(dataStrings[currRec], " ,\t\n\r\f");
            /** for each attribute value, we determine which type */
            for(int currAttr=0; currAttr<numAttrs; currAttr++)
            {
                String currValue = tokens.nextToken();
                //System.out.println("currAttr: " + currAttr + " currValue: " + currValue);
                /** if categorical attribute, or class attribute, just add the value
                 * to the list for that attribute
                 */
                if(isMissing(currValue)==0)
                {
                    if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
                    {
                        catValues[currAttr].add(currValue);
                    }
                    /** if numerical, convert the String value to a double, and store to list.
                     * Also check if we have a new high or low value.
                     */
                    else if(attrTypes[currAttr]==1)
                    {
                        double numValue = Double.parseDouble(currValue);
                        double currMax = numHighDomain[currAttr];
                        double currLow = numLowDomain[currAttr];
                        if(numValue>currMax)
                        {
                            numHighDomain[currAttr]=numValue;
                        }
                        if(numValue<currLow)
                        {
                            numLowDomain[currAttr]=numValue;
                        }
                        numValues[currAttr].add(numValue);
                     }
                }
            }
        }

       /** remove duplicate values from each list, and find intervals for numerical
        * values
        */
        List [] allValues = new List[numAttrs];
        for(int currAttr=0; currAttr<numAttrs; currAttr++)
        {

            /** if categorical attribute, or class attribute
             */
            if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
            {
                allValues[currAttr] = GeneralFunctions.removeDuplicateValuesString(catValues[currAttr]);
            }
            /** if numerical, convert the String value to a double, and store to list.
             * Also check if we have a new high or low value.
             */
            else if(attrTypes[currAttr]==1)
            {
                allValues[currAttr] = GeneralFunctions.removeDuplicateValuesDouble(numValues[currAttr]);
                /** find intervals */
                intervals[currAttr] = GeneralFunctions.findInterval(allValues[currAttr]);
            }
        }
        /** Now have all info we need to name file, just need to build the output String */
        StringBuilder outStr = new StringBuilder();
        /*<p>Note on format of nameFile</p>
         * <ul>
         *   <li><strong>First line:</strong> class attribute index, number of class values</li>
         *   <li><strong>Second line:</strong> number of records, number of attributes</li>
         *   <li><strong>Categorical attribute:</strong> <code>c</code>, attribute name, number of categories, values</li>
         *   <li><strong>Numerical attribute:</strong> <code>n</code>, attribute name, low domain,
         *       high domain, interval, number of values</li>
         * </ul>
         */
        /** first line */
        int numClasses=0;
        if (classIndex > -1)
         numClasses= allValues[classIndex].size();
        String firstLine = classIndex + ", " + numClasses + ",\n";
        outStr.append(firstLine);
        /** second line */
        String secondLine = dataStrings.length + ", " + numAttrs + ",\n";
        outStr.append(secondLine);
        /** now for each attribute */
        for(int currAttr=0; currAttr<numAttrs; currAttr++)
        {
            StringBuilder currLine= new StringBuilder(); //better for longer strings
            /** categorical or class attribute */
            if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
            {
                currLine.append("c, ");
                currLine.append(attrNames[currAttr]); //attr name
                currLine.append(", ");
                List currCatAttr = allValues[currAttr];
                int numCats = currCatAttr.size();
                currLine.append(numCats); //number of categories
                currLine.append(", ");
                /** now print values */
                for(int i=0; i<numCats; i++)
                {
                    currLine.append(currCatAttr.get(i));
                    currLine.append(", ");
                }

            }
            /* numerical */
            else
            {
                currLine.append("n, ");
                currLine.append(attrNames[currAttr]); //attr name
                currLine.append(", ");
                double numVals = ((numHighDomain[currAttr]-numLowDomain[currAttr])/intervals[currAttr])+1;
                int nums = (int) Math.round(numVals);
                /*low domain, high domain, interval, number of values */
                String otherDetails = numLowDomain[currAttr]+ ", "+numHighDomain[currAttr]+ ", "
                        + intervals[currAttr]+ ", " + nums + ",";
                currLine.append(otherDetails);
            }
            currLine.append("\n");//end line
            outStr.append(currLine.toString());
        }
        //write the output string to file
        fileManager.writeToFile(outFile, outStr.toString());

        return retStr;
    }

//this method will normalise a datafile
 public void normaliseFile(String srcFile,String destFile,
         String []attrTypeStr,int sPos, double lb, double ub)
    {
        StringTokenizer oToken;
        String oStr;
        int noR,noA;
        double curVal;
        noA=attrTypeStr.length;
        double []max=new double[noA];
        double []min=new double[noA];
        for(int i=sPos;i<noA;i++)
        {
            max[i]=Double.NEGATIVE_INFINITY;//defaults
            min[i]=Double.POSITIVE_INFINITY;//defaults
        }
        FileManager fileManager = new FileManager();
        File outF=new File(destFile);
        String [] oDataFile = fileManager.readFileAsArray(new File(srcFile));
        noR=oDataFile.length;

        String [][]dbTmp=new String[noR][noA];

        //finding min and max values of each numerical atributes
        for(int i=0;i<noR;i++)
        {
            oToken = new StringTokenizer(oDataFile[i], " ,\t\n\r\f");
            if(sPos==1)
            {
                dbTmp[i][0]=oToken.nextToken();
            }

            for(int j=sPos;j<noA;j++)
            {
                  oStr=oToken.nextToken();
                  dbTmp[i][j]=oStr;
                  if (attrTypeStr[j].equals("n"))
                    {
                        if(isMissing(oStr)==1)
                            curVal=0.0;
                        else
                        {
                            curVal=Double.parseDouble(oStr);
                            if(curVal<min[j])min[j]=curVal;
                            if(curVal>max[j])max[j]=curVal;
                        }
                    }
            }
        }
        //now normalising att numerical attributes
        DecimalFormat df = new DecimalFormat("####0.00000");
        for(int i=0;i<noR;i++)
        {
            String rec="",newStr="";
            if(sPos==1)
            {
                rec=dbTmp[i][0];
            }

            for(int j=sPos;j<noA;j++)
            {
                oStr=dbTmp[i][j];
                if(attrTypeStr[j].equals("n"))
                {
                     if(isMissing(oStr)==1)
                     {
                         newStr=oStr;
                    }
                     else
                     {
                            curVal=Double.parseDouble(oStr);
                     double dnom=max[j]-min[j]+lb;
                     if(dnom!=0)
                     {
                        curVal=((curVal-min[j]+lb)/dnom)*ub;
                     }
                     else
                     {
                         curVal = 0.0;
                         }
                      newStr=""+df.format(curVal);
                    }
                 }
                else
                    newStr=oStr;

                if(sPos==0&&j==sPos)
                    rec=newStr;
                else
                     rec=rec+", "+newStr;
            }
             if(i<noR-1)
                 rec=rec+"\n";
             if(i==0)
                fileManager.writeToFile(outF, rec);
             else
                fileManager.appendToFile(outF, rec);
        }
    }

 /*
  * this function will indicate whether or not a value is missing.
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
//this method will normalise a data array
 private void normalise2DArray(String [][]src,String [][]dest,int[]attrType, double lb, double ub)
    {
        int noR,noA;
        double curVal;
        noA=attrType.length;
        double []max=new double[noA];
        double []min=new double[noA];
        for(int i=0;i<noA;i++)
        {
            max[i]=Double.NEGATIVE_INFINITY;//defaults
            min[i]=Double.POSITIVE_INFINITY;//defaults
        }
        copy2DArray(src,dest);
        noR=src.length;
        //finding min and max values of each numerical atributes
        for(int i=0;i<noR;i++)
        {
            for(int j=0;j<noA;j++)
            {
                 if (attrType[j]==1)
                    {
                        curVal=Double.parseDouble(dest[i][j]);
                        if(curVal<min[j])min[j]=curVal;
                        if(curVal>max[j])max[j]=curVal;
                     }
           }
        }
        //now normalising att numerical attributes
        for(int i=0;i<noR;i++)
        {
            for(int j=0;j<noA;j++)
            {
                if (attrType[j]==1)
                {
                     curVal=Double.parseDouble(dest[i][j]);
                     double dnom=max[j]-min[j]+lb;
                     if(dnom!=0)
                     {
                        curVal=((curVal-min[j]+lb)/dnom)*ub;
                     }
                     else
                        curVal=0.0;
                    dest[i][j]=curVal+"";
                 }
            }
        }
    }
/**
  * A method to copy a 2D array
  */
 private void copy2DArray(String [][]src, String [][]dest)
 {
      for(int i=0; i<src.length;i++)
     {
         System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
     }
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
 * This method calculates mean (/frequency), variance and correlation
 * of a given data set.
 */
 public void calMeanStd(String dataF, int noa,int []atype, int []ds,
        String [][]dv,String [][]dstat)
{
    FileManager fileManager = new FileManager();
    String [][] df = fileManager.readFileAs2DArray(new File(dataF));
    int nor=df.length;
    int tna=0;

    for(int j=0;j<noa;j++)
    {
        if(atype[j]==1)
        {
            tna++;
            double mean=0.0,var=0.0,std=0.0,sum=0.0,cnt=0.0;

            for(int i=0;i<nor;i++)
            {
                if(isMissing(df[i][j])==0)
                {
                    sum+=Double.parseDouble(df[i][j]);
                    cnt++;
                }
            }
            mean=sum/cnt;
            sum=0.0;
            for(int i=0;i<nor;i++)
            {
                if(isMissing(df[i][j])==0)
                {
                    sum+=Math.pow(Double.parseDouble(df[i][j])-mean,2.0);
                }
            }
            var=sum/cnt;
            std=Math.sqrt(var);
            dstat[j][0]=mean+"";
            dstat[j][1]=std+"";
        }
        else
        {
            int []frq=new int[ds[j]];
            for(int d=0;d<ds[j];d++)
            {
                frq[d]=0;
            }
            for(int i=0;i<nor;i++)
            {
                if(isMissing(df[i][j])==0)
                {
                    for(int d=0;d<ds[j];d++)
                    {
                        if(df[i][j].equals(dv[j][d]))
                        {
                            frq[d]++;break;
                        }
                    }
                }
            }
            for(int d=0;d<ds[j];d++)
            {
                dstat[j][d]=frq[d]+"";
            }
        }
    }
    String outF = fileManager.changedFileName(dataF, "_tout");
    File outFile=new File(outF);
    for(int j=0;j<noa;j++)
    {
        String rec="Attr: "+j+"\n";
        if(j==0)
            fileManager.writeToFile(outFile, rec);
        else
            fileManager.appendToFile(outFile, rec);

        for(int d=0;d<ds[j];d++)
           {
            rec=dv[j][d]+", "+dstat[j][d]+"\n";
            fileManager.appendToFile(outFile, rec);
            }
   }

  //correlation calculation
    fileManager.appendToFile(outFile, "Correlation Analysis:\n");
    int []lna=new int [tna];
    int t=0;
    for(int j=0; j<noa; j++)
    {
        if(atype[j]==1)
        {
            lna[t]=j;t++;
        }
    }
    double [][]dataNum=new double[nor][tna];
    String str="";
    for(int i=0;i<nor;i++)
    {
        for(int j=0; j<tna; j++)
        {
            str=df[i][lna[j]];
            if(isMissing(str)==1)
                dataNum[i][j]=0.0;
            else
                dataNum[i][j]=Double.parseDouble(str);
         }
    }

    //find correlation
    double [][]corNum=new double[tna][tna];
    MatrixCalculation mxCal=new MatrixCalculation();
    mxCal.computeCorrelation(dataNum,nor, tna, corNum);
    //print correlation
    DecimalFormat dfm = new DecimalFormat("####0.00");
    int n=corNum.length;
    int m=corNum[0].length;

    for (int i = 0; i < n; i++) {
        String rec="";
         for (int j = 0; j < m; j++) {
            rec=rec+dfm.format(corNum[i][j])+", ";
         }
         fileManager.appendToFile(outFile, rec+"\n");
      }

 }
/*
 * This method calculates mean (/frequency), variance and correlation
 * of the Original data set, the missing data set, and the imputed data set.
 */
public void meanStdOriMisImp(String attrF, String dataOri, String dataMis,String dataImp)
{
    FileManager fileManager = new FileManager();

    String nFile = fileManager.changedFileName(dataOri, "_tname");
    String gtest= extractDomainInfo(attrF,dataOri,nFile);
    String [] nFileT = fileManager.readFileAsArray(new File(nFile));
    int noa=nFileT.length-2;
    int []ds=new int[noa];//domain size
    int []atype=new int[noa];
    int max=0, i,j;
    StringTokenizer tokenizer;
    for(i=2;i<noa+2;i++)
    {
        tokenizer = new StringTokenizer(nFileT[i], " ,\t\n\r\f");
        String aty=tokenizer.nextToken();
        if(aty.equals("c"))
        {
            aty=tokenizer.nextToken();
            ds[i-2]=Integer.parseInt(tokenizer.nextToken());
            atype[i-2]=0;
        }
        else
        {
            ds[i-2]=2; //0->mean, 1-> variance
            atype[i-2]=1;
        }
        if(ds[i-2]>max)max=ds[i-2];
    }
   String [][]dv=new String[noa][max];
   String [][]dstat=new String[noa][max];
   for(i=2;i<noa+2;i++)
    {
        tokenizer = new StringTokenizer(nFileT[i], " ,\t\n\r\f");
        String aty=tokenizer.nextToken();
        if(aty.equals("c"))
        {
            aty=tokenizer.nextToken();
            aty=tokenizer.nextToken();
            for(j=0;j<ds[i-2];j++)
                dv[i-2][j]=tokenizer.nextToken();
        }
        else{
            dv[i-2][0]="Mean";
            dv[i-2][1]="Std";
        }
    }
    calMeanStd(dataOri,noa,atype,ds,dv,dstat);
    if (dataMis.length()>0)calMeanStd(dataMis,noa,atype,ds,dv,dstat);
    if (dataImp.length()>0)calMeanStd(dataImp,noa,atype,ds,dv,dstat);
}
}
