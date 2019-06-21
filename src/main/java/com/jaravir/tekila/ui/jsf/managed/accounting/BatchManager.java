/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed.accounting;

import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.module.accounting.PaymentPurpose;
import com.jaravir.tekila.module.accounting.entity.TransactionType;
import com.jaravir.tekila.module.accounting.processor.DocumentHandler;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSession;

import com.jaravir.tekila.module.subscription.persistence.management.ContractSwitcherFacadeLocal;
import com.jaravir.tekila.tools.WrapperAgreementChangeBatch;
import com.jaravir.tekila.tools.WrapperBatch;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author sajabrayilov
 */
@ManagedBean
@SessionScoped
public class BatchManager implements Serializable {

    private transient static final Logger log = Logger.getLogger(BatchManager.class);

    @EJB
    private DocumentHandler proccessor;

    @EJB
    private ContractSwitcherFacadeLocal contractSwitcherFacadeLocal;

    @EJB
    private UserPersistenceFacade userFacade;
    private String desc;
    private transient UploadedFile file;

    private transient UploadedFile agreementChangesFile;

    private UIComponent batchStatusChangeTable;
    private List<WrapperBatch> wrapperBatchList;
    private Set<WrapperAgreementChangeBatch> wrapperAgreementChangeBatchSet = new HashSet<>();

    public UIComponent getBatchStatusChangeTable() {
        return batchStatusChangeTable;
    }

    public void setBatchStatusChangeTable(UIComponent batchStatusChangeTable) {
        this.batchStatusChangeTable = batchStatusChangeTable;
    }


    public List<WrapperBatch> getWrapperBatchList() {
        return wrapperBatchList;
    }

