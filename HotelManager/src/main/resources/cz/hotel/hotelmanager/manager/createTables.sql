CREATE TABLE "ROOM" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "NUMBER" INTEGER NOT NULL,
    "CAPACITY" INTEGER NOT NULL,
    "TYPE" VARCHAR(255),
    "PRICE" DECIMAL NOT NULL
);
CREATE TABLE "GUEST" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "ROOMID" BIGINT REFERENCES ROOM (ID),
    "NAME" VARCHAR(255) NOT NULL,
    "PHONENUMBER" VARCHAR(15) NOT NULL,
    "ADDRESS" VARCHAR(50) NOT NULL
);