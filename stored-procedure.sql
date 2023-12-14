INSERT INTO movieId SELECT substring(max(id), 3) FROM movies;
INSERT INTO starId SELECT substring(max(id), 3) FROM stars;
DELIMITER $$
CREATE PROCEDURE add_movie (
    IN movie_title VARCHAR(100),
    IN movie_year INTEGER,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32),
    OUT message VARCHAR(100)
)
sp: BEGIN
    DECLARE nextMovieId INT DEFAULT 0;
    DECLARE gId INT DEFAULT 0;
    DECLARE sId VARCHAR(10) DEFAULT NULL;
    DECLARE nextStarId INT DEFAULT 0;


    SELECT COUNT(*)
    INTO nextMovieId
    FROM movies
    WHERE title=movie_title and year=movie_year and director=movie_director;

    IF nextMovieId > 0 THEN
       SELECT "duplicate movie"
           INTO message;
       LEAVE sp;
    END IF;


    SELECT maxMovieId+1
    INTO nextMovieId
    FROM movieId;

    SELECT maxStarId+1
    INTO nextStarId
    FROM starId;

    UPDATE movieId
    SET maxMovieId = nextMovieId
    WHERE maxMovieId = nextMovieId-1;

    UPDATE starId
    SET maxStarId = nextStarId
    WHERE maxStarId = nextStarId-1;

    INSERT INTO movies (id, title, year, director, price)
    SELECT CONCAT('tt', nextMovieId), movie_title, movie_year, movie_director, 10
    WHERE NOT EXISTS (SELECT * FROM movies
                               WHERE title=movie_title and year=movie_year and director=movie_director);

    INSERT INTO genres(name)
    SELECT genre_name
    WHERE NOT EXISTS (
        SELECT *
        FROM genres
        WHERE name = genre_name);

    SELECT id
    INTO gId
    FROM genres
    WHERE name = genre_name;

    INSERT INTO genres_in_movies(genreId, movieId)
    VALUES(gId, CONCAT('tt', nextMovieId));

    INSERT INTO stars(id, name)
    SELECT CONCAT("nm", nextStarId), star_name
    WHERE NOT EXISTS (SELECT * FROM stars
        WHERE name=star_name);

    SELECT id
    INTO sId
    FROM stars
    WHERE name=star_name
    LIMIT 1;

    INSERT INTO stars_in_movies(starId, movieId)
    VALUES(sId, CONCAT('tt', nextMovieId));

    SELECT CONCAT('Movie Id: ', CONCAT('tt',nextMovieId), ', Star Id: ', sId, ', Genre Id: ', gId)
    INTO message;
END
$$

DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_star (
    IN star_name VARCHAR(100),
    IN star_birth_year INTEGER,
    OUT message VARCHAR(100)
)
BEGIN
    DECLARE sId VARCHAR(10) DEFAULT NULL;
    DECLARE nextStarId INT DEFAULT 0;

    SELECT maxStarId+1
    INTO nextStarId
    FROM starId;

    UPDATE starId
    SET maxStarId = nextStarId
    WHERE maxStarId = nextStarId-1;

    INSERT INTO stars(id, name, birthYear)
    VALUES (CONCAT("nm", nextStarId), star_name, star_birth_year);

    SELECT CONCAT("Star added successfully. Star Id: nm", nextStarId)
    INTO message;
END
$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_star_in_movie (
    IN star_name VARCHAR(100),
    IN movie_id VARCHAR(100)
)
BEGIN
    DECLARE sId VARCHAR(10) DEFAULT NULL;

    SELECT COUNT(*)
    INTO sId
    FROM stars
    WHERE name = star_name;

    IF sId = 0 THEN
        CALL add_star(star_name, null, @message);
    END IF;

    SELECT id
    INTO sId
    FROM stars
    WHERE name = star_name
    LIMIT 1;

    INSERT IGNORE INTO stars_in_movies(starId, movieId)
    VALUES (sId, movie_id);
END
$$
DELIMITER ;