import java.io.*;
import java.util.*;

/*
 * Placement Portal - PS No. 56
 * Data Structures: BST, HashMap, PriorityQueue, Sorting, File Handling
 *
 * Data file format (placements.txt):
 * S|studentId|studentName|cgpa
 * J|jobId|companyName|jobRole|minimumCgpa
 * A|jobId|studentId
 */

public class placement_portal {

    // -------------------- Student BST --------------------

    static class Student {
        String id;
        String name;
        double cgpa;

        Student(String id, String name, double cgpa) {
            this.id = id;
            this.name = name;
            this.cgpa = cgpa;
        }
    }

    static class StudentNode {
        Student student;
        StudentNode left;
        StudentNode right;

        StudentNode(Student student) {
            this.student = student;
        }
    }

    static StudentNode studentRoot;

    static StudentNode insertStudentNode(StudentNode root, Student student) {
        if (root == null) {
            return new StudentNode(student);
        }

        int comparison = student.id.compareToIgnoreCase(root.student.id);

        if (comparison < 0) {
            root.left = insertStudentNode(root.left, student);
        } else if (comparison > 0) {
            root.right = insertStudentNode(root.right, student);
        }
        return root;
    }

    static Student searchStudentNode(StudentNode root, String id) {
        while (root != null) {
            int comparison = id.compareToIgnoreCase(root.student.id);

            if (comparison == 0) {
                return root.student;
            } else if (comparison < 0) {
                root = root.left;
            } else {
                root = root.right;
            }
        }
        return null;
    }

    static void displayStudentsInOrder(StudentNode root) {
        if (root == null) {
            return;
        }

        displayStudentsInOrder(root.left);
        System.out.printf("%-10s | %-20s | CGPA %.2f%n",
                root.student.id, root.student.name, root.student.cgpa);
        displayStudentsInOrder(root.right);
    }

    static void collectStudents(StudentNode root, List<Student> result) {
        if (root == null) {
            return;
        }

        collectStudents(root.left, result);
        result.add(root.student);
        collectStudents(root.right, result);
    }

    // -------------------- Job and Application Data --------------------

    static class Job {
        String id;
        String company;
        String role;
        double minimumCgpa;

        Job(String id, String company, String role, double minimumCgpa) {
            this.id = id;
            this.company = company;
            this.role = role;
            this.minimumCgpa = minimumCgpa;
        }
    }

    static class Candidate {
        Student student;

        Candidate(Student student) {
            this.student = student;
        }
    }

    // HashMap provides direct lookup using job ID.
    static HashMap<String, Job> jobs = new HashMap<>();

    // Each job ID maps to the IDs of students who have applied.
    static HashMap<String, HashSet<String>> applications = new HashMap<>();

    // Higher CGPA receives higher priority.
    static PriorityQueue<Candidate> createCandidateQueue() {
        return new PriorityQueue<>((a, b) -> {
            int cgpaOrder = Double.compare(b.student.cgpa, a.student.cgpa);

            if (cgpaOrder != 0) {
                return cgpaOrder;
            }

            return a.student.id.compareToIgnoreCase(b.student.id);
        });
    }

    static final String DATA_FILE = "placements.txt";
    static int nextStudentNumber = 101;

    static Scanner scanner = new Scanner(System.in);

    // -------------------- Input Helpers --------------------

    static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static double readCgpa(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);

                if (value >= 0.0 && value <= 10.0) {
                    return value;
                }

