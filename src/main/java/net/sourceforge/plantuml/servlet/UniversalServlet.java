package net.sourceforge.plantuml.servlet;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;

import javax.imageio.IIOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by janusz on 12/16/15.
 */
public class UniversalServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String source = req.getParameter("source");
        String action = req.getParameter("action");
        switch (action) {
            case "image": doSendImage(req, resp, source); break;
            case "encode": doEncode(req, resp, source); break;
            default: super.doPost(req, resp);
        }
    }

    private void doEncode(HttpServletRequest req, HttpServletResponse resp, String source) throws IOException {
        Transcoder transcoder = TranscoderUtil.getDefaultTranscoder();
        try {
            String compressed = transcoder.encode(source);
            String urlEncoded = URLEncoder.encode(compressed, "UTF-8");
            resp.setContentType("text/plain");
            resp.getWriter().append(urlEncoded);
            resp.flushBuffer();
        } catch (IOException e) {
            send400(resp, "Could not encode text");
        }
    }

    private void doSendImage(HttpServletRequest req, HttpServletResponse resp, String source) throws IOException {
        String format = req.getParameter("format");
        FileFormat fileFormat;
        switch(format) {
            case "png": fileFormat = FileFormat.PNG; break;
            case "svg": fileFormat = FileFormat.SVG; break;
            case "atxt": fileFormat = FileFormat.ATXT; break;
            default: send400(resp, "Unknown format: " + format); return;
        }
        DiagramResponse dr = new DiagramResponse(resp, fileFormat);
        try {
            dr.sendDiagram(source);
        } catch (IIOException iioe) {
            // Browser has closed the connection, so the HTTP OutputStream is closed
            // Silently catch the exception to avoid annoying log
        }
    }

    private void send400(HttpServletResponse resp, String message) throws IOException {
        resp.sendError(400, message);
    }

}
