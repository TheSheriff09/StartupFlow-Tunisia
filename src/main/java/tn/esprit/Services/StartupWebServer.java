package tn.esprit.Services;

import com.sun.net.httpserver.HttpServer;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * StartupWebServer — A lightweight built-in HTTP server that serves
 * a beautiful HTML page for a startup so that scanning the QR code
 * with a phone/tablet opens the browser and shows all startup details.
 *
 * Usage:
 *   StartupWebServer srv = new StartupWebServer(startup, plans, investmentScore);
 *   srv.start();                        // starts on a free port
 *   String url = srv.getUrl(startup);   // http://192.168.x.x:PORT/startup/42
 *   // ... show QR with that url ...
 *   srv.stop();                         // call when dialog closes
 */
public class StartupWebServer {

    private HttpServer           server;
    private final Startup        startup;
    private final List<BusinessPlan> plans;
    private final double         investmentScore;
    private int                  port;

    public StartupWebServer(Startup startup, List<BusinessPlan> plans, double investmentScore) {
        this.startup         = startup;
        this.plans           = plans;
        this.investmentScore = investmentScore;
    }

    // ── Lifecycle ─────────────────────────────────────────────

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0); // port=0 → OS picks free port
        port   = server.getAddress().getPort();

        server.createContext("/startup/" + startup.getStartupID(), exchange -> {
            byte[] html = buildHtml().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
        });

        // Redirect root and /startup to the correct path
        server.createContext("/", exchange -> {
            String redirect = "/startup/" + startup.getStartupID();
            exchange.getResponseHeaders().set("Location", redirect);
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "startup-web-server");
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public int getPort() { return port; }

    /**
     * Returns the full URL to embed in the QR code.
     * Uses the machine's LAN IP so phones on the same network can reach it.
     */
    public String getUrl() {
        return "http://" + getLocalIp() + ":" + port + "/startup/" + startup.getStartupID();
    }

    // ── HTML builder ──────────────────────────────────────────

    private String buildHtml() {
        String band     = InvestmentScorer.band(investmentScore);
        String hexColor = InvestmentScorer.hexColor(investmentScore);
        String reco     = investmentScore >= 70
                ? "✅ Strong Investment Candidate"
                : investmentScore >= 40
                ? "⚠️ Moderate Risk – additional due diligence recommended"
                : "🔴 High Risk – significant concerns identified";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>").append(esc(startup.getName())).append(" — StartupFlow</title>")
          .append("<style>")
          .append("*{box-sizing:border-box;margin:0;padding:0}")
          .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;")
          .append("background:linear-gradient(135deg,#1e0a3c 0%,#3b1f6b 100%);min-height:100vh;padding:24px 16px;}")
          .append(".card{background:rgba(255,255,255,0.07);border-radius:20px;padding:24px;")
          .append("border:1px solid rgba(167,139,250,0.30);max-width:600px;margin:0 auto 20px;}")
          .append("h1{color:#fff;font-size:1.6rem;margin-bottom:6px}")
          .append("h2{color:#a78bfa;font-size:1rem;margin:18px 0 10px;text-transform:uppercase;letter-spacing:.06em}")
          .append(".badge{display:inline-block;padding:4px 14px;border-radius:20px;font-size:.8rem;font-weight:700;}")
          .append(".score-ring{display:flex;align-items:center;gap:14px;margin:10px 0}")
          .append(".score-val{font-size:2.4rem;font-weight:800;}")
          .append(".grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:12px}")
          .append(".field{background:rgba(237,233,254,0.08);border-radius:12px;padding:12px}")
          .append(".field-label{font-size:.72rem;font-weight:700;color:#a78bfa;text-transform:uppercase;letter-spacing:.05em;margin-bottom:4px}")
          .append(".field-value{font-size:.92rem;color:#e9d5ff;font-weight:600;word-break:break-word}")
          .append(".plan{background:rgba(255,255,255,0.06);border-radius:12px;padding:14px;margin-bottom:10px;border-left:3px solid #7c3aed}")
          .append(".plan-title{color:#fff;font-weight:700;font-size:.95rem;margin-bottom:6px}")
          .append(".plan-meta{color:#c4b5fd;font-size:.80rem}")
          .append(".reco{margin-top:8px;color:#e9d5ff;font-size:.88rem;line-height:1.5}")
          .append("footer{text-align:center;color:rgba(255,255,255,0.25);font-size:.72rem;margin-top:24px}")
          .append("</style></head><body>");

        // ── Header card ──────────────────────────────────────
        sb.append("<div class='card'>")
          .append("<h1>").append(esc(startup.getName())).append("</h1>");

        if (startup.getSector() != null)
            sb.append("<span class='badge' style='background:rgba(124,58,237,0.35);color:#ddd6fe;margin-right:6px'>")
              .append(esc(startup.getSector())).append("</span>");
        if (startup.getStage() != null)
            sb.append("<span class='badge' style='background:rgba(109,40,217,0.25);color:#c4b5fd;margin-right:6px'>")
              .append(esc(startup.getStage())).append("</span>");
        if (startup.getStatus() != null)
            sb.append("<span class='badge' style='background:rgba(55,48,163,0.4);color:#a5b4fc;'>")
              .append(esc(startup.getStatus())).append("</span>");

        sb.append("</div>"); // end header card

        // ── Investment Score card ─────────────────────────────
        sb.append("<div class='card'>")
          .append("<h2>Investment Score</h2>")
          .append("<div class='score-ring'>")
          .append("<span class='score-val' style='color:").append(hexColor).append("'>")
          .append(String.format("%.0f", investmentScore)).append("</span>")
          .append("<div>")
          .append("<div style='color:#94a3b8;font-size:.8rem'>out of 100</div>")
          .append("<div style='font-weight:700;color:").append(hexColor).append(";font-size:.9rem'>")
          .append(esc(band)).append("</div>")
          .append("</div></div>")
          .append("<div class='reco'>").append(esc(reco)).append("</div>")
          .append("</div>");

        // ── Details grid card ─────────────────────────────────
        sb.append("<div class='card'><h2>Details</h2><div class='grid'>");
        addField(sb, "Funding",    startup.getFundingAmount() != null
                ? String.format("$%,.2f", startup.getFundingAmount()) : "N/A");
        addField(sb, "KPI Score",  startup.getKpiScore() != null
                ? String.format("%.1f / 10", startup.getKpiScore()) : "N/A");
        addField(sb, "Created",    startup.getCreationDate() != null
                ? startup.getCreationDate().toString() : "N/A");
        addField(sb, "Incubator",  startup.getIncubatorProgram());
        addField(sb, "Last Eval",  startup.getLastEvaluationDate() != null
                ? startup.getLastEvaluationDate().toString() : "N/A");
        sb.append("</div>");
        if (startup.getDescription() != null && !startup.getDescription().isBlank()) {
            sb.append("<div style='margin-top:14px'>")
              .append("<div class='field-label' style='color:#a78bfa;margin-bottom:6px'>Description</div>")
              .append("<div style='color:#ddd6fe;font-size:.88rem;line-height:1.6'>")
              .append(esc(startup.getDescription())).append("</div></div>");
        }
        sb.append("</div>");

        // ── Business Plans card ───────────────────────────────
        sb.append("<div class='card'><h2>Business Plans</h2>");
        if (plans == null || plans.isEmpty()) {
            sb.append("<p style='color:#94a3b8;font-size:.85rem'>No business plans attached.</p>");
        } else {
            for (BusinessPlan bp : plans) {
                double ps  = InvestmentScorer.scorePlan(bp);
                String phx = InvestmentScorer.hexColor(ps);
                sb.append("<div class='plan'>")
                  .append("<div class='plan-title'>📋 ").append(esc(bp.getTitle() != null ? bp.getTitle() : "Untitled")).append("</div>")
                  .append("<div class='plan-meta'>");
                if (bp.getStatus()          != null) sb.append("Status: ").append(esc(bp.getStatus())).append(" &nbsp;·&nbsp; ");
                if (bp.getFundingRequired() != null) sb.append("Funding: $").append(String.format("%,.0f", bp.getFundingRequired())).append(" &nbsp;·&nbsp; ");
                if (bp.getTimeline()        != null) sb.append("Timeline: ").append(esc(bp.getTimeline()));
                sb.append("</div>")
                  .append("<div style='margin-top:6px;font-size:.78rem;font-weight:700;color:").append(phx).append("'>")
                  .append(String.format("Plan Score: %.0f / 100", ps)).append("</div>")
                  .append("</div>");
            }
        }
        sb.append("</div>");

        sb.append("<footer>Powered by StartupFlow &nbsp;·&nbsp; Scanned via QR</footer>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void addField(StringBuilder sb, String label, String value) {
        String v = (value != null && !value.isBlank()) ? value : "N/A";
        sb.append("<div class='field'>")
          .append("<div class='field-label'>").append(esc(label)).append("</div>")
          .append("<div class='field-value'>").append(esc(v)).append("</div>")
          .append("</div>");
    }

    private static String esc(String s) {
        if (s == null) return "N/A";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    // ── Network helper ────────────────────────────────────────

    /**
     * Returns the best local LAN IP address (non-loopback, non-link-local IPv4).
     * Falls back to loopback if none found.
     */
    public static String getLocalIp() {
        try {
            // Try connecting to a public address to discover the preferred outgoing interface
            try (DatagramSocket s = new DatagramSocket()) {
                s.connect(InetAddress.getByName("8.8.8.8"), 53);
                String ip = s.getLocalAddress().getHostAddress();
                if (ip != null && !ip.equals("0.0.0.0")) return ip;
            }
            // Fallback: enumerate interfaces
            var interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                var addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()
                            && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}