    public void setWrapperBatchList(List<WrapperBatch> wrapperBatchList) {
        this.wrapperBatchList = wrapperBatchList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void uploadPaymentFile() {
        if (file == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            proccessor.parsePaymentFile(file.getInputstream(), userFacade.find((Long) session.getAttribute("userID")), PaymentPurpose.BALANCE, desc);
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + file.getFileName(),
                    "Cannot proccess file: " + file.getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handlePaymentFileUploadWithDesc(FileUploadEvent event) {
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            if (event.getFile() == null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return;
            }

            try {
                HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
                proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")), PaymentPurpose.BALANCE, desc);
                FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (Exception ex) {
                FacesMessage message = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Cannot proccess file: " + event.getFile().getFileName(),
                        "Cannot proccess file: " + event.getFile().getFileName()
                );

                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    public void handlePaymentFileUpload(FileUploadEvent event) {
        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")), PaymentPurpose.BALANCE, desc);
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void uploadVATPaymentFile() {
        if (file == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            proccessor.parsePaymentFile(file.getInputstream(), userFacade.find((Long) session.getAttribute("userID")), PaymentPurpose.VAT, desc);
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + file.getFileName(),
                    "Cannot proccess file: " + file.getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleVATPaymentFileUpload(FileUploadEvent event) {
        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")), PaymentPurpose.VAT, desc);
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void uploadCreditAdjustmentFile() {
        if (file == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long)session.getAttribute("userID")), PaymentPurpose.VAT);
            proccessor.parseAdjustmentFile(file.getInputstream(), userFacade.find((Long) session.getAttribute("userID")), TransactionType.CREDIT, desc);
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + file.getFileName(),
                    "Cannot proccess file: " + file.getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleCreditAdjustment(FileUploadEvent event) {
        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long)session.getAttribute("userID")), PaymentPurpose.VAT);
            proccessor.parseAdjustmentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")), TransactionType.CREDIT, desc);
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void uploadDebitAdjustmentFile() {
        if (file == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long)session.getAttribute("userID")), PaymentPurpose.VAT);
            proccessor.parseAdjustmentFile(file.getInputstream(), userFacade.find((Long) session.getAttribute("userID")), TransactionType.DEBIT, desc);
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + file.getFileName(),
                    "Cannot proccess file: " + file.getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleDebitAdjustment(FileUploadEvent event) {
        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //proccessor.parsePaymentFile(event.getFile().getInputstream(), userFacade.find((Long)session.getAttribute("userID")), PaymentPurpose.VAT);
            proccessor.parseAdjustmentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")), TransactionType.DEBIT, desc);
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleEquipmentFileUpload(FileUploadEvent event) {
        log.debug("File: " + event.getFile().getFileName());

        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            log.debug("Starting file parsing....");
            proccessor.parseEquipmentFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")));
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public StreamedContent getSampleFile() {
        InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demos/sample_import.xlsx");
        StreamedContent streamedContent = new DefaultStreamedContent(stream, "xlsx", "sample_import_download.xlsx");
        return streamedContent;
    }

    public StreamedContent getSampleStatusChangeFile() {
        InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demos/sample_status_change_import.xlsx");
        StreamedContent streamedContent = new DefaultStreamedContent(stream, "xlsx", "sample_status_change_download.xlsx");
        return streamedContent;
    }

    public StreamedContent getSampleAgreementChangeFile() {
        InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demos/sample_agreement_change_import.xlsx");
        StreamedContent streamedContent = new DefaultStreamedContent(stream, "xlsx", "sample_agreement_change_download.xlsx");
        return streamedContent;
    }

    public void handleMinipopFileUpload(FileUploadEvent event) {
        log.debug("File: " + event.getFile().getFileName());

        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            log.debug("Starting file parsing....");
            proccessor.parseMinipopFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")));
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleUsersFileUpload(FileUploadEvent event) {
        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            Future<String> result = proccessor.parseUsersFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")));

            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleImsiFileUpload(FileUploadEvent event) {
        log.debug("File: " + event.getFile().getFileName());

        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            log.debug("Starting file parsing....");
            proccessor.parseImsiFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")));
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleMsisdnFileUpload(FileUploadEvent event) {
        log.debug("File: " + event.getFile().getFileName());

        if (event.getFile() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            log.debug("Starting file parsing....");
            proccessor.parseMsisdnFile(event.getFile().getInputstream(), userFacade.find((Long) session.getAttribute("userID")));
            FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess file: " + event.getFile().getFileName(),
                    "Cannot proccess file: " + event.getFile().getFileName()
            );

            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void uploadStatusChangeFile() {

        if (file == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {

            wrapperBatchList = null;

            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

            wrapperBatchList = proccessor.parseStatusChangeFile(file.getInputstream(), userFacade.find((Long) session.getAttribute("userID")));

            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    file.getFileName(),
                    "Cannot proccess status change file: " + file.getFileName()
            );
            log.error("Exception on uploadStatusChangeFile ===> ", ex);
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }


    public void uploadAgreementChangeFile() throws InterruptedException {

        if (agreementChangesFile == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "File not uploaded", "File not uploaded");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {

            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            log.info("Trying to change subscription contracts , User -> " + userFacade.find((Long) session.getAttribute("userID")));
            wrapperAgreementChangeBatchSet.clear();
            wrapperAgreementChangeBatchSet = proccessor.parseAgreementChangeFile(agreementChangesFile.getInputstream());

            FacesMessage message = new FacesMessage("Succesful", agreementChangesFile.getFileName() + " is uploaded and prcoessed.");
            FacesContext.getCurrentInstance().addMessage(null, message);

        } catch (Exception ex) {
            wrapperAgreementChangeBatchSet = new HashSet<>();
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cannot proccess status change file: " + agreementChangesFile != null ? agreementChangesFile.getFileName() : "",
                    "Cannot proccess status change file: " + agreementChangesFile != null ? agreementChangesFile.getFileName() : ""
            );
            log.error("Exception on uploadStatusChangeFile ===> ", ex);
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }


    public void applyAgreementChangesIntoSystem() {
        if (this.wrapperAgreementChangeBatchSet == null || this.wrapperAgreementChangeBatchSet.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning",
                    agreementChangesFile.getFileName() + " no request found to apply changes"));
        }
        log.info("Overall " + wrapperAgreementChangeBatchSet.size() + "elements to process");
        wrapperAgreementChangeBatchSet.stream()
                .map(batch -> batch.getAgreement() + " - " + batch.getOldAgreement())
                .forEach(log::info);
        for (WrapperAgreementChangeBatch agreementChange : wrapperAgreementChangeBatchSet) {
            log.info("working on batch " + agreementChange);
            try {
                if (agreementChange.getMessage() == null || agreementChange.getMessage().isEmpty()) {
                    contractSwitcherFacadeLocal.switchToNewContract(agreementChange);
                    agreementChange.setMessage("SUCCESS");
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                log.error("errors>>>>>>>>>>>>>>>" + ex);
                log.error("errors>>>>>>>>>>>>>>>" + ex.getMessage());
                String message = "error happened " + (ex.getMessage() == null ? "" : ex.getMessage());
                agreementChange.setMessage(message);
            }

        }

    }





    /*public void onPaymentFileUpload (FileUploadEvent ev) {
        UploadedFile fileL = ev.getFile();
        log.debug("Uploaded file: " + fileL.getFileName());
        //log.debug("Uploaded file: " + file.getFileName());
        /*try {            
            XSSFWorkbook wb = new XSSFWorkbook(file.getInputstream());
            XSSFSheet sheet = wb.getSheetAt(0);
            
            Iterator<Row> rowIterator = sheet.iterator();
            Iterator<Cell> cellIterator = null;
            Row row = null;
            Cell cell = null;
            int columnCounter = 1;
            
            rowIterator.next();
            
            while (rowIterator.hasNext()) {                
                row = rowIterator.next();
                cellIterator = row.iterator();
                
                while (cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:                            
                            System.out.print(String.format("{%s} ", cell.getStringCellValue()));
                        break;
                        case Cell.CELL_TYPE_NUMERIC:
                            if (columnCounter == 1)
                                log.debug(String.format("{%s} ", Double.valueOf(cell.getNumericCellValue()).longValue()));
                            else if (columnCounter == 3) 
                                log.debug(String.format("{%s} ", Double.valueOf(cell.getNumericCellValue() * 100000).longValue()));                                
                        break;
                        default:
                            log.debug("N/A");
                    }
                    cell = null;
                    columnCounter++;
                } // cell iteration ENDS
                
                //System.out.println();
                
                cellIterator = null;
                row = null;
                columnCounter = 1;
            } // row iteration ENDS           
        }

        catch (Exception ex) { 
            log.error("Cannot upload file: ", ex);
        }
    }*/


    public UploadedFile getAgreementChangesFile() {
        return agreementChangesFile;
    }

    public void setAgreementChangesFile(UploadedFile agreementChangesFile) {
        this.agreementChangesFile = agreementChangesFile;
    }

    public Set<WrapperAgreementChangeBatch> getWrapperAgreementChangeBatchSet() {
        return wrapperAgreementChangeBatchSet;
    }

    public void setWrapperAgreementChangeBatchSet(Set<WrapperAgreementChangeBatch> wrapperAgreementChangeBatchSet) {
        this.wrapperAgreementChangeBatchSet = wrapperAgreementChangeBatchSet;
    }
}
