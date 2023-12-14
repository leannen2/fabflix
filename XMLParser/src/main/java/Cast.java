public class Cast {
    private final String movieId;

    private final String starName;

    public Cast(String movieId, String actorName) {
        this.movieId = movieId;
        this.starName = actorName;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getActorName() {
        return starName;
    }

    public String toString() {
        return  "Movie Id: " + getMovieId() + ", " +
                "Star Name: " + getActorName() + ".";
    }
}
