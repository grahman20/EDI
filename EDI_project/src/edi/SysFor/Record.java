

package SysFor;

import java.io.Serializable;

/**
 * Represents a single record in a data set. Note that the record only stores
 * minimal information, additional data is stored in the <code>Dataset</code>
 * class.
 * <p><strong>Version 1.1</strong>: Made object serializable for easy storage to file</p>
 *
 * @author helengiggins
 * @version 1.0 28/11/2010
 * @version 1.1 7/02/2011
 * @see Dataset
 */
public class Record implements Serializable{

    /* instance variables */

    /** unique identifier for this record */
    private int id;
    /** the attribute values for this record */
    private String [] record;


    /**
     * Class constructor, initializes all values to their defaults.
     */
    public Record()
    {
        id = -1;
        record = null;
    }

    /**
     * Class constructor, creates a new record setting all params except
     * for which decision tree node the record is in.
     *
     * @param id a unique identifier for this record
     * @param record the attribute values for the record
     */
    public Record(int id, String [] record)
    {
       this.id = id;
       this.record = record;
    }

    /**
     * Returns the unique identifier for the record.
     *
     * @return id for this record
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns all of the attribute values for this record.
     *
     * @return all attribute values as <code>String []</code>
     */
    public String [] getAllValues()
    {
        return record;
    }

    /**
     * Returns the attribute value at the passed attribute index. If the index
     * is out of bounds the method returns <code>null</code>.
     *
     * @param index the index of the attribute of wanted value
     * @return the value of the attribute at <code>index</code>, or null if out
     * of bounds
     */
    public String getValueAtIndex(int index)
    {
        /** check that the passed index is in bounds */
        if(index<record.length && index >=0)
        {
            return record[index];
        }
        else
        {
            return null;
        }
    }

    private void setData(int id, String [] record)
    {
        this.id = id;
        this.record = new String[record.length];
        //copy record info into new array
        System.arraycopy(record, 0, this.record, 0, record.length);
    }

    /**
     * Makes a deep copy of the record. That is, rather than just copy object
     * references it makes new objects and copies all info over.
     *
     * @return a copy of the record
     */
    public Record copy()
    {
        Record copy = new Record();
        copy.setData(this.getId(), this.getAllValues());
        return copy;
    }

    /**
     * Makes a deep copy of the record, and removes the value at the passed attribute
     * index. So the returned record will be of original size -1. The record will
     * keep the same record ID.
     *
     * @param attrIndex the index of the attribute value to be removed
     * @return the record without the value at attrIndex
     */
    public Record removeAttribute (int attrIndex)
    {
        Record copy = this.copy();
        /** create a new record array of values, minus value at attrIndex */
        String [] newRecord = new String [record.length-1];
        /* copy values before attrIndex */
        for(int i=0; i<attrIndex; i++)
            newRecord[i]=copy.getValueAtIndex(i);
        /* copy values after attrIndex */
        for(int i=attrIndex+1; i<record.length; i++)
            newRecord[i-1]=copy.getValueAtIndex(i);
        copy.setData(id, newRecord);
        return copy;
    }

    /**
     * Returns a <code>String</code> representation of the current state of the object.
     *
     * @return all class info
     */
    @Override
    public String toString()
    {
        StringBuilder retStr = new StringBuilder();
        String temp = "Record " + id + ": ";
        retStr.append(temp);
        //loop over attribute values
        for(int i=0; i<record.length; i++)
        {
            temp = record[i] + ", ";
            retStr.append(temp);
        }
        retStr.append("\n");
        return retStr.toString();
    }

}