                System.out.println("CGPA must be between 0 and 10.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static int readChoice() {
        System.out.print("Enter your choice: ");
        String input = scanner.nextLine().trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // -------------------- Student Operations --------------------

    static void addStudent() {
        System.out.println("\n--- Add Student ---");

        String name = readText("Enter student name: ");

        if (name.isEmpty() || name.contains("|")) {
            System.out.println("Invalid name. Please enter a non-empty name without '|'.");
            return;
        }

        double cgpa = readCgpa("Enter CGPA (0-10): ");

        String id;
        do {
            id = "ST" + nextStudentNumber++;
        } while (searchStudentNode(studentRoot, id) != null);

        Student student = new Student(id, name, cgpa);
        studentRoot = insertStudentNode(studentRoot, student);

        System.out.println("Student added: " + student.id + " | "
                + student.name + " | CGPA " + String.format("%.1f", student.cgpa));
    }

    static void searchStudent() {
        System.out.println("\n--- Search Student ---");
        String id = readText("Enter student ID: ");

        Student student = searchStudentNode(studentRoot, id);

        if (student == null) {
            System.out.println("Student " + id + " not found.");
        } else {
            System.out.printf("Student found: %s | %s | CGPA %.2f%n",
                    student.id, student.name, student.cgpa);
        }
    }

    static void displayAllStudents() {
        System.out.println("\n--- Student Profiles (BST Order) ---");

        if (studentRoot == null) {
            System.out.println("No student records available.");
            return;
        }

        System.out.printf("%-10s | %-20s | %s%n", "Student ID", "Name", "CGPA");
        System.out.println("------------------------------------------------");
        displayStudentsInOrder(studentRoot);
    }

    // -------------------- Job Operations --------------------

    static void addJob() {
        System.out.println("\n--- Add Job Opening ---");

        String id = readText("Enter job ID: ").toUpperCase();

        if (id.isEmpty() || id.contains("|")) {
            System.out.println("Invalid job ID.");
            return;
        }

        if (jobs.containsKey(id)) {
            System.out.println("A job with ID " + id + " already exists.");
            return;
        }

        String company = readText("Enter company name: ");
        String role = readText("Enter job role: ");

        if (company.isEmpty() || role.isEmpty()
                || company.contains("|") || role.contains("|")) {
            System.out.println("Company and role must be non-empty and cannot contain '|'.");
            return;
        }

        double minimumCgpa = readCgpa("Enter minimum CGPA (0-10): ");

        Job job = new Job(id, company, role, minimumCgpa);
        jobs.put(id, job);
        applications.put(id, new HashSet<>());

        System.out.println("Job added: " + job.id + " | " + job.company
                + " | " + job.role + " | Minimum CGPA " + job.minimumCgpa);
    }

    static void searchJob() {
        System.out.println("\n--- Search Job ---");

        String id = readText("Enter job ID: ").toUpperCase();
        Job job = jobs.get(id);

        if (job == null) {
            System.out.println("Job " + id + " not found.");
        } else {
            displayJob(job);
        }
    }

    static void displayJob(Job job) {
        System.out.printf("%s | %s | %s | Minimum CGPA %.2f%n",
                job.id, job.company, job.role, job.minimumCgpa);
    }

    static void displayAllJobs() {
        System.out.println("\n--- Available Job Openings ---");

        if (jobs.isEmpty()) {
            System.out.println("No job openings available.");
            return;
        }

        ArrayList<String> ids = new ArrayList<>(jobs.keySet());
        Collections.sort(ids);

        for (String id : ids) {
            displayJob(jobs.get(id));
        }
    }

    // -------------------- Application and Shortlist --------------------

    static void applyForJob() {
        System.out.println("\n--- Apply for Job ---");

        String studentId = readText("Enter student ID: ").toUpperCase();
        Student student = searchStudentNode(studentRoot, studentId);

        if (student == null) {
            System.out.println("Student " + studentId + " not found.");
            return;
        }

        String jobId = readText("Enter job ID: ").toUpperCase();
        Job job = jobs.get(jobId);

        if (job == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }

        HashSet<String> appliedStudents =
                applications.computeIfAbsent(jobId, key -> new HashSet<>());

        if (appliedStudents.contains(student.id)) {
            System.out.println("Application rejected: student has already applied for this job.");
            return;
        }

        if (student.cgpa < job.minimumCgpa) {
            System.out.printf("Not eligible. Required CGPA: %.2f | Your CGPA: %.2f%n",
                    job.minimumCgpa, student.cgpa);
            return;
        }

        appliedStudents.add(student.id);
        System.out.println("Application successful: " + student.id + " | "
                + student.name + " | CGPA " + String.format("%.1f", student.cgpa));
    }

    static void displayShortlist() {
        System.out.println("\n--- Candidate Shortlist ---");

        String jobId = readText("Enter job ID: ").toUpperCase();
        Job job = jobs.get(jobId);

        if (job == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }

        HashSet<String> appliedIds =
                applications.getOrDefault(jobId, new HashSet<>());

        PriorityQueue<Candidate> queue = createCandidateQueue();

        for (String studentId : appliedIds) {
            Student student = searchStudentNode(studentRoot, studentId);

            if (student != null && student.cgpa >= job.minimumCgpa) {
                queue.offer(new Candidate(student));
            }
        }

        if (queue.isEmpty()) {
            System.out.println("No eligible applications for job " + jobId + ".");
            return;
        }

        System.out.println("Shortlist for " + job.id + " | " + job.company
                + " | Minimum CGPA " + job.minimumCgpa);

        int rank = 1;
        while (!queue.isEmpty()) {
            Student student = queue.poll().student;
            System.out.printf("%d. %s | %s | %.1f%n",
                    rank++, student.name, student.id, student.cgpa);
        }
    }

    // -------------------- File Handling --------------------

    static void loadData() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            System.out.println("No existing " + DATA_FILE
                    + " found. Starting with an empty portal.");
            return;
        }

