**Illumio Technical Assessment**

Problem Statement:

This program parse a file name flow_logs.txt(present in src/main/resources folder) containing flow log data and maps each row to a tag based on a lookup_table.cvs(also present in resources folder). The lookup table is defined as a csv file, and it has 3 columns, dstport,protocol,tag.   The dstport and protocol combination decide what tag can be applied. 

To run this program:

1. Clone this project.
2. run: mvn clean compile exec:java

This will store output in output.txt at top level.

NOTE: I am using file protocols.cvs to decide protocol and there corresponding number which is taken from Amazon website.