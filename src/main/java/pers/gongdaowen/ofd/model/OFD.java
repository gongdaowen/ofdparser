package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "OFD")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFD {

    public Map<String, Object> ObjectMap = new HashMap<>();

    @XmlAttribute(name = "DocType")
    public String DocType;
    @XmlAttribute(name = "Version")
    public String Version;

    public DocBody DocBody;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocBody {
        public DocInfo DocInfo;
        public String DocRoot;
        public String Signatures;

        public OFDDocument $OFDDocument;
        public OFDSignatures $OFDSignatures;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocInfo {
        public String DocID;
        public String Creator;
    }
}
