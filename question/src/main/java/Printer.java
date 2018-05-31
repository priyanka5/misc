import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Printer {

    private final PrintWriter pw;
    private final List<Main.Response> entries;

    public Printer(List<Main.Response> entries, OutputStream os) {
        this.entries = entries;
        this.pw = new PrintWriter(os);
    }

    public void head() {
        pw.println("<html><head>");
        pw.println("<style>\n" +
                "body {\n" +
                "    font-family: arial,sans-serif;\n" +
                "    color: #333;\n" +
                "}\n" +
                "div.maxheight {\n" +
                "    border: 1px solid #e0e0e0;\n" +
                "    max-height: 200px;\n" +
                "    overflow: auto;\n" +
                "}\n" +
                "h1 {\n" +
                "    margin-top: 50px;\n" +
                "    padding-bottom: 10px;\n" +
                "    border-bottom: 1px solid #333;\n" +
                "}\n" +
                "h2 {\n" +
                "    margin-top: 30px;\n" +
                "}\n" +
                "table {\n" +
                "    border-spacing: 0;\n" +
                "    border-collapse: collapse;\n" +
                "    font-size: 14px;\n" +
                "    width: 100%;\n" +
                "}\n" +
                "tr {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "}\n" +
                "th {\n" +
                "    text-align: left;\n" +
                "    margin: 0;\n" +
                "    padding: 5px 15px 5px 5px;\n" +
                "}\n" +
                "td {\n" +
                "    margin: 0;\n" +
                "    padding: 5px 15px 5px 5px;\n" +
                "}\n" +
                "tr:nth-child(odd){\n" +
                "  background-color: #e0e0e0;\n" +
                "}\n" +
                "</style>\n");
        pw.println("</head><body>");
    }

    public void foot() {
        pw.println("</body>");
    }

    public Printer line(String l) {
        pw.println(l);
        return this;
    }

    public Printer h1(String v) {
        pw.println("<h1>" + v + "</h1>");
        return this;
    }

    public Printer h2(String v) {
        pw.println("<h2>" + v + "</h2>");
        return this;
    }

    public Printer countYesNo(BooleanCollector c) {
        int yes = 0;
        int no = 0;

        for (Main.Response e : entries) {
            Boolean b = c.getBoolean(e);
            if (b != null) {
                if (b) {
                    yes++;
                } else {
                    no++;
                }
            }
        }

        pw.println("<div class=\"maxheight\">");
        pw.println("<table><tr><th>Yes</th><th>No</th>");
        pw.println("<tr><td>" + yes + "</td><td>" + no + "</td>");
        pw.println("</table>");
        pw.println("</div>");

        return this;
    }
    public Printer countString(StringCollector c) {
        Map<String, Integer> m = new HashMap<>();

        for (Main.Response e : entries) {
            String b = c.getString(e);
            if (b != null) {
                if (!m.containsKey(b)) {
                    m.put(b, 0);
                }
                m.put(b, m.get(b) + 1);
            }
        }

        List<Stat> l = new LinkedList<>();
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            l.add(new Stat(e.getKey(), e.getValue()));
        }

        Collections.sort(l);

        pw.println("<div class=\"maxheight\">");
        pw.println("<table>");
        for (Stat s : l) {
            pw.println("<tr><td>" + s.key + "</td><td>" + s.value + "</td></tr>");
        }
        pw.println("</table>");
        pw.println("</div>");

        return this;
    }

    public Printer ranges(NumCollector c, Integer... ranges) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        int overflowKey = 0;
        int overflowValue = 0;

        for (Integer r : ranges) {
            map.put(r, 0);
        }

        for (Main.Response e : entries) {
            Integer n = c.getInt(e);

            if (n != null) {
                boolean added = false;
                for (int i = 0; i < ranges.length; i++) {
                    if (n <= ranges[i]) {
                        map.put(ranges[i], map.get(ranges[i]) + 1);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    if (n > overflowKey) {
                        overflowKey = n;
                    }
                    overflowValue += 1;
                }
            }
        }

        pw.println("<div class=\"maxheight\">");
        pw.println("<table>");

        int previous = 0;
        pw.println("<tr>");
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            pw.println("<th>" + previous + "-" + e.getKey() + "</th>");
            previous = e.getKey();
        }
        if (overflowKey != 0) {
            pw.println("<th>" + previous + "-" + overflowKey + "</th>");
        }
        pw.println("</tr>");
        pw.println("<tr>");
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            pw.println("<td>" + e.getValue() + "</td>");
            previous = e.getKey();
        }
        if (overflowKey != 0) {
            pw.println("<td>" + overflowValue + "</td>");
        }
        pw.println("</tr>");

        pw.println("</table>");
        pw.println("</div>");

        return this;
    }

    public Printer countSet(SetCollector collector, int maxLength) {
        Map<String, Integer> m = new HashMap<String, Integer>();

        for (Main.Response e : entries) {
            for (String t : collector.set(e)) {
                if (!m.containsKey(t)) {
                    m.put(t, 0);
                }
                m.put(t, m.get(t) + 1);
            }
        }

        List<Stat> stats = new LinkedList<Stat>();
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            if (!e.getKey().equals("")) {
                stats.add(new Stat(e.getKey(), e.getValue()));
            }
        }

        Collections.sort(stats);

        pw.println("<div class=\"maxheight\">");
        pw.println("<table>");
        for (Stat stat : stats) {
            pw.println("<tr><td>" + maxLength(stat.key, maxLength) + "</td><td>" + stat.value + "</td></tr>");
        }
        pw.println("</table>");
        pw.println("</div>");

        return this;
    }

    public Printer listFiltered(Filter filter, StringCollector... collector) {
        pw.println("<div class=\"maxheight\">");
        pw.println("<table>");
        for (Main.Response r : entries) {
            if (filter == null || filter.include(r)) {
                String[] s = new String[collector.length];
                boolean add = false;
                for (int i = 0; i < collector.length; i++) {
                    s[i] = collector[i].getString(r);
                    if (s[i] != null && !s[i].toLowerCase().equals("no")) {
                        add = true;
                    }
                }
                if (add) {
                    pw.println("<tr>");
                    for (String l : s) {
                        pw.println("<td>" + l + "</td>");
                    }
                    pw.println("</tr>");
                }
            }
        }
        pw.println("</table>");
        pw.println("</div>");
        return this;
    }

    public Printer list(StringCollector... collector) {
        listFiltered(null, collector);
        return this;
    }

    private String maxLength(String s, int l) {
        return s.length() > l ? s.substring(0, l) : s;
    }

    public interface Filter {
        public boolean include(Main.Response r);
    }

    private static class Stat implements Comparable<Stat> {
        private String key;
        private Integer value;

        public Stat(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public int compareTo(Stat stat) {
            return stat.value.compareTo(value);
        }
    }

    public interface SetCollector {
        Set<String> set(Main.Response e);
    }

    public interface NumCollector {
        Integer getInt(Main.Response e);
    }

    public interface BooleanCollector {
        Boolean getBoolean(Main.Response e);
    }

    public interface StringCollector {
        String getString(Main.Response e);
    }

    public void close() {
        pw.close();
    }

}
