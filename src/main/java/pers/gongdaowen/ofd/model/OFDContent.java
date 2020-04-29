package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Page")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDContent {

    public Area Area;
    public Template Template;
    public Content Content;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Area {
        public String PhysicalBox;
        public String ApplicationBox;
        public String BleedBox;
        public String CropBox;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Content {
        public Layer Layer = new Layer();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Template {
        @XmlAttribute(name = "TemplateID")
        public String TemplateID;
        @XmlAttribute(name = "ZOrder")
        public String ZOrder;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Layer {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "DrawParam")
        public String DrawParam;

        public List<PathObject> PathObject = new ArrayList<>();
        public List<TextObject> TextObject = new ArrayList<>();
        public List<ImageObject> ImageObject = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PathObject {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Boundary")
        public String Boundary;
        @XmlAttribute(name = "CTM")
        public String CTM;
        @XmlAttribute(name = "LineWidth")
        public Float LineWidth;

        public Color StrokeColor;
        public String AbbreviatedData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TextObject {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Boundary")
        public String Boundary;
        @XmlAttribute(name = "CTM")
        public String CTM;
        @XmlAttribute(name = "Font")
        public String Font;
        @XmlAttribute(name = "Size")
        public Float Size;

        public Color FillColor;
        public Color StrokeColor;
        public List<TextCode> TextCode;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ImageObject {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Boundary")
        public String Boundary;
        @XmlAttribute(name = "CTM")
        public String CTM;
        @XmlAttribute(name = "ResourceID")
        public String ResourceID;
    }

    @XmlType(name = "Content.Page")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Color {
        @XmlAttribute(name = "Value")
        public String Value;
        @XmlAttribute(name = "Alpha")
        public String Alpha;
        @XmlAttribute(name = "ColorSpace")
        public String ColorSpace;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TextCode {
        @XmlAttribute(name = "X")
        public Float X;
        @XmlAttribute(name = "Y")
        public Float Y;
        @XmlAttribute(name = "DeltaX")
        public String DeltaX;
        @XmlAttribute(name = "DeltaY")
        public String DeltaY;
        @XmlValue
        public String Text;
    }
}
