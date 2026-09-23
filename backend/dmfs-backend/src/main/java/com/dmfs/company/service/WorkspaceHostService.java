package com.dmfs.company.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceHostService {

    public String resolveWorkspaceSlug(
            HttpServletRequest request
    ) {

        String host =
                request.getServerName();

        if (host == null || host.isBlank()) {
            return null;
        }

        host =
                host.trim()
                        .toLowerCase();

        /*
         * PLATFORM HOSTS
         */

        if ("localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "www".equals(host)) {

            return null;
        }

        /*
         * LOCAL COMPANY WORKSPACE
         *
         * tukupala.localhost
         *        ↓
         * tukupala
         */

        if (host.endsWith(".localhost")) {

            String workspace =
                    host.substring(
                            0,
                            host.length()
                                    - ".localhost".length()
                    );

            if (workspace.isBlank()) {
                return null;
            }

            return workspace;
        }

        /*
         * PRODUCTION COMPANY WORKSPACE
         *
         * tukupala.dmfs.com
         *        ↓
         * tukupala
         *
         * The actual production domain is not hardcoded.
         */

        String[] parts =
                host.split("\\.");

        if (parts.length < 3) {
            return null;
        }

        String workspace =
                parts[0]
                        .trim()
                        .toLowerCase();

        if (workspace.isBlank()
                || "www".equals(workspace)
                || "api".equals(workspace)) {

            return null;
        }

        return workspace;
    }
}
