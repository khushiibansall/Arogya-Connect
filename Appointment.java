import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Appointment {
    private static Doctor[] doctors;
    private static Graph doctorGraph;
    private static final int PATIENT_NODE_INDEX = 5; // Adding patient as the 6th node

    // Static variables to store patient coordinates
    private static double patientLatitude;
    private static double patientLongitude;

    public static void initializeDoctors() {
        // Example doctors
        doctors = new Doctor[5];
        doctors[0] = new Doctor(0, "Sharma", "Cardiologist", 30.316496, 78.032188, new String[]{"10 AM", "11 AM", "1 PM"}); // Dehradun
        doctors[1] = new Doctor(1, "Reddy", "Cardiologist", 28.613895, 77.209006, new String[]{"9 AM", "11 AM", "2 PM"}); // Delhi
        doctors[2] = new Doctor(2, "Singh", "Cardiologist", 19.075983, 72.877655, new String[]{"10 AM", "1 PM", "5 PM"}); // Mumbai
        doctors[3] = new Doctor(3, "Kulkarni", "Cardiologist", 18.520430, 73.856743, new String[]{"9 AM", "11 AM", "4 PM"}); // Pune
        doctors[4] = new Doctor(4, "Malhotra", "Cardiologist", 28.459497, 77.026638, new String[]{"10 AM", "1 PM", "6 PM"}); // Gurgaon


    }

    public static void bookAppointment(Scanner scanner) {
        System.out.println("\nSelect the type of doctor you need:");
        System.out.println("1. Physician/ चिकित्सक");
        System.out.println("2. Cardiologist/ हृदय रोग विशेषज्ञ");
        System.out.println("3. Surgeon/ शल्य चिकित्सक");
        System.out.println("4. Dermatologist/ त्वचा रोग विशेषज्ञ");
        System.out.print("Enter your choice: ");
        int doctorType = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        // Fetch patient's public IP and location
        String userIp = PublicIPService.getPublicIP();
        System.out.println("Your IP: " + userIp);
        String coordinates = PublicIPService.getCoordinatesFromIP(userIp);
        if (coordinates.startsWith("Latitude/Longitude not available") || coordinates.startsWith("Unable")) {
            System.out.println("Error fetching your coordinates.");
            return; // Exit if unable to fetch coordinates
        }
        System.out.println("Coordinates from IP service: " + coordinates);

        // Extract latitude and longitude from coordinates
        String[] locationParts = coordinates.split(",");
        try {
            patientLatitude = Double.parseDouble(locationParts[0].trim());
            patientLongitude = Double.parseDouble(locationParts[1].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error parsing coordinates. Exiting appointment booking.");
            return;
        }

        System.out.println("Parsed patient coordinates: Latitude=" + patientLatitude + ", Longitude=" + patientLongitude);

        // Check if the coordinates are valid (not 0,0)
        if (patientLatitude == 0.0 && patientLongitude == 0.0) {
            System.out.println("Error: Patient coordinates are invalid (0.0, 0.0).");
            return;
        }

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Save patient info and create patient object
        UserInputApp.saveToDatabase(name, age, patientLatitude, patientLongitude);
        Patient patient = new Patient(name, age, patientLatitude, patientLongitude, getDoctorType(doctorType));

        // Initialize the graph with an extra node for the patient
        doctorGraph = new Graph(doctors.length + 1);

        // Calculate and add patient-to-doctor edges using patient coordinates
        for (Doctor doctor : doctors) {
            double distance = DistanceCalculator.calculateDistance(patientLatitude, patientLongitude, doctor.latitude, doctor.longitude);
            doctorGraph.addEdge(PATIENT_NODE_INDEX, doctor.id, distance);
        }

        // Calculate and add doctor-to-doctor edges
        for (int i = 0; i < doctors.length; i++) {
            for (int j = i + 1; j < doctors.length; j++) {
                double distance = DistanceCalculator.calculateDistance(
                    doctors[i].latitude, doctors[i].longitude,
                    doctors[j].latitude, doctors[j].longitude
                );
                doctorGraph.addEdge(doctors[i].id, doctors[j].id, distance);
            }
        }
        GraphVisualizer.createGraph(List.of(doctors), patientLatitude, patientLongitude);




        // Use Dijkstra's algorithm to find the shortest distances to each doctor
        matchDoctors(patient, doctorType, scanner);
    }


    private static String getDoctorType(int doctorType) {
        switch (doctorType) {
            case 1: return "Physician";
            case 2: return "Cardiologist";
            case 3: return "Surgeon";
            case 4: return "Dermatologist";
            default: return "Unknown";
        }
    }

    private static void matchDoctors(Patient patient, int doctorType, Scanner scanner) {
        System.out.println("\nSearching for nearby doctors...");

        // Run Dijkstra’s algorithm from the patient node
        double[] distances = doctorGraph.dijkstra(PATIENT_NODE_INDEX);

        // Filter doctors by specialization and sort by Dijkstra distance
        List<Doctor> filteredDoctors = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if (doctor.specialization.equals(getDoctorType(doctorType))) {
                filteredDoctors.add(doctor);
            }
        }
        filteredDoctors.sort(Comparator.comparingDouble(doc -> distances[doc.id]));

        // Display sorted doctors based on distance
        System.out.println("We found the following " + getDoctorType(doctorType) + "s:");
        for (int i = 0; i < filteredDoctors.size(); i++) {
            Doctor doctor = filteredDoctors.get(i);
            System.out.printf("%d. %s (%.2f km away)\n", i + 1, doctor.name, distances[doctor.id]);
        }

        // Patient selects a doctor
        System.out.print("Select a doctor: ");
        int selectedDoctor = scanner.nextInt();
        scanner.nextLine();
        Doctor chosenDoctor = filteredDoctors.get(selectedDoctor - 1);
        displayTimeSlots(chosenDoctor, scanner);
    }

    private static void displayTimeSlots(Doctor doctor, Scanner scanner) {
        System.out.println("\nAvailable time slots for Dr. " + doctor.name + ":");
        for (String slot : doctor.availableSlots) {
            System.out.println("- " + slot);
        }

        System.out.print("Select a time slot: ");
        int slot = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.println("Appointment confirmed with Dr. " + doctor.name + " at " + doctor.availableSlots[slot - 1]);
    }
}
