package com.yowyob.feedback.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Projection interface for feedback with member and project details.
 * Used for retrieving enriched feedback data.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-02-02
 * @version 1.0
 */
public interface FeedbackWithDetails {
    UUID getFeedbackId();
    OffsetDateTime getFeedbackDateTime();
    String getContent();
    String[] getAttachments();
    UUID getTargetProjectId();
    UUID getMemberId();
    Integer getNumberOfLikes();
    Integer getNumberOfComments();
    String getMemberPseudo();
    String getProjectName();
}
