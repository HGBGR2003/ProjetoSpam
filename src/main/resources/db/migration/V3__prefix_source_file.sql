UPDATE emails
SET source_file = 'spam/' || source_file
WHERE is_spam = true
  AND source_file IS NOT NULL
  AND source_file NOT LIKE 'spam/%';
