package pers.gongdaowen.ofd.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(namespace = "http://www.ofdspec.org/2016", name = "Attachments")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFDAttachments {

    public List<Attachment> Attachment = new ArrayList<>();

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Attachment {
        @XmlAttribute(name = "ID")
        public String ID;
        @XmlAttribute(name = "Name")
        public String Name;
        @XmlAttribute(name = "Format")
        public String Format;
        @XmlAttribute(name = "CreationDate")
        public String CreationDate;
        @XmlAttribute(name = "Size")
        public Double Size;
        @XmlAttribute(name = "Visible")
        public Boolean Visible;

        public String FileLoc;

        // 文件的字节数组
        public byte[] $FileData;
    }
}
