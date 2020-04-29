package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Annotations")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDAnnotations {

    public List<Page> Page = new ArrayList<>();

    @XmlType(name = "Annotations.Page")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Page {
        @XmlAttribute(name = "PageID")
        public String PageID;

        public String FileLoc;
        public PageAnnot PageAnnot = new PageAnnot();
    }

    @XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "PageAnnot")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PageAnnot {
        public List<Annot> Annot = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Annot {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Type")
        public String Type;
        @XmlAttribute(name = "Subtype")
        public String Subtype;

        public Parameters Parameters = new Parameters();
        public Appearance Appearance = new Appearance();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Parameters {
        public List<Parameter> Parameter = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Parameter {
        @XmlAttribute(name = "Name")
        public String Name;
        @XmlValue
        public String Value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Appearance {
        @XmlAttribute(name = "Boundary")
        public String Boundary;
    }
}
