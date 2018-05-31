import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main {

    private final List<Response> responses;
    private final Printer m;

    public static void main(String[] args) throws IOException {
        new Main();
    }

    public Main() throws IOException {
        responses = parseResponses(new FileInputStream(new File("Keycloak Questionnaire.csv")));

        m = new Printer(responses, new FileOutputStream("report.html"));

        m.head();

        m.line("Total responses: " + responses.size());

        m.h1("Securing Apps");
        m.countSet(e -> e.howToSecureApps, 50);

        m.h2("Do you want a proxy?");
        m.countYesNo(e -> e.lightweightProxy);


        m.h1("Features");
        m.countSet(e -> e.features, 50);

        m.h2("Missing features");
        m.list(e -> e.missingFeatures);

        m.h1("Customization");

        m.h2("Themes");
        m.countSet(e -> e.themes, 10);

        m.h2("SPIs");
        m.countSet(e -> e.spis, 50);

        m.h2("Significant changes");
        m.list(e -> e.significantChanges);

        m.h2("Customizations");
        m.list(e -> e.customizations);


        m.h1("Deployment size");

        m.h2("Realms");
        m.ranges(e -> e.numRealms, 1, 2, 5, 10, 50, 100);

        m.h2("Users");
        m.ranges(e -> e.numUsers, 1000, 10000, 100000, 1000000);

        m.h2("Active Users");
        m.ranges(e -> e.numActiveUsers, 1000, 10000, 100000, 1000000);

        m.h2("Logins per hour");
        m.ranges(e -> e.numLoginsPerHour, 1000, 10000, 100000, 1000000);

        m.h2("Clients");
        m.ranges(e -> e.numClients, 10, 100, 1000);


        m.h1("Project");

        m.h2("Project size");
        m.countString(e -> e.howDoYouUse);

        m.h2("Describe how you use");
        m.list(e -> e.desribeHowYouUse);


        m.h1("Deployment");

        m.h2("Deployment method");
        m.countString(e -> e.howDeploy);

        m.h2("DB");
        m.countString(e -> e.db);

        m.h2("Hardware");
        m.list(e -> e.hardware);

        m.h2("Clustering");
        m.countString(e -> e.clustering);


        m.h1("Comments");

        m.h2("Experience");
        m.list(e -> e.desribeYourExperience);

        m.h2("Additional comments");
        m.list(e -> e.additionalComments);

        m.h2("Last comments");
        m.list(e -> e.lastComments);


        m.h1("Users");
        m.h2("Public references");
        m.listFiltered(e->e.publicReference && e.companyName != null, e-> e.companyName, e->e.username, e->e.contactName);

        m.h2("Others references");
        m.listFiltered(e->!e.publicReference || e.companyName == null, e-> e.companyName, e->e.username, e->e.contactName);

        m.foot();

        m.close();
    }

    private List<Response> parseResponses(InputStream is) throws IOException {
        String s = IOUtils.toString(is);
        String[] split = s.split("\"\n\"");

        List<Response> responses = new LinkedList<>();

        for (int i = 1; i < split.length; i++) {
            String l = split[i];
            l = "\"" + l + "\"";
            responses.add(new Response(l));
        }

        return responses;
    }

    public static class Response {
        private String timestamp;
        private String username;
        private String contactName;
        private String companyName;
        private String howDoYouUse;
        private String desribeHowYouUse;
        private String desribeYourExperience;
        private Boolean publicReference;
        private Set<String> features;
        private Set<String> howToSecureApps;
        private Boolean lightweightProxy;
        private Set<String> themes;
        private Set<String> spis;
        private String significantChanges;
        private String customizations;
        private Integer numUsers;
        private Integer numActiveUsers;
        private Integer numLoginsPerHour;
        private Integer numClients;
        private Integer numRealms;
        private String clustering;
        private String db;
        private String hardware;
        private String howDeploy;
        private String additionalComments;
        private String missingFeatures;
        private String lastComments;

        public Response(String l) {
            l = l.substring(1, l.length() - 1);
            String[] e = l.split("\",\"");

            try {
                int i = 0;
                timestamp = asString(e[i++]);
                username = asString(e[i++]);
                contactName = asString(e[i++]);
                companyName = asString(e[i++]);
                howDoYouUse = asString(e[i++]);
                desribeHowYouUse = asString(e[i++]);
                desribeYourExperience = asString(e[i++]);
                publicReference = asBoolean(e[i++]);
                features = asSet(e[i++]);
                howToSecureApps = asSet(e[i++]);
                lightweightProxy = asBoolean(e[i++]);
                themes = asSet(e[i++]);
                spis = asSet(e[i++]);
                significantChanges = asString(e[i++]);
                customizations = asString(e[i++]);
                numUsers = asInt(e[i++]);
                numActiveUsers = asInt(e[i++]);
                numLoginsPerHour = asInt(e[i++]);
                numClients = asInt(e[i++]);
                numRealms = asInt(e[i++]);
                clustering = asString(e[i++]);
                db = asString(e[i++]);
                hardware = asString(e[i++]);
                howDeploy = asString(e[i++]);
                additionalComments = asString(e[i++]);
                missingFeatures = asString(e[i++]);
                lastComments = asString(e[i++]);
            } catch (ArrayIndexOutOfBoundsException exz) {
            }
        }
    }

    private static final String asString(String s) {
        if (s.equals("")) {
            return null;
        } else {
            return s;
        }
    }

    private static final Integer asInt(String s) {
        if (s.equals("")) {
            return null;
        } else {
            return Integer.parseInt(s);
        }
    }

    private static final Boolean asBoolean(String s) {
        if (s.equals("")) {
            return null;
        }
        return s.equals("Yes") ? true : false;
    }

    private static final Set<String> asSet(String s) {
        return new HashSet<>(Arrays.asList(s.split(";")));
    }

}
