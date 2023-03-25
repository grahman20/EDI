/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi;
import java.io.*;
/**
 *
 * @author grahman
 * This is an implementation of an existing algorithm kNN.
 */

public class kNN_ary
{
/** instance variables */
    /** default file names */
    private int noOfRec; // total no. of missing attributes of the data file
    private int kk; // k-NN
    private double [][]knnIdAndDist;
    private int cnt;
    String  rec;
    /*
     * this method will take control and call the appropriate method
     */
    public int[] runkNN(int []attrNtype,String []testRec, String [][]dFile,
            int k, int cRow)
    {
       kk=k;
       cnt=0;
       knnIdAndDist=new double[kk][2];
       noOfRec=dFile.length;
       double tmpD=0;
       for(int i=0;i<noOfRec;i++)
       {
           if(i!=cRow)
           {
           tmpD=calculateEuclidianDistance(testRec,dFile, i,attrNtype);
           neighboursList(i,tmpD);
           }
       }
       int []kID=new int[kk];
       for (int i=0;i<kk;i++)
       {
           kID[i]=(int)knnIdAndDist[i][0];
       }
       return kID;
    }

//private void printScore(String trainFile,String kNNFile,String distFile)
//{
////    System.out.print("Distance of "+kk+" Neighbours\n");
//    String []dFile = fileManager.readFileAsArray(new File(trainFile));
//    File kFile=new File(kNNFile);
//    File diFile=new File(distFile);
//    int u=0;
//    double dist=0.0;
//
//    for(int i=0;i<noOfRec;i++)
//        {   dist=chkNeighbour(i);
//            if(dist>=0)
//            {
//
//             }
//         }
//}

public double chkNeighbour(int recId)
  {
    double dist=-1.0;
     for(int i=0; i<cnt;i++)
        {
            if(recId==knnIdAndDist[i][0])
            {
               dist=knnIdAndDist[i][1];
               break;
            }
        }
    return dist;
}
public void neighboursList(int recId, double dist)
{
    int max=-1,i;
    double big=Double.NEGATIVE_INFINITY;
    if(cnt>=kk)
    {
        for(i=0; i<cnt;i++)
        {
            if(big<knnIdAndDist[i][1])
            {
               max=i;
               big=knnIdAndDist[i][1];
            }
        }
        if(big>dist)
        {
           knnIdAndDist[max][1]= dist;
           knnIdAndDist[max][0]= recId;
        }
    }
 else
    {
        knnIdAndDist[cnt][1]= dist;
        knnIdAndDist[cnt][0]= recId;
        cnt++;
    }

}
 /*
  * This method calculate distances between two records
  * for categorical dist=0 if values are equal otherwise 1
  * for numerical d=sqrt((a-b)^2)
  */
public double calculateEuclidianDistance(String []testRec, String [][]trainRec,
        int row, int[] attrType)
{
    double dist=Double.POSITIVE_INFINITY, totdist=0.0,a,b;
    int noAttr=testRec.length;
    for(int i=0;i<noAttr;i++)
    {
        if (isMissing(trainRec[row][i])==0 && isMissing(testRec[i])==0)
        {
            if(attrType[i]==1)
            {
                a=Double.parseDouble(trainRec[row][i]);
                b=Double.parseDouble(testRec[i]);
                totdist+=Math.pow((a-b), 2);
           }
            else
            {
                if(trainRec[row][i].equals(testRec[i]))
                    totdist+= 0;
                else
                    totdist+= 1;
            }
        }
    }
    dist=Math.sqrt(totdist);
    return dist;
}
/*
  * this function will indicate whether or not a value is missing.
  */

 private int isMissing(String oStr)
    {
        int ret=0;
//        try{
            if(oStr.equals("")||oStr.equals("?")||oStr.equals("�"))
                     {
                         ret=1;
                    }
//        }
//        catch(Exception e)
//            {ret=1;}
      return ret;
    }
}