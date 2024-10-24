package com.ecews.bipit.data;

import jakarta.persistence.*;


@Entity
public class DocumentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String documentName;

    @Column(nullable = false)
    private boolean isPopulated;

    public DocumentStatus() {
    }

    public DocumentStatus(String documentName, boolean isPopulated) {
        this.documentName = documentName;
        this.isPopulated = isPopulated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public boolean isPopulated() {
        return isPopulated;
    }

    public void setPopulated(boolean populated) {
        isPopulated = populated;
    }


}

