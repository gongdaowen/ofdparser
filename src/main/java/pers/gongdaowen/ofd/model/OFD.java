package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "OFD")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFD {

    @XmlTransient
    public Map<String, Object> ObjectMap = new HashMap<>();
    @XmlTransient
    public Map<Integer, List<OFDSignatures.Sign>> PageSignMap = new HashMap<>();

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
        public String Author;
        public String CreationDate;
        public CustomDatas CustomDatas;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CustomDatas {
        public List<CustomData> CustomData = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CustomData {
        @XmlAttribute(name = "Name")
        public String Name;
        @XmlValue
        public String Value;
    }
}
