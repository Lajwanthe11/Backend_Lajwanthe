package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.Role;

import com.example.platformadmin.rbac.dto.response.RoleAssignmentReportRow;
import com.example.platformadmin.rbac.service.RoleAssignmentExportService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class RoleAssignmentExportServiceImpl implements RoleAssignmentExportService {

    @Override
    public byte[] toExcel(List<RoleAssignmentReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            var sheet = workbook.createSheet("Role Assignments");
            String[] headers = {
                    "User Role ID", "User ID", "Role ID", "Role Code", "Role Name",
                    "Effective Date", "Expiry Date", "Status", "Primary", "Assigned At"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (RoleAssignmentReportRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                set(row, 0, item.userRoleId());
                set(row, 1, item.userId());
                set(row, 2, item.roleId());
                set(row, 3, item.roleCode());
                set(row, 4, item.roleName());
                set(row, 5, item.effectiveDate());
                set(row, 6, item.expiryDate());
                set(row, 7, item.status());
                set(row, 8, item.primaryRole());
                set(row, 9, item.assignedAt());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate Excel role assignment report", ex);
        }
    }

    @Override
    public byte[] toPdf(List<RoleAssignmentReportRow> rows) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Role Assignment Report", titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            String[] headers = {
                    "User ID", "Role ID", "Role Code", "Role Name",
                    "Effective", "Expiry", "Status", "Primary"
            };
            for (String header : headers) {
                addHeader(table, header);
            }

            for (RoleAssignmentReportRow row : rows) {
                addCell(table, row.userId());
                addCell(table, row.roleId());
                addCell(table, row.roleCode());
                addCell(table, row.roleName());
                addCell(table, row.effectiveDate());
                addCell(table, row.expiryDate());
                addCell(table, row.status());
                addCell(table, row.primaryRole());
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate PDF role assignment report", ex);
        }
    }

    private void set(Row row, int index, Object value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : String.valueOf(value));
    }

    private void addHeader(PdfPTable table, String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        table.addCell(new PdfPCell(new Phrase(value, font)));
    }

    private void addCell(PdfPTable table, Object value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 7);
        table.addCell(new Phrase(value == null ? "" : String.valueOf(value), font));
    }
}
