/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SysFor;

/**
 *
 * @author helengiggins
 */
public class Constants {

    /** the minimum number of records in a leaf node */
    public static final int MIN_RECORDS =20;    //for adult: 100, credit approval: 20
    /** the minimum gain ratio for See5 splits */
    public static final double MIN_GAIN_RATIO = 0.01;//default 0.01
    /** the confidence factor for pruning */
    public static final double ConfidFactor = 0.25;//default 0.25

    /** the number of desired tree for multiple tree algorithm */
    public static final int NUM_TREES =5;
    /** the max difference between best GR and alternate GR for multiple tree */
    public static final double MT_THRESHOLD = 0.30;
    /** the theta threshold used in selecting candidate split points for multiple
     * trees
     */
    public static final double MT_THETA = 0.30;

    /** prediction constants */
    /** the percentage of the size of |D|/|numLeaves| used in voting */
    public static final double RULE_PERC = 0.10;
    /** the minimum support for voting (theta) */
    public static final int MIN_SUPPORT = 5;

}
