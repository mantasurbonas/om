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
Maps into 3 objects of Order class, whereas first Order (ID == 1) contains Items 'Tires' and 'Lightbulbs', while other two Orders contains one Item each ('Windshield' and 'Belt' respectively).


Java code:
```
List<Order> orders = om.readEntities(resultset);

orders.size()==3
orders.get(0).getItems().size() == 2
orders.get(1).getItems().size() == 1
```

Think of this as a "superskiny Hibernate" minus all the SQL / HQL / etc bloatware.
