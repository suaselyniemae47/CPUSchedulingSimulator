import java.util.*;

class Process implements Cloneable {
    String pid;
    int at, bt, priority, remBt;
    int ct, tat, wt, rt = -1;

    public Process(String pid, int at, int bt, int priority) {
        this.pid = pid;
        this.at = at;
        this.bt = bt;
        this.priority = priority;
        this.remBt = bt;
    }

    @Override
    public Process clone() {
        Process p = new Process(this.pid, this.at, this.bt, this.priority);
        p.remBt = this.remBt;
        p.ct = this.ct;
        p.tat = this.tat;
        p.wt = this.wt;
        p.rt = this.rt;
        return p;
    }
}

class GanttBlock {
    String pid;
    int start, end;

    public GanttBlock(String pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
}

public class CPUSchedulerSimulator {

    // Print Simple Gantt Chart matching the style in image
    public static void printGanttChart(List<GanttBlock> timeline) {
        if (timeline.isEmpty()) return;

        // Merge consecutive blocks of the same process
        List<GanttBlock> merged = new ArrayList<>();
        for (GanttBlock b : timeline) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).pid.equals(b.pid) 
                && merged.get(merged.size() - 1).end == b.start) {
                merged.get(merged.size() - 1).end = b.end;
            } else {
                merged.add(new GanttBlock(b.pid, b.start, b.end));
            }
        }

        System.out.println("\nGantt Chart\n");
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder(String.valueOf(merged.get(0).start));

        for (GanttBlock b : merged) {
            line1.append("| ").append(b.pid).append(" ");
            String endStr = String.valueOf(b.end);
            int spacing = 4 + (b.pid.length() - endStr.length());
            line2.append(" ".repeat(Math.max(1, spacing))).append(endStr);
        }
        line1.append("|");

        System.out.println(line1);
        System.out.println(line2);
        System.out.println();
    }

    public static void printMetrics(List<Process> processes) {
        processes.sort(Comparator.comparing(p -> p.pid));
        double totalTat = 0, totalWt = 0, totalRt = 0;
        int n = processes.size();

        System.out.printf("%-10s%-6s%-6s%-6s%-6s%-6s%-6s%n", "Process", "AT", "BT", "CT", "TAT", "WT", "RT");
        for (Process p : processes) {
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;
            totalTat += p.tat;
            totalWt += p.wt;
            totalRt += p.rt;
            System.out.printf("%-10s%-6d%-6d%-6d%-6d%-6d%-6d%n", 
                p.pid, p.at, p.bt, p.ct, p.tat, p.wt, p.rt);
        }

        System.out.println();
        System.out.printf("Average Waiting Time: %.2f%n", totalWt / n);
        System.out.printf("Average Turnaround Time: %.2f%n", totalTat / n);
        System.out.printf("Average Response Time: %.2f%n", totalRt / n);
    }

    // 1. FCFS
    public static void runFCFS(List<Process> procs) {
        System.out.println("\nCPU SCHEDULING — FCFS\n");
        procs.sort(Comparator.comparing((Process p) -> p.at).thenComparing(p -> p.pid));
        int currentTime = 0;
        List<GanttBlock> timeline = new ArrayList<>();

        for (Process p : procs) {
            if (currentTime < p.at) {
                timeline.add(new GanttBlock("IDLE", currentTime, p.at));
                currentTime = p.at;
            }
            p.rt = currentTime - p.at;
            int start = currentTime;
            currentTime += p.bt;
            p.ct = currentTime;
            timeline.add(new GanttBlock(p.pid, start, currentTime));
        }
        printGanttChart(timeline);
        printMetrics(procs);
    }

    // 2. SJF (Non-Preemptive)
    public static void runSJF(List<Process> procs) {
        System.out.println("\nCPU SCHEDULING — SJF (NON-PREEMPTIVE)\n");
        int n = procs.size(), completed = 0, currentTime = 0;
        boolean[] isCompleted = new boolean[n];
        List<GanttBlock> timeline = new ArrayList<>();

        while (completed < n) {
            int idx = -1, minBt = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (procs.get(i).at <= currentTime && !isCompleted[i] && procs.get(i).bt < minBt) {
                    minBt = procs.get(i).bt;
                    idx = i;
                }
            }
            if (idx != -1) {
                Process p = procs.get(idx);
                p.rt = currentTime - p.at;
                int start = currentTime;
                currentTime += p.bt;
                p.ct = currentTime;
                isCompleted[idx] = true;
                completed++;
                timeline.add(new GanttBlock(p.pid, start, currentTime));
            } else {
                int nextAt = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) if (!isCompleted[i] && procs.get(i).at < nextAt) nextAt = procs.get(i).at;
                timeline.add(new GanttBlock("IDLE", currentTime, nextAt));
                currentTime = nextAt;
            }
        }
        printGanttChart(timeline);
        printMetrics(procs);
    }

    // 3. SRTF (Preemptive SJF)
    public static void runSRTF(List<Process> procs) {
        System.out.println("\nCPU SCHEDULING — SRTF\n");
        int n = procs.size(), completed = 0, currentTime = 0;
        List<GanttBlock> timeline = new ArrayList<>();

        while (completed < n) {
            int idx = -1, minRem = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (procs.get(i).at <= currentTime && procs.get(i).remBt > 0 && procs.get(i).remBt < minRem) {
                    minRem = procs.get(i).remBt;
                    idx = i;
                }
            }
            if (idx != -1) {
                Process p = procs.get(idx);
                if (p.rt == -1) p.rt = currentTime - p.at;
                timeline.add(new GanttBlock(p.pid, currentTime, currentTime + 1));
                p.remBt--;
                currentTime++;
                if (p.remBt == 0) { p.ct = currentTime; completed++; }
            } else {
                timeline.add(new GanttBlock("IDLE", currentTime, currentTime + 1));
                currentTime++;
            }
        }
        printGanttChart(timeline);
        printMetrics(procs);
    }

    // 4. Priority Scheduling (Non-Preemptive, lower number = higher priority)
    public static void runPriority(List<Process> procs) {
        System.out.println("\nCPU SCHEDULING — PRIORITY\n");
        int n = procs.size(), completed = 0, currentTime = 0;
        boolean[] isCompleted = new boolean[n];
        List<GanttBlock> timeline = new ArrayList<>();

        while (completed < n) {
            int idx = -1, highestPriority = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (procs.get(i).at <= currentTime && !isCompleted[i] && procs.get(i).priority < highestPriority) {
                    highestPriority = procs.get(i).priority;
                    idx = i;
                }
            }
            if (idx != -1) {
                Process p = procs.get(idx);
                p.rt = currentTime - p.at;
                int start = currentTime;
                currentTime += p.bt;
                p.ct = currentTime;
                isCompleted[idx] = true;
                completed++;
                timeline.add(new GanttBlock(p.pid, start, currentTime));
            } else {
                int nextAt = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) if (!isCompleted[i] && procs.get(i).at < nextAt) nextAt = procs.get(i).at;
                timeline.add(new GanttBlock("IDLE", currentTime, nextAt));
                currentTime = nextAt;
            }
        }
        printGanttChart(timeline);
        printMetrics(procs);
    }

    // 5. Round Robin
    public static void runRoundRobin(List<Process> procs, int quantum) {
        System.out.println("\nCPU SCHEDULING — ROUND ROBIN\n");
        System.out.println("Time Quantum: " + quantum + "\n");
        
        procs.sort(Comparator.comparing((Process p) -> p.at).thenComparing(p -> p.pid));
        Queue<Process> queue = new LinkedList<>();
        int n = procs.size(), currentTime = 0, completed = 0, idx = 0;
        List<GanttBlock> timeline = new ArrayList<>();

        while (idx < n && procs.get(idx).at <= currentTime) queue.add(procs.get(idx++));

        while (completed < n) {
            if (queue.isEmpty()) {
                int nextAt = procs.get(idx).at;
                timeline.add(new GanttBlock("IDLE", currentTime, nextAt));
                currentTime = nextAt;
                while (idx < n && procs.get(idx).at <= currentTime) queue.add(procs.get(idx++));
            }
            Process p = queue.poll();
            if (p.rt == -1) p.rt = currentTime - p.at;

            int execTime = Math.min(quantum, p.remBt);
            int start = currentTime;
            currentTime += execTime;
            p.remBt -= execTime;
            timeline.add(new GanttBlock(p.pid, start, currentTime));

            while (idx < n && procs.get(idx).at <= currentTime) queue.add(procs.get(idx++));

            if (p.remBt > 0) queue.add(p);
            else { p.ct = currentTime; completed++; }
        }
        printGanttChart(timeline);
        printMetrics(procs);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();
        int quantum = 3;

        // Default sample data matching your screenshot prompt (P1, P2, P3, P4)
        processes.add(new Process("P1", 0, 7, 2));
        processes.add(new Process("P2", 1, 4, 1));
        processes.add(new Process("P3", 2, 3, 3));
        processes.add(new Process("P4", 3, 5, 4));

        while (true) {
            System.out.println("==================================");
            System.out.println("CPU SCHEDULING SIMULATOR");
            System.out.println("==================================");
            System.out.println("1. FCFS");
            System.out.println("2. SJF");
            System.out.println("3. SRTF");
            System.out.println("4. Priority");
            System.out.println("5. Round Robin");
            System.out.println("6. Compare Algorithms");
            System.out.println("7. Exit");
            System.out.print("\nSelect Algorithm: ");
            
            int choice = Integer.parseInt(sc.nextLine().trim());

            if (choice == 7) {
                System.out.println("Exiting simulator. Goodbye!");
                break;
            }

            if (choice == 5) {
                System.out.print("\nTime Quantum: ");
                quantum = Integer.parseInt(sc.nextLine().trim());
            }

            System.out.print("\nEnter Number of Processes: ");
            int n = Integer.parseInt(sc.nextLine().trim());
            processes.clear();

            for (int i = 0; i < n; i++) {
                System.out.println("Process " + (i + 1) + ":");
                System.out.print("  Process ID (e.g. P1): ");
                String pid = sc.nextLine().trim();
                System.out.print("  Arrival Time: ");
                int at = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Burst Time: ");
                int bt = Integer.parseInt(sc.nextLine().trim());
                int pr = 1;
                if (choice == 4) {
                    System.out.print("  Priority: ");
                    pr = Integer.parseInt(sc.nextLine().trim());
                }
                processes.add(new Process(pid, at, bt, pr));
            }

            // Run selected algorithm using deep copies
            List<Process> copyList = new ArrayList<>();
            for (Process p : processes) copyList.add(p.clone());

            switch (choice) {
                case 1: runFCFS(copyList); break;
                case 2: runSJF(copyList); break;
                case 3: runSRTF(copyList); break;
                case 4: runPriority(copyList); break;
                case 5: runRoundRobin(copyList, quantum); break;
                case 6:
                    System.out.println("\n--- COMPARISON SUMMARY ---");
                    runFCFS(deepCopy(processes));
                    runSJF(deepCopy(processes));
                    runSRTF(deepCopy(processes));
                    runPriority(deepCopy(processes));
                    runRoundRobin(deepCopy(processes), quantum);
                    break;
                default:
                    System.out.println("Invalid selection!");
            }
            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
        }
        sc.close();
    }

    private static List<Process> deepCopy(List<Process> list) {
        List<Process> copy = new ArrayList<>();
        for (Process p : list) copy.add(p.clone());
        return copy;
    }
}