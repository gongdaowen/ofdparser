package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "", name = "eInvoice")
@XmlAccessorType(XmlAccessType.FIELD)
public class Invoice {

    public String DocID;
    public String InvoiceCode;
    public String InvoiceNo;
    public String IssueDate;
    public String InvoiceCheckCode;
    public String MachineNo;
    public String TaxControlCode;
    public Buyer Buyer;
    public Seller Seller;
    public String TaxInclusiveTotalAmount;
    public String Note;
    public String InvoiceClerk;
    public String Payee;
    public String Checker;
    public String TaxTotalAmount;
    public String TaxExclusiveTotalAmount;
    public String GraphCode;
    public String InvoiceSIA1;
    public String InvoiceSIA2;
    public GoodsInfos GoodsInfos;
    public String Tpvn;
    public String Signature;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Buyer {
        public String BuyerName;
        public String BuyerTaxID;
        public String BuyerAddrTel;
        public String BuyerFinancialAccount;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Seller {
        public String SellerName;
        public String SellerTaxID;
        public String SellerAddrTel;
        public String SellerFinancialAccount;
        public String Issuea1;
        public String Issuea2;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GoodsInfos {
        public List<GoodsInfo> GoodsInfo = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GoodsInfo {
        public String Item;
        public String Specification;
        public String MeasurementDimension;
        public String Price;
        public String Quantity;
        public String Amount;
        public String TaxScheme;
        public String TaxAmount;
    }
}
