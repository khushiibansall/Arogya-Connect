// src/MedicineReminderApp.java
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Scanner;

public class MedicineReminderApp {
    private static MinHeap reminderHeap = new MinHeap();
    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d MMM yyyy hh:mm a")
            .toFormatter(Locale.ENGLISH);

    public static void setMedicineReminder(Scanner scanner) {
        // Start the reminder checking thread
        Thread reminderThread = new Thread(() -> {
            while (true) {
                checkReminders();
                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        reminderThread.setDaemon(true); // Ensure the thread exits when the main program exits
        reminderThread.start();

        System.out.println("Medicine Reminder App");
        while (true) {
            System.out.println("\n1. Add Reminder");
            System.out.println("2. Check Next Reminder");
            System.out.println("3. Go Back");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    System.out.print("Enter medicine name: ");
                    String medicine = scanner.nextLine();
                    System.out.print("Enter time (e.g., 15 Jan 2024 09:46 AM): ");
                    String inputTime = scanner.nextLine();
                    try {
                        LocalDateTime time = LocalDateTime.parse(inputTime, formatter);
                        reminderHeap.insert(new Reminder(time, "Time to take your " + medicine));
                        System.out.println("Reminder added!");
                    } catch (Exception e) {
                        System.out.println("Invalid date/time format. Please use the format 'd MMM yyyy hh:mm a'.");
                        System.out.println("Error details: " + e.getMessage());
                    }
                    break;
                case 2:
                    if (!reminderHeap.isEmpty()) {
                        Reminder nextReminder = reminderHeap.peekMin();
                        System.out.println("Next Reminder: " + nextReminder.message + " at " + nextReminder.time.format(formatter));
                    } else {
                        System.out.println("No reminders set.");
                    }
                    break;
                case 3:
                    // Go back to the main menu
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Method to check if the next reminder is due
    private static void checkReminders() {
        if (!reminderHeap.isEmpty()) {
            Reminder nextReminder = reminderHeap.peekMin();
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(nextReminder.time) || now.isEqual(nextReminder.time)) {
                // Show system notification instead of console message
                showSystemNotification(nextReminder.message, nextReminder.time);
                reminderHeap.extractMin(); // Remove the reminder after displaying it
            }
        }
    }

    // Method to display system notification
    private static void showSystemNotification(String message, LocalDateTime time) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            // Optional: specify an icon for the notification
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Medicine Reminder");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Medicine Reminder Notification");

            try {
                tray.add(trayIcon);
                trayIcon.displayMessage("Reminder", message + " at " + time.format(formatter), MessageType.INFO);
                tray.remove(trayIcon); // Remove after displaying
            } catch (AWTException e) {
                System.err.println("Error showing notification: " + e.getMessage());
            }
        } else {
            System.out.println("System notifications are not supported on this platform.");
        }
    }
}
