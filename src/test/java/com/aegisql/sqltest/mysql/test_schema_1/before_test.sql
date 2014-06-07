-- before test SQL file
DROP TABLE IF EXISTS test_schema_1.new_table;
CREATE TABLE IF NOT EXISTS test_schema_1.new_table (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(45) NOT NULL,
PRIMARY KEY (`ID`));

INSERT INTO test_schema_1.new_table (ID,NAME) VALUES (0,'mike');

INSERT INTO test_schema_1.new_table (ID,NAME) VALUES (0,'nikita');
