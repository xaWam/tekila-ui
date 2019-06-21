package com.jaravir.tekila.ui.servlet.export;

import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.exception.NoPrivilegeFoundException;
import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;
import com.jaravir.tekila.module.report.export.pdf.PDFExporter;
import com.jaravir.tekila.module.report.export.xlsx.ExcelExporter;
import com.jaravir.tekila.module.subscription.persistence.entity.Addendum;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sajabrayilov on 27.01.2015.
 */
public class DataExportServlet extends HttpServlet {
    @EJB private ExcelExporter excelExporter;
    @EJB private PDFExporter pdfExporter;
    @EJB private SecurityManager securityManager;
    @EJB private SubscriptionPersistenceFacade subscriptionFacade;

    private final static Map<String, String> formatMap = new HashMap<>();

    private final static Logger log = Logger.getLogger(DataExportServlet.class);

    static {
        formatMap.put("pdf", "application/pdf");
        formatMap.put("xlsx", "application/vnd.ms-excel");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Pathinfo: " + req.getPathInfo());
        log.debug("Parameters" + req.getParameterMap());

        String report = req.getPathInfo();
        String fileName = req.getParameter("file");

        if (report.equals("/payments")) {
            generatePaymentsReport(req, resp, fileName);
        }

        else if (report.equals("/contract")) {
            downloadContract(req, resp, fileName);
        }

        else if (report.equals("/addendum")) {
            downloadAddendum(req, resp, fileName);
        }

    }

    private void generatePaymentsReport (HttpServletRequest req, HttpServletResponse resp, String fileName) throws ServletException, IOException  {
        if (req.getUserPrincipal() == null) {
            req.getRequestDispatcher("/login.xhtml").forward(req, resp);
            return;
        }

        if (!securityManager.checkUIPermissions("PaymentReport", Privilege.READ)
                && !securityManager.checkUIPermissions("OwnPaymentReport", Privilege.READ)
                ) {
            log.error("Not enough privileges to generate report");
            String referer = req.getHeader("Referer");

            if (referer != null)
                resp.sendRedirect(referer);

            return;
        }

        String format = req.getParameter("format");

        if (!formatMap.containsKey(format))
            throw new IllegalArgumentException("Format not found");

        if (fileName == null || fileName.isEmpty())
            fileName = "Payments_Report";

        fileName = fileName + '.' + format;

        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType(formatMap.get(format) + "; charset=utf-8");
        resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        resp.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        resp.setHeader("Expires", "0");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date fromDate = null;
        Date toDate = null;
        try {
            fromDate = dateFormat.parse(req.getParameter("from"));
            toDate = dateFormat.parse(req.getParameter("to"));
        } catch (ParseException e) {
            log.error("Could not parse date parameters", e);
            return;
        }
        String providerId = req.getParameter("providerid");
        String externalUserName = req.getParameter("extusername");
        try {
            if (format.equals("xlsx"))
                excelExporter.generatePaymentReport(out, fromDate, toDate, providerId, externalUserName);
            else if (format.equals("pdf"))
                pdfExporter.generatePaymentReport(out, fromDate, toDate);
        }
        catch (NoPrivilegeFoundException ex) {
            log.error(ex);
        }
        out.flush();
        out.close();
    }

    private void downloadContract (HttpServletRequest req, HttpServletResponse resp, String fileName) throws ServletException, IOException  {
        if (req.getUserPrincipal() == null) {
            req.getRequestDispatcher("/login.xhtml").forward(req, resp);
            return;
        }
        String referer = req.getHeader("Referer");

        if (req.getParameter("sbn") == null)
            resp.sendRedirect(referer);

        if (!securityManager.checkUIPermissions("Subscription", Privilege.READ)
                ) {
            log.error("Not enough privileges to view document");

            if (referer != null)
                resp.sendRedirect(referer);

            return;
        }

        String format = req.getParameter("format");

        if (!formatMap.containsKey(format))
            throw new IllegalArgumentException("Format not found");

        if (fileName == null || fileName.isEmpty())
            fileName = "Payments_Report";

        fileName = fileName + '.' + format;

        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType(formatMap.get(format) + "; charset=utf-8");
        resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        resp.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        resp.setHeader("Expires", "0");

        try {
          Subscription subscription = subscriptionFacade.find(Long.valueOf(req.getParameter("sbn")));
          if (subscription == null)
              resp.sendRedirect(referer);

          out.write(subscription.getContract().getDocument());
        }
        catch (Exception ex) {
            log.error(ex);
        }
        out.flush();
        out.close();
    }

    private void downloadAddendum (HttpServletRequest req, HttpServletResponse resp, String fileName) throws ServletException, IOException  {
        if (req.getUserPrincipal() == null) {
            req.getRequestDispatcher("/login.xhtml").forward(req, resp);
            return;
        }
        String referer = req.getHeader("Referer");

        if (req.getParameter("sbn") == null || req.getParameter("addendum") == null)
            resp.sendRedirect(referer);

        if (!securityManager.checkUIPermissions("Subscription", Privilege.READ)
                ) {
            log.error("Not enough privileges to view document");

            if (referer != null)
                resp.sendRedirect(referer);

            return;
        }

        String format = req.getParameter("format");

        if (!formatMap.containsKey(format))
            throw new IllegalArgumentException("Format not found");

        if (fileName == null || fileName.isEmpty())
            fileName = "Payments_Report";

        fileName = fileName + '.' + format;

        try {
            Addendum addendum = subscriptionFacade.findAddendum(Long.valueOf(req.getParameter("addendum")));

            if (addendum == null)
                resp.sendRedirect(referer);

            ServletOutputStream out = resp.getOutputStream();
            resp.setContentType(formatMap.get(format) + "; charset=utf-8");
            resp.setHeader("Content-Disposition", "attachment; filename=" + addendum.getName());
            resp.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            resp.setHeader("Expires", "0");

            out.write(addendum.getDocument());

            out.flush();
            out.close();
        }
        catch (Exception ex) {
            log.error(ex);
        }

    }
}
