
package SysFor;

import java.io.File;
import java.io.Serializable;
import java.util.*;
/**
 * Stores a data set and also associated information.
 * 
 * <p><strong>Version 2 Update:</strong> change made to data structure for numerical
 * attributes. Now we will only look at values actually appearing in the dataset
 * rather than assuming that all values between min and max domain, with a fixed
 * interval, appear.</p>
 * <p><strong>Version 2.1 Update: </strong>: Made object serializable for easy 
 * storage to file.</p>
 *
 * @author helengiggins
 * @version 1.0 28/11/2010
 * @version 2.0 24/01/2011
 * @version 2.1 7/02/2011
 */
public class Dataset implements Serializable{

    /* instance variables */
   
    /** the records in the dataset */
    private Record [] allRecords;
    /** the number of records in the data set */
    private int numRecords;
    /** the number of attributes in the data set */
    private int numAttributes;
    /** the number of classes in the class attribute */
    private int numClasses;
    /** the total number of attribute values */
    private int totalAttrValues;
    /** attribute types, 'n' for numerical, 'c' for categorical */
    private char [] attrType;
    /** attribute names */
    private String [] attrNames;
    /** class attribute index */
    private int classIndex;
    /** number of attribute values in an attribute */
    private int[] attrDomainSize;
    /** progressive domain sizes - how many attribute values up to this attribute */
    private int[] attrProgDomainSize;
    /** attribute value floor, for numeric attributes */
    private double[] attrLowDomain;
    /** attribute value ceiling, for numeric attributes */
    private double[] attrHighDomain;
    /** attribute intervals, for numeric attributes */
    private double[] attrIntervals;
    /** stores the attribute values for all attributes */
    private String [][] attrValues;

    /** keeps track of which records fall into which class */
    private List<Record> [] totalClassSupport;
    /** keeps track of support counts for each attribute value
     * Note the last position in the class support is all records
     * for that attribute value (totalClassSupport[totalValueIndex][numClasses]
     */
    private List<Record> [][] attrClassSupport;
    
    
    /**
     * Class constructor, initializes all values to their defaults.
     */
    public Dataset()
    {
       allRecords = null;
       numRecords = 0;
       numAttributes = 0;
       numClasses=0;
       totalAttrValues =0;
       attrType= null;
       attrNames = null;
       classIndex = -1;
       attrDomainSize = null;
       attrProgDomainSize = null;
       attrLowDomain = null;
       attrHighDomain = null;
       attrIntervals = null;
       attrValues = null;
       totalClassSupport = null;
       attrClassSupport = null;
    }

    /**
     * Class constructor, creates a data set based on a names file (attr info) and
     * data set (flat file of records).
     *
     * @param nameFile the names file represented as array of <code>String</code>,
     * each line of file in one array position
     * @param dataFile the data file represented as array of <code>String</code>,
     * each record in one array position
     */
    public Dataset(String [] nameFile, String [] dataFile)
    {
        createDataset(nameFile, dataFile);

    }

    /**
     * Used by the parametric constructor to set up all the data structures
     * for the dataset. Reads the name and data file, and then counts all
     * class supports.
     * <p>Updated with V2.0 to now read all values for numerical attributes, rather
     * than assume all values within a certain range appear.</p>
     *
     * @param nameFile the names file represented as array of <code>String</code>,
     * each line of file in one array position
     * @param dataFile the data file represented as array of <code>String</code>,
     * each record in one array position
     */
    private void createDataset(String [] nameFile, String [] dataFile)
    {
        /* first extract the attribute info */
        extractAttributeInfo(nameFile);
        /* extract and store the records to allRecords array */
        extractRecords(dataFile);
        /* count the class supports */
        countAllClassSupports();
//        System.out.println("numAttrValues" + totalAttrValues);//commentd by gea
        //System.out.println(this + " " + this.classSupportsToString());
    }

    /**
     * Extract all attribute information from the passed names file. Note the
     * file should be passed as a <code>String</code> array.
     * <p>Note on format of nameFile</p>
     * <ul>
     *   <li><strong>First line:</strong> class attribute index, number of class values</li>
     *   <li><strong>Second line:</strong> number of records, number of attributes</li>
     *   <li><strong>Categorical attribute:</strong> <code>c</code>, attribute name, number of categories, values</li>
     *   <li><strong>Numerical attribute:</strong> <code>n</code>, attribute name, low domain,
     *       high domain, interval, number of values</li>
     * </ul>
     * <p>From V2.0 also reads individual values for numerical attributes</p>
     *
     * @param nameFile the names file represented as array of <code>String</code>,
     * each line of file in one array position
     */
    public void extractAttributeInfo(String [] nameFile)
    {
         /** first array position contains the class attr index and number of
          * class values. Use tokenizer to extract individual data
          */
        StringTokenizer tokenizer = new StringTokenizer(nameFile[0], " ,\t\n\r\f");
        String str = tokenizer.nextToken(); //get class attr index
        classIndex = Integer.parseInt(str);
        str = tokenizer.nextToken();
        numClasses = Integer.parseInt(str);

        /** second array position (line of file) contains the number of records
         * and number of attributes
         */
        tokenizer = new StringTokenizer(nameFile[1], " ,\t\n\r\f");
        str = tokenizer.nextToken();
        //numRecords = Integer.parseInt(str); //HG: Will now get the number of records from the size of data file
        str = tokenizer.nextToken();
        numAttributes = Integer.parseInt(str);

        /** can now initialise class variables before reading individual attr info */
        //allRecords = new Record [numRecords];
        attrType = new char[numAttributes];
        attrNames = new String[numAttributes];
        attrDomainSize = new int[numAttributes];
        attrProgDomainSize = new int[numAttributes];
        attrLowDomain = new double[numAttributes];
        attrHighDomain = new double[numAttributes];
        attrIntervals = new double[numAttributes];
        attrValues = new String [numAttributes][];

        /** now loop over the attributes to extract info */
        for (int i = 0; i < (numAttributes); i++)
        {
            tokenizer = new StringTokenizer(nameFile[i+2], " ,\t\n\r\f");
            /** first token is the attribute type */
            str = tokenizer.nextToken();
            int attrIndex = i; //attribute index
            String attrName = ""; //attribute name

            /** deal with numerical and categorical attributes slightly differently */
            if (str.equals("c"))
            {
                attrType[attrIndex] = 'c';

                /** next token is the name of the attribute */
                str = tokenizer.nextToken();
                attrNames[attrIndex] = str;

                /** next token is the number of attribute values */
                str = tokenizer.nextToken();
                attrDomainSize[attrIndex] = Integer.parseInt(str);
                //HG >> 2.0 Now read in extractRecords method
                /** increment progressive attribute value domain size.
                 *  nextProgressive = currentProgress + currentDomain
                 */
                //if(attrIndex+1<numAttributes)
                //{
                //  attrProgDomainSize[attrIndex+1] = attrProgDomainSize[attrIndex] +
                //          attrDomainSize[attrIndex];
                //}
                //totalAttrValues+=attrDomainSize[attrIndex]; //HG 2.0, now
                // <<

                /** create the array for storing all attr values for current attr */
                attrValues [attrIndex] = new String[attrDomainSize[attrIndex]];

                /** loop over all the values and store */
                for (int j = 0; j < attrDomainSize[attrIndex]; j++)
                {
                    /** get attribute value */
                    str = tokenizer.nextToken();
                    attrValues[attrIndex][j] = str; //store value
                }

            }
            else if (str.equals("n"))
            {
                attrType[attrIndex] = 'n';

                /** next token is the name of the attribute */
                str = tokenizer.nextToken();
                attrNames[attrIndex] = str;

                /** reads the lower domain, floor value for the numerical attr */
                str = tokenizer.nextToken();
                attrLowDomain[attrIndex] = Double.parseDouble(str);

                /** reads the higher domain, ceiling value for the numerical attr */
                str = tokenizer.nextToken();
                attrHighDomain[attrIndex] = Double.parseDouble(str);

                /** the interval value, difference between attribute values */
                str = tokenizer.nextToken();
                attrIntervals[attrIndex] = Double.parseDouble(str);

                /** the number of attribute values */
                /** Note: Updated in V2.0 - Now stores the values shown in name
                 * file at this stage. The real values will be corrected when we
                 * read the data file.
                 */
                str = tokenizer.nextToken();
                //HG >> 2.0 Now read in extractRecords methods.
                //attrDomainSize[attrIndex] = Integer.parseInt(str);
                
                /** increment progressive attribute value domain size */
                //if(attrIndex+1<numAttributes)
                //{
                //  attrProgDomainSize[attrIndex+1] = attrProgDomainSize[attrIndex] +
                //          attrDomainSize[attrIndex];
                //}
                //totalAttrValues+=attrDomainSize[attrIndex]; //add to total number of attr values
                //HG <<

                //>> HG v2.0 removed setting the values, they will now be set from
                //           the extractRecords method
                /** create the array for storing all attr values for current attr */
                //attrValues [attrIndex] = new String[attrDomainSize[attrIndex]];
                
                /** generate the individual numeric values */
                //double value;
                //for (int m = 0; m < attrDomainSize[attrIndex]; m++)
                //{
                    /** generate from the low domain and interval */
                 //   value = attrLowDomain[attrIndex] + (m * attrIntervals[attrIndex]);
                 //   str = Double.toString(value);
                 //   attrValues[attrIndex][m] = str; //store value
                //}
                //<< HG

            }// end of else if(str.equals("n"))
        }// end of for(int i=0; i<numAttributes; i++)
    }

