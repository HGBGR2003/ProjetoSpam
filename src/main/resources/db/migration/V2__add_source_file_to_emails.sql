ALTER TABLE emails
    ADD COLUMN source_file VARCHAR(500);

CREATE UNIQUE INDEX uq_emails_source_file ON emails (source_file)
    WHERE source_file IS NOT NULL;
