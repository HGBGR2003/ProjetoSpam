package spam.redes.bayseianas.emails.Emails;

import org.springframework.stereotype.Service;
 
import java.util.regex.Pattern;
 
@Service
public class EmailCleanerService {
 
    // ── Headers SMTP ─────────────────────────────────────────────────────────
 
    /** Linhas individuais de header SMTP */
    private static final Pattern SMTP_HEADER_LINE = Pattern.compile(
            "^(Return-Path|Delivered-To|Received|Message-Id|Message-ID|" +
            "X-UIDL|X-PMFLAGS|MIME-Version|Content-Type|Content-Length|" +
            "Content-Transfer-Encoding|X-Priority|X-MSMail-Priority|" +
            "X-Mailer|Lines|Reply-To|Bcc|Date|From|To|Subject):\\s.*$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    );
 
    // ── HTML ─────────────────────────────────────────────────────────────────
 
    private static final Pattern HTML_COMMENTS  = Pattern.compile("<!--.*?-->",   Pattern.DOTALL);
    private static final Pattern DOCTYPE        = Pattern.compile("<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAGS      = Pattern.compile("<[^>]+>",      Pattern.DOTALL);
    private static final Pattern HTML_ENTITIES  = Pattern.compile("&[a-zA-Z0-9#]+;");
 
    // ── URLs ─────────────────────────────────────────────────────────────────
 
    private static final Pattern URLS = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            Pattern.CASE_INSENSITIVE
    );
 
    // ── Artefatos do corpus SpamAssassin ─────────────────────────────────────
 
    private static final Pattern CORPUS_ARTIFACTS = Pattern.compile(
            "^(Lead Generator.*|end\\s*)$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    );
 
    // ── Separadores visuais ───────────────────────────────────────────────────
 
    private static final Pattern VISUAL_SEPARATORS = Pattern.compile(
            "^[\\*\\-=_/\\s]{3,}$",
            Pattern.MULTILINE
    );
 
    // ── Caracteres indesejados ────────────────────────────────────────────────
 
    private static final Pattern NON_ASCII     = Pattern.compile("[^\\x00-\\x7F]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
 
    // ── Espaçamento ──────────────────────────────────────────────────────────
 
    private static final Pattern MULTIPLE_SPACES   = Pattern.compile("[ \\t]{2,}");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");
 
    // ────────────────────────────────────────────────────────────────────────
    //  API pública
    // ────────────────────────────────────────────────────────────────────────
 
    /**
     * Recebe o texto bruto de um arquivo de email e devolve o corpo limpo,
     * pronto para tokenização e treinamento Naive Bayes.
     *
     * @param rawEmail conteúdo completo do arquivo .txt
     * @return texto limpo e normalizado
     */
    public String clean(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return "";
        }
 
        String text = rawEmail;
 
        // 1. Separar cabeçalho do corpo pelo primeiro "\n\n" (RFC 2822)
        text = stripSmtpHeaderBlock(text);
 
        // 2. Remover linhas de header que possam ter sobrado no corpo
        text = SMTP_HEADER_LINE.matcher(text).replaceAll("");
 
        // 3. HTML
        text = HTML_COMMENTS.matcher(text).replaceAll(" ");
        text = DOCTYPE.matcher(text).replaceAll(" ");
        text = HTML_TAGS.matcher(text).replaceAll(" ");
        text = HTML_ENTITIES.matcher(text).replaceAll(" ");
 
        // 4. URLs
        text = URLS.matcher(text).replaceAll(" ");
 
        // 5. Artefatos do corpus
        text = CORPUS_ARTIFACTS.matcher(text).replaceAll("");
 
        // 6. Separadores visuais
        text = VISUAL_SEPARATORS.matcher(text).replaceAll("");
 
        // 7. Caracteres de controle e não-ASCII
        text = CONTROL_CHARS.matcher(text).replaceAll(" ");
        text = NON_ASCII.matcher(text).replaceAll(" ");
 
        // 8. Lowercase
        text = text.toLowerCase();
 
        // 9. Normalizar espaços
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        text = MULTIPLE_NEWLINES.matcher(text).replaceAll("\n\n");
 
        return text.strip();
    }
 
    // ────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────────
 
    /**
     * Localiza o primeiro bloco de linha em branco (separador RFC 2822)
     * e descarta tudo o que vem antes, incluindo o separador.
     */
    private String stripSmtpHeaderBlock(String text) {
        int unixSep    = text.indexOf("\n\n");
        int windowsSep = text.indexOf("\r\n\r\n");
 
        int sep    = -1;
        int offset = 2;
 
        if (unixSep >= 0 && windowsSep >= 0) {
            if (unixSep <= windowsSep) { sep = unixSep; }
            else                       { sep = windowsSep; offset = 4; }
        } else if (unixSep >= 0)   { sep = unixSep; }
        else if (windowsSep >= 0)  { sep = windowsSep; offset = 4; }
 
        return (sep >= 0) ? text.substring(sep + offset) : text;
    }
}
