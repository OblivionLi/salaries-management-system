package com.balaur.backend.responses;

import java.util.List;
import java.util.stream.Collectors;

public class LinkUtils {
    public static List<Link> generateLinks(String method, String version, String id) {
        List<Link> links = List.of(
                new Link("get", "/api/" + version + "/salaries", "GET", version),
                new Link("add", "/api/" + version + "/salaries/add", "POST", version),
                new Link("edit", "/api/" + version + "/salaries/edit/{id}", "PATCH", version),
                new Link("delete", "/api/" + version + "/salaries/delete/{id}", "DELETE", version)
        );

        return links.stream()
                .map(link -> {
                    String href = link.getHref().replace("{id}", id);
                    String rel = link.getMethod().equalsIgnoreCase(method) ? "self" : link.getRel();

                    return new Link(rel, href, link.getMethod(), link.getVersion());
                })
                .collect(Collectors.toList());
    }

    public static List<Link> generateErrorLink(String rel, String method, String version, String id) {
        String defaultHref;

        if (rel.equalsIgnoreCase("get")) {
            defaultHref = "/api/" + version + "/salaries";
        } else {
            defaultHref = "/api/" + version + "/salaries/" + rel + "/{id}";
        }

        List<Link> links = List.of(new Link("self", defaultHref, method, version));

        return links.stream()
                .map(link -> {
                    String href = rel.equalsIgnoreCase("get") ? link.getHref() : link.getHref()
                            .replace("{id}", id)
                            .replace("{rel}", rel);

                    String finalRel = method.equalsIgnoreCase("get") ? "self" : rel;

                    return new Link(finalRel, href, link.getMethod(), link.getVersion());
                })
                .collect(Collectors.toList());
    }
}
