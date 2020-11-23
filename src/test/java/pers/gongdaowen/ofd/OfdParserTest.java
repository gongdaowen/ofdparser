package pers.gongdaowen.ofd;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.junit.Test;
import pers.gongdaowen.ofd.OFDInfo;
import pers.gongdaowen.ofd.OfdParser;
import pers.gongdaowen.ofd.model.Invoice;
import pers.gongdaowen.ofd.model.OFD;
import pers.gongdaowen.ofd.model.OFDDocument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OfdParserTest {

    @Test
    public void parseOfd() throws Exception {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("src/test/resources/百望云增值税发票.ofd");

        for (String fileName : fileNames) {
            OFDInfo ofdInfo = OfdParser.parse(new File(fileName));

            System.out.println("验签：" + ofdInfo.verify());

            Invoice invoice = ofdInfo.getInvoice();
            System.out.println("发票信息：" + invoice.TaxInclusiveTotalAmount + "-" + invoice.TaxInclusiveTotalAmountCN);

            OFDPainter ofdPainter = ofdInfo.getOfdPainter();
            ofdPainter.setPageHandler(new OFDPainter.PageHandler() {
                @Override
                public void before(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi) {
                    int pageIndex = ofd.DocBody.$OFDDocument.Pages.Page.indexOf(page);
                    if (pageIndex != 0) { // 不是第一页
                        g.drawLine(0, 0, (int) (page.$Rectangle.getWidth() * (dpi / 25.4f)), 2);
                    }
                }

                @Override
                public void after(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi) {

                }
            });
            int dpi = 150;
            System.out.println("\n--转成单个图像--");
            {
                String singleFilename = fileName + "-image-0-single.jpg";
                BufferedImage image = ofdPainter.convertAsSingleImage(dpi);
                ImageIO.write(image, "JPG", new File(singleFilename));
                System.out.println("file：" + singleFilename);
            }

            System.out.println("\n--转成多个图像--");
            {
                List<BufferedImage> images = ofdPainter.convertAsImages(dpi);
                for (int i = 0; i < images.size(); i++) {
                    String filename = fileName + "-image-" + (i + 1) + ".jpg";
                    ImageIO.write(images.get(i), "JPG", new File(filename));
                    System.out.println("file：" + filename);
                }
            }

            System.out.println("\n--转成单个SVG矢量图--");
            {
                String svgSingleFilename = fileName + "-svg-0-single.svg";
                SVGGraphics2D singleSVG = ofdPainter.convertAsSingleSVG(dpi);
                singleSVG.stream(new FileWriter(svgSingleFilename));
                System.out.println("file：" + svgSingleFilename);
            }

            System.out.println("\n--转成多个SVG矢量图--");
            {
                List<SVGGraphics2D> svgs = ofdPainter.convertAsSVGs(dpi);
                for (int i = 0; i < svgs.size(); i++) {
                    String svgFilename = fileName + "-svg-" + (i + 1) + ".svg";
                    svgs.get(i).stream(new FileWriter(svgFilename));
                    System.out.println("file：" + svgFilename);
                }
            }

            System.out.println("\n--SVG文件转成JPG图像--");
            {
                String svgSingleFilename = fileName + "-tc-svg2image.svg";
                SVGGraphics2D singleSVG = ofdPainter.convertAsSingleSVG(dpi);
                singleSVG.stream(new FileWriter(svgSingleFilename));
                // 转换参数
                Transcoder transcoder = new JPEGTranscoder();
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.99f);
                // 需要转换的svg文件
                TranscoderInput input = new TranscoderInput(new FileInputStream(svgSingleFilename));
                // 输出成字节数组
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(out);
                // 转成字节数组
                transcoder.transcode(input, output);
                // 转成图像
                BufferedImage image2 = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
                ImageIO.write(image2, "JPG", new File(fileName + "-tc-svg2image.jpg"));
                System.out.println("file：" + fileName + "-tc-svg2image.jpg");
            }
        }
    }
}
