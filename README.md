# EDI
EDI uses two layers/steps of imputation namely the Early-Imputation step and the Advanced-Imputation step. In the early imputation step we first impute the missing values (both numerical and categorical) using existing techniques. The main goal of this step is to carry out an initial imputation and thereby refine the records having missing values so that they can be used in the second layer of imputation through an existing technique called DMI. The original DMI ignores the records having missing values. Therefore, we argue that if a data set has a huge number of missing values then the imputation accuracy of DMI may suffer significantly since it ignores a huge number of records.

# Reference

Rahman, M. G. and Islam, M. Z. (2013): A Novel Framework Using Two Layers of Missing Value Imputation, In Proc. of the 11th Australasian Data Mining Conference (AusDM 13), Canberra, Australia, 13-15 November 2013. 

## BibTeX
```
@inproceedings{rahman2013novel,
  title={A novel framework using two layers of missing value imputation},
  author={Rahman, Md Geaur and Islam, Md Zahidul},
  booktitle={Australasian data mining conference (AusDM 13), CRPIT},
  volume={146},
  year={2013}
}
```

@author Gea Rahman <https://csusap.csu.edu.au/~grahman/>
  
# Two folders:
 
 1. EDI_project (NetBeans project)
 2. SampleData 
 
EDI is developed based on Java programming language (jdk1.8.0_211) using NetBeans IDE (8.0.2). 
 
# How to run:
 
	1. Open project in NetBeans
	2. Run the project

# Sample input and output:
run:
Please enter the name of the file containing the 2 line attribute information.(example: c:\data\attrinfo.txt)

C:\SampleData\attrinfo.txt

Please enter the name of the data file having missing values: (example: c:\data\data.txt)

C:\SampleData\data.txt

Please enter the name of the output file: (example: c:\data\out.txt)

C:\SampleData\output.txt


Imputation by EDI is done. The completed data set is written to: 

C:\SampleData\output.txt
