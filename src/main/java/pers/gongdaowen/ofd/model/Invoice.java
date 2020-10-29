package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "", name = "eInvoice")
@XmlAccessorType(XmlAccessType.FIELD)
public class Invoice {

    /**
     * 文档ID
     */
    public String DocID;
    /**
     * 发票代码
     */
    public String InvoiceCode;
    /**
     * 发票号码
     */
    public String InvoiceNo;
    /**
     * 发票日期
     */
    public String IssueDate;
    /**
     * 发票校验码
     */
    public String InvoiceCheckCode;
    /**
     * 机器编码
     */
    public String MachineNo;
    /**
     * 密文区（4行）
     */
    public String TaxControlCode;
    /**
     * 购方信息（名称、税号、地址电话、开户银行及账号）
     */
    public Buyer Buyer;
    /**
     * 销方信息（名称、税号、地址电话、开户银行及账号）
     */
    public Seller Seller;
    /**
     * 含税金额
     */
    public String TaxInclusiveTotalAmount;
    /**
     * 含税金额
     */
    public String TaxInclusiveTotalAmountCN;
    /**
     * 备注栏
     */
    public String Note;
    /**
     * 开票人
     */
    public String InvoiceClerk;
    /**
     * 收款人
     */
    public String Payee;
    /**
     * 复核人
     */
    public String Checker;
    /**
     * 税额
     */
    public String TaxTotalAmount;
    /**
     * 不含税金额
     */
    public String TaxExclusiveTotalAmount;
    /**
     * 二维码信息
     */
    public String GraphCode;
    /**
     *
     */
    public String InvoiceSIA1;
    /**
     *
     */
    public String InvoiceSIA2;
    /**
     * 发票明细信息
     */
    public GoodsInfos GoodsInfos;
    /**
     *
     */
    public String Tpvn;
    /**
     * 签名数据
     */
    public String Signature;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Buyer {
        /**
         * 购方名称
         */
        public String BuyerName;
        /**
         * 购方税号
         */
        public String BuyerTaxID;
        /**
         * 购方地址电话
         */
        public String BuyerAddrTel;
        /**
         * 购方开户银行及账号
         */
        public String BuyerFinancialAccount;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Seller {
        /**
         * 销方名称
         */
        public String SellerName;
        /**
         * 销方税号
         */
        public String SellerTaxID;
        /**
         * 销方地址电话
         */
        public String SellerAddrTel;
        /**
         * 销方开户银行及账号
         */
        public String SellerFinancialAccount;
        /**
         *
         */
        public String Issuea1;
        /**
         *
         */
        public String Issuea2;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GoodsInfos {
        /**
         * 发票明细信息
         */
        public List<GoodsInfo> GoodsInfo = new ArrayList<>();
    }

    /**
     * 明细信息
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GoodsInfo {
        /**
         * 项目名称
         */
        public String Item;
        /**
         * 规格型号
         */
        public String Specification;
        /**
         * 单位
         */
        public String MeasurementDimension;
        /**
         * 单价
         */
        public String Price;
        /**
         * 数量
         */
        public String Quantity;
        /**
         * 金额
         */
        public String Amount;
        /**
         * 税率
         */
        public String TaxScheme;
        /**
         * 税额
         */
        public String TaxAmount;
    }
}
