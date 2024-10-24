package com.ecews.bipit.repository;

import com.ecews.bipit.data.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



@Repository
public interface DocumentStatusRepository extends JpaRepository<DocumentStatus, Long> {
    Optional<DocumentStatus> findByDocumentName(String documentName);
}
