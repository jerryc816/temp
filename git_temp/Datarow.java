package don.api.office;

import java.util.Date;

class Datarow {
    private String name;
    private String email;
    private Date date;
    private double salary;

    public Datarow(String name, String email, Date date, double salary) {
        this.name = name;
        this.email = email;
        this.date = date;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
    
}
