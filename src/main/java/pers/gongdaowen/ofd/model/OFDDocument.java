package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Document")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDDocument {

    public CommonData CommonData = new CommonData();
    public Pages Pages = new Pages();
    public String Annotations;
    public String Attachments;

    public OFDAnnotations $OFDAnnotations;
    public OFDAttachments $OFDAttachments;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CommonData {
        public String MaxUnitID;
        public PageArea PageArea;
        public String DocumentRes;
        public String PublicRes;
        public List<Page> TemplatePage = new ArrayList<>();

        public OFDDocumentRes $OFDDocumentRes;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PageArea {
        public String PhysicalBox;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Pages {
        public List<Page> Page = new ArrayList<>();
    }

    @XmlType(name = "Document.Page")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Page {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "BaseLoc")
        public String BaseLoc;

        public int $Index;
        public OFDContent $OFDContent;
        public Rectangle2D $Rectangle;
    }
}
