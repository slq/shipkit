package org.shipkit.internal.notes.improvements;

import org.json.simple.DeserializationException;
import org.json.simple.JsonObject;
import org.shipkit.internal.gradle.util.StringUtil;
import org.shipkit.internal.notes.model.Improvement;
import org.shipkit.internal.notes.util.GitHubListFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

class GitHubTicketFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubTicketFetcher.class);

    Collection<Improvement> fetchTickets(String apiUrl, String repository, String readOnlyAuthToken, Collection<String> ticketIds, Collection<String> labels,
                                         boolean onlyPullRequests) {
        List<Improvement> out = new LinkedList<Improvement>();
        if (ticketIds.isEmpty()) {
            return out;
        }
        LOG.info("Querying GitHub API for {} tickets", ticketIds.size());

        Queue<Long> tickets = queuedTicketNumbers(ticketIds);

        try {
            GitHubIssues issues = GitHubIssues.forRepo(apiUrl, repository, readOnlyAuthToken)
                    .state("closed")
                    .labels(StringUtil.join(labels, ","))
                    .filter("all")
                    .direction("desc")
                    .browse();

            while (!tickets.isEmpty() && issues.hasNextPage()) {
                List<JsonObject> page = issues.nextPage();

                out.addAll(extractImprovements(
                        dropTicketsAboveMaxInPage(tickets, page),
                        page, onlyPullRequests));
            }
        } catch (Exception e) {
            throw new RuntimeException("Problems fetching " + ticketIds.size() + " tickets from GitHub", e);
        }
        return out;
    }

    private Queue<Long> dropTicketsAboveMaxInPage(Queue<Long> tickets, List<JsonObject> page) {
        if (page.isEmpty()) {
            return tickets;
        }
        BigDecimal highestId = (BigDecimal) page.get(0).get("number");
        while (!tickets.isEmpty() && tickets.peek() > highestId.longValue()) {
            tickets.poll();
        }
        return tickets;
    }

    private Queue<Long> queuedTicketNumbers(Collection<String> ticketIds) {
        List<Long> tickets = new ArrayList<Long>();
        for (String id : ticketIds) {
            tickets.add(Long.parseLong(id));
        }
        Collections.sort(tickets);
        PriorityQueue<Long> longs = new PriorityQueue<Long>(tickets.size(), Collections.reverseOrder());
        longs.addAll(tickets);
        return longs;
    }

    private static List<Improvement> extractImprovements(Collection<Long> tickets, List<JsonObject> issues,
                                                         boolean onlyPullRequests) {
        if(tickets.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<Improvement> pagedImprovements = new ArrayList<Improvement>();
        for (JsonObject issue : issues) {
            Improvement i = GitHubImprovementsJSON.toImprovement(issue);
            if (tickets.remove(i.getId())) {
                if (!onlyPullRequests || i.isPullRequest()) {
                    pagedImprovements.add(i);
                }

                if (tickets.isEmpty()) {
                    return pagedImprovements;
                }
            }
        }
        return pagedImprovements;
    }

    private static class GitHubIssues {

        private final GitHubListFetcher fetcher;

        private GitHubIssues(String nextPageUrl) {
            fetcher = new GitHubListFetcher(nextPageUrl);
        }

        boolean hasNextPage() {
            return fetcher.hasNextPage();
        }

        List<JsonObject> nextPage() throws IOException, DeserializationException {
            return fetcher.nextPage();
        }

        static GitHubIssuesBuilder forRepo(String apiUrl, String repository, String readOnlyAuthToken) {
            return new GitHubIssuesBuilder(apiUrl, repository, readOnlyAuthToken);
        }

        private static class GitHubIssuesBuilder {
            private final String apiUrl;
            private final String readOnlyAuthToken;
            private final String repository;
            private String state;
            private String filter;
            private String direction;
            private String labels;

            GitHubIssuesBuilder(String apiUrl, String repository, String readOnlyAuthToken) {
                this.apiUrl = apiUrl;
                this.repository = repository;
                this.readOnlyAuthToken = readOnlyAuthToken;
            }

            GitHubIssuesBuilder state(String state) {
                this.state = state;
                return this;
            }

            GitHubIssuesBuilder filter(String filter) {
                this.filter = filter;
                return this;
            }

            GitHubIssuesBuilder direction(String direction) {
                this.direction = direction;
                return this;
            }

            /**
             * Only list issues with given labels, comma separated list.
             * Empty string is ok and means that we are interested in all issues, regardless of the label.
             */
            public GitHubIssuesBuilder labels(String labels) {
                this.labels = labels;
                return this;
            }

            GitHubIssues browse() {
                // see API doc: https://developer.github.com/v3/issues/
                String nextPageUrl = String.format("%s%s%s%s%s%s%s",
                        apiUrl + "/repos/" + repository + "/issues",
                        "?access_token=" + readOnlyAuthToken,
                        state == null ? "" : "&state=" + state,
                        filter == null ? "" : "&filter=" + filter,
                        "&labels=" + labels,
                        direction == null ? "" : "&direction=" + direction,
                        "&page=1"
                );
                return new GitHubIssues(nextPageUrl);
            }
        }
    }
}
