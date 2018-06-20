# Data Access 

The basics of document storage are managed by a number of generic data access layer (referred to as Data Access Objects or DAO)
 in the implementation code.

Individual documents are controlled by the [DocDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/docstore/DocDao.kt), 
and Interfaces, which are always read only, are controlled the [InterfaceDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/docstore/InterfaceDao.kt) 
Finally the  [DocsDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/docstore/DocsDao.kt) is 
simply a collection of all the daos.

Basically a DocDao is built for each type in the GraphQL schema and an InterfaceDao for each interface. 
The rules for saving, retrieving and searching each DocDao are then driven from the GraphQL schema.

A schematic of the basic wiring implemented by the DocsDao is below.

<img src="images/docs-dao-wiring.png" width="800"> 


_[Original Diagram](https://www.lucidchart.com/invitations/accept/c1bc70c1-c36d-41fa-9e2b-9d27859fdabf)_