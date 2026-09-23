package com.dmfs.company.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceHostService {

    private static final String PLATFORM_DOMAIN =
            "dronemining.jmsolutions.co.tz";

    public String resolveWorkspaceSlug(
            HttpServletRequest request
    ) {

        String host = request.getServerName();

        if (host == null || host.isBlank()) {
            return null;
        }

        host = host.trim().toLowerCase();

        /*
         * LOCAL PLATFORM HOSTS
         */
        if ("localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)) {

            return null;
        }

        /*
         * LOCAL COMPANY WORKSPACE
         *
         * Example:
         * tukupala.localhost
         *        ↓
         * tukupala
         */
        if (host.endsWith(".localhost")) {

            String workspace =
                    host.substring(
                            0,
                            host.length() - ".localhost".length()
                    );

            if (workspace.isBlank()) {
                return null;
            }

            return workspace;
        }

        /*
         * PRODUCTION PLATFORM HOST
         *
         * dronemining.jmsolutions.co.tz
         *        ↓
         * no workspace
         */
        if (PLATFORM_DOMAIN.equals(host)) {
            return null;
        }

        /*
         * PRODUCTION COMPANY WORKSPACE
         *
         * radai.dronemining.jmsolutions.co.tz
         *        ↓
         * radai
         */
        String workspaceSuffix =
                "." + PLATFORM_DOMAIN;

        if (host.endsWith(workspaceSuffix)) {

            String workspace =
                    host.substring(
                            0,
                            host.length() - workspaceSuffix.length()
                    );

            if (workspace.isBlank()) {
                return null;
            }

            if ("www".equals(workspace)
                    || "api".equals(workspace)) {

                return null;
            }

            return workspace;
        }

        /*
         * Unknown host
         */
        return null;
    }
} 