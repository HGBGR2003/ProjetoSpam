package spam.redes.bayseianas.emails.Emails;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class EmailCleanerService {

    private static final Pattern SMTP_HEADER_LINE = Pattern.compile(
            "^(Return-Path|Delivered-To|Received|Message-Id|Message-ID|" +
                    "X-UIDL|X-PMFLAGS|MIME-Version|Content-Type|Content-Length|" +
                    "Content-Transfer-Encoding|X-Priority|X-MSMail-Priority|" +
                    "X-Mailer|Lines|Reply-To|Bcc|Date|From|To|Subject):\\s.*$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final Pattern HTML_COMMENTS = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern DOCTYPE = Pattern.compile("<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>", Pattern.DOTALL);
    private static final Pattern HTML_ENTITIES = Pattern.compile("&[a-zA-Z0-9#]+;");

    
    private static final Pattern URLS = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CORPUS_ARTIFACTS = Pattern.compile(
            "^(Lead Generator.*|end\\s*)$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final Pattern VISUAL_SEPARATORS = Pattern.compile(
            "^[\\*\\-=_/\\s]{3,}$",
            Pattern.MULTILINE);

    private static final Pattern NON_ASCII = Pattern.compile("[^\\x00-\\x7F]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \\t]{2,}");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");

    public String clean(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return "";
        }

        String text = rawEmail;

        text = stripSmtpHeaderBlock(text);

        text = SMTP_HEADER_LINE.matcher(text).replaceAll("");

        text = HTML_COMMENTS.matcher(text).replaceAll(" ");
        text = DOCTYPE.matcher(text).replaceAll(" ");
        text = HTML_TAGS.matcher(text).replaceAll(" ");
        text = HTML_ENTITIES.matcher(text).replaceAll(" ");

        text = URLS.matcher(text).replaceAll(" ");
        text = CORPUS_ARTIFACTS.matcher(text).replaceAll("");
        text = VISUAL_SEPARATORS.matcher(text).replaceAll("");

        text = CONTROL_CHARS.matcher(text).replaceAll(" ");
        text = NON_ASCII.matcher(text).replaceAll(" ");

        text = text.toLowerCase();

        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        text = MULTIPLE_NEWLINES.matcher(text).replaceAll("\n\n");

        return text.strip();
    }

    private String stripSmtpHeaderBlock(String text) {
        int unixSep = text.indexOf("\n\n");
        int windowsSep = text.indexOf("\r\n\r\n");

        int sep = -1;
        int offset = 2;

        if (unixSep >= 0 && windowsSep >= 0) {
            if (unixSep <= windowsSep) {
                sep = unixSep;
            } else {
                sep = windowsSep;
                offset = 4;
            }
        } else if (unixSep >= 0) {
            sep = unixSep;
        } else if (windowsSep >= 0) {
            sep = windowsSep;
            offset = 4;
        }

        return (sep >= 0) ? text.substring(sep + offset) : text;
    }
}
