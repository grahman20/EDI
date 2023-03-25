/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Main {
        /** command line reader */
    BufferedReader stdIn;
        /** class name, used in logging errors */
    static String className = edi.Main.class.getName();
    
    public Main()
    {
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main terminal=new Main();
        String fileAttrInfo = terminal.inputFileName("Please enter the name of the file containing the 2 line attribute information.(example: c:\\data\\attrinfo.txt?)");
        String fileDataFileIn= terminal.inputFileName("Please enter the name of the data file having missing values: (example: c:\\data\\data.txt?)");
        String fileOutput = terminal.inputFileName("Please enter the name of the output file: (example: c:\\data\\out.txt?)");
        //call EDI
        String DMIopt="SDMI"; //A string about DMI option "NDMI" or "SDMI" , default value="SDMI"
        int k=10; //the value of k for k-nearest neighbors
        EDI edi=new EDI();
        edi.runEDI(fileAttrInfo, fileDataFileIn, fileOutput, k, DMIopt);
        System.out.println("\nImputation by EDI is done. The completed data set is written to: \n"+fileOutput);
    }
      

    /**
     * Given a message to display to the user, ask user to enter a file name.
     *
     * @param message message to user prompting for filename
     * @return filename entered by user
     */
    private String inputFileName(String message)
    {
        String fileName = "";
        try
        {
            System.out.println(message);
            fileName = stdIn.readLine();
        }
        catch (IOException ex)
        {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
        }
        return fileName;
    }

}
