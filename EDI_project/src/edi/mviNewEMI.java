/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi;
import SysFor.*;
import java.io.*;
import java.util.*;
import Jama.*;
/**
 *
 * @author grahman
 */
public class mviNewEMI
{
    /** used for managing reading and writing to file */

double[] attrMean;
String  []aty;
/**
 * Implementation of the EMI algorithm
 *
 * @param attrFile contains 2 lines attributes types and name information
 * @param dataFile data set having missing values to be imputed
 * @param outputFile filename of the imputed data set
 */
    public void runEMI(String attrFile, String dataFile, String outputFile)
    {
        FileManager fileManager = new FileManager();
        String [][]dataset=fileManager.readFileAs2DArray(new File(dataFile));
        int totalRecords=dataset.length;
        int totalAttrs=dataset[0].length;
        int []attrNType=new int[totalAttrs];
        String  []attrSType=new String[totalAttrs];
        aty=new String[totalAttrs];
        int numAttr=getAttrType(attrFile,attrNType,attrSType); //set attr info
        int [][]MV=new int[totalRecords][totalAttrs];
        int []MR=new int[totalRecords];
        int flg;
        int totalMissing=0;
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

       if(totalMissing>0)
        {
           runNewEMI(dataset, MV,MR, attrNType,totalMissing, 0,0);
        }
       //print to a file
       arrayToFile(dataset,outputFile);
          
   }
   // this method is used to print score to file
private void arrayToFile(String [][]data,String outF)
{
        FileManager fileManager=new FileManager();
        int totalRecords=data.length;
        int totalAttrs=data[0].length;
        File outFile=new File(outF);
        for(int i=0;i<totalRecords;i++)
        {
            String rec="";
            for(int j=0;j<totalAttrs;j++)
           {
            rec=rec+data[i][j]+", ";
           }
           if(i<totalRecords-1)
               rec=rec+"\n";
           if(i==0)
               fileManager.writeToFile(outFile, rec);
           else
               fileManager.appendToFile(outFile, rec);
        }
}
/**
 * runNewEMI in the implementation of the EMI algorithm
 *
 * @param data 2D array takes data elements as string, and contains the imputed values
 * @param MissingMatrix 2D array where 1-> Missing, 0->non-missing
 * @param MissingRecord 1D array where 1-> Missing in a record, 0->non-missing
 * @param attrNType 1D array where 1-> numerical, 0->categorical
 * @param sPos indicates the starting attr position of a record
 * @param MissAvailable 0-> consider missing is missing, 1->consider missing is available
 */
    
public void runNewEMI(String [][]data, int [][]MissingMatrix,
        int[] MissingRecord, int[] attrNType, int totalMissing, int sPos,int MissAvailable)
{
    int totalRecords=data.length;
    int totalAttrs=data[0].length;
    int []avgImputation=new int[totalAttrs];
    int[] newAttrNType=new int[totalAttrs];
    double[] attrAvgVal=new double[totalAttrs];
    attrMean=new double[totalAttrs];

    for(int c=sPos; c<totalAttrs;c++)
    {
         avgImputation[c]=0; newAttrNType[c]=attrNType[c];attrAvgVal[c]=0.0; attrMean[c]=0.0;
    }
    calAttrMean(data,attrNType);
    //checking whether or not require avg imputation
    int noOfAvgImp=chkAvgImp(data,totalRecords,totalAttrs,
            attrNType,sPos,avgImputation, newAttrNType,attrAvgVal);
    //copy string data into an array of double type and cal avg
    int numAttr=0;
    for(int c=sPos; c<totalAttrs;c++)
    {
            if(newAttrNType[c]==1)
            {
                numAttr++;
            }
    }
   
    
    if(totalRecords>numAttr)
    {//EM imputation
        emAlgorithmImplementation(data,totalRecords, totalAttrs,numAttr,
             totalMissing, MissingMatrix, MissingRecord, newAttrNType, sPos,MissAvailable);
    }
    else
    {   //mean imputation
         for(int c=sPos; c<totalAttrs;c++)
        {
            if(newAttrNType[c]==1)
            {
                double total=0.0; int cnt=0;
                for(int i=0; i<totalRecords;i++)
                {
                    if(isMissing(data[i][c])==0)
                    {
                        total+=Double.parseDouble(data[i][c]);
                        cnt++;
                    }
                }
                if(cnt>0)attrAvgVal[c]=total/(double)cnt;
            }

        }

        noOfAvgImp=0;
        for(int c=sPos; c<totalAttrs;c++)
        {
            if(attrNType[c]==1)
            {
                avgImputation[c]=1;noOfAvgImp++;
            }
        }
     }

    //if avg imp require then imputes by avg value
    if(noOfAvgImp>0)
    {
        for(int c=sPos; c<totalAttrs;c++)
        {
            if(avgImputation[c]==1)
            {
               for(int i=0; i<totalRecords;i++)
                {
                    if(MissingMatrix[i][c]==1)
                    {
                        data[i][c]=attrAvgVal[c]+"";
                    }
                }

            }

        }
    }


}
//calculate attr mean

private void calAttrMean(String [][]data, int []attrType)
{
    int totalAttrs=data[0].length;
    int totalRecords=data.length;
    for(int c=0; c<totalAttrs;c++)
        {
            if(attrType[c]==1)
            {
                double total=0.0; int cnt=0;
                for(int i=0; i<totalRecords;i++)
                {
                    if(isMissing(data[i][c])==0)
                    {
                        total+=Double.parseDouble(data[i][c]);
                        cnt++;
                    }
                }
                if(cnt>0)attrMean[c]=total/(double)cnt;
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

 

    /**
  * set attr info and return no. of numerical attr
  */
    private int getAttrType(String attrFile,int []attrNType,String  []attrSType)
    {
         FileManager fileManager=new FileManager();
         String [][]tmpAty=fileManager.readFileAs2DArray(new File(attrFile));
         int nAttr=tmpAty[0].length;
         int numAttr=0;
         for(int i=0; i<nAttr;i++)
         {
             if(tmpAty[0][i].equals("1"))
             {
                 attrNType[i]=1;
                 attrSType[i]="n";
                 aty[i]="n";
                 numAttr++;
             }
            else
             {
                 attrNType[i]=0;
                 attrSType[i]="c";
                 aty[i]="c";
             }
         }
        return numAttr;
    }
    /*
     * check any attr require avg imputation
     */
    private int chkAvgImp(String [][]dFile,int noOfRecords,int noOfAttrs,
            int []attrType,int sPos,int []avgImputation,
            int []NewAttrNType,double[] attrAvgVal )
    {
         
         int totAvg=0;
         double preVal, curVal;
         String str="";
         for(int c=sPos;c<noOfAttrs;c++)
         {
             if (attrType[c]==1)
            {
                int cnt=0;
                preVal=0.0;curVal=0.0;
                for(int i=0; i<noOfRecords;i++)
                {
                    str=dFile[i][c];
                    if (isMissing(str)==1)
                        curVal=0.0;
                    else
                        curVal=Double.parseDouble(str);
                    if(curVal!=preVal)
                    {
                        preVal=curVal;
                        cnt++;
                    }

                   if(cnt>=2)break;
                }
                if(cnt<=1&& noOfRecords!=1)
                 {
                        avgImputation[c]=1;
                        NewAttrNType[c]=0;
                        attrAvgVal[c]=curVal;
                        totAvg++;
                 }
             }
         }
         return totAvg;
    }
    /*
     * Implementation of EM algorithm here for MVI
     */
    public void emAlgorithmImplementation(String [][]data,int noOfRecords,
            int noOfAttrs,int noOfNumericalAttrs, int totalMissingValues,
            int [][]MissingMatrix, int[] MissingRecord,
            int []attrNType, int sPos,int MissAvailable)
    {
        double [][]dataOriginal=new double[noOfRecords][noOfNumericalAttrs];

        double val;
        int [][]nMissingMatrix=new int[noOfRecords][noOfNumericalAttrs]; //0->available, 1->Missing, numerical only
        int[] nMissingRecord=new int[noOfRecords];
        //finding the cell locations (row, column) of missing values
        //into the arrays msRow and msCol respectively
        //and finding the data without categorical attributes into  dataOriginal array
        int nc=0;
        
        for(int j=0;j<noOfAttrs;j++)
        {
            if (attrNType[j]==1)
            {
                for(int i=0; i<noOfRecords;i++)
                {
                        if (MissingMatrix[i][j]==1)
                        {
                           if(isMissing(data[i][j])==1)
                               val = 0.0;
                           else
                               val=Double.parseDouble(data[i][j]);
                            nMissingMatrix[i][nc]=1;
                           
                        }
                        else
                        {
                           val=Double.parseDouble(data[i][j]);
                           nMissingMatrix[i][nc]=0;
                        }
                        dataOriginal[i][nc]=val;
                }
                nc++;
            }
        }
        for(int i=0; i<noOfRecords;i++)
        {
            nMissingRecord[i]=0;
            for(int j=0;j<noOfNumericalAttrs;j++)
            {
                if (nMissingMatrix[i][j]==1)
                 {
                    nMissingRecord[i]=1;break;
                }
            }

        }

        double [][]dataCurrent=new double[noOfRecords][noOfNumericalAttrs];
        double []mu=new double[noOfNumericalAttrs];
        initilizeArray(mu);
        double []muPrevious=new double[noOfNumericalAttrs];
        double [][]cov=new double[noOfNumericalAttrs][noOfNumericalAttrs];
        double [][]covPrevious=new double[noOfNumericalAttrs][noOfNumericalAttrs];
           
       MatrixCalculation mxCal=new MatrixCalculation();

       int T=0;
       int meanError;
       int covError;
       int loopTerminator =generateLoopTerminator();
       //copy of original data for updating
       for(int i=0; i<noOfRecords;i++)
       {
           for(int j=0; j<noOfNumericalAttrs;j++)
           {
                dataCurrent[i][j]=dataOriginal[i][j];
           }
        }

       do{
           for(int i=0; i<noOfNumericalAttrs;i++)
           {
               muPrevious[i]=mu[i];
               for(int j=0; j<noOfNumericalAttrs;j++)
               {
                    covPrevious[i][j]=cov[i][j];
               }
            }
           
           computeMean(dataCurrent, nMissingMatrix, MissAvailable, mu);
           mxCal.computeCovariance(dataCurrent, noOfRecords, noOfNumericalAttrs,  mu,cov);

           for(int i=0;i<noOfRecords;i++)
           {
               if(nMissingRecord[i]==1)
               {
                emImputation(dataCurrent,i,nMissingMatrix,T,mu,cov,noOfNumericalAttrs);
               }
           }

           //mean error calculation
           meanError= calMeanError(muPrevious,mu);
           //covariance matrix (det) error calculation
           covError=calCovError(covPrevious,cov);
           T++;
           if(T>loopTerminator)break;
//           if(T>5)break;
//           if(T%1000==0)
//               System.out.println("EM iteration: "+T);

       }while(meanError==1 || covError==1);

       /*
        * Imputation done!
        * copying data from double array to string array
        */
        //writing dataset to the file oFile after imputation
       nc=0;
       for(int c=0;c<noOfAttrs;c++)
       {
           if(attrNType[c]==1)
           {
               for(int i=0; i<noOfRecords;i++)
               {
                   if(MissingMatrix[i][c]==1)
                   {
                       if(dataCurrent[i][nc]!=Double.NaN)
                       {
                            data[i][c]= dataCurrent[i][nc]+"";
                       }
                       if(isMissing(data[i][c])==1)
                       {
                            data[i][c]=attrMean[c]+"";
                       }
                   }
               }
               nc++;
           }
       }
    }
   

    //impute value for a specific cell and
    //EM Implementation.
    public void emImputation(double [][]dataElement,int msRow,int [][]MissingMatrix,
            int T, double[]mu,double [][]cov,
            int noOfNumericalAttrs)
    {
       int r,c,k,j;
       int m=0;
       
       for(j=0; j<noOfNumericalAttrs;j++)
       {
           if(MissingMatrix[msRow][j]==1)m++;
        }

       int a=noOfNumericalAttrs-m;
       double []X_a=new double[a];
       double []X_m=new double[m];
       double []Mu_a=new double[a];
       double []Mu_m=new double[m];
       double [][]cov_aa=new double[a][a];
       double [][]cov_mm=new double[m][m];
       double [][]cov_am=new double[a][m];
//       double [][]B=new double[a][m];
      
       Matrix matX_a,matX_m, matMu_a,matMu_m,matcov_aa,matcov_mm,matcov_am,matcov_ma;
       Matrix matinvOfcov_aa,matB,  matC,mate;
       // finding available data elements for the record msRow
       // finding the mean vector except attribute msCol
       k=0;
       for(c=0;c<noOfNumericalAttrs;c++)
       {
           if(MissingMatrix[msRow][c]==0)
           {
               X_a[k]=dataElement[msRow][c];
               Mu_a[k]=mu[c];
               k++;
           }
       }

       MatrixCalculation mxCal=new MatrixCalculation();
       //converting the data array to matrix
       matX_a= mxCal.oneDArrayToMatrix(X_a);
//       matX_a.print(4,2);
       matMu_a= mxCal.oneDArrayToMatrix(Mu_a);
//       matMu_a.print(4,2);
       //initialising missing cell
        for(c=0,k=0;c<noOfNumericalAttrs;c++)
        {
            if(MissingMatrix[msRow][c]==1)
            {
                X_m[k]=dataElement[msRow][c];
                Mu_m[k]=mu[c];//mean value of missing attribute
                if (k<m)k++;
            }
        }
       
       matX_m= mxCal.oneDArrayToMatrix(X_m);//convert to matrix
//       matX_m.print(4,2);
       matMu_m= mxCal.oneDArrayToMatrix(Mu_m);//convert to matrix
//       matMu_m.print(4,2);

       //if do not have any availble values then simple add the mean
       if(a>0)
       {
           //finding covariance only for the attributes having available values
           k=0;
           for(r=0;r<noOfNumericalAttrs;r++)
           {
               j=0;
               if(MissingMatrix[msRow][r]==0)
               {
                   for(c=0;c<noOfNumericalAttrs;c++)
                   {
                       if(MissingMatrix[msRow][c]==0)
                       {
                           cov_aa[k][j]=cov[r][c];
                           j++;
                       }
                   }
                   k++;
               }
            }
//           int covMN=k;
//           int covMM=j;
           matcov_aa= new Matrix(cov_aa);//convert to matrix of available cov
          double tt=matcov_aa.det();
//          System.out.println(tt);
          if(tt!=0)
          {
    
          k=0;
           for(r=0;r<noOfNumericalAttrs;r++)
           {
               j=0;
               if(MissingMatrix[msRow][r]==1)
               {
                   for(c=0;c<noOfNumericalAttrs;c++)
                   {
                       if(MissingMatrix[msRow][c]==1)
                       {
                           cov_mm[k][j]=cov[r][c];
                           j++;
                       }
                   }
                   k++;
               }
            }


           matcov_mm=new Matrix(cov_mm);//convert to matrix
    //       matcov_mm.print(4,2);
           //finding cross covariance for avaliable and missing
           k=0;
           for(r=0;r<noOfNumericalAttrs;r++)
           {
               j=0;
               if(MissingMatrix[msRow][r]==0)
               {
                   for(c=0;c<noOfNumericalAttrs;c++)
                   {
                       if(MissingMatrix[msRow][c]==1)
                       {
                           cov_am[k][j]=cov[r][c];
                           j++;
                       }
                   }
                   k++;
               }
            }
          matcov_am= new Matrix(cov_am);//convert to matrix of cross cov of available & missing
    //      matcov_am.print(4,2);
          matcov_ma=matcov_am.transpose(); //create matrix of cross cov for missing & available
    //      matcov_ma.print(4,2);


          //implementing EM equation

          try
          {
          matinvOfcov_aa=matcov_aa.inverse();
//            matinvOfcov_aa=mxCal.pesudoInverse(matcov_aa, covMN, covMM);

    //      matinvOfcov_aa.print(4,2);
          matB=matinvOfcov_aa.times(matcov_am);
    //      matB.print(4,2);
    //      mattransposeOfB=matB.transpose();
          matC=matcov_ma.times(matinvOfcov_aa.times(matcov_am));
    //      matC.print(4,2);

          ////
          Matrix Xa_Minus_Mua=matX_a.minus(matMu_a);
          Matrix Xa_Minus_Mua_TimesB=Xa_Minus_Mua.times(matB);
          matX_m=matMu_m.plus(Xa_Minus_Mua_TimesB);

          ///following the calculation of the residual matrix e (1xPm), for first iteration
          // with mean zero and unknown covariance matrix C (Pm x Pm)
          if(T==0)
          {
            mate = generateResidualMatrix(X_m,matMu_m, matcov_mm,matC, msRow, m);
            matX_m=matX_m.plus(mate);
            }
              }
          catch(Exception ex)
          {
            matX_m=matMu_m;
          }
           }
         else
          {
              matX_m=matMu_m;
            }
        }
       else
        {
           matX_m=matMu_m;
        }
      //result
      mxCal.matrixTo1DArray(matX_m,X_m,m);
      // update dataset
       for(c=0,k=0;c<noOfNumericalAttrs;c++)
        {
            if(MissingMatrix[msRow][c]==1)
            {
                dataElement[msRow][c]=X_m[k];
                if(k<m)k++;
            }
        }
      
    }

/* the following method generates perturbed dataset
 @param matX_m matrix of a recored of missing attributes
 @param matMu_m mean matrix of missing attributes
 @param matCov_m covariance matrix of missing attributes
 @param pertVar covariance matrix of the data set
 @param recNum record number having missiong values
 @param m no. of missing values
 @return a residual matrix with mean zero and unknown covariance
 */

    public Matrix  generateResidualMatrix(double []matX_m,Matrix matMu_m,
            Matrix matCov_m,Matrix pertVar,int recNum,int m)
    {
            Matrix output, cholPertVariance,pertVariance;
            Matrix mean, randomMatrix;
            CholeskyDecomposition cholesky;
            pertVariance=pertVar;
            cholesky = pertVariance.chol();
            cholPertVariance = cholesky.getL();

            mean = calculateMeanZero(matX_m,matMu_m,matCov_m,recNum,m);//return zero mean vector
           //mean.print(5,2);
            randomMatrix = generateRandomVariates(m);
           // output = [m1 m2 m3] + (h_s)*z
           // where, z = random matrix;
           //        hs = cholesky decomposition of the cov matrix;
           //        [m1 m2 m3] = is the mean vector;
           // output = mean.transpose().plus(randomMatrix.times(cholPertVariance));
           // The following line complies with the sequence of multiplications
           // advised in the original equaltion of GADP method
            output = mean.plus(pertVar.times(randomMatrix.transpose()));
            output = output.transpose(); // it produces a matrix of dim.(1x6)
//            output.print(5,2);
            return output;
    }// end of generateResidualMatrix()

// this method returns a vector of expected values for a perturbed record
    // given the original record
    public Matrix calculateMeanZero(double []X_m,Matrix matMu_m,Matrix matCov_m,int recNum,int m){
        Matrix c, mean, result,covXX,covYX,covYY;
        MatrixCalculation mxCal=new MatrixCalculation();

        double [] c_rec = new double[m];
        for (int i=0;i<m;i++)
            c_rec[i]=0.0;//X_m[i];
            c = mxCal.oneDArrayToMatrix(c_rec);
//        covXX =  matCov_m;
//        covYX = covXX.times(.35);
//        covYY = covXX;
//        result = c.minus(matMu_m).transpose();
//        result = covYX.times(covXX.inverse().times(result));
//        mean = matMu_m.transpose().plus(result);
        return c.transpose();//mean;
    }

    // following method generates a vector of random variates
    // from a standard normal distribution
    public Matrix generateRandomVariates(int numAttr){
        Matrix rndMat;
        double[] randVariates = new double[numAttr];
        for(int i=0; i<numAttr; i++)
           randVariates[i] = 0;
        Random rand = new Random();
        for(int j=0; j<numAttr; j++){
           // randVariates[j] = rand.nextNormal();
           randVariates[j] = rand.nextGaussian();
        }
        MatrixCalculation mxCal=new MatrixCalculation();
       //converting the data array to matrix
        rndMat =  mxCal.oneDArrayToMatrix(randVariates);
        // The following print outs are to see the dimensions of the
        // rndMat matrix:
        // System.out.println("Printing Z vector: ");
       return rndMat;
    }


    /*
     * Calculate mean based on the available values.
     */
    private void computeMean(double[][]DataElement, int [][]MissingMatrix,int msFlag,double []mu )
    {
        int totRec;
        int numAttr=DataElement[0].length;
        int numRecords=DataElement.length;
        for(int i=0; i<numAttr; i++)
            mu[i] = 0;
        double sum=0;
        for(int i=0; i<numAttr; i++)
        {
           sum = 0.0;totRec=0;
           for(int j = 0; j<numRecords; j++)
           {
                   
                   if(MissingMatrix[j][i]==0 ||msFlag==1) //0->available, 1->Missing
                   {
                       totRec++;//counting available rows of an attribute i
                       sum += DataElement[j][i];
                   }
           }
          if(totRec>0)     mu[i] = sum/(double)totRec;
        }

    }

    //check whether an attribute has found in the array msCol.
public int chkAttrIsMiss(int sCol,int []msCol,int sPos,int ePos)
    {
        int i, flag=0;
        for(i=sPos;i<ePos;i++)
        {
            if(msCol[i]==sCol)
            {
                flag=1;break;
            }
        }
        return flag;
    }

    //claculate the mean error and
    //check whether a missing attribute has conversed or not.
    public int calMeanError(double []preMu,double []curMu)
    {
       
        int isError=1, tA=0;
        double diff=0.0;
        double cuT=0.0,cyAvg=0.0;
        double preT=0.0,preAvg=0.0;
        tA=preMu.length;
        for(int i=0;i<tA;i++)
        {
            cuT=cuT+curMu[i];
            preT=preT+preMu[i];
        }
        if(tA>0)
        {
            cyAvg= cuT/tA;
            preAvg= preT/tA;
        }

        diff=Math.abs(preAvg-cyAvg);

         if(diff<0.0000000001)
         {
            isError=0;
         }

        
        return isError;
    }

     //claculate the covariance error and
    //check whether a missing attribute has conversed or not.
    public int calCovError(double [][]preCov,double [][]curCov)
    {

        int isError=1;
        double diff=0.0;
        double preDet=0.0;
        double curDet=0.0;
        try
        {
            Matrix preCov_mat=new Matrix(preCov);
            Matrix curCov_mat=new Matrix(curCov);
            preDet=preCov_mat.det();
            curDet=curCov_mat.det();
            diff=Math.abs(preDet-curDet);
            if(diff<0.0000000001)
             {
                isError=0;
             }
        }
        catch(Exception e)
        {
         }
        return isError;
    }
    //following method will generate a random number between 1000 and 32767
    //In case of INFINITE loop, it will be used as loop terminator
    public int generateLoopTerminator()
    {
        Random rand = new Random();
        return (1000+rand.nextInt(31767));
    }
    //following method will initialize an array.(data type double)  
     public void initilizeArray(double []curArr)
    {
      for(int i=0;i<curArr.length;i++)
          curArr[i]=0.0;
     }

}
