import java.util.*;


public class Main {

    //        ---------------------------  static variables  -----------------------------------------------
    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Interval> parent = new ArrayList<>();
    static ArrayList<Integer> size = new ArrayList<>();
    static ArrayList<Interval> sortedIntervals = new ArrayList<>();

    private static class Interval {
        int a;
        int b;
        int w;
        Interval successor;
        boolean isActive = true;
        int pathSize = -1;
        Interval representative; //  namayandeye disjoint set tree.

        public Interval(int a, int b, int w) {
            this.a = a;
            this.b = b;
            this.w = w;
        }
    }

    //  container store all points of intervals to sort them with bucket sort and use it to find successors.
    private static class Container {
        int value;
        Interval interval;
        boolean isStart;

        public Container(int value, Interval interval, boolean isStart) {
            this.value = value;
            this.interval = interval;
            this.isStart = isStart;
        }
    }


    //    ----------------------------------------------- methods -----------------------------------------------
    //  this bucketSort sort Interval list by their end points.
    public static ArrayList<Interval> bBucketSort(ArrayList<Interval> intervals, int n) {
        ArrayList<Interval> sortedIntervalList = new ArrayList<>();
        ArrayList<Interval> finalSortedIntervalList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            sortedIntervalList.add(null);
        }
        for (Interval interval : intervals) {
            sortedIntervalList.set(interval.b, interval);
        }
        for (Interval interval : sortedIntervalList) {
            if (interval != null)
                finalSortedIntervalList.add(interval);
        }
        return finalSortedIntervalList;
    }

    //  this bucketSort sort all point stored in containers.
    public static ArrayList<Container> pBucketSort(ArrayList<Container> containers, int n) {
        ArrayList<Container> finalSortedArrayList = new ArrayList<>();
        ArrayList<Container> sortedArrayList = new ArrayList<>();
        for (int i = 0; i < n; i++)
            sortedArrayList.add(null);
        for (Container container : containers)
            sortedArrayList.set(container.value, container);
        for (Container container : sortedArrayList)
            if (container != null)
                finalSortedArrayList.add(container);

        return finalSortedArrayList;
    }
    //   these two functions written separate because they are sorting two different type of values.


    //  a function to reverse sorted containers array to iterate from end to beginning
    public static <T> List<T> reverseList(List<T> list) {
        List<T> reversedList = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            reversedList.add(list.get(i));
        }
        return reversedList;
    }

    //  set every intervals successor
    public static void setSuccessor(ArrayList<Container> containers) {
        List<Container> reversedContainer = reverseList(containers);
        Interval lastSeen = null;
        for (Container container : reversedContainer) {
            if (!container.isStart)
                lastSeen = container.interval;
            else
                container.interval.successor = lastSeen;
        }
    }


    //    ----------------------------------------------- union & find -----------------------------------------------
    static Interval find(Interval x) {
        Interval p = parent.get(sortedIntervals.indexOf(x));
        if (p == x)
            return x;
        parent.set(sortedIntervals.indexOf(x), find(p));
        return parent.get(sortedIntervals.indexOf(x));
    }

    static boolean union(Interval x, Interval y) {
        Interval rootX = find(x);
        Interval rootY = find(y);
        if (rootY == rootX)
            return false;
        if (size.get(sortedIntervals.indexOf(rootY)) > size.get(sortedIntervals.indexOf(rootX))) {
            Interval temp = rootX;
            rootX = rootY;
            rootY = temp;
        }
        parent.set(sortedIntervals.indexOf(rootY), rootX);
        int newSize = Integer.sum(size.get(sortedIntervals.indexOf(rootX)), size.get(sortedIntervals.indexOf(rootY)));
        size.set(sortedIntervals.indexOf(rootX), newSize);
        return true;
    }


    //        ---------------------------  MAIN Function  -----------------------------------------------

    public static void main(String[] args) {
    //        ---------------------------  variable definition  -----------------------------------------------
        ArrayList<Container> containers = new ArrayList<>();
        Stack<Interval> active = new Stack<>();
        Stack<Interval> specialInactive = new Stack<>();

        // input values line by line and add them to interval array and container.
        int n = Integer.parseInt(scanner.next());
        if (n == 0)
            return;
        for (int i = 0; i < n; i++) {
            int a = scanner.nextInt();
            int b = scanner.nextInt();
            int w = scanner.nextInt();
            Interval interval = new Interval(a, b, w);
            containers.add(new Container(a, interval, true));
            containers.add(new Container(b, interval, false));
            sortedIntervals.add(interval);
        }
        sortedIntervals = bBucketSort(sortedIntervals, (int) Math.pow(10, 5));
        containers = pBucketSort(containers, (int) Math.pow(10, 5));
        setSuccessor(containers);
        for (Interval interval : sortedIntervals) {
            parent.add(interval);
            size.add(1);
        }


        //  push first interval and its path to active stack and start filling stack
        int i = 0;
        sortedIntervals.get(0).pathSize = sortedIntervals.get(0).w;
        active.push(sortedIntervals.get(i));
        sortedIntervals.get(0).representative = sortedIntervals.get(0);

        for (i++; i < sortedIntervals.size(); i++) {
            Interval interval = sortedIntervals.get(i);
            //  if an interval successor equals to itself it means the interval is inactive
            if (find(interval.successor) == interval) {
                interval.isActive = false;
                specialInactive.push(interval);
            }
            // if find(interval successor) is inactive it means that the interval is inactive too
            // (successor is in special inactive stack or has joined to an inactive or active interval).
            else if (!find(interval.successor).isActive) {
                interval.isActive = false;
                union(find(interval.successor), interval);
            }
            //  and if none of these condition has occurred it meant this interval is active and must calculate its path and push it into active stack.
            else if (find(interval.successor).isActive) {
                interval.pathSize = interval.w + find(interval.successor).representative.pathSize;
                while (interval.pathSize < active.peek().pathSize) {
                    active.peek().isActive = false; //set top interval with longer path inactive.
                    union(interval, active.pop());
                    find(interval).representative = interval;
                }
                while (!specialInactive.isEmpty())
                    union(interval, specialInactive.pop());
                if (interval.representative == null)
                    interval.representative = interval;
                active.push(interval);
            }
        }

        // set inactive interval shorted path.
        for (Interval interval : sortedIntervals) {
            if (!interval.isActive && find(interval).representative != null) {
                interval.pathSize = interval.w + find(interval.successor).representative.pathSize; // change to find(interval.successor)
            }
        }

        //  print the result with its line number.
        i = 1;
        for (Interval interval : sortedIntervals)
            System.out.println(i++ + " " + interval.pathSize);
    }
}
