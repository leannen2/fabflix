public class Employee {

    private final String name;

    private final int age;

    private final int id;

    private final String type;

    public Employee(String name, int id, int age, String type) {
        this.name = name;
        this.age = age;
        this.id = id;
        this.type = type;

    }

    public int getAge() {
        return age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String toString() {

        return "Name:" + getName() + ", " +
                "Type:" + getType() + ", " +
                "ID:" + getId() + ", " +
                "Age:" + getAge() + ".";
    }
}