    /**
     * Read the records from the passed data file. Note the
     * file should be passed as a {@link String} array. Method assumes
     * that this data file has at least the number of records shown in the
     * corresponding name file.
     * <p>Updated in V2.0 to also extract all numerical attribute values as it reads
     * each line of the file it adds to a list for each attribute. Which is then
     * passed to another method to update the attribute data.</p>
     *
     * @param dataFile the data file being examined
     */
    public void extractRecords(String [] dataFile)
    {
        /** create data structure to temporarily store numeric values.
         * These lists will later need to be ordered, and have any duplicate
         * values removed.
         */
        ArrayList [] allNumValues = new ArrayList[numAttributes];
        /** get the number of records */
        numRecords = dataFile.length; //HG: now read the number of records directly here, rather than from name file
        allRecords = new Record [numRecords];
        
        StringTokenizer tokenizer;
        /* loop over all records, create a Record and store.
         * Records are given consecutive record IDs, starting at 0.*/
        for(int recIndex=0; recIndex<numRecords; recIndex++)
        {

            /** need to tokenize the records to extract individual values */
            tokenizer = new StringTokenizer(dataFile[recIndex]," ,\t\n\r\f");
            String [] record = new String[numAttributes];
            for(int currAttr=0; currAttr<numAttributes; currAttr++)
            {
                String currValue = tokenizer.nextToken();
                record[currAttr] = currValue;
                /** on first record, create the lists to store numerical values */
                if(recIndex==0 && attrType[currAttr]=='n')
                {
                    allNumValues[currAttr] = new ArrayList<Double>();
                }
                if(attrType[currAttr]=='n')
                {
                    //System.out.print(currValue + " ");
                    /** store value to temp list */
                    allNumValues[currAttr].add(Double.parseDouble(currValue));
                }
                /** on the last record, we want to sort numerical values,
                 * remove duplicates, and store values.
                 */
                if(recIndex==(numRecords-1) && attrType[currAttr]=='n')
                {
                    //System.out.print(allNumValues[currAttr].size() + " ");
                    processNumericalValues(currAttr,allNumValues[currAttr]);
                }
                /** adjust progressive domain size for all attributes on last record
                 *  and determine total number of attr values.
                 */
                if(recIndex==(numRecords-1))
                {
                    if(currAttr+1<numAttributes)
                    {
                      attrProgDomainSize[currAttr+1] = attrProgDomainSize[currAttr] +
                              attrDomainSize[currAttr];
                    }
                    totalAttrValues+=attrDomainSize[currAttr]; //add to total number of attr values
                }
            }
            Record tempRec = new Record(recIndex, record);
            allRecords[recIndex] = tempRec;
        }
    }

    /**
     * Given a raw list of numerical values, as they appear in the records, remove
     * duplicate values and sort the list. Then store the values, and adjust
     * certain variables related to the numerical attribute.
     *
     * @param attrNum the attribute number
     * @param valueList unsorted list of all values appearing in all records for this attribute
     */
    private void processNumericalValues(int attrNum, ArrayList <Double> valueList)
    {
        /** using a general method remove duplicate values and sort in order
         *
         */
        List <Double> sortedList = GeneralFunctions.removeDuplicateValuesDouble(valueList);
        //System.out.println("attr: " + attrNum + " " + sortedList.size());
        /** Now convert to Strings to store values */
        String [] tempVals = new String[sortedList.size()];
        for(int i=0; i<sortedList.size(); i++)
        {
            //System.out.print(" " + sortedList.get(i));
            tempVals[i]=String.valueOf(sortedList.get(i));
        }
        
        attrValues[attrNum] = tempVals;
        //System.out.println();
        /** now need to adjust variables related to this attribute */
        attrDomainSize[attrNum] = sortedList.size(); //number of values for this attribute
        attrIntervals[attrNum]= GeneralFunctions.findInterval(sortedList); //smallest interval, in case it differs from name file
    }

