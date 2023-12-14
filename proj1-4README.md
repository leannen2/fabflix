Project 4 Demo Link: https://youtu.be/GypRare0K4A
Search with Full Text is on a separate page from Main Page.
To use Full Text search, click on the link on the Main Page labeled "Search Movie With Fulltext".

Project 3 Demo Link: https://youtu.be/Y4Y-ADCYXHE

PreparedStatement Files:
DomParser.java
AddMovieServlet.java
AddStarServlet.java
AuthPaymentServlet.java
EmployeeLoginServlet.java
GenresServlet.java
LoginServlet.java
MetaDataServlet.java
MovieListServlet.java
PlaceOrderServlet.java
SingleMovieServlet.java
SingleStarServlet.java
StarsServlet.java

Parsing Time Optimizations:
1. Used batches to reduce number of calls to MySQL.
2. Created Indexes for Genres.name and Stars.name to speed up lookup by name.
The naive approach is to create and send an insert call for each new update to the database. Using batches sends a set of inserts instead which improves the data insert times.
I was also also getting genre and star id using their names which requires a whole table look up, which was slowing down my code a lot. I sped this process up a lot by creating indexes for both tables' name column.

Inconsistent Data Report at cs122b-project3-DomParser/parseErrors.txt.

Project 1 Demo Link: https://youtu.be/-rCuqNHP2LA
Project 2 Demo Link: https://youtu.be/0BVEqyeoOzk
Contributors: Leanne Nguyen (did all work)

String selectQuery =  "select * from movies as m, ratings as r ";
            String whereQuery = "where m.id = r.movieId  ";
            if (genre != null) {
                selectQuery += ", genres as g, genres_in_movies as gim ";
                whereQuery += "and g.id = gim.genreId and gim.movieId = m.id and g.name = '" + genre + "' ";
            }
            if (title != null) {
                whereQuery += "and m.title like '%" + title + "%' ";
            }
            if (year != null) {
                whereQuery += "and m.year = ? ";
            }
            if (director != null) {
                whereQuery += "and m.director like '%" + director + "%' ";
            }
            if (starName != null) {
                whereQuery += "and exists (select s.name from stars as s, stars_in_movies as sim where sim.movieId = m.id and sim.starId = s.id and s.name like '%" + starName + "%') ";
            }
            if (alpha != null) {
                if (alpha.equals("*")) {
                    whereQuery += "and m.title REGEXP '^[^A-Za-z0-9]' ";
                } else {
                    whereQuery += "and m.title like '" + alpha + "%' ";
                }
            }
            String query = selectQuery + whereQuery;

            query += "order by " + order1 + ", " + order2 + " ";

            Integer offsetNum = (pageNumber-1) * numOfMoviesDisplay;
            query += "limit " + numOfMoviesDisplay + " offset " + offsetNum;
