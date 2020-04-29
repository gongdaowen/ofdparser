package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Signatures")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDSignatures {

    public String MaxSignId;
    public List<Sign> Signature = new ArrayList<>();

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Sign {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "BaseLoc")
        public String BaseLoc;

        public Signature $Signature;
    }

    @XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Signature")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Signature {
        public SignedInfo SignedInfo;
        public String SignedValue;

        public byte[] $ToSignData;
        public byte[] $SignData;
        public byte[] $CertData;

        public byte[] $FileData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SignedInfo {
        public Provider Provider;
        public String SignatureMethod;
        public String SignatureDateTime;
        public References References = new References();
        public StampAnnot StampAnnot;
        public Seal Seal;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Provider {
        @XmlAttribute(name = "ProviderName")
        public String ProviderName;
        @XmlAttribute(name = "Version")
        public String Version;
        @XmlAttribute(name = "Company")
        public String Company;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class References {
        @XmlAttribute(name = "CheckMethod")
        public String CheckMethod;

        public List<Reference> Reference = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Reference {
        @XmlAttribute(name = "FileRef")
        public String FileRef;

        public String CheckValue;

        // 文件的字节数组
        public byte[] $FileData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class StampAnnot {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "PageRef")
        public String PageRef;
        @XmlAttribute(name = "Boundary")
        public String Boundary;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Seal {
        public String BaseLoc;

        public byte[] $FileData;
        public OFD $OFD;
    }
}
