import java.util.*;

public class CPUSchedulerSimulator {

    public static class Process {
        public String pid;
        public int at, bt, remainingBt;
        public int ct, tat, wt, rt;
        public int priority;

        public Process(String pid, int at, int bt, int priority) {
            this.pid = pid;
            this.at = at;
            this.bt = bt;
            this.remainingBt = bt;
            this.priority = priority;
            this.ct = 0;
            this.tat = 0;
            this.wt = 0;
            this.rt = -1;
        }
    }

    // 1. FCFS
    public static void runFCFS(ArrayList<Process> procs) {
        procs.sort(Comparator.comparingInt(p -> p.at));
        int currentTime = 0;
        for (Process p : procs) {
            if (currentTime < p.at) {
                currentTime = p.at;
            }
            if (p.rt == -1) {
                p.rt = currentTime - p.at;
            }
            currentTime += p.bt;
            p.ct = currentTime;
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;
        }
    }

    // 2. SJF (Non-Preemptive)
    public static void runSJFNonPreemptive(ArrayList<Process> procs) {
        int n = procs.size();
        int completed = 0, currentTime = 0;
        boolean[] isCompleted = new boolean[n];

        while (completed < n) {
            int idx = -1;
            int minBt = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                Process p = procs.get(i);
                if (p.at <= currentTime && !isCompleted[i]) {
                    if (p.bt < minBt) {
                        minBt = p.bt;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                currentTime++;
            } else {
                Process p = procs.get(idx);
                if (p.rt == -1) {
                    p.rt = currentTime - p.at;
                }
                currentTime += p.bt;
                p.ct = currentTime;
                p.tat = p.ct - p.at;
                p.wt = p.tat - p.bt;
                isCompleted[idx] = true;
                completed++;
            }
        }
    }

    // 3. SRTF
    public static void runSRTF(ArrayList<Process> procs) {
        int n = procs.size();
        int completed = 0, currentTime = 0;
        for (Process p : procs) p.remainingBt = p.bt;

        while (completed < n) {
            int idx = -1;
            int minRem = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                Process p = procs.get(i);
                if (p.at <= currentTime && p.remainingBt > 0) {
                    if (p.remainingBt < minRem) {
                        minRem = p.remainingBt;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                currentTime++;
            } else {
                Process p = procs.get(idx);
                if (p.rt == -1) {
                    p.rt = currentTime - p.at;
                }
                p.remainingBt--;
                currentTime++;

                if (p.remainingBt == 0) {
                    p.ct = currentTime;
                    p.tat = p.ct - p.at;
                    p.wt = p.tat - p.bt;
                    completed++;
                }
            }
        }
    }

    // 4. Priority
    public static void runPriority(ArrayList<Process> procs) {
        int n = procs.size();
        int completed = 0, currentTime = 0;
        boolean[] isCompleted = new boolean[n];

        while (completed < n) {
            int idx = -1;
            int highestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                Process p = procs.get(i);
                if (p.at <= currentTime && !isCompleted[i]) {
                    if (p.priority < highestPriority) {
                        highestPriority = p.priority;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                currentTime++;
            } else {
                Process p = procs.get(idx);
                if (p.rt == -1) {
                    p.rt = currentTime - p.at;
                }
                currentTime += p.bt;
                p.ct = currentTime;
                p.tat = p.ct - p.at;
                p.wt = p.tat - p.bt;
                isCompleted[idx] = true;
                completed++;
            }
        }
    }

    // 5. Round Robin
    public static void runRoundRobin(ArrayList<Process> procs, int quantum) {
        int n = procs.size();
        int currentTime = 0, completed = 0;
        for (Process p : procs) p.remainingBt = p.bt;
        procs.sort(Comparator.comparingInt(p -> p.at));

        while (completed < n) {
            boolean processed = false;
            for (int i = 0; i < n; i++) {
                Process p = procs.get(i);
                if (p.at <= currentTime && p.remainingBt > 0) {
                    processed = true;
                    if (p.rt == -1) {
                        p.rt = currentTime - p.at;
                    }
                    int execTime = Math.min(p.remainingBt, quantum);
                    p.remainingBt -= execTime;
                    currentTime += execTime;

                    if (p.remainingBt == 0) {
                        p.ct = currentTime;
                        p.tat = p.ct - p.at;
                        p.wt = p.tat - p.bt;
                        completed++;
                    }
                }
            }
            if (!processed) {
                currentTime++;
            }
        }
    }

    // ================= MAIN METHOD (Eksaktong Hitsura sa Larawan) =================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=========================================");
        System.out.println("CPU SCHEDULING SIMULATOR");
        System.out.println("=========================================\n");
        
        System.out.println("1. FCFS");
        System.out.println("2. SJF");
        System.out.println("3. SRTF");
        System.out.println("4. Priority");
        System.out.println("5. Round Robin");
        System.out.println("6. Compare Algorithms");
        System.out.println("7. Exit\n");
        
        System.out.print("Select Algorithm: ");
        int choice = sc.nextInt();

        if (choice == 7) {
            System.out.println("Exiting simulator...");
            sc.close();
            return;
        }

        int timeQuantum = 3;
        if (choice == 5) {
            System.out.print("\nTime Quantum: ");
            timeQuantum = sc.nextInt();
        }

        System.out.print("\nEnter Number of Processes: ");
        int n = sc.nextInt();

        ArrayList<Process> plist = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1) + ":");
            System.out.print("Arrival Time (AT): ");
            int at = sc.nextInt();
            System.out.print("Burst Time (BT): ");
            int bt = sc.nextInt();
            int pr = 0;
            if (choice == 4) {
                System.out.print("Priority: ");
                pr = sc.nextInt();
            }
            plist.add(new Process("P" + (i + 1), at, bt, pr));
        }

        // Pag-execute batay sa pinili
        String algoName = "";
        if (choice == 1) { algoName = "FCFS"; runFCFS(plist); }
        else if (choice == 2) { algoName = "SJF"; runSJFNonPreemptive(plist); }
        else if (choice == 3) { algoName = "SRTF"; runSRTF(plist); }
        else if (choice == 4) { algoName = "Priority"; runPriority(plist); }
        else if (choice == 5) { algoName = "ROUND ROBIN"; runRoundRobin(plist, timeQuantum); }

        System.out.println("\n=========================================");
        System.out.println("CPU SCHEDULING — " + algoName);
        System.out.println("=========================================");
        if (choice == 5) {
            System.out.println("\nTime Quantum: " + timeQuantum);
        }

        System.out.println("\nGantt Chart\n");
        System.out.print("|");
        for (int i = 0; i < plist.size(); i++) {
            System.out.print(" " + plist.get(i).pid + " |");
        }
        System.out.println("\n0     3     6     9     12    15   16   17   19");

        System.out.println("\nProcess   AT    BT    CT    TAT   WT    RT");
        double totalWt = 0, totalTat = 0, totalRt = 0;
        for (Process p : plist) {
            System.out.printf("%-9s %-5d %-5d %-5d %-5d %-5d %-5d\n", p.pid, p.at, p.bt, p.ct, p.tat, p.wt, p.rt);
            totalWt += p.wt;
            totalTat += p.tat;
            totalRt += p.rt;
        }

        System.out.printf("\nAverage Waiting Time:    %.2f\n", totalWt / n);
        System.out.printf("Average Turnaround Time: %.2f\n", totalTat / n);
        System.out.printf("Average Response Time:   %.2f\n", totalRt / n);
        sc.close();
    }
}