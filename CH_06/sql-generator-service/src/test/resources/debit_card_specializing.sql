CREATE TABLE IF NOT EXISTS "customers" (
	"CustomerID"	INTEGER NOT NULL ,
	"Segment"	VARCHAR,
	"Currency"	VARCHAR
	-- ,
	-- PRIMARY KEY("CustomerID")
);
CREATE TABLE IF NOT EXISTS "gasstations" (
	"GasStationID"	INTEGER NOT NULL ,
	"ChainID"	INTEGER,
	"Country"	VARCHAR,
	"Segment"	VARCHAR
	-- ,
	-- PRIMARY KEY("GasStationID")
);
CREATE TABLE IF NOT EXISTS "products" (
	"ProductID"	INTEGER NOT NULL ,
	"Description"	VARCHAR
	-- ,
	-- PRIMARY KEY("ProductID")
);
CREATE TABLE IF NOT EXISTS "yearmonth" (
	"CustomerID"	INTEGER NOT NULL,
	"Date"	INTEGER NOT NULL,
	"Consumption"	DOUBLE
	-- ,
	-- FOREIGN KEY("CustomerID") REFERENCES "customers"("CustomerID") on update cascade on delete cascade,
	-- PRIMARY KEY("CustomerID","Date")
);
CREATE TABLE IF NOT EXISTS "transactions_1k" (
	"TransactionID"	INTEGER,
	"Date"	DATE,
	"Time"	VARCHAR,
	"CustomerID"	INTEGER,
	"CardID"	INTEGER,
	"GasStationID"	INTEGER,
	"ProductID"	INTEGER,
	"Amount"	INTEGER,
	"Price"	DOUBLE
	-- ,
	-- PRIMARY KEY("TransactionID" AUTOINCREMENT)
);
