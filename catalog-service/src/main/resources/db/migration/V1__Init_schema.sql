
CREATE TABLE book (
    id			        BIGSERIAL PRIMARY KEY NOT NULL,
    isbn		        VARCHAR(255) NOT NULL,
    title		        VARCHAR(255) UNIQUE NOT NULL,
    author		        VARCHAR(255) NOT NULL,
    price               FLOAT8 NOT NULL,
    publisher           VARCHAR(255) NOT NULL,
    create_date         TIMESTAMP NOT NULL,
    last_modified_date  TIMESTAMP NOT NULL,
    version		        INTEGER NOT NULL
);