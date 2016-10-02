CREATE KEYSPACE IF NOT EXISTS my_keyspace
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE my_keyspace.my_table (
  my_key text PRIMARY KEY,
  my_value text
);

INSERT INTO my_keyspace.my_table ( my_key, my_value )
  VALUES ( 'my_key_1', 'my_value_1' );
