package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Res")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDDocumentRes {

    @XmlAttribute(name = "BaseLoc")
    public String BaseLoc;

    public ColorSpaces ColorSpaces = new ColorSpaces();
    public Fonts Fonts = new Fonts();
    public MultiMedias MultiMedias = new MultiMedias();
    public DrawParams DrawParams = new DrawParams();

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ColorSpaces {
        public List<ColorSpace> ColorSpace = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ColorSpace {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Type")
        public String Type;
        @XmlAttribute(name = "BitsPerComponent")
        public String BitsPerComponent;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Fonts {
        public List<Font> Font = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Font {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "FontName")
        public String FontName;
        @XmlAttribute(name = "FamilyName")
        public String FamilyName;
        @XmlAttribute(name = "Charset")
        public String Charset;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MultiMedias {
        public List<MultiMedia> MultiMedia = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MultiMedia {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Type")
        public String Type;
        @XmlAttribute(name = "Format")
        public String Format;

        public String MediaFile;

        // 文件的字节数组
        public byte[] $FileData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DrawParams {
        public List<DrawParam> DrawParam = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DrawParam {
        @XmlAttribute(name = "ID")
        public String ID;

        public Color FillColor;
        public Color StrokeColor;
    }

    @XmlType(name = "Res.Page")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Color {
        @XmlAttribute(name = "Value")
        public String Value;
        @XmlAttribute(name = "Alpha")
        public String Alpha;
        @XmlAttribute(name = "ColorSpace")
        public String ColorSpace;
    }
}