    /**
     * For each attribute value count the support for each class. That is, in
     * how many records do the attribute value and class value coincide. Also
     * count overall class supports, for all attributes. Note that for the
     * attribute value supports the last array position holds a total support
     * for that value, sum for all class values.
     *
     */
    public void countAllClassSupports()
    {
        totalClassSupport = new ArrayList[numClasses];
        /* note: +1 is so we can store all support for that attr value
         * in the last position. Saves having to sum for all class values
         */
        attrClassSupport = new ArrayList[totalAttrValues][numClasses+1];
        /** populate the arrays with empty Record lists */
        //Thread.dumpStack();
        for(int i=0; i<totalAttrValues; i++)
        {
            for(int j=0; j<numClasses; j++)
            {
                
                totalClassSupport[j] = new <Record>ArrayList();
                attrClassSupport[i][j] = new <Record>ArrayList();
            }
            attrClassSupport[i][numClasses] = new <Record>ArrayList();

        }

        /** loop over all records and then all attributes
         * to count the support for each class value
         */
        for(int recIndex=0; recIndex<numRecords; recIndex++)
        {
            /* Get the record's class value and find its attribute value index.
             * Also increment the total support for this class value by adding
             * the record to the list at classValueIndex
             */
            String recClassValue = allRecords[recIndex].getValueAtIndex(classIndex);
            int classValueIndex = getAttrValueIndex(classIndex, recClassValue);
            totalClassSupport[classValueIndex].add(allRecords[recIndex]);

            for(int attrIndex=0; attrIndex<numAttributes; attrIndex++)
            {
                /* increment the support counts this attribute value
                 * by adding the record to the list for this value
                 */
                String attrValue = allRecords[recIndex].getValueAtIndex(attrIndex);
                int totalAttrIndex = attrProgDomainSize[attrIndex]+getAttrValueIndex(attrIndex,attrValue);
                attrClassSupport[totalAttrIndex][classValueIndex].add(allRecords[recIndex]);
                /** now also add the record to the total support for that attr value,
                 * this goes at the end of the class values
                 */
                attrClassSupport[totalAttrIndex][numClasses].add(allRecords[recIndex]);
            }
            
        }
        /** testing */
        //System.out.println(classSupportsToString());
    }

    /**
     * Finds the attribute index for the passed attribute value. Will return -1 in the
     * event that the passed value is not a valid value for this attribute.
     *
     * @param attrIndex the attribute we want to find the value index for
     * @param attrValue the value we want to find the attribute index for
     * @return the attribute value index, or -1 if not a valid attribute value
     */
    public int getAttrValueIndex(int attrIndex, String attrValue)
    {
        for(int i = 0; i<attrDomainSize[attrIndex]; i++)
        {
            /** when categorical, compare values directly */
            if(attrType[attrIndex]=='c')
            {
                if(attrValues[attrIndex][i].equals(attrValue))
                {
                    return i;
                }
            }
            /** need to check slightly differently if numerical, to take into account
             * that all numerical values are stored as doubles.
             */
            else
            {
                double d1 = Double.parseDouble(attrValues[attrIndex][i]);
                double d2 = Double.parseDouble(attrValue);
                //System.out.println("d1:" + d1 + " d2:" + d2);
                /* make sure the values are the same, within a small threshhold of difference */
                if(Math.abs(d1-d2)<GeneralFunctions.Threshold)
                {
                    return i;
                }
            }
        }
        System.out.println("@@@@@@@@@@@@@@@@@getAttrValueIndex ERROR: Value not found - " + attrIndex + " " + attrValue);
        return -1; //return error value if we don't find the class value
    }

    /**
     * Finds the attribute index for the first value greater than the passed attribute value. Will return -1 in the
     * event that there is no value greater than the passed value for this attribute.
     * Only for numerical values. Note: will return error value if last value is passed.
     *
     * @param attrIndex the attribute we want to find the value index for (numerical only)
     * @param attrValue the value we want to find the attribute index for
     * @return the attribute value index of the next value, or -1 if not a valid attribute value
     */
    public int getAttrValueIndexPlusOne(int attrIndex, String attrValue)
    {
        /** only search up to second last value, otherwise we can't return the next value */
        for(int i = 0; i<(attrDomainSize[attrIndex]); i++)
        {
            /** when categorical, ignore */

            /** Find the first value greater than the passed attrValue, and return
             * the index
             */

            double d1 = Double.parseDouble(attrValues[attrIndex][i]);
            double d2 = Double.parseDouble(attrValue);
            //System.out.println( attrIndex + " d1:" + d1 + " d2:" + d2); //testing
            /* check if the current value is greater than the passed value */
            if(d1>d2)
            {
                return i; //index of value greater than the passed
            }
        }
        System.out.println("@@@@@@@@@@@@@@@@@getAttrValueIndexPlusOne ERROR: Value not found - " + attrIndex + " " + attrValue);
        return -1; //return error value if we don't find the class value
    }

    /**
     * Return the attribute value after the passed value. Will return null in the event
     * that the value is not found, or passed value is last value. For numerical values only.
     *
     * @param attrIndex attribute index
     * @param attrValue value before required value
     * @return value after the passed value, in numerical order
     */
    public String getAttrValuePlusOne(int attrIndex, String attrValue)
    {
        /** only want to search to second last value, otherwise we can't return next value */
        for(int i = 0; i<(attrDomainSize[attrIndex]); i++)
        {
            /** when categorical, ignore */

            /** need to check slightly differently if numerical, to take into account
             * that all numerical values are stored as doubles.
             */

            double d1 = Double.parseDouble(attrValues[attrIndex][i]);
            //add one interval value to the passed attrValue
            double d2 = Double.parseDouble(attrValue);
            //System.out.println("d1:" + d1 + " d2:" + d2); //testing
            /* make sure the values are the same, within a small threshhold of difference */
            if(Math.abs(d1-d2)<GeneralFunctions.Threshold)
            {
                return attrValues[attrIndex][i+1]; //return value after passed value
            }
        }     
        System.out.println("@@@@@@@@@@@@@@@@@getAttrValuePlusOne ERROR: Value not found - " + attrIndex + " " + attrValue);
        return null; //return error value if we don't find the class value
    }

    /**
     * Returns the total number of records in the dataset.
     *
     * @return the total number of records in the dataset
     */
    public int getNumberOfRecords()
    {
        return numRecords;
    }

    /**
     * Returns the number of attributes in this dataset.
     *
     * @return the number of attributes in this dataset
     */
    public int getNumberOfAttributes()
    {
        return numAttributes;
    }

    /**
     * Returns the attribute index of the class.
     *
     * @return the attribute index of the class
     */
    public int getClassIndex()
    {
        return classIndex;
    }

    /**
     * Returns the attribute type for the passed attribute index. 'c' for categorical,
     * 'n' for numerical.
     *
     * @param index the attribute index
     * @return the attribute type for the passed attribute index
     */
    public char getAttributeType(int index)
    {
        return attrType[index];
    }

    /**
     * Return the attribute index for a given attribute name.
     *
     * @param attrName the {@link String} name of the attribute
     * @return the index of the attribute name, or -1 if not found
     */
    public int getAttributeIndex(String attrName)
    {
        int attrIndex = -1;
        for(int currAttr=0; currAttr<this.numAttributes; currAttr++)
        {
            if(this.attrNames[currAttr].equals(attrName))
                return currAttr;
        }
        System.out.println("@@@@@@@@@@@@@@@@@ERROR: Value not found - " + attrName);
        return attrIndex; //error
    }

