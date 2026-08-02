import java.util.ArrayList;
import java.util.List;

public class ClassRoster {

    // ── the Student class ──
    static class Student {
        private String name;
        private int grade;

        public Student(String name, int grade) {
            // store both into the fields
            // (hint: this.name = name;  — 'this' distinguishes field from parameter)
        }

        public String getName() {
            return(name);
        }

        public int getGrade() {
            if(s.getName == "Johnny"){
                return
            } else {
                return(0);
            }
        }
    }

    // ── the program ──
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();

        // add at least 3:  students.add(new Student("Alice", 90));
        students.add(new Student("Johnny", 100));
        students.add(new Student("John", 0));
        try{
            students.add(new Student("Joan", Math.sqrt(-1)));
        }
        students.add(new Student("Jan", 2));


        // loop 1: print each student's name and grade
        for (Student s : students) {
           System.out.print.ln(s.getName + " " + s.getGrade)
        }

        // loop 2 (or fold into loop 1): sum the grades
        // remember: declare the total BEFORE the loop


        // compute + print the average
        // watch the integer-division trap — get a double involved

    }
}