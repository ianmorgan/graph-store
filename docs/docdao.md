# Doc Dao

The basics of document storage are managed by a [DocDao](https://github.com/ianmorgan/doc-store/blob/master/src/main/java/ianmorgan/docstore/DocDao.kt)

The [DocsDao](https://github.com/ianmorgan/doc-store/blob/master/src/main/java/ianmorgan/docstore/DocsDao.kt) is 
simply a collection of all the docs with their Dao. Basically a DodDao is built for each type 
in GraphQL schema. The rules for each DocDao is driven from the GraphQL schema.

A schematic of the basic wiring implemented by the DocsDao is below.

<img src="images/docs-dao-wiring.png"  width="800"> 


_[Original](https://www.lucidchart.com/invitations/accept/c1bc70c1-c36d-41fa-9e2b-9d27859fdabf) _