    /**
     * Returns an attribute type in the format used for Prediction. Note
     * the return values are as follows; 0=categorical, 1=numerical, 2=class,
     * -1=error.
     * @param index an attribute index
     * @return 0=categorical, 1=numerical, 2=class, -1=error
     */
    public int getAttributeTypeForPrediction(int index)
    {
        if(index==classIndex)
        {
            return 2;
        }
        if(attrType[index]=='c')
        {
            return 0;
        }
        if(attrType[index]=='n')
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Returns the lowest domain for the passed attribute.
     *
     * @param attrIndex the attribute index, assumes in correct range
     * @return the lowest domain for the passed attribute
     */
    public double getAttrLowDomain(int attrIndex)
    {
        return attrLowDomain[attrIndex];
    }

    /**
     * Returns the highest domain for the passed attribute.
     *
     * @param attrIndex the attribute index, assumes in correct range
     * @return the highest domain for the passed attribute
     */
    public double getAttrHighDomain(int attrIndex)
    {
        return attrHighDomain[attrIndex];
    }

    /**
     * Returns the number of attribute values for the passed attribute.
     *
     * @param attrIndex the attribute index, assumes in correct range
     * @return the number of attribute values for the passed attribute
     */
    public int getNumberOfAttributeValues(int attrIndex)
    {
        return attrDomainSize[attrIndex];
    }

    /**
     * Returns all attribute values for the selected attribute index.
     *
     * @param attrIndex attribute index
     * @return all attribute values for the passed attribute index
     */
    public String [] getAllAttrValues(int attrIndex)
    {
        return attrValues[attrIndex];
    }

    /**
     * Return the name of attribute at passed attribute index.
     * @param attrIndex the attribute index
     * @return name of attribute
     */
    public String getAttributeName(int attrIndex)
    {
        return attrNames[attrIndex];
    }

    /**
     * Return the number of records in the dataset.
     *
     * @return number of records in the dataset
     */
    public int getNumRecords()
    {
        return numRecords;
    }

    /**
     * Returns the support counts for each class for the whole data set.
     * That is, how many times does each class appear in the whole data
     * set.
     *
     * @return the support counts for each class for the whole data set
     */
    public int [] getTotalClassSupports()
    {
        /* loop over the totalClassSupports array and count the number
         * of records for each class
         */
        int [] totalSupports = new int[numClasses];
        for(int i=0; i<numClasses; i++)
        {
            totalSupports[i] = totalClassSupport[i].size();
            //System.out.println("totalClassSupport[" + i + "]: " + totalSupports[i]);
        }
        return totalSupports;
    }

    /**
     * Returns the class supports for each class value, for a given attribute value.
     *
     * @param attrIndex the attribute index
     * @param attrValueIndex the attribute value index, e.g. first attribute value has index 0.
     * @return the class supports for given attribute value
     */
    public int [] getAttrValueClassSupports(int attrIndex, int attrValueIndex)
    {
        int [] classSupports = new int[numClasses];
        /* what number attribute value does the attribute start from */
        int attrStartIndex = attrProgDomainSize[attrIndex];
        /* overall value index is the start index + the passed attrValueIndex */
        int attrValueIndexTotal = attrStartIndex + attrValueIndex;
        /* get the number of records that have both the a class value and the given attribute value */
        for(int currClassIndex =0; currClassIndex<numClasses; currClassIndex++){
            classSupports[currClassIndex] = attrClassSupport[attrValueIndexTotal][currClassIndex].size();
        }

        return classSupports;
    }

    /**
     * For a given attribute, for each attribute value, how many records in the data set
     * have that attribute value. Note that the values in the returned array should sum to
     * the total number of records. In terms of algorithm, this gives the <code>|D<sub>i</sub>|</code>
     * values for a given attribute <code>T</code>.
     *
     * @param attrIndex the attribute index
     * @return each cell represents the number of records with a particular attribute value
     */
    public int [] getPartitionSizes(int attrIndex)
    {
        /** in the attrClassSupport array, the last position for each class value
         * gives the total number of records with this attribute value
         */
        int [] attrValueSizes = new int [attrDomainSize[attrIndex]];
        /** loop over all values and store supports as we go */
        for(int currValIndex = 0 ;
              currValIndex<attrDomainSize[attrIndex]; currValIndex++)
        {
            attrValueSizes[currValIndex] = attrClassSupport[attrProgDomainSize[attrIndex] +currValIndex][numClasses].size();
        }
        return attrValueSizes;
    }

    /**
     * Returns the attribute value for the passed attribute index and value index.
     * @param attrIndex the attribute index
     * @param valueIndex the attribute value index, e.g. first value is at index 0
     * @return attribute value
     */
    public String getAttributeValue(int attrIndex, int valueIndex)
    {
        return attrValues[attrIndex][valueIndex];
    }

    /**
     * Returns a set of datasets which are partitions of the original dataset.
     * The partitions are decided by the split attribute and split value. This method
     * uses the value of the split string to determine which algorithm was used.
     *
     * <p>If See5 and a categorical attribute, then the split string is "all" to indicate
     * that all values are split points.</p>
     *
     * @param attrIndex the split attribute index
     * @param split the split value index, or another value to indicate the type of split
     * @return a partition of the dataset based on the split values
     */
    public Dataset[] generateDatasetPartitions(int attrIndex, String split){
        Dataset [] partitions = new Dataset [0]; //wont know size until we know what sort of partition
        /** determine the type of split */

        /** categorical attribute */
        if(getAttributeType(attrIndex)=='c')
        {
            /** See5 split - need to partition for each attribute value */
            if(split.equals("all"))
            {
                int recordCount = 0;
                /** number of partitions will be the number of attribute values */
                partitions = new Dataset[attrValues[attrIndex].length];
                for(int currValueIndex=0; currValueIndex<attrValues[attrIndex].length;
                currValueIndex++)
                {
                    List <Record> currPartition = getRecordsWithAttrValue(attrIndex, currValueIndex);
                    //System.out.println("currValueIndex: "+ currValueIndex + " currPartition: " + currPartition.size());
                    recordCount+= currPartition.size();
                    /** create a new dataset with the current record partition */
                    Record [] recordPartition = new Record[currPartition.size()];
                    recordPartition = currPartition.toArray(recordPartition);
                    //for(int i=0; i<recordPartition.length; i++)
                    //{
                    //    System.out.print(recordPartition[i]);
                    //}
                    partitions[currValueIndex] = createPartitionDataset(this, recordPartition);
                    //System.out.println("###############################");
                }
                
            }
            else /** we have an Explore split on one attribute value */
            {
                /** convert the split string to a value index */
                int valueIndex = Integer.parseInt(split);
                /** get all records not containing the value at valueIndex */
                List <Record> currPartition = getRecordsWithoutAttrValue(attrIndex, valueIndex);
            }
        }
        /** numerical value */
        else
        {
                /** number of partitions is 2, split at the split value */
                partitions = new Dataset[2];
                /** get split valueIndex */
                int splitIndex = Integer.parseInt(split);
                //System.out.println("splitIndex: " + splitIndex);
                List <Record> lowerPartition = getRecordsWithAttrValueBefore(attrIndex, splitIndex);
                /** create a new dataset with the current record partition */
                Record [] recordPartition = new Record[lowerPartition.size()];
                recordPartition = lowerPartition.toArray(recordPartition);
                partitions[0] = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, false);
                List <Record> higherPartition = getRecordsWithAttrValueAfter(attrIndex, splitIndex);
                /** create a new dataset with the current record partition */
                recordPartition = new Record[higherPartition.size()];
                recordPartition = higherPartition.toArray(recordPartition);
                partitions[1] = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, true);
                //System.out.println("l:" + lowerPartition.size() + " h:" + higherPartition.size());

        }
        return partitions;
    }

    /**
     * Returns a set of files for datasets which are partitions of the original dataset.
     * The partitions are decided by the split attribute and split value. This method
     * uses the value of the split string to determine which algorithm was used.
     *
     * <p>If See5 and a categorical attribute, then the split string is "all" to indicate
     * that all values are split points.</p>
     *
     * @param attrIndex the split attribute index
     * @param split the split value index, or another value to indicate the type of split
     * @param fileName adds this to the front of each numbered output file
     * @return filenames of a partition of the dataset based on the split values
     */
    public List<File> generateDatasetPartitionsToFile(int attrIndex, String split, String fileName)
    {
        FileManager fileManager= new FileManager();
        List<File> partitions = new ArrayList<File>(); //store the fileNames of each partition.
        /** determine the type of split */

        /** categorical attribute */
        if(getAttributeType(attrIndex)=='c')
        {
            /** See5 split - need to partition for each attribute value */
            if(split.equals("all"))
            {
                int recordCount = 0;
                /** number of partitions will be the number of attribute values */
                for(int currValueIndex=0; currValueIndex<attrValues[attrIndex].length;
                currValueIndex++)
                {
                    List <Record> currPartition = getRecordsWithAttrValue(attrIndex, currValueIndex);
                    //System.out.println("currValueIndex: "+ currValueIndex + " currPartition: " + currPartition.size());
                    recordCount+= currPartition.size();
                    /** create a new dataset with the current record partition */
                    Record [] recordPartition = new Record[currPartition.size()];
                    recordPartition = currPartition.toArray(recordPartition);
                    //for(int i=0; i<recordPartition.length; i++)
                    //{
                    //    System.out.print(recordPartition[i]);
                    //}

                    /** create the partition dataset and write to file, then store
                     * the file to the return list
                     */
                    Dataset temp = createPartitionDataset(this, recordPartition);
                    File tempFile = new File(fileName + currValueIndex + ".ds");
                    fileManager.writeDatasetToFile(tempFile, temp);
                    partitions.add(tempFile);
                    //System.out.println("###############################");
                }

            }
            else /** we have an Explore split on one attribute value */
            {
                /** convert the split string to a value index */
                int valueIndex = Integer.parseInt(split);
                /** get all records not containing the value at valueIndex */
                List <Record> currPartition = getRecordsWithoutAttrValue(attrIndex, valueIndex);
            }
        }
        /** numerical value */
        else
        {
                /** number of partitions is 2, split at the split value */
                /** get split valueIndex */
                int splitIndex = Integer.parseInt(split);
                //System.out.println("splitIndex: " + splitIndex);
                List <Record> lowerPartition = getRecordsWithAttrValueBefore(attrIndex, splitIndex);
                /** create a new dataset with the current record partition */
                Record [] recordPartition = new Record[lowerPartition.size()];
                recordPartition = lowerPartition.toArray(recordPartition);
                //partitions[0] = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, false);
                /** create the partition dataset and write to file, then store
                 * the file to the return list
                 */
                Dataset temp = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, false);
                File tempFile = new File(fileName +  "1.ds");
                fileManager.writeDatasetToFile(tempFile, temp);
                partitions.add(tempFile);
                List <Record> higherPartition = getRecordsWithAttrValueAfter(attrIndex, splitIndex);
                /** create a new dataset with the current record partition */
                recordPartition = new Record[higherPartition.size()];
                recordPartition = higherPartition.toArray(recordPartition);
                //partitions[1] = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, true);
                /** create the partition dataset and write to file, then store
                 * the file to the return list
                 */
                temp = createPartitionDataset(this, recordPartition, attrIndex, splitIndex, true);
                tempFile = new File(fileName + "2.ds");
                String message = fileManager.writeDatasetToFile(tempFile, temp);
                if(message.length()>0)
                {
                    System.out.println(message);
                }
                partitions.add(tempFile);
                //System.out.println("l:" + lowerPartition.size() + " h:" + higherPartition.size());

        }
        return partitions;
    }

    /**
     * Returns all records containing a certain attribute value.
     *
     * @param attrIndex the attribute index
     * @param valueIndex the value index
     * @return all records containing a certain attribute value
     */
    public List<Record> getRecordsWithAttrValue(int attrIndex, int valueIndex)
    {
        /** get the attr value index for the passed attribute value */
        int totalValueIndex = attrProgDomainSize[attrIndex] + valueIndex;
        /** return the record list for this attribute value, stored in the last
         * array position of the supportCount data structure.
         */
        return attrClassSupport[totalValueIndex][numClasses];
    }

    /**
     * Returns a list of all records that do not contain a particular attribute value.
     *
     * @param attrIndex the attribute index
     * @param valueIndex the value index, the value we don't want records containing
     * @return all records not containing a certain attribute value
     */
    public List<Record> getRecordsWithoutAttrValue(int attrIndex, int valueIndex)
    {
        /** Note: method assumes that records are stored in order of ID number,
         * that is, a record to the left has a lower ID than the record to the
         * right. Not necessarily in sequence though.
         */

        /** get the attr value index for the passed attribute value */
        int totalValueIndex = attrProgDomainSize[attrIndex] + valueIndex;
         /** get the record list for this attribute value, stored in the last
         * array position of the supportCount data structure.
         */
        List<Record> notRecords = attrClassSupport[totalValueIndex][numClasses];
        //System.out.println("notRecords: " + notRecords.size());
        /** now need to get all records excluding this list */
        List<Record> isRecords = new ArrayList<Record>(); //the return list
        int notRecId = -1; //stores the id of the 'not' record
        /** check if there are any records with the passed value */
        Record currNotRecord = null;
        if(!notRecords.isEmpty()){
            currNotRecord = notRecords.remove(0); //get first 'not' record
            notRecId = currNotRecord.getId();
        }
        /** loop over all records, and only store those that are not in 'not records'
         * list. Note, if this list was returned empty, this will return all records.
         */
        for(int currRecord=0; currRecord<numRecords; currRecord++)
        {
            int currRecordId = allRecords[currRecord].getId();
            /** if IDs don't match, add currRecord to our return list */
            if(notRecId!=currRecordId)
            {
                isRecords.add(allRecords[currRecord]);
            }
            /** if we match IDs, time to move to next not record */
            if(notRecId==currRecordId && !notRecords.isEmpty())
            {
                currNotRecord = notRecords.remove(0);
                notRecId = currNotRecord.getId();
            }
        }
        //System.out.println("isRecords: " + isRecords.size());
        return isRecords;
    }

    /**
     * Returns all records with attribute value less than or equal to the value
     * at valueIndex. Suitable for numerical attributes only.
     *
     * @param attrIndex attribute index
     * @param valueIndex value index
     * @return all records with attribute value less than or equal to the value
     * at valueIndex
     */
    public List<Record> getRecordsWithAttrValueBefore(int attrIndex, int valueIndex)
    {
        /** get the attr value the passed attribute value index */
        double attrValue = Double.parseDouble(attrValues[attrIndex][valueIndex]);
        List<Record> records = new ArrayList<Record>(); //the return list

        /** loop over all records and only return those records with an
         * attribute value less than or equal to attrValue
         */
        for(int currRecord=0; currRecord<numRecords; currRecord++)
        {
            /** get the current record's value for attribute at attrIndex */
            double currValue = Double.parseDouble(allRecords[currRecord].getValueAtIndex(attrIndex));
            if(currValue<=attrValue)
            {
                records.add(allRecords[currRecord]);
            }
        }

        return records;
    }

    /**
     * Returns all records with attribute value greater than the value
     * at valueIndex. Suitable for numerical attributes only.
     *
     * @param attrIndex attribute index
     * @param valueIndex value index
     * @return all records with attribute value greater than the value
     * at valueIndex
     */
    public List<Record> getRecordsWithAttrValueAfter(int attrIndex, int valueIndex)
    {
        /** get the attr value the passed attribute value index */
        double attrValue = Double.parseDouble(attrValues[attrIndex][valueIndex]);
        List<Record> records = new ArrayList<Record>(); //the return list

        /** loop over all records and only return those records with an
         * attribute value less than or equal to attrValue
         */
        for(int currRecord=0; currRecord<numRecords; currRecord++)
        {
            /** get the current record's value for attribute at attrIndex */
            double currValue = Double.parseDouble(allRecords[currRecord].getValueAtIndex(attrIndex));
            if(currValue>attrValue)
            {
                records.add(allRecords[currRecord]);
            }
        }

        return records;
    }


    /**
     * Returns all records in the current dataset.
     *
     * @return all records
     */
    private Record [] getAllRecords()
    {
        return allRecords;
    }

    /**
     * Return the number of class values.
     *
     * @return the number of classes in the class attribute
     */
    private int getNumClasses()
    {
        return numClasses;
    }

    /**
     * Return the total number of attribute values.
     *
     * @return total number of attribute values
     */
    private int getTotalAttrValues()
    {
        return totalAttrValues;
    }

    /**
     * Return the attribute types for all attributes. 'n'=numerical, 'c'=categorical.
     * @return the attribute types for all attributes
     */
    private char [] getAttrType()
    {
        return attrType;
    }

    /**
     * Return attribute names for all attributes.
     * @return attribute names for all attributes
     */
    private String [] getAttrNames()
    {
        return attrNames;
    }

    /**
     * Return the number of attribute values for all attributes.
     * @return the number of attribute values for all attributes
     */
    private int[] getAttrDomainSize()
    {
        return attrDomainSize;
    }

    /**
     * Return the starting index for each attribute, where we list all attribute
     * values in one list.
     * @return the starting index for each attribute when attributes are listed together
     */
    private int [] getAttrProgDomainSize()
    {
        return attrProgDomainSize;
    }

    /**
     * Return the lowest values for each attribute. Only really set for numerical
     * attributes, categorical attributes will default to 0.0.
     * @return lowest values for each attribute
     */
    private double [] getAttrLowDomain()
    {
        return attrLowDomain;
    }

    /**
     * Return the highest values for each attribute. Only really set for numerical
     * attributes, categorical attributes will default to 0.0.
     * @return highest values for each attribute
     */
    private double [] getAttrHighDomain()
    {
        return attrHighDomain;
    }

    /**
     * Return the value interval for each attribute. Only really set for numerical
     * attributes, categorical attributes will default to 0.0.
     * @return value intervals for all attributes
     */
    private double [] getAttrIntervals()
    {
        return attrIntervals;
    }

    /**
     * Return all attribute values as a 2D array.
     * @return all attribute values
     */
    private String [][] getAttrValues()
    {
        return attrValues;
    }

    /**
     * Makes a deep copy of the passed dataset. That is, copies information and
     * not just object references.
     *
     * @param dataset the dataset to be copied
     * @return a new dataset with all information copied over
     */
    public Dataset copy(Dataset dataset)
    {
        Dataset copy = new Dataset();
        copy.setAllData(dataset.getAllRecords(),dataset.getNumberOfAttributes(),
                dataset.getNumClasses(), dataset.getTotalAttrValues(), dataset.getAttrType(),
                dataset.getAttrNames(), dataset.getClassIndex(),dataset.getAttrDomainSize(),
                dataset.getAttrProgDomainSize(), dataset.getAttrLowDomain(),
                dataset.getAttrHighDomain(), dataset.getAttrIntervals(),
                dataset.getAttrValues());
        return copy;
    }

    /**
     * Returns a new dataset with only the passed records. All attribute information
     * remains unchanged. Suitable for use when splitting on categorical attributes
     * in the See5 algorithm. When we know that these attributes will not be selected
     * again as a split attribute. Creates a deep copy of the dataset, so no references
     * from the old dataset a reused.
     *
     * @param dataset the current dataset
     * @param partition a partition of records
     * @return a copy of the dataset only containing the partition of passed records
     */
    public Dataset createPartitionDataset(Dataset dataset, Record [] partition)
    {
        Dataset copy = new Dataset();
        copy.setAllData(partition,dataset.getNumberOfAttributes(),
                dataset.getNumClasses(), dataset.getTotalAttrValues(), dataset.getAttrType(),
                dataset.getAttrNames(), dataset.getClassIndex(),dataset.getAttrDomainSize(),
                dataset.getAttrProgDomainSize(), dataset.getAttrLowDomain(),
                dataset.getAttrHighDomain(), dataset.getAttrIntervals(),
                dataset.getAttrValues());
        return copy;
    }

    /**
     * Create a partition of the current dataset based on the passed records.
     * Updates the attribute's domain size and attribute values, so that for the
     * split attribute, only those values included in the split are used. This method
     * is designed only for a numerical split of the type used in See5 algorithm.
     * Note that the returned dataset is a deep copy, so no object references from
     * the old dataset a used.
     *
     * @param dataset the current dataset
     * @param partition a subset of the records in the dataset
     * @param attrIndex the split attribute index, which is numerical
     * @param valueIndex the split value index
     * @param higher indication of which side of the split we're dealing with,
     * higher (greater than), or lower, (less than or equal to)
     * @return a partition of the passed dataset with only the passed records
     */
    public Dataset createPartitionDataset(Dataset dataset, Record [] partition,
            int attrIndex, int valueIndex, boolean higher)
    {
        Dataset copy = new Dataset();
        /** need to adjust numerical value ranges */
        if(!higher) /* less than equal to valueIndex */
        {
            /* find how many value currently in attribute */
            int currAttrSize = dataset.getNumberOfAttributeValues(attrIndex);
            /** create copies of the current numerical attribute info and update
             * the values */
            int [] newAttrDomainSize = new int[numAttributes];
            System.arraycopy(dataset.getAttrDomainSize(), 0, newAttrDomainSize, 0, numAttributes);
            newAttrDomainSize[attrIndex] = valueIndex+1;
            int [] newAttrProgDomainSize =  new int[numAttributes];
            System.arraycopy(dataset.getAttrProgDomainSize(), 0, newAttrProgDomainSize, 0, numAttributes);
            /** need to loop over later attributes to update their starting index */
            for(int i=attrIndex+1; i<numAttributes; i++)
            {
                 newAttrProgDomainSize[i]=dataset.getAttrProgDomainSize()[i]-(currAttrSize-(valueIndex+1));
            }
             //System.out.println("attrProgDomainSize:" + newAttrProgDomainSize[attrIndex+1]  + " valInd:" + valueIndex);
            double [] newAttrLowDomain = new double[numAttributes];
            System.arraycopy(dataset.getAttrLowDomain(), 0, newAttrLowDomain, 0, numAttributes);
            newAttrLowDomain[attrIndex] =
                    Double.parseDouble(dataset.getAttrValues()[attrIndex][valueIndex+1]);//current value at valueIndex+1;
            String [][] newAttrValues = new String[numAttributes][];
            /** copy all old values over */
            for(int i=0; i<numAttributes; i++)
            {
                newAttrValues[i] = dataset.getAttrValues()[i];
            }
            /** now replace values for current attribute */
            String [] currAttrValues = newAttrValues[attrIndex];
            String [] newCurrAttrValues = new String[newAttrDomainSize[attrIndex]];
            for(int i=0; i<=valueIndex; i++)
            {
                newCurrAttrValues [i]=currAttrValues[i];
            }
            newAttrValues[attrIndex] = newCurrAttrValues;

            /** be sure to do a deep copy of the dataset with new attribute ranges */
            copy.setAllData(partition,dataset.getNumberOfAttributes(),
                dataset.getNumClasses(), dataset.getTotalAttrValues(), dataset.getAttrType(),
                dataset.getAttrNames(), dataset.getClassIndex(),newAttrDomainSize,
                newAttrProgDomainSize, newAttrLowDomain,
                dataset.getAttrHighDomain(), dataset.getAttrIntervals(),
                newAttrValues);

        }
        else /** partition for greater than valueIndex */
        {
            /* find how many value currently in attribute */
            int currAttrSize = dataset.getNumberOfAttributeValues(attrIndex);
            //System.out.println("currAttrSize:" + currAttrSize + " valInd:" + valueIndex);
            /** create copies of the current numerical attribute info and update
             * the values */
            int [] newAttrDomainSize = new int[numAttributes];
            System.arraycopy(dataset.getAttrDomainSize(), 0, newAttrDomainSize, 0, numAttributes);
            newAttrDomainSize[attrIndex] = currAttrSize-(valueIndex+1);
            int [] newAttrProgDomainSize =  new int[numAttributes];
            System.arraycopy(dataset.getAttrProgDomainSize(), 0, newAttrProgDomainSize, 0, numAttributes);
            /** need to loop over later attributes to update their starting index */
            for(int i=attrIndex+1; i<numAttributes; i++)
            {
                 newAttrProgDomainSize[i]=dataset.getAttrProgDomainSize()[i]-(valueIndex+1);
            }
            double [] newAttrLowDomain = new double[numAttributes];
            System.arraycopy(dataset.getAttrLowDomain(), 0, newAttrLowDomain, 0, numAttributes);
            newAttrLowDomain[attrIndex] =
                    Double.parseDouble(dataset.getAttrValues()[attrIndex][valueIndex+1]);//current value at valueIndex+1;
            String [][] newAttrValues = new String[numAttributes][];
            /** copy all old values over */
            for(int i=0; i<numAttributes; i++)
            {
                newAttrValues[i] = dataset.getAttrValues()[i];
            }
            /** now replace values for current attribute */
            String [] currAttrValues = newAttrValues[attrIndex];
            String [] newCurrAttrValues = new String[newAttrDomainSize[attrIndex]];
            for(int i=valueIndex+1; i<currAttrSize; i++)
            {
                int j=i-(valueIndex+1);
                newCurrAttrValues [j]=currAttrValues[i];
            }
            newAttrValues[attrIndex] = newCurrAttrValues;

            copy.setAllData(partition,dataset.getNumberOfAttributes(),
                dataset.getNumClasses(), dataset.getTotalAttrValues(), dataset.getAttrType(),
                dataset.getAttrNames(), dataset.getClassIndex(),newAttrDomainSize,
                newAttrProgDomainSize, newAttrLowDomain,
                dataset.getAttrHighDomain(), dataset.getAttrIntervals(),
                newAttrValues);
        }
        return copy;
    }

    /**
     * Remove all attributes in the passed list from the dataset and return. Note
     * attributes are passed as a list of attribute indices.
     *
     * @param dataset the {@link Dataset} we want to remove attributes from
     * @param remAttrInds the indices of the attributes we want removed from the
     * passed {@link Dataset}
     * @return the {@link Dataset} with attributes removed
     */
    public Dataset removeAttributesFromDataset(Dataset dataset, int [] remAttrInds)
    {
        Dataset newDataset = dataset;
        /** we will decrement this value each time we remove an attribute, and use it
         * to adjust the current attribute number. For example, if the first attribute
         * we remove is attribute index 1, and the second is meant to be index 2. Once we've
         * removed attribute index 1, what was index 2 in the original dataset is now index 1.
         */
        int adjValue = 0; 
        for(int i=0; i<remAttrInds.length; i++)
        {
            int currAttrIndex = remAttrInds[i]+adjValue; //adjust index since we're removing them as we go.
            newDataset = removeAttributeFromDataset(newDataset, currAttrIndex);
            adjValue--;
        }
        //System.out.println(newDataset); //testing
        //System.out.println(newDataset.classSupportsToString()); //testing
        return newDataset;
    }

    /**
     *
     * @param dataset the
     * @param attrIndex
     * @return
     */
    private Dataset removeAttributeFromDataset(Dataset dataset, int attrIndex)
    {
        int newNumAttrs = dataset.getNumberOfAttributes()-1; //the number of attributes in the new dataset
        int numValueInRemoved = dataset.getNumberOfAttributeValues(attrIndex); //the number of values in attribute being removed
        int newTotalAttrValues = dataset.getTotalAttrValues()-numValueInRemoved; //new total number of attribute values
        int newClassIndex = dataset.getClassIndex(); //new class attribute index
        /** check if the class index was higher than the attribute we're removing */
        if(dataset.getClassIndex()>attrIndex)
            newClassIndex--;

        /** Step 1: remove the attribute's values from the dataset array */
        String [][] newAttrValues = new String[newNumAttrs][]; //all attr values
        char [] newAttrType = new char[newNumAttrs]; //attr type array
        String [] newAttrNames = new String[newNumAttrs]; //attr names array
        int [] newDomainSize = new int[newNumAttrs]; //attr domains
        int [] newProgDomainSize = new int[newNumAttrs]; //reference to starting index in classSupport structure
        double [] newLowDomain = new double[newNumAttrs]; //low attr value
        double [] newHighDomain = new double[newNumAttrs]; //high attr value
        double [] newIntervals = new double[newNumAttrs]; //attr intervals

        //copy all attributes before and after index over to arrays
        for(int a=0; a<attrIndex; a++)
        {
            newAttrValues[a] = dataset.getAllAttrValues(a);
            newAttrType[a] = dataset.getAttributeType(a);
            newAttrNames[a] = dataset.getAttributeName(a);
            newDomainSize[a] = dataset.getNumberOfAttributeValues(a);
            newProgDomainSize[a] = dataset.attrProgDomainSize[a];
            newLowDomain[a] = dataset.attrLowDomain[a];
            newHighDomain[a] = dataset.attrHighDomain[a];
            newIntervals[a] = dataset.attrIntervals[a];
        }
        for(int a=attrIndex+1; a<dataset.getNumberOfAttributes(); a++)
        {
            newAttrValues[a-1] = dataset.getAllAttrValues(a);
            newAttrType[a-1] = dataset.getAttributeType(a);
            newAttrNames[a-1] = dataset.getAttributeName(a);
            newDomainSize[a-1] = dataset.getNumberOfAttributeValues(a);
            newProgDomainSize[a-1] = dataset.attrProgDomainSize[a]-numValueInRemoved;//adjust down by numValuesInRemoved
            newLowDomain[a-1] = dataset.attrLowDomain[a];
            newHighDomain[a-1] = dataset.attrHighDomain[a];
            newIntervals[a-1] = dataset.attrIntervals[a];
        }

        /** Step 2: remove attribute value from all records */
        Record [] newRecords = new Record[dataset.getNumRecords()];
        Record [] currentRecords = dataset.getAllRecords();
        for(int currRec=0; currRec<dataset.getNumRecords(); currRec++)
        {
            //makes a deep copy and removes attribute value from record.
            newRecords[currRec] = currentRecords[currRec].removeAttribute(attrIndex);
        }

        /** Step 3: make a new dataset will all new values */
        Dataset copy = new Dataset();
        copy.setAllData(newRecords,newNumAttrs, dataset.getNumClasses(),newTotalAttrValues, newAttrType,
                newAttrNames, newClassIndex ,newDomainSize, newProgDomainSize, newLowDomain,
                newHighDomain, newIntervals, newAttrValues);

        /** Step 4: reset class supports */
        copy.countAllClassSupports();

        return copy;
    }


    /**
     * Used to set all data in a dataset. Used in copy method. Note, when setting
     * objects, such as lists and arrays, it makes a new array and copies the info
     * rather than simply copying object references.
     *
     * @param allRecords all records in the dataset
     * @param numAttributes number of attributes
     * @param numClasses number of class values
     * @param totalAttrValues total number of attribute values
     * @param attrType attribute types
     * @param attrNames attribute names
     * @param classIndex index of class attribute
     * @param attrDomainSize number of values in each attribute
     * @param attrProgDomainSize start index for each attribute in class supports
     * @param attrLowDomain low values for each numerical attribute
     * @param attrHighDomain high values for each numerical attribute
     * @param attrIntervals value intervals for numerical attributes
     * @param attrValues all attribute values
     */
    private void setAllData(Record [] allRecords, int numAttributes, int numClasses,
            int totalAttrValues, char [] attrType, String [] attrNames, int classIndex,
            int[] attrDomainSize, int[] attrProgDomainSize, double[] attrLowDomain,
            double[] attrHighDomain, double[] attrIntervals, String [][] attrValues)
    {
        this.numRecords = allRecords.length;
        this.allRecords = new Record[numRecords];
        /** make deep copies of all records */
        for(int i=0; i<numRecords; i++)
        {
            this.allRecords[i] = allRecords[i].copy();
        }
        this.numAttributes = numAttributes;
        this.numClasses = numClasses;
        this.totalAttrValues = totalAttrValues;
        this.attrType = new char[numAttributes];
        this.attrNames = new String[numAttributes];
        this.classIndex = classIndex;
        this.attrDomainSize = new int[numAttributes];
        this.attrProgDomainSize = new int[numAttributes];
        this.attrLowDomain = new double[numAttributes];
        this.attrHighDomain = new double[numAttributes];
        this.attrIntervals = new double[numAttributes];
        this.attrValues = new String[numAttributes][];
        this.totalClassSupport = null;
        this.attrClassSupport = null;
        /** copy all info from attr arrays */
        for(int i=0; i<numAttributes; i++)
        {
            this.attrType[i] = attrType[i];
            this.attrNames[i] = attrNames[i];
            this.attrDomainSize[i] = attrDomainSize[i];
            this.attrProgDomainSize[i] = attrProgDomainSize[i];
            this.attrLowDomain[i] = attrLowDomain[i];
            this.attrHighDomain[i] = attrHighDomain[i];
            this.attrIntervals[i] = attrIntervals[i];
            /** copy values for this attribute */
            this.attrValues[i] = new String[attrValues[i].length];
            //System array copy of values for current attribute
            System.arraycopy(attrValues[i], 0, this.attrValues[i], 0, attrValues[i].length);
        }
        /** recreate support data structures */
        this.countAllClassSupports();
    }

    /**
     * The data stored in the data set.
     *
     * @return a <code>String</code> representation of all data stored in the data set
     */
    @Override
    public String toString()
    {
       String nl = "\n";
       StringBuilder retStr= new StringBuilder();
       retStr.append("Number of Records:");
       retStr.append(numRecords);
       retStr.append(nl);
       /** attribute info */
       retStr.append("Attribute Info:\n");
       String temp = "Number of attributes: " + numAttributes + nl;
       retStr.append(temp);
       temp="Attributes: " + nl;
       retStr.append(temp);
       //add all attr names
       for(int i=0; i<numAttributes; i++)
       {
           temp = attrNames[i] + " (" + attrDomainSize[i] + " values) " +
                   "starting at the " + attrProgDomainSize [i] + "'th value"+ nl;
           retStr.append(temp);
       }
       temp="Total Number of attribute values: " + totalAttrValues + nl;
       retStr.append(temp);
       //add class attribute
       temp= nl + "Class Attribute: " + attrNames[classIndex] + nl;
       retStr.append(temp);

       //records
       for(int i=0; i<numRecords; i++){
           retStr.append(allRecords[i].toString());
       }
       return retStr.toString();
    }

    /**
     * Returns a String representation on the current support counts. Should only
     * be used for testing.
     *
     * @return all class and attribute value supports
     */
    public String classSupportsToString()
    {
        StringBuilder retStr = new StringBuilder();
        retStr.append("Total class value supports:\n");
        for(int i=0; i<numClasses; i++) {
            String temp = "" + attrValues [classIndex][i] + " " + totalClassSupport[i].size() + "\n";
            retStr.append(temp);
        }
        for(int i=0; i<totalAttrValues; i++)
        {
            //if(i==62|i==70){
            //retStr.append("#####################################\n");
            //}
            for(int j=0; j<numClasses+1; j++)
            {
               retStr.append(attrClassSupport[i][j].size());
               retStr.append(" ");
            }
            retStr.append("\n");
        }
        return retStr.toString();
    }

    /**
     * Once a dataset has been created, this method can be used to change the class
     * attribute. It will reset the class support count data structure.
     *
     * @param classIndex the index of the new class attribute
     */
    public void setNewClass(int classIndex)
    {
        this.classIndex = classIndex;
        numClasses = attrDomainSize[classIndex];
        countAllClassSupports();
    }

}
