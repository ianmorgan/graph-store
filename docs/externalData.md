# External Data  

## Overview 

Some resources may be held in external systems, typically accessed via a REST 
style API.

These are referred to as 'externalDao's and must at a minimum implement 
the [ReaderDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/dal/ReaderDao.java).

Currently only one implementation is supported, [ConfigurableRestDocDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/dal/ConfigurableRestDocDao.kt
). As the name suggest a configuration can be injected in change URL mapping and data mappings. Future 
enhancements may allow the use of custom implementations.

## Setting up a new 
