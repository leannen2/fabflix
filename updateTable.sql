USE moviedb;

ALTER TABLE movies
ADD price DECIMAL(5,2);

ALTER TABLE movies
ADD FULLTEXT(title);


ALTER TABLE sales
ADD quantity INTEGER;

UPDATE movies SET price = 10 + (20 - 10) * RAND() WHERE 1;

INSERT INTO employees (email, password, fullname)
VALUES('classta@email.edu', 'uwk+ruanBdjqE//cUmu7OleYhBjfindxMnQVNxB0ALtxOOUaqn6kCFWTPS6SjzYf', 'TA CS122B');