        int loadedRecords = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                try {
                    if (parts[0].equals("S") && parts.length == 4) {
                        String id = parts[1];
                        String name = parts[2];
                        double cgpa = Double.parseDouble(parts[3]);

                        if (searchStudentNode(studentRoot, id) == null) {
                            Student student = new Student(id, name, cgpa);
                            studentRoot = insertStudentNode(studentRoot, student);
                            updateNextStudentNumber(id);
                            loadedRecords++;
                        }
                    } else if (parts[0].equals("J") && parts.length == 5) {
                        String id = parts[1];
                        String company = parts[2];
                        String role = parts[3];
                        double minimumCgpa = Double.parseDouble(parts[4]);

                        jobs.put(id, new Job(id, company, role, minimumCgpa));
                        applications.putIfAbsent(id, new HashSet<>());
                        loadedRecords++;
                    } else if (parts[0].equals("A") && parts.length == 3) {
                        String jobId = parts[1];
                        String studentId = parts[2];

                        applications.computeIfAbsent(jobId, key -> new HashSet<>())
                                .add(studentId);
                        loadedRecords++;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid record: " + line);
                }
            }

            System.out.println("Data loaded successfully. Records read: " + loadedRecords);
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    static void updateNextStudentNumber(String id) {
        if (id != null && id.matches("ST\\d+")) {
            try {
                int number = Integer.parseInt(id.substring(2));
                if (number >= nextStudentNumber) {
                    nextStudentNumber = number + 1;
                }
            } catch (NumberFormatException ignored) {
                // Keep the current sequence if the ID is unusually large.
            }
        }
    }

    static void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            List<Student> students = new ArrayList<>();
            collectStudents(studentRoot, students);

            for (Student student : students) {
                writer.println("S|" + student.id + "|" + student.name + "|" + student.cgpa);
            }

            ArrayList<String> jobIds = new ArrayList<>(jobs.keySet());
            Collections.sort(jobIds);

            for (String jobId : jobIds) {
                Job job = jobs.get(jobId);
                writer.println("J|" + job.id + "|" + job.company + "|"
                        + job.role + "|" + job.minimumCgpa);
            }

            for (String jobId : jobIds) {
                HashSet<String> studentIds =
                        applications.getOrDefault(jobId, new HashSet<>());

                ArrayList<String> sortedStudentIds = new ArrayList<>(studentIds);
                Collections.sort(sortedStudentIds);

                for (String studentId : sortedStudentIds) {
                    writer.println("A|" + jobId + "|" + studentId);
                }
            }

            System.out.println("Data saved successfully to " + DATA_FILE + ".");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // -------------------- Main Menu --------------------

    static void showMenu() {
        System.out.println("\n======================================");
        System.out.println("          PLACEMENT PORTAL");
        System.out.println("======================================");
        System.out.println("1. Add Student");
        System.out.println("2. Add Job Opening");
        System.out.println("3. Search Student");
        System.out.println("4. Search Job");
        System.out.println("5. Apply for Job");
        System.out.println("6. Display Shortlist");
        System.out.println("7. Display All Students");
        System.out.println("8. Display All Jobs");
        System.out.println("9. Save Data");
        System.out.println("0. Save and Exit");
        System.out.println("--------------------------------------");
    }

    public static void main(String[] args) {
        loadData();

        boolean running = true;

        while (running) {
            showMenu();
            int choice = readChoice();

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    addJob();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    searchJob();
                    break;
                case 5:
                    applyForJob();
                    break;
                case 6:
                    displayShortlist();
                    break;
                case 7:
                    displayAllStudents();
                    break;
                case 8:
                    displayAllJobs();
                    break;
                case 9:
                    saveData();
                    break;
                case 0:
                    saveData();
                    running = false;
                    System.out.println("Thank you for using the Placement Portal!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid menu option.");
            }
        }

        scanner.close();
    }
}
