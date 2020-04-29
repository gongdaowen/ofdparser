import org.junit.Test;
import pers.gongdaowen.ofd.OFDInfo;
import pers.gongdaowen.ofd.OfdParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class OfdParserTest {

    @Test
    public void parseOfd() throws Exception {
        OFDInfo ofd = OfdParser.parse(new File("src/test/resources/百望云增值税发票.ofd"));
//        System.out.println("验签：" + ofd.verify());
//        Invoice invoice = ofd.getInvoice();
//        System.out.println("发票信息：" + invoice);
        BufferedImage image = ofd.convertAsSingleImage(150);
        ImageIO.write(image, "JPG", new File("src/test/resources/百望云增值税发票.ofd.jpg"));
    }
}
