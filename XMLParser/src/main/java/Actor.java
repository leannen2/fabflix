public class Actor {
    private final String name;

    private final Integer birthYear;

    public Actor(String name, Integer birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getName() {
        return name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public String toString() {
        return "Name: " + getName() + ", " +
                "Birth Year: " + getBirthYear() + ". ";
    }
}
