package com.jogdesktopapp.Jog_Desktop_App;


import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import java.util.Map;

public class PrinterStatusChecker {
    public static void checkPrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        if (printServices.length == 0) {
            System.out.println("No printers found.");
            return;
        }

        System.out.println("Number of connected printers: " + printServices.length);

        for (PrintService printer : printServices) {
            System.out.println("Printer: " + printer.getName());

            PrintServiceAttributeSet attributes = printer.getAttributes();

            // Check if printer is accepting jobs
            PrinterIsAcceptingJobs acceptingJobs = (PrinterIsAcceptingJobs) attributes.get(PrinterIsAcceptingJobs.class);
            if (acceptingJobs != null) {
                System.out.println("Accepting Jobs: " + acceptingJobs);
            }

            // Check printer state
            PrinterState printerState = (PrinterState) attributes.get(PrinterState.class);
            if (printerState != null) {
                System.out.println("Printer State: " + printerState);
            }

            // Check detailed state reasons
            PrinterStateReasons stateReasons = (PrinterStateReasons) attributes.get(PrinterStateReasons.class);
            if (stateReasons != null) {
                System.out.println("State Reasons:");
                for (Map.Entry<PrinterStateReason, ?> entry : stateReasons.entrySet()) { // Removed .Severity
                    System.out.println(" - " + entry.getKey() + ": " + entry.getValue());
                }
            }

            System.out.println("----------------------------");
        }
    }
}