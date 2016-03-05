# gugit :: OM
Fast universal Object Mapper for java. Efficient master - detail mapping from any flat (cartesian product) resultset.

Usage:

data in form of:
```
ORDER_ID | ORDER_DATE | ITEM_ID | ITEM_DESCR | ITEM_QTY
     1   | 2016-01-01 |   1789  | Tires      |      4
     1   | 2016-01-01 |   1797  | Lightbulbs |      2
     2   | 2010-07-14 |   2748  | Windshield |      1
     3   | 2008-09-01 |   0123  | Belt A784Q |      2
```
maps into Java POJOs:
```
Order {id:1, date: 2016-01-01}
    +---> Item {id: 1789, descr: 'Tires', qty: 4 }
    +---> Item {id: 1797, descr: 'Lightbulbs', qty: 2 }

Order {id: 2, date: 2010-07-14}
    +---> Item {id: 2748, descr: 'Windshield', qty: 1}
    
Order {id: 3, date: 2008-09-01}
    +---> Item {id: 0123, descr: 'Belt A784Q', qty: 2}
```

Java code:
```
List<Order> orders = om.readEntities(resultset);

orders.size()==3
orders.get(0).getItems().size() == 2
orders.get(1).getItems().get(0).getDescr() == 'Windshield'
```

Think of this as a "superskiny Hibernate" minus all the SQL / HQL / whatever bloatware. Saves tons of time when mapping a native query resultset into POJOs.
