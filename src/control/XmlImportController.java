package control;

import entity.Supplier;
import entity.SupplyItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
/**
 * Controller class responsible for importing inventory and supplier data
 * from an XML file (Supir system format) and persisting it to the database.
 */
public class XmlImportController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private SupplierController supplierController;
    private SupplyItemController supplyItemController;

    public XmlImportController() {
        this.supplierController = new SupplierController();
        this.supplyItemController = new SupplyItemController(this.supplierController); 
    }

    public ImportResult importDataFromXml(String xmlFilePath) {
        ImportResult result = new ImportResult(); 
        File xmlFile = new File(xmlFilePath);

        if (!xmlFile.exists()) {
            result.addErrorMessage("XML file not found: " + xmlFilePath);
            return result;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // --- Step 1: Parse and Process Suppliers from XML ---
            NodeList supplierNodeList = doc.getElementsByTagName("supplier");
            for (int i = 0; i < supplierNodeList.getLength(); i++) {
                Node supplierNode = supplierNodeList.item(i);
                if (supplierNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element supplierElement = (Element) supplierNode;
                    Supplier parsedSupplier = new Supplier();
                    String supplierCode = supplierElement.getAttribute("supplierCode");
                    parsedSupplier.setSupplierCode(supplierCode);
                    parsedSupplier.setSupplierName(supplierElement.getAttribute("supplierName"));
                    String recentSupplyDateStr = supplierElement.getAttribute("recentSupplyDate");

                    try {
                        if (recentSupplyDateStr != null && !recentSupplyDateStr.isEmpty() && !recentSupplyDateStr.equalsIgnoreCase("N/A")) {
                            parsedSupplier.setRecentSupplyDate(LocalDate.parse(recentSupplyDateStr, DATE_FORMATTER));
                        }
                    } catch (DateTimeParseException e) {
                        result.addErrorMessage("Supplier '" + supplierCode + "': Invalid recentSupplyDate format '" + recentSupplyDateStr + "'. Date not set. Error: " + e.getMessage());
                    }

                    Supplier existingSupplier = supplierController.getSupplierByCode(supplierCode);
                    String operationErrorMessage; 

                    if (existingSupplier != null) {
                        existingSupplier.setSupplierName(parsedSupplier.getSupplierName());
                        if (parsedSupplier.getRecentSupplyDate() != null) { 
                            existingSupplier.setRecentSupplyDate(parsedSupplier.getRecentSupplyDate());
                        }
                        operationErrorMessage = supplierController.updateSupplier(existingSupplier);
                        if (operationErrorMessage == null) { 
                            result.incrementSuppliersUpdated();
                        } else {
                            result.incrementSuppliersFailed();
                            result.addErrorMessage("Supplier '" + supplierCode + "': Update failed - " + operationErrorMessage);
                        }
                    } else {
                        operationErrorMessage = supplierController.addSupplier(parsedSupplier);
                        if (operationErrorMessage == null) { 
                            result.incrementSuppliersAdded();
                        } else {
                            result.incrementSuppliersFailed();
                            result.addErrorMessage("Supplier '" + supplierCode + "': Add failed - " + operationErrorMessage);
                        }
                    }
                }
            }

            // --- Step 2: Parse and Process Supply Items from XML ---
            NodeList itemNodeList = doc.getElementsByTagName("item");
            for (int i = 0; i < itemNodeList.getLength(); i++) {
                Node itemNode = itemNodeList.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) itemNode;
                    SupplyItem parsedItem = new SupplyItem();
                    String itemBarcode = itemElement.getAttribute("barcode");
                    parsedItem.setBarcode(itemBarcode);
                    parsedItem.setProductTitle(itemElement.getAttribute("productTitle"));
                    parsedItem.setProductDetails(itemElement.getAttribute("productDetails"));
                    parsedItem.setCategory(itemElement.getAttribute("category"));

                    try {
                        String availableUnitsStr = itemElement.getAttribute("availableUnits");
                        if (availableUnitsStr != null && !availableUnitsStr.isEmpty()) {
                            parsedItem.setAvailableUnits(Integer.parseInt(availableUnitsStr));
                        }
                        String thresholdStockStr = itemElement.getAttribute("thresholdStock");
                        if (thresholdStockStr != null && !thresholdStockStr.isEmpty()) {
                            parsedItem.setThresholdStock(Integer.parseInt(thresholdStockStr));
                        }
                    } catch (NumberFormatException e) {
                        result.addErrorMessage("Item '" + itemBarcode + "': Invalid number format for units/threshold. Skipping these values. Error: " + e.getMessage());
                    }
                    
                    String expirationDateStr = itemElement.getAttribute("expirationDate");
                    if (expirationDateStr != null && !expirationDateStr.trim().isEmpty() && !expirationDateStr.equalsIgnoreCase("N/A")) {
                        try {
                            parsedItem.setExpirationDate(LocalDate.parse(expirationDateStr, DATE_FORMATTER));
                        } catch (DateTimeParseException e) {
                            result.addErrorMessage("Item '" + itemBarcode + "': Invalid expirationDate format '" + expirationDateStr + "'. Date not set. Error: " + e.getMessage());
                        }
                    }

                    String itemSupplierCode = itemElement.getAttribute("supplierCode");
                    if (itemSupplierCode != null && !itemSupplierCode.isEmpty()) {
                        Supplier linkedSupplier = this.supplierController.getSupplierByCode(itemSupplierCode); // Use the instance from constructor
                        if (linkedSupplier != null) {
                            parsedItem.setSupplier(linkedSupplier);
                        } else {
                             result.addMessage("Warning for Item '" + itemBarcode + "': XML references Supplier Code '" + itemSupplierCode + "' which was not found in DB or could not be added/updated from this XML batch. Item will be processed without this specific supplier link if added/updated.");
                        }
                    }

                    SupplyItem existingItem = supplyItemController.getSupplyItemByBarcode(itemBarcode);
                    String operationErrorMessage; 

                    if (existingItem != null) {
                        existingItem.setProductTitle(parsedItem.getProductTitle());
                        existingItem.setProductDetails(parsedItem.getProductDetails());
                        existingItem.setCategory(parsedItem.getCategory());
                        existingItem.setAvailableUnits(parsedItem.getAvailableUnits()); 
                        existingItem.setExpirationDate(parsedItem.getExpirationDate()); 
                        existingItem.setThresholdStock(parsedItem.getThresholdStock()); 
                        existingItem.setSupplier(parsedItem.getSupplier()); 

                        operationErrorMessage = supplyItemController.updateSupplyItem(existingItem);
                        if (operationErrorMessage == null) { 
                            result.incrementItemsUpdated();
                        } else {
                            result.incrementItemsFailed();
                            result.addErrorMessage("Item '" + itemBarcode + "': Update failed - " + operationErrorMessage);
                        }
                    } else {
                        if (parsedItem.getSupplierCode() != null && !parsedItem.getSupplierCode().isEmpty() && parsedItem.getSupplier() == null) {
                            result.addErrorMessage("Item '" + itemBarcode + "': Cannot add. Linked supplier code '" + parsedItem.getSupplierCode() + "' not found in database.");
                            result.incrementItemsFailed();
                            continue;
                        }
                        operationErrorMessage = supplyItemController.addSupplyItem(parsedItem); 
                        if (operationErrorMessage == null) { 
                            result.incrementItemsAdded();
                        } else {
                            result.incrementItemsFailed();
                            result.addErrorMessage("Item '" + itemBarcode + "': Add failed - " + operationErrorMessage);
                        }
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            result.addErrorMessage("Critical XML Parsing Error: " + e.getMessage());
            e.printStackTrace(); 
        } catch (Exception e) { 
            result.addErrorMessage("Unexpected critical error during import: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}