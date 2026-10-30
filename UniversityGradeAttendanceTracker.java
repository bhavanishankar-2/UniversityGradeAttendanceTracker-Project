
import java.sql.*;
import java.util.Scanner;

public class UniversityGradeAttendanceTracker {

    static final String URL =
        "jdbc:mysql://localhost:3306/university_tracker";
    static final String USER = "root";
    static final String PASSWORD = "your_password";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("University Grade & Attendance Tracker");

            while (true) {
                System.out.println("\n1. Add Student");
                System.out.println("2. View Students");
                System.out.println("3. Record Attendance");
                System.out.println("4. Enter Marks");
                System.out.println("5. Student Report");
                System.out.println("6. Exit");

                System.out.print("Enter choice: ");
                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        addStudent();
                        break;

                    case 2:
                        viewStudents();
                        break;

                    case 3:
                        recordAttendance();
                        break;

                    case 4:
                        enterMarks();
                        break;

                    case 5:
                        studentReport();
                        break;

                    case 6:
                        con.close();
                        sc.close();
                        System.out.println("Thank you!");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void addStudent() throws SQLException {

        System.out.print("Enter Roll Number: ");
        String roll = sc.nextLine();

        System.out.print("Enter Student Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Department: ");
        String dept = sc.nextLine();

        System.out.print("Enter Semester: ");
        int semester = Integer.parseInt(sc.nextLine());

        String sql = "INSERT INTO students "
                + "(roll_no, name, department, semester) "
                + "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, roll);
        ps.setString(2, name);
        ps.setString(3, dept);
        ps.setInt(4, semester);

        ps.executeUpdate();

        System.out.println("Student added successfully!");
    }

    static void viewStudents() throws SQLException {

        String sql = "SELECT * FROM students";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("\n--- Student Details ---");

        while (rs.next()) {
            System.out.println(
                "Roll Number: " + rs.getString("roll_no")
            );
            System.out.println(
                "Name: " + rs.getString("name")
            );
            System.out.println(
                "Department: " + rs.getString("department")
            );
            System.out.println(
                "Semester: " + rs.getInt("semester")
            );
            System.out.println("----------------------");
        }
    }

    static void recordAttendance() throws SQLException {

        System.out.print("Enter Roll Number: ");
        String roll = sc.nextLine();

        System.out.print("Enter Subject: ");
        String subject = sc.nextLine();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        System.out.print("Enter Status (P/A): ");
        String status = sc.nextLine().trim().toUpperCase();

        if (!status.equals("P") && !status.equals("A")) {
            System.out.println("Enter P for Present or A for Absent.");
            return;
        }

        String sql = "INSERT INTO attendance "
                + "(roll_no, subject, class_date, status) "
                + "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, roll);
        ps.setString(2, subject);
        ps.setDate(3, Date.valueOf(date));
        ps.setString(4, status);

        ps.executeUpdate();

        System.out.println("Attendance recorded successfully!");
    }

    static void enterMarks() throws SQLException {

        System.out.print("Enter Roll Number: ");
        String roll = sc.nextLine();

        System.out.print("Enter Subject: ");
        String subject = sc.nextLine();

        System.out.print("Enter Marks (0-100): ");
        double marks = Double.parseDouble(sc.nextLine());

        if (marks < 0 || marks > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return;
        }

        String sql = "INSERT INTO grades (roll_no, subject, marks) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE marks = VALUES(marks)";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, roll);
        ps.setString(2, subject);
        ps.setDouble(3, marks);

        ps.executeUpdate();

        System.out.println("Marks saved successfully!");
        System.out.println("Grade: " + calculateGrade(marks));
    }

    static String calculateGrade(double marks) {

        if (marks >= 90)
            return "A+";
        else if (marks >= 80)
            return "A";
        else if (marks >= 70)
            return "B";
        else if (marks >= 60)
            return "C";
        else
            return "D/F";
    }

    static void studentReport() throws SQLException {

        System.out.print("Enter Roll Number: ");
        String roll = sc.nextLine();

        String studentSql =
            "SELECT * FROM students WHERE roll_no = ?";

        PreparedStatement ps = con.prepareStatement(studentSql);
        ps.setString(1, roll);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            System.out.println("Student not found!");
            return;
        }

        System.out.println("\n--- Student Report ---");
        System.out.println("Roll Number: " + rs.getString("roll_no"));
        System.out.println("Name: " + rs.getString("name"));
        System.out.println("Department: " + rs.getString("department"));
        System.out.println("Semester: " + rs.getInt("semester"));

        String attendanceSql =
            "SELECT subject, COUNT(*) AS total, "
            + "SUM(CASE WHEN status = 'P' THEN 1 ELSE 0 END) AS present "
            + "FROM attendance WHERE roll_no = ? GROUP BY subject";

        PreparedStatement aps = con.prepareStatement(attendanceSql);
        aps.setString(1, roll);

        ResultSet ars = aps.executeQuery();

        System.out.println("\n--- Attendance ---");

        while (ars.next()) {
            int total = ars.getInt("total");
            int present = ars.getInt("present");

            double percentage =
                total == 0 ? 0 : present * 100.0 / total;

            System.out.printf(
                "%s: %.2f%% attendance%n",
                ars.getString("subject"),
                percentage
            );

            if (percentage < 75) {
                System.out.println("Warning: Low attendance!");
            }
        }

        String gradeSql =
            "SELECT subject, marks FROM grades WHERE roll_no = ?";

        PreparedStatement gps = con.prepareStatement(gradeSql);
        gps.setString(1, roll);

        ResultSet grs = gps.executeQuery();

        System.out.println("\n--- Grades ---");

        while (grs.next()) {
            double marks = grs.getDouble("marks");

            System.out.println(
                grs.getString("subject")
                + " | Marks: " + marks
                + " | Grade: " + calculateGrade(marks)
            );
        }
    }